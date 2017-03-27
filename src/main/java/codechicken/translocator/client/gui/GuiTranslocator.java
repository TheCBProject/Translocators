package codechicken.translocator.client.gui;

import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.FontUtils;
import codechicken.lib.util.ItemUtils;
import codechicken.translocator.container.ContainerItemTranslocator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public class GuiTranslocator extends GuiContainer {

    public GuiTranslocator(Container par1Container) {
        super(par1Container);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0);
        GlStateManager.color(1, 1, 1, 1);

        TextureUtils.changeTexture("textures/gui/container/dispenser.png");
        drawTexturedModalRect(0, 0, 0, 0, xSize, ySize);

        fontRendererObj.drawString(I18n.translateToLocal(((ContainerItemTranslocator) inventorySlots).getName()), 6, 6, 0x404040);
        fontRendererObj.drawString(I18n.translateToLocal("container.inventory"), 6, 72, 0x404040);
        GlStateManager.popMatrix();
    }

    //@Override //TODO NEI Method.
    public void drawSlotItem(Slot par1Slot, ItemStack itemstack, int i, int j, String s) {
        itemRender.renderItemAndEffectIntoGUI(itemstack, i, j);
        FontUtils.drawItemQuantity(i, j, itemstack, null, 0);
        itemRender.renderItemOverlayIntoGUI(fontRendererObj, ItemUtils.copyStack(itemstack, 1), i, j, null);
    }
}
