package cn.elytra.mod.productive_tinker_io.network.payload;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.blockEntity.BasinBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record EmptyTankButtonPacket(BlockPos blockPos) implements CustomPacketPayload {

    public static final Type<EmptyTankButtonPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ProductiveTinkerIo.MODID, "empty_tank_button"));
    public static final StreamCodec<ByteBuf, EmptyTankButtonPacket> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, c -> c.blockPos, EmptyTankButtonPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(@Nullable ServerPlayer serverPlayer) {
        if(serverPlayer != null && serverPlayer.level().getBlockEntity(blockPos) instanceof BasinBlockEntity basinBlockEntity) {
            basinBlockEntity.fluidHandler.setFluid(FluidStack.EMPTY);
            basinBlockEntity.markDirtyAndSync();
        }
    }
}
