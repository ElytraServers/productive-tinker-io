package cn.elytra.mod.productive_tinker_io.common.recipe;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.dataComponent.SpeedUpgradeComponent;
import cn.elytra.mod.productive_tinker_io.common.recipe.util.CraftingInputUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpeedUpgradeRecipe extends CustomRecipe {

    public static final RecipeSerializer<SpeedUpgradeRecipe> SERIALIZER = RecipeSerializer.register("speed_upgrade", new SimpleCraftingRecipeSerializer<>(SpeedUpgradeRecipe::new));

    private static final double FACTOR_INCREMENT = 0.1;

    public SpeedUpgradeRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static boolean isIceKind(ItemStack stack) {
        return stack.is(Items.ICE) || stack.is(Items.PACKED_ICE) || stack.is(Items.BLUE_ICE);
    }

    @Override
    public boolean matches(@NotNull CraftingInput craftingInput, @NotNull Level level) {
        ItemStack speedUpgradeItem = CraftingInputUtils.singleOrNull(craftingInput, i -> i.is(ProductiveTinkerIo.SPEED_UPGRADE));
        if(speedUpgradeItem == null) return false;
        int packedIceCount = CraftingInputUtils.count(craftingInput, SpeedUpgradeRecipe::isIceKind);
        return packedIceCount > 0;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput craftingInput, @Nullable HolderLookup.Provider provider) {
        ItemStack speedUpgradeItem = CraftingInputUtils.singleOrNull(craftingInput, i -> i.is(ProductiveTinkerIo.SPEED_UPGRADE));
        if(speedUpgradeItem == null) return ItemStack.EMPTY;
        int packedIceCount = CraftingInputUtils.count(craftingInput, SpeedUpgradeRecipe::isIceKind);
        if(packedIceCount <= 0) return ItemStack.EMPTY;
        ItemStack result = speedUpgradeItem.copy();
        result.update(ProductiveTinkerIo.SPEED_UPGRADE_COMPONENT.get(), SpeedUpgradeComponent.of(0.0), suc -> {
            // increase the value by n over 10 of the remaining
            // where n should always be less than 10, so it won't ever reach to the max
            double remainingIncrement = 1.0 - suc.getClampedValue();
            double increment = remainingIncrement * (packedIceCount / 10.0) * FACTOR_INCREMENT;
            return suc.copyIncreased(increment);
        });
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        // slots should be less than 10, so 9
        return x * y <= 9;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

}
