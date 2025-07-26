package cn.elytra.mod.productive_tinker_io.common;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.network.payload.EmptyTankButtonPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;

@EventBusSubscriber(modid = ProductiveTinkerIo.MODID)
class EventHandler {

    @SubscribeEvent
    public static void registerBlockEntityCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ProductiveTinkerIo.BASIN_BLOCK_ENTITY.get(), (be, side) -> be.getFluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ProductiveTinkerIo.BASIN_BLOCK_ENTITY.get(), (be, side) -> be.getItemHandler());
    }

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ModList.get().getModFileById(ProductiveTinkerIo.MODID).versionString());

        registrar.playToServer(EmptyTankButtonPacket.TYPE, EmptyTankButtonPacket.CODEC, onServer(EmptyTankButtonPacket::handle));
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> onServer(BiConsumer<T, ServerPlayer> handler) {
        return (payload, context) -> context.enqueueWork(() -> handler.accept(payload, (ServerPlayer) context.player()));
    }

}
