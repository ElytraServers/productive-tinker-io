package cn.elytra.mod.productive_tinker_io.data.lootTable;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;

public class ProductiveTinkerIoBlockDrops extends BlockLootSubProvider {

    private static final Function<DeferredHolder<Block, ? extends Block>, Block> EXTRACT_VALUE = DeferredHolder::value;

    public ProductiveTinkerIoBlockDrops(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ProductiveTinkerIo.BLOCKS.getEntries().stream().map(EXTRACT_VALUE).toList();
    }

    @Override
    protected void generate() {
        this.dropSelf(ProductiveTinkerIo.BASIN_BLOCK.get());
    }
}
