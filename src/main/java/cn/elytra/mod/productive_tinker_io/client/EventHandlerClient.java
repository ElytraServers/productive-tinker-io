package cn.elytra.mod.productive_tinker_io.client;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.client.screen.BasinScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ProductiveTinkerIo.MODID, value = Dist.CLIENT)
public class EventHandlerClient {

    @SubscribeEvent
    public static void registerMenus(RegisterMenuScreensEvent event) {
        event.register(ProductiveTinkerIo.BASIN_MENU_TYPE.get(), BasinScreen::new);
    }

}
