package cn.elytra.mod.productive_tinker_io.integration.jei.recipe;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.recipe.SpeedUpgradeRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpeedUpgradeRecipeExtension implements ICraftingCategoryExtension<SpeedUpgradeRecipe> {
    @Override
    public void setRecipe(@NotNull RecipeHolder<SpeedUpgradeRecipe> recipeHolder, @NotNull IRecipeLayoutBuilder builder, @NotNull ICraftingGridHelper craftingGridHelper, @NotNull IFocusGroup focuses) {
        SpeedUpgradeRecipe value = recipeHolder.value();
        ItemStack speedUpgrade = new ItemStack(ProductiveTinkerIo.SPEED_UPGRADE.get());
        Ingredient upgradeInput = Ingredient.of(speedUpgrade);
        Ingredient iceInput = Ingredient.of(Items.ICE, Items.PACKED_ICE, Items.BLUE_ICE);
        ItemStack output = value.getOutput(speedUpgrade, 1);

        int width = getWidth(recipeHolder);
        int height = getHeight(recipeHolder);
        craftingGridHelper.createAndSetIngredients(builder, List.of(upgradeInput, iceInput), width, height);
        craftingGridHelper.createAndSetOutputs(builder, List.of(output));
    }
}
