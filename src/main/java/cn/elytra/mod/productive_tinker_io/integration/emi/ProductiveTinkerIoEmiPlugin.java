package cn.elytra.mod.productive_tinker_io.integration.emi;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.integration.emi.recipe.EmiSpeedUpgradeRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.minecraft.resources.ResourceLocation;

@EmiEntrypoint
public class ProductiveTinkerIoEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipe(new EmiSpeedUpgradeRecipe(ResourceLocation.fromNamespaceAndPath(ProductiveTinkerIo.MODID, "/speed_upgrade")));
    }
}
