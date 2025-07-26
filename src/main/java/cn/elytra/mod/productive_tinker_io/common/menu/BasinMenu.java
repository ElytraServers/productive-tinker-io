package cn.elytra.mod.productive_tinker_io.common.menu;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.blockEntity.BasinBlockEntity;
import cy.jdkdigital.productivelib.container.AbstractContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.Objects;

public class BasinMenu extends AbstractContainer {

    private final Inventory playerInventory;
    private final BasinBlockEntity blockEntity;

    public BasinMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, getBlockEntity(playerInventory, data));
    }

    private static BasinBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null!");
        Objects.requireNonNull(data, "data cannot be null!");
        if(playerInventory.player.level().getBlockEntity(data.readBlockPos()) instanceof BasinBlockEntity blockEntity) {
            return blockEntity;
        }
        throw new IllegalStateException("Incorrect block entity!");
    }

    public BasinMenu(int windowId, Inventory playerInventory, BasinBlockEntity blockEntity) {
        super(ProductiveTinkerIo.BASIN_MENU_TYPE.get(), windowId);
        this.playerInventory = playerInventory;
        this.blockEntity = blockEntity;

        addDataSlots(new ContainerData() {
            @Override
            public int get(int i) {
                return switch(i) {
                    case 0 -> blockEntity.coolingTime;
                    case 1 -> blockEntity.maxCoolingTime;
                    default -> throw new IllegalStateException("Unknown identifier " + i);
                };
            }

            @Override
            public void set(int i, int value) {
                switch(i) {
                    case 0 -> blockEntity.coolingTime = value;
                    case 1 -> blockEntity.maxCoolingTime = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        addSlot(new SlotItemHandler(blockEntity.castInv, 0, 68, 33));
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 128, 34));
        addSlot(new SlotItemHandler(blockEntity.upgradeHandler, 0, 153, 25));
        addSlot(new SlotItemHandler(blockEntity.upgradeHandler, 1, 153, 43));

        layoutPlayerInventorySlots(playerInventory, 0, 8, 84);
    }

    @Override
    protected BlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.getBlockPos().distSqr(player.blockPosition()) <= 64 && !blockEntity.isRemoved();
    }

    public int getProgressBarLength(int maxLength) {
        return (int) ((maxLength * 1.0 * blockEntity.coolingTime) / Math.max(1, blockEntity.maxCoolingTime));
    }

    public int getFluidBarLength(int maxLength) {
        double value = ((maxLength * 1.0 * blockEntity.fluidHandler.getFluidAmount()) / Math.max(1, blockEntity.fluidHandler.getCapacity()));
        return value < 1 && value > 0 ? 1 : (int) value; // keep some remaining if they're less than 1
    }

    public FluidStack getContainedFluid() {
        return blockEntity.fluidHandler.getFluid();
    }

    public boolean isBasinMode() {
        return blockEntity.isBasinMode();
    }
}
