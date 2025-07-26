package cn.elytra.mod.productive_tinker_io;

import cn.elytra.mod.productive_tinker_io.common.block.BasinBlock;
import cn.elytra.mod.productive_tinker_io.common.blockEntity.BasinBlockEntity;
import cn.elytra.mod.productive_tinker_io.common.menu.BasinMenu;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
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

    public static final DeferredBlock<Block> BASIN_BLOCK = BLOCKS.register("basin", () -> new BasinBlock());
    public static final DeferredItem<BlockItem> BASIN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("basin", BASIN_BLOCK);
    // public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasinBlockEntity>> BASIN_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("basin", () -> BlockEntityType.Builder.of(BasinBlockEntity::new, BASIN_BLOCK.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<BasinMenu>> BASIN_MENU_TYPE = MENU_TYPES.register("basin", () -> IMenuTypeExtension.create(BasinMenu::new));

    public ProductiveTinkerIo(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(BASIN_BLOCK_ITEM);
        }
    }

}
