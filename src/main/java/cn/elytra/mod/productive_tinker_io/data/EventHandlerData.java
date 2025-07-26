package cn.elytra.mod.productive_tinker_io.data;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

import static cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo.MODID;

@EventBusSubscriber(modid = MODID)
class EventHandlerData {

    private static String name(ItemLike itemLike) {
        return BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath();
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if(event.getModContainer().getModId().equals(MODID)) {
            DataGenerator gen = event.getGenerator();
            PackOutput output = gen.getPackOutput();
            CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();
            ExistingFileHelper helper = event.getExistingFileHelper();

            gen.addProvider(event.includeClient(), new LanguageProvider(output, MODID, "en_us") {
                @Override
                protected void addTranslations() {
                    add(ProductiveTinkerIo.BASIN_BLOCK.get(), "Smart Basin");
                }
            });

            gen.addProvider(event.includeClient(), new BlockStateProvider(output, MODID, helper) {
                @Override
                protected void registerStatesAndModels() {
                    // models().cubeTop()
                    simpleBlockWithItem(ProductiveTinkerIo.BASIN_BLOCK.get(), models().cubeTop(name(ProductiveTinkerIo.BASIN_BLOCK.get()), ResourceLocation.fromNamespaceAndPath(MODID, "block/smart_output_side"), ResourceLocation.fromNamespaceAndPath(MODID, "block/smart_output_top")));
                }
            });


        }
    }

}
