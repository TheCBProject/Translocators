package codechicken.translocators.client.gui;

import codechicken.lib.texture.TextureUtils;
import codechicken.translocators.container.ContainerItemTranslocator;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiTranslocator extends ContainerScreen<ContainerItemTranslocator> {

    public GuiTranslocator(ContainerItemTranslocator container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        titleLabelX = 6;
        titleLabelY = 6;
        inventoryLabelX = 6;
        inventoryLabelX = 72;
    }

    @Override
    public void render(MatrixStack mStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(mStack);
        super.render(mStack, mouseX, mouseY, partialTicks);
        renderTooltip(mStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack mStack, float f, int i, int j) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        TextureUtils.changeTexture("textures/gui/container/dispenser.png");
        blit(mStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
