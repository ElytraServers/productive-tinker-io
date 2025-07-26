package cn.elytra.mod.productive_tinker_io.common.blockEntity;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.menu.BasinMenu;
import cy.jdkdigital.productivelib.common.block.entity.CapabilityBlockEntity;
import cy.jdkdigital.productivemetalworks.Config;
import cy.jdkdigital.productivemetalworks.recipe.BlockCastingRecipe;
import cy.jdkdigital.productivemetalworks.recipe.ItemCastingRecipe;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.util.RecipeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
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

public class BasinBlockEntity extends CapabilityBlockEntity implements MenuProvider {

    public int coolingTime = 0;
    public int maxCoolingTime = 1; // non-zero to prevent divided by zero

    private @Nullable ItemCastingRecipe recipe;
    private @Nullable FluidStack consumedFluid;

    public ItemStackHandler castInv = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirtyAndSync();
        }
    };

    // possible the output
    public ItemStackHandler itemHandler = new ItemStackHandler(1) {

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirtyAndSync();
        }
    };

    public ItemStackHandler upgradeHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirtyAndSync();
        }

        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
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
        return itemHandler;
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
            onTickCooling(level);
        }
        if(!isCooling()) {
            onTickFindRecipe(level);
        }
    }

    private void onTickCooling(ServerLevel level) {
        if(recipe == null || consumedFluid == null) {
            recipe = null;
            consumedFluid = null;
            return;
        }

        if(--coolingTime <= 0) { // recipe done
            ItemStack resultItem = recipe.getResultItem(level, consumedFluid);
            ItemStack resultItemRemaining = itemHandler.insertItem(0, resultItem, false);
            if(!resultItemRemaining.isEmpty()) {
                // something goes wrong, the item is failed to inserted to the slot, we need to drop it to the world
                ItemEntity itemEntity = new ItemEntity(level, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5, resultItemRemaining);
                level.addFreshEntity(itemEntity);
            }

            recipe = null;
            consumedFluid = null;
            coolingTime = 0;
            maxCoolingTime = 1;
            markDirtyAndSync();
        }
    }

    private void onTickFindRecipe(ServerLevel level) {
        ItemCastingRecipe recipe = findRecipe(level, castInv.getStackInSlot(0), fluidHandler.getFluid());
        if(recipe != null) {
            int recipeAmountFluid = recipe.getFluidAmount(level, fluidHandler.getFluid());
            ItemStack resultItem = recipe.getResultItem(level, fluidHandler.getFluid());
            if(fluidHandler.getFluidAmount() >= recipeAmountFluid && itemHandler.insertItem(0, resultItem, true).isEmpty()) {
                this.coolingTime = (int) (recipeAmountFluid / Config.foundryCoolingModifier);
                this.maxCoolingTime = coolingTime;
                this.recipe = recipe;
                this.consumedFluid = fluidHandler.drain(recipeAmountFluid, IFluidHandler.FluidAction.EXECUTE);

                markDirtyAndSync();
            }
        }
    }

    private void sync(ServerLevel level) {
        invalidateCapabilities();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
    }

    public boolean isCooling() {
        return coolingTime > 0;
    }

    public boolean isBasinMode() {
        for(int i = 0; i < upgradeHandler.getSlots(); i++) {
            // TODO: replace it with an upgrader
            if(upgradeHandler.getStackInSlot(i).is(MetalworksRegistrator.CASTING_BASIN.get().asItem())) {
                return true;
            }
        }
        return false;
    }

    private ItemCastingRecipe findRecipe(Level level, ItemStack cast, FluidStack fluid) {
        boolean isTableMode = !isBasinMode();
        if(isTableMode && cast.is(Items.BUCKET)) {
            return new ItemCastingRecipe(Ingredient.of(cast), new SizedFluidIngredient(FluidIngredient.single(fluid), 1000), FluidUtil.getFilledBucket(fluid), true);
        } else {
            if(!isTableMode && fluid.is(MetalworksRegistrator.MOLTEN_WAX.get())) {
                if(cast.getItem() instanceof BlockItem castItem) {
                    Waxable waxData = castItem.getBlock().builtInRegistryHolder().getData(NeoForgeDataMaps.WAXABLES);
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

    public void dropItemStackOnRemove() {

    }

    @Override
    public void savePacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.savePacketNBT(tag, provider);

        tag.put("cast", castInv.serializeNBT(provider));
        tag.put("upgraders", upgradeHandler.serializeNBT(provider));
        tag.putInt("coolingTime", coolingTime);
        tag.putInt("maxCoolingTime", maxCoolingTime);

        if(recipe != null) {
            tag.put("recipe", getRecipeSerializer().codec().encoder().encodeStart(NbtOps.INSTANCE, recipe).getOrThrow());
        }
        if(consumedFluid != null) {
            tag.put("consumedFluid", consumedFluid.save(provider));
        }
    }

    @Override
    public void loadPacketNBT(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadPacketNBT(tag, provider);

        if(tag.contains("cast")) {
            castInv.deserializeNBT(provider, tag.getCompound("cast"));
        }
        if(tag.contains("upgraders")) {
            upgradeHandler.deserializeNBT(provider, tag.getCompound("upgraders"));
        }
        if(tag.contains("coolingTime")) {
            coolingTime = tag.getInt("coolingTime");
        }
        if(tag.contains("maxCoolingTime")) {
            maxCoolingTime = tag.getInt("maxCoolingTime");
        }
        if(tag.contains("recipe")) {
            recipe = getRecipeSerializer().codec().decoder().decode(NbtOps.INSTANCE, tag.getCompound("recipe"))
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
            sync(serverLevel);
        }
        setChanged();
    }
}
