package cn.elytra.mod.productive_tinker_io.integration.jei;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.recipe.SpeedUpgradeRecipe;
import cn.elytra.mod.productive_tinker_io.integration.jei.recipe.SpeedUpgradeRecipeExtension;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class ProductiveTinkerIoJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ProductiveTinkerIo.MODID, "jei");

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addExtension(SpeedUpgradeRecipe.class, new SpeedUpgradeRecipeExtension());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeHolder<CraftingRecipe> value = new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath(ProductiveTinkerIo.MODID, "speed_upgrade"), new SpeedUpgradeRecipe(CraftingBookCategory.MISC));
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(value));
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return UID;
    }

}
