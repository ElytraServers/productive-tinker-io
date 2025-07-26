package cn.elytra.mod.productive_tinker_io.client.screen;

import cn.elytra.mod.productive_tinker_io.ProductiveTinkerIo;
import cn.elytra.mod.productive_tinker_io.common.menu.BasinMenu;
import cn.elytra.mod.productive_tinker_io.network.payload.EmptyTankButtonPacket;
import cy.jdkdigital.productivelib.util.FluidContainerUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BasinScreen extends AbstractContainerScreen<BasinMenu> {

    private static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(ProductiveTinkerIo.MODID, "textures/gui/basin.png");

    private Button emptyTankButton;

    public BasinScreen(BasinMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(emptyTankButton = new Button(getGuiLeft() - 20, getGuiTop() + getYSize() - 150, 20, 20, Component.empty(), (btn) -> {
            PacketDistributor.sendToServer(new EmptyTankButtonPacket(menu.getBlockPos()));
        }, Supplier::get) {
            {
                this.active = false;
            }

            @Override
            protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                guiGraphics.blit(GUI, getX(), getY(), 178, 82, 20, 20);
            }
        });
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        // background
        guiGraphics.blit(GUI, getGuiLeft(), getGuiTop(), 0, 0, getXSize(), getYSize());

        // draw arrow
        int arrowLength = menu.getProgressBarLength(24);
        guiGraphics.blit(GUI, getGuiLeft() + 93, getGuiTop() + 33, 176, 0, arrowLength == 0 ? 0 : 24 - arrowLength, 17);

        // draw fluids
        int fluidAmount = menu.getFluidBarLength(52);
        FluidContainerUtil.renderTiledFluid(guiGraphics, this, menu.getContainedFluid(), 26, 15 + 52 - fluidAmount, 12, fluidAmount, 0);

        // draw mode (draw on top of the default)
        if(menu.isBasinMode()) {
            guiGraphics.blit(GUI, getGuiLeft() + 77, getGuiTop() + 52, 177, 104, 16, 16);
            guiGraphics.blit(GUI, getGuiLeft() + 59, getGuiTop() + 52, 177, 121, 16, 16);
        }

        // update button
        emptyTankButton.active = Screen.hasShiftDown();
    }

}
