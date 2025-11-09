package cn.elytra.mod.productive_tinker_io.integration.emi.recipe;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.recipe.SpeedUpgradeRecipe;
import cn.elytra.mod.productive_tinker_io.util.Utils;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmiSpeedUpgradeRecipe extends EmiPatternCraftingRecipe {

    private static final Lazy<SpeedUpgradeRecipe> RECIPE = Lazy.of(() -> new SpeedUpgradeRecipe(CraftingBookCategory.MISC));

    private static final Lazy<List<Item>> ICE_LIST = Lazy.of(() -> List.of(Items.ICE, Items.PACKED_ICE, Items.BLUE_ICE));
    private static final Lazy<List<EmiStack>> ICE_EMI_LIST = Utils.mapLazy(ICE_LIST, l -> l.stream().map(EmiStack::of).toList());

    public EmiSpeedUpgradeRecipe(ResourceLocation id) {
        // wrapping the list with ArrayList to fix the error
        super(new ArrayList<>(ICE_EMI_LIST.get()), EmiStack.of(ProductiveTinkerIo.SPEED_UPGRADE), id);
    }

    private List<ItemStack> getItems(Random random) {
        NonNullList<ItemStack> list = NonNullList.withSize(9, ItemStack.EMPTY);
        // first slot is always speed upgrade
        list.set(0, new ItemStack(ProductiveTinkerIo.SPEED_UPGRADE.get()));
        // n ices to place (1-8)
        int n = random.nextInt(8) + 1;
        List<Item> iceList = ICE_LIST.get();
        for(int i = 0; i < n; i++) {
            // what kind of ice to place
            int iceListIdx = random.nextInt(iceList.size());
            list.set(i + 1, new ItemStack(iceList.get(iceListIdx)));
        }
        return list;
    }

    @Override
    public SlotWidget getInputWidget(int slot, int x, int y) {
        return new GeneratedSlotWidget(r -> {
            List<ItemStack> items = getItems(r);
            if(slot < 9) {
                return EmiStack.of(items.get(slot));
            }
            return EmiStack.EMPTY;
        }, unique, x, y);
    }

    @Override
    public SlotWidget getOutputWidget(int x, int y) {
        return new GeneratedSlotWidget(r -> {
            List<ItemStack> items = getItems(r);
            ItemStack result = RECIPE.get().assemble(CraftingInput.of(3, 3, items), null);
            return EmiStack.of(result);
        }, unique, x, y);
    }
}
