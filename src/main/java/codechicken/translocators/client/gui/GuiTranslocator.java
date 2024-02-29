package codechicken.translocators.client.gui;

import codechicken.translocators.container.ContainerItemTranslocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
    public void render(PoseStack mStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(mStack);
        super.render(mStack, mouseX, mouseY, partialTicks);
        renderTooltip(mStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack mStack, float f, int i, int j) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        blit(mStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
