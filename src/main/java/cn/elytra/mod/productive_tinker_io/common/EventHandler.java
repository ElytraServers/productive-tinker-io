package cn.elytra.mod.productive_tinker_io.common;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = ProductiveTinkerIo.MODID)
class EventHandler {

    @SubscribeEvent
    public static void registerBlockEntityCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ProductiveTinkerIo.BASIN_BLOCK_ENTITY.get(), (be, side) -> be.getFluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ProductiveTinkerIo.BASIN_BLOCK_ENTITY.get(), (be, side) -> be.getItemHandler());
    }

}
