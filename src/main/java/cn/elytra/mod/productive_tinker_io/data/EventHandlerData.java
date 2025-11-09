package cn.elytra.mod.productive_tinker_io.data;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import cy.jdkdigital.productivemetalworks.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

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
                    add(ProductiveTinkerIo.SPEED_UPGRADE.get(), "Speed Upgrade");
                    add(ProductiveTinkerIo.BASIN_UPGRADE.get(), "Basin Upgrade");
                }
            });

            gen.addProvider(event.includeClient(), new BlockStateProvider(output, MODID, helper) {
                @Override
                protected void registerStatesAndModels() {
                    simpleBlockWithItem(ProductiveTinkerIo.BASIN_BLOCK.get(), models().cubeTop(name(ProductiveTinkerIo.BASIN_BLOCK.get()), ResourceLocation.fromNamespaceAndPath(MODID, "block/smart_output_side"), ResourceLocation.fromNamespaceAndPath(MODID, "block/smart_output_top")));
                }
            });

            gen.addProvider(event.includeClient(), new ItemModelProvider(output, MODID, helper) {
                @Override
                protected void registerModels() {
                    basicItem(ProductiveTinkerIo.SPEED_UPGRADE.get());
                    basicItem(ProductiveTinkerIo.BASIN_UPGRADE.get());
                }
            });

            gen.addProvider(event.includeServer(), new RecipeProvider(output, provider) {
                @Override
                protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProductiveTinkerIo.BASIN_BLOCK.get())
                            .pattern("bib")
                            .pattern("i i")
                            .pattern("bib")
                            .define('b', ModTags.Items.FIRE_BRICKS)
                            .define('i', Blocks.ICE)
                            .unlockedBy(getHasName(ProductiveTinkerIo.BASIN_BLOCK.get()), has(ProductiveTinkerIo.BASIN_BLOCK.get()))
                            .save(recipeOutput);

                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProductiveTinkerIo.SPEED_UPGRADE.get())
                            .pattern(" i ")
                            .pattern("i i")
                            .pattern(" i ")
                            .define('i', Blocks.ICE)
                            .unlockedBy(getHasName(ProductiveTinkerIo.BASIN_UPGRADE.get()), has(ProductiveTinkerIo.BASIN_UPGRADE.get()))
                            .save(recipeOutput);

                    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ProductiveTinkerIo.BASIN_UPGRADE.get())
                            .requires(MetalworksRegistrator.CASTING_BASIN.get())
                            .unlockedBy(getHasName(ProductiveTinkerIo.BASIN_UPGRADE.get()), has(ProductiveTinkerIo.BASIN_UPGRADE.get()))
                            .save(recipeOutput);
                }
            });
        }
    }

}
