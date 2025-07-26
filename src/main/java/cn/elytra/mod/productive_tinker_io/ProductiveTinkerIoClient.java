package cn.elytra.mod.productive_tinker_io;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ProductiveTinkerIo.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ProductiveTinkerIo.MODID, value = Dist.CLIENT)
public class ProductiveTinkerIoClient {

    public ProductiveTinkerIoClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        ProductiveTinkerIo.LOGGER.info("HELLO FROM CLIENT SETUP");
        ProductiveTinkerIo.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
