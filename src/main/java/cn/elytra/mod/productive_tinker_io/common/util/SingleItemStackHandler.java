package cn.elytra.mod.productive_tinker_io.common.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class SingleItemStackHandler extends ItemStackHandler {

    public SingleItemStackHandler() {
    }

    public SingleItemStackHandler(int size) {
        super(size);
        if(size != 1) {
            throw new IllegalArgumentException("only one stack is allowed, but accepted " + size);
        }
    }

    public SingleItemStackHandler(NonNullList<ItemStack> stacks) {
        super(stacks);
        if(stacks.size() != 1) {
            throw new IllegalArgumentException("only one stack is allowed, but accepted " + stacks.size() + " list");
        }
    }

    public ItemStack getItemStack() {
        return getStackInSlot(0);
    }

    public void setItemStack(ItemStack stack) {
        setStackInSlot(0, stack);
    }

}
