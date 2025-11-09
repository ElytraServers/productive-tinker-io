package cn.elytra.mod.productive_tinker_io.common.dataComponent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Items with this data component is considered as Speed Upgrade of Smart Basin.
 * The factor is ranged from 0 to 1, which with higher value reduces more recipe time, {@code 1.0} will make recipes instantly finished.
 *
 * @param recipeTimeFactor the recipe time reduce factor
 */
public record SpeedUpgradeComponent(double recipeTimeFactor) implements TooltipProvider {
    public static final Codec<SpeedUpgradeComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("recipeTimeFactor").forGetter(SpeedUpgradeComponent::recipeTimeFactor)
            ).apply(instance, SpeedUpgradeComponent::new));
    public static final StreamCodec<FriendlyByteBuf, SpeedUpgradeComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SpeedUpgradeComponent::recipeTimeFactor,
            SpeedUpgradeComponent::new
    );

    public static SpeedUpgradeComponent of(final double recipeTimeFactor) {
        return new SpeedUpgradeComponent(recipeTimeFactor);
    }

    public SpeedUpgradeComponent copyIncreased(final double increment) {
        return new SpeedUpgradeComponent(this.recipeTimeFactor() + increment);
    }

    public double getClampedValue() {
        return Mth.clamp(recipeTimeFactor, 0, 1);
    }

    @Override
    public void addToTooltip(@NotNull Item.TooltipContext tooltipContext, @NotNull Consumer<Component> consumer, @NotNull TooltipFlag tooltipFlag) {
        consumer.accept(Component.translatable("productive_tinker_io.speed_upgrade.tooltip", Component.literal(String.format("%.2f%%", getClampedValue() * 100)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
    }
}
