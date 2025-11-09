package cn.elytra.mod.productive_tinker_io.common.dataComponent;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Items with this data component is considered as Basin Upgrade of Smart Basin, which switches the mode to Basin Mode.
 */
public record BasinUpgradeComponent() implements TooltipProvider {
    private static final BasinUpgradeComponent INSTANCE = new BasinUpgradeComponent();

    public static final Codec<BasinUpgradeComponent> CODEC = Codec.unit(BasinUpgradeComponent::instance);
    public static final StreamCodec<FriendlyByteBuf, BasinUpgradeComponent> STREAM_CODEC = StreamCodec.unit(instance());

    public static BasinUpgradeComponent instance() {
        return INSTANCE;
    }

    @Override
    public void addToTooltip(@NotNull Item.TooltipContext tooltipContext, @NotNull Consumer<Component> consumer, @NotNull TooltipFlag tooltipFlag) {
        consumer.accept(Component.translatable("productive_tinker_io.basin_upgrade.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
