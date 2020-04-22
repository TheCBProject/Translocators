package codechicken.translocators.client.gui;

import codechicken.lib.texture.TextureUtils;
import codechicken.translocators.container.ContainerItemTranslocator;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiTranslocator extends ContainerScreen<ContainerItemTranslocator> {

    public GuiTranslocator(ContainerItemTranslocator container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(guiLeft, guiTop, 0);
        GlStateManager.color4f(1, 1, 1, 1);

        TextureUtils.changeTexture("textures/gui/container/dispenser.png");
        blit(0, 0, 0, 0, xSize, ySize);

        font.drawString(title.getFormattedText(), 6, 6, 0x404040);
        font.drawString(playerInventory.getName().getFormattedText(), 6, 72, 0x404040);
        GlStateManager.popMatrix();
    }
}
