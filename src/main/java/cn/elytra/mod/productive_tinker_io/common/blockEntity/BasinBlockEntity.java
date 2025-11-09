package cn.elytra.mod.productive_tinker_io.common.blockEntity;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.dataComponent.SpeedUpgradeComponent;
import cn.elytra.mod.productive_tinker_io.common.menu.BasinMenu;
import cy.jdkdigital.productivelib.common.block.entity.CapabilityBlockEntity;
import cy.jdkdigital.productivemetalworks.Config;
import cy.jdkdigital.productivemetalworks.recipe.BlockCastingRecipe;
import cy.jdkdigital.productivemetalworks.recipe.ItemCastingRecipe;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.RecipeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class BasinBlockEntity extends CapabilityBlockEntity implements MenuProvider {

    public int coolingTime = 0;
    public int maxCoolingTime = 1; // non-zero to prevent divided by zero

    private @Nullable ItemCastingRecipe lastRecipe;
    private @Nullable FluidStack consumedFluid;

    /**
     * @return ratio of reduced time (0.2 = 20% off), or -1 if not a valid upgrade.
     */
    public static double getSpeedUpgradeValue(ItemStack stack) {
        if(stack.has(ProductiveTinkerIo.SPEED_UPGRADE_COMPONENT)) {
            SpeedUpgradeComponent suc = stack.get(ProductiveTinkerIo.SPEED_UPGRADE_COMPONENT);
            assert suc != null;
            return suc.getClampedValue();
        }
        return -1;
    }

    public static boolean isBasinUpgrade(ItemStack stack) {
        return stack.has(ProductiveTinkerIo.BASIN_UPGRADE_COMPONENT);
    }

    // the casting item slot
    public ItemStackHandler invCasting = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirtyAndSync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    // the output item slot
    public ItemStackHandler invOutput = new ItemStackHandler(1) {

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirtyAndSync();
        }
    };

    // the upgraders slots, size 2
    public ItemStackHandler invUpgrade = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirtyAndSync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isValidUpgradeItem(stack);
        }
    };

    // the fluid tank
    public FluidTank fluidHandler = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            markDirtyAndSync();
        }
    };

    public BasinBlockEntity(BlockPos pos, BlockState blockState) {
        super(ProductiveTinkerIo.BASIN_BLOCK_ENTITY.get(), pos, blockState);
    }

    public IItemHandler getItemHandler() {
        return invOutput;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BasinBlockEntity basinBlockEntity) {
        if(level instanceof ServerLevel serverLevel) {
            basinBlockEntity.onServerTick(serverLevel, blockPos, blockState, basinBlockEntity);
        }
    }

    public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, BasinBlockEntity basinBlockEntity) {
        if(basinBlockEntity.isCooling()) {
            basinBlockEntity.coolingTime--;
        }
    }

    private void onServerTick(ServerLevel level, BlockPos blockPos, BlockState blockState, BasinBlockEntity basinBlockEntity) {
        if(isCooling()) {
            // reduce coolingTime,
            // when it's 0, finish the recipe, and we'll find the recipe below.
            onTickCooling();
        }
        if(!isCooling()) {
            // when there's no recipe or when the cooling is finished,
            // we try to find a recipe.
            onTickFindRecipe(level);
        }
    }

    private void onTickCooling() {
        if(lastRecipe == null || consumedFluid == null) {
            // invalid state, where the consumedFluid should never be null when the recipe is processing.
            coolingTime = 0;
            maxCoolingTime = 0;
            return;
        }

        if(--coolingTime <= 0) { // recipe done
            finishRecipeAndResetStates(this.lastRecipe);
        }
    }

    private void onTickFindRecipe(ServerLevel level) {
        int whileLoopCount = 0; // threshold
        while(!isCooling() && whileLoopCount++ < 1000) { // when not processing, try find a recipe.
            // retry the last recipe if possible for performance
            if(lastRecipe == null || !tryProcessRecipe(lastRecipe)) {
                // if failed, find another recipe then
                ItemCastingRecipe recipe = findRecipe(level, invCasting.getStackInSlot(0), fluidHandler.getFluid());
                if(recipe == null) break;
                if(!tryProcessRecipe(recipe)) break;
            }

            // save and sync
            markDirtyAndSync();

            // check 0-tick recipe, immediately finish it
            if(!isCooling()) {
                finishRecipeAndResetStates(this.lastRecipe);
            }
        }
    }

    /**
     * Dump the output of the recipe and reset the status of this block entity.
     */
    private void finishRecipeAndResetStates(ItemCastingRecipe recipe) {
        ItemStack resultItem = recipe.getResultItem(level, consumedFluid);
        ItemStack insertRemaining = invOutput.insertItem(0, resultItem, false);
        if(!insertRemaining.isEmpty()) {
            // something goes wrong, the item is failed to inserted to the slot, we need to drop it to the world
            Containers.dropItemStack(level, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, insertRemaining);
        }

        lastRecipe = null;
        consumedFluid = null;
        coolingTime = 0;
        maxCoolingTime = 1;
        markDirtyAndSync();
    }

    /**
     * @return {@code true} when the fluid is drained, time is set up, and the item should be able to be outputted.
     */
    private boolean tryProcessRecipe(@NotNull ItemCastingRecipe recipe) {
        int recipeFluidInput = recipe.getFluidAmount(level, fluidHandler.getFluid());
        ItemStack recipeItemOutput = recipe.getResultItem(level, fluidHandler.getFluid());

        // check input amount and output space
        if(fluidHandler.getFluidAmount() >= recipeFluidInput && invOutput.insertItem(0, recipeItemOutput, true).isEmpty()) {
            this.consumedFluid = fluidHandler.drain(recipeFluidInput, IFluidHandler.FluidAction.EXECUTE);
            this.maxCoolingTime = getUpgradeReducedRecipeTime((int) (recipeFluidInput / Config.foundryCoolingModifier), invUpgrade);
            this.coolingTime = this.maxCoolingTime;
            this.lastRecipe = recipe;
            return true;
        }
        return false;
    }

    public boolean isCooling() {
        return coolingTime > 0;
    }

    public boolean isBasinMode() {
        for(int i = 0; i < invUpgrade.getSlots(); i++) {
            if(isBasinUpgrade(invUpgrade.getStackInSlot(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copied from metalworks.
     */
    private ItemCastingRecipe findRecipe(Level level, ItemStack cast, FluidStack fluid) {
        boolean isTableMode = !isBasinMode();
        if(isTableMode && cast.is(Items.BUCKET)) {
            return new ItemCastingRecipe(Ingredient.of(cast), new SizedFluidIngredient(FluidIngredient.single(fluid), 1000), FluidUtil.getFilledBucket(fluid), true);
        } else {
            if(!isTableMode && fluid.is(MetalworksRegistrator.MOLTEN_WAX.get())) {
                if(cast.getItem() instanceof BlockItem castItem) {
                    // Waxable waxData = castItem.getBlock().builtInRegistryHolder().getData(NeoForgeDataMaps.WAXABLES);
                    Waxable waxData = BuiltInRegistries.BLOCK.wrapAsHolder(castItem.getBlock()).getData(NeoForgeDataMaps.WAXABLES);
                    if(waxData != null) {
                        return new BlockCastingRecipe(Ingredient.of(cast), new SizedFluidIngredient(FluidIngredient.single(fluid), 50), waxData.waxed().asItem().getDefaultInstance(), true);
                    }
                }
            }

            ItemCastingRecipe compatRecipe = RecipeHelper.getCompatRecipe(level, cast, fluid, isTableMode);
            if(compatRecipe != null) {
                return compatRecipe;
            } else if(isTableMode) {
                RecipeHolder<ItemCastingRecipe> recipe = RecipeHelper.getItemCastingRecipe(level, cast, fluid);
                return recipe != null ? recipe.value() : null;
            } else {
                RecipeHolder<BlockCastingRecipe> recipe = RecipeHelper.getBlockCastingRecipe(level, cast, fluid);
                return recipe != null ? recipe.value() : null;
            }
        }
    }

    /**
     * Drop the items when the tile is broken.
     * Invoked by the block.
     */
    public void dropItemStackOnRemove() {
        if(level == null) {
            return;
        }

        Stream<ItemStack> stream = Stream.<ItemStack>builder().add(invCasting.getStackInSlot(0)).add(invOutput.getStackInSlot(0)).add(invUpgrade.getStackInSlot(0)).add(invUpgrade.getStackInSlot(1)).build();
        stream.filter(it -> !it.isEmpty()).forEach(item -> Containers.dropItemStack(level, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, item));
    }

    /**
     * Check if an item is a valid upgrade.
     * For now, it can be the casting Basin from metalworks and the speed upgrades from productivelib, tier 1 and 2.
     *
     * @param stack the item to check
     * @return {@code true} if the item is a valid upgrade.
     */
    public static boolean isValidUpgradeItem(ItemStack stack) {
        return isBasinUpgrade(stack) || getSpeedUpgradeValue(stack) > 0;
    }

    /**
     * Iterate the given upgrades, find the first valid speeding upgrade, and by the map, reduce the recipe time.
     *
     * @param originalTime the original time
     * @param upgrades     the upgrades
     * @return the reduced recipe time
     */
    private static int getUpgradeReducedRecipeTime(int originalTime, ItemStackHandler upgrades) {
        double timeReduction = 0.0;
        for(int i = 0; i < upgrades.getSlots(); i++) {
            ItemStack upgrade = upgrades.getStackInSlot(i);
            double speedUpgrade = getSpeedUpgradeValue(upgrade);
            if(speedUpgrade > 0) {
                timeReduction = speedUpgrade;
                break;
            }
        }
        return Math.max(0, (int) (originalTime * (1 - timeReduction)));
    }

    @Override
    public void savePacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.savePacketNBT(tag, provider);

        tag.put("cast", invCasting.serializeNBT(provider));
        tag.put("upgraders", invUpgrade.serializeNBT(provider));
        tag.putInt("coolingTime", coolingTime);
        tag.putInt("maxCoolingTime", maxCoolingTime);

        if(lastRecipe != null) {
            tag.put("recipe", getRecipeSerializer().codec().encoder().encodeStart(NbtOps.INSTANCE, lastRecipe).getOrThrow());
        }
        if(consumedFluid != null) {
            tag.put("consumedFluid", consumedFluid.save(provider));
        }
    }

    @Override
    public void loadPacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadPacketNBT(tag, provider);

        if(tag.contains("cast")) {
            invCasting.deserializeNBT(provider, tag.getCompound("cast"));
        }
        if(tag.contains("upgraders")) {
            invUpgrade.deserializeNBT(provider, tag.getCompound("upgraders"));
        }
        if(tag.contains("coolingTime")) {
            coolingTime = tag.getInt("coolingTime");
        }
        if(tag.contains("maxCoolingTime")) {
            maxCoolingTime = tag.getInt("maxCoolingTime");
        }
        if(tag.contains("recipe")) {
            lastRecipe = getRecipeSerializer().codec().decoder().decode(NbtOps.INSTANCE, tag.getCompound("recipe"))
                    .getOrThrow().getFirst();
        }
        if(tag.contains("consumedFluid")) {
            consumedFluid = FluidStack.parse(provider, tag.getCompound("consumedFluid")).orElseThrow();
        }
    }

    @SuppressWarnings("unchecked")
    private static RecipeSerializer<ItemCastingRecipe> getRecipeSerializer() {
        return (RecipeSerializer<ItemCastingRecipe>) MetalworksRegistrator.ITEM_CASTING.get();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return super.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return new BasinMenu(i, inventory, this);
    }

    public void markDirtyAndSync() {
        if(level instanceof ServerLevel serverLevel) {
            invalidateCapabilities();
            serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
        setChanged();
    }
}
