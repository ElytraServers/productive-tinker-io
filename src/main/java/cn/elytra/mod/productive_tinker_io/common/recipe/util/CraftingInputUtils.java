package cn.elytra.mod.productive_tinker_io.common.recipe.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import java.util.function.Predicate;

public class CraftingInputUtils {

    public static int count(CraftingInput input, Predicate<ItemStack> predicate) {
        return (int) input.items().stream().filter(predicate).count();
    }

    public static ItemStack singleOrNull(CraftingInput input, Predicate<ItemStack> predicate) {
        ItemStack result = null;
        for(ItemStack stack : input.items()) {
            if(predicate.test(stack)) {
                // found the 2nd matching item
                if(result != null) {
                    return null;
                } else {
                    result = stack;
                }
            }
        }
        return result;
    }
}
