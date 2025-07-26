package cn.elytra.mod.productive_tinker_io.integration.jade;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.blockEntity.BasinBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

import java.util.Optional;

public class BasinJadeProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, BasinJadeProvider.Data> {

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ProductiveTinkerIo.MODID, "basin");

    public static final BasinJadeProvider INSTANCE = new BasinJadeProvider();

    public record Data(boolean isCooling) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, Data::isCooling, Data::new);
    }

    private BasinJadeProvider() {
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        Optional<Data> optional = decodeFromData(blockAccessor);
        if(optional.isPresent()) {
            Data data = optional.get();
            if(data.isCooling()) {
                tooltip.add(Component.literal("Cooling"));
            }
        }
    }

    @Nullable
    @Override
    public BasinJadeProvider.Data streamData(BlockAccessor blockAccessor) {
        BasinBlockEntity blockEntity = (BasinBlockEntity) blockAccessor.getBlockEntity();
        return new Data(blockEntity.isCooling());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

}
