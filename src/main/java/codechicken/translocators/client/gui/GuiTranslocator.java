package codechicken.translocators.client.gui;

import codechicken.translocators.container.ContainerItemTranslocator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiTranslocator extends AbstractContainerScreen<ContainerItemTranslocator> {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("textures/gui/container/dispenser.png");

    public GuiTranslocator(ContainerItemTranslocator container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        titleLabelX = 6;
        titleLabelY = 6;
        inventoryLabelX = 6;
        inventoryLabelX = 72;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float f, int i, int j) {
        graphics.blit(BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
