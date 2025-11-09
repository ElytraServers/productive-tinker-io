package cn.elytra.mod.productive_tinker_io;

import cn.elytra.mod.productive_tinker_io.common.block.BasinBlock;
import cn.elytra.mod.productive_tinker_io.common.blockEntity.BasinBlockEntity;
import cn.elytra.mod.productive_tinker_io.common.dataComponent.BasinUpgradeComponent;
import cn.elytra.mod.productive_tinker_io.common.dataComponent.SpeedUpgradeComponent;
import cn.elytra.mod.productive_tinker_io.common.menu.BasinMenu;
import cn.elytra.mod.productive_tinker_io.common.recipe.SpeedUpgradeRecipe;
import com.mojang.logging.LogUtils;
import cy.jdkdigital.productivemetalworks.registry.MetalworksRegistrator;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(ProductiveTinkerIo.MODID)
public class ProductiveTinkerIo {

    public static final String MODID = "productive_tinker_io";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SpeedUpgradeComponent>> SPEED_UPGRADE_COMPONENT = DATA_COMPONENT_TYPES.register("speed_upgrade", () -> DataComponentType.<SpeedUpgradeComponent>builder().persistent(SpeedUpgradeComponent.CODEC).networkSynchronized(SpeedUpgradeComponent.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BasinUpgradeComponent>> BASIN_UPGRADE_COMPONENT = DATA_COMPONENT_TYPES.register("basin_upgrade", () -> DataComponentType.<BasinUpgradeComponent>builder().persistent(BasinUpgradeComponent.CODEC).networkSynchronized(BasinUpgradeComponent.STREAM_CODEC).build());

    public static final DeferredBlock<Block> BASIN_BLOCK = BLOCKS.register("basin", () -> new BasinBlock());
    public static final DeferredItem<BlockItem> BASIN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("basin", BASIN_BLOCK);
    public static final DeferredItem<Item> SPEED_UPGRADE = ITEMS.registerItem("speed_upgrade", (p) -> new Item(p.component(SPEED_UPGRADE_COMPONENT, SpeedUpgradeComponent.of(0.2))));
    public static final DeferredItem<Item> BASIN_UPGRADE = ITEMS.registerItem("basin_upgrade", (p) -> new Item(p.component(BASIN_UPGRADE_COMPONENT, BasinUpgradeComponent.instance())));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasinBlockEntity>> BASIN_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("basin", () -> BlockEntityType.Builder.of(BasinBlockEntity::new, BASIN_BLOCK.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<BasinMenu>> BASIN_MENU_TYPE = MENU_TYPES.register("basin", () -> IMenuTypeExtension.create(BasinMenu::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SpeedUpgradeRecipe>> SPEED_UPGRADE_RECIPE = RECIPE_SERIALIZERS.register("speed_upgrade", () -> SpeedUpgradeRecipe.SERIALIZER);

    public ProductiveTinkerIo(IEventBus modEventBus, ModContainer ignoredModContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        DATA_COMPONENT_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        LOGGER.info("Star our projects at https://github.com/ElytraServers");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTab() == MetalworksRegistrator.TAB.get()) {
            event.accept(BASIN_BLOCK_ITEM);
            event.accept(SPEED_UPGRADE);
            event.accept(BASIN_UPGRADE);

            ItemStack maxSpeedUpgrade = Util.make(SPEED_UPGRADE.toStack(), stack -> {
                stack.set(DataComponents.ITEM_NAME, Component.translatable(ProductiveTinkerIo.SPEED_UPGRADE.get().getDescriptionId() + ".max"));
                stack.set(DataComponents.RARITY, Rarity.EPIC);
                stack.set(SPEED_UPGRADE_COMPONENT.get(), SpeedUpgradeComponent.of(1.0));
            });
            event.accept(maxSpeedUpgrade);
        }
    }

}
