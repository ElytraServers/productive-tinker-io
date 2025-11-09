package cn.elytra.mod.productive_tinker_io.common;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.dataComponent.BasinUpgradeComponent;
import cn.elytra.mod.productive_tinker_io.common.dataComponent.SpeedUpgradeComponent;
import cn.elytra.mod.productive_tinker_io.network.payload.EmptyTankButtonPacket;
import cy.jdkdigital.productivelib.registry.LibItems;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        List<Component> tooltips = event.getToolTip();
        Consumer<Component> consumer = tooltips::add;
        // basin upgrade
        itemStack.addToTooltip(ProductiveTinkerIo.BASIN_UPGRADE_COMPONENT, event.getContext(), consumer, event.getFlags());
        // speed upgrade
        itemStack.addToTooltip(ProductiveTinkerIo.SPEED_UPGRADE_COMPONENT, event.getContext(), consumer, event.getFlags());
    }

    @SubscribeEvent
    public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        // basin upgrade
        event.modify(MetalworksRegistrator.CASTING_BASIN.get(), builder ->
                builder.set(ProductiveTinkerIo.BASIN_UPGRADE_COMPONENT.get(), BasinUpgradeComponent.instance()));
        // speed upgrade
        event.modify(LibItems.UPGRADE_TIME.get(), builder ->
                builder.set(ProductiveTinkerIo.SPEED_UPGRADE_COMPONENT.get(), new SpeedUpgradeComponent(0.2)));
        event.modify(LibItems.UPGRADE_TIME_2.get(), builder ->
                builder.set(ProductiveTinkerIo.SPEED_UPGRADE_COMPONENT.get(), new SpeedUpgradeComponent(0.7)));
    }

}
