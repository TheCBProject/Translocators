package codechicken.translocator.client.gui;

import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.render.FontUtils;
import codechicken.lib.render.TextureUtils;
import codechicken.translocator.container.ContainerItemTranslocator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.opengl.GL11;

public class GuiTranslocator extends GuiContainer {
    public GuiTranslocator(Container par1Container) {
        super(par1Container);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glPushMatrix();
        GL11.glTranslated(guiLeft, guiTop, 0);
        GL11.glColor4f(1, 1, 1, 1);

        TextureUtils.changeTexture("textures/gui/container/dispenser.png");
        drawTexturedModalRect(0, 0, 0, 0, xSize, ySize);

        fontRendererObj.drawString(I18n.translateToLocal(((ContainerItemTranslocator) inventorySlots).getName()), 6, 6, 0x404040);
        fontRendererObj.drawString(I18n.translateToLocal("container.inventory"), 6, 72, 0x404040);
        GL11.glPopMatrix();
    }

    //@Override //TODO NEI Method.
    public void drawSlotItem(Slot par1Slot, ItemStack itemstack, int i, int j, String s) {
        itemRender.renderItemAndEffectIntoGUI(itemstack, i, j);
        FontUtils.drawItemQuantity(i, j, itemstack, null, 0);
        itemRender.renderItemOverlayIntoGUI(fontRendererObj, InventoryUtils.copyStack(itemstack, 1), i, j, null);
    }
}
