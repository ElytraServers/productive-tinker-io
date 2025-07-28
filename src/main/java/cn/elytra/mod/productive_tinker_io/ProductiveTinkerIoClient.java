package cn.elytra.mod.productive_tinker_io;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ProductiveTinkerIo.MODID, dist = Dist.CLIENT)
//@EventBusSubscriber(modid = ProductiveTinkerIo.MODID, value = Dist.CLIENT)
public class ProductiveTinkerIoClient {

    public ProductiveTinkerIoClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

}
