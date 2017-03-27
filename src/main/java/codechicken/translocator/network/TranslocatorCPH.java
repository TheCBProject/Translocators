package codechicken.translocator.network;

import codechicken.lib.inventory.InventorySimple;
import codechicken.lib.packet.ICustomPacketHandler.IClientPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.ClientUtils;
import codechicken.translocator.Translocator;
import codechicken.translocator.client.gui.GuiTranslocator;
import codechicken.translocator.container.ContainerItemTranslocator;
import codechicken.translocator.tile.TileTranslocator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;

public class TranslocatorCPH implements IClientPacketHandler {
    public static Object channel = Translocator.instance;

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, INetHandlerPlayClient handler) {
        switch (packet.getType()) {
        case 1:
        case 2:
        case 3:
            TileEntity tile = mc.world.getTileEntity(packet.readPos());
            if (tile instanceof TileTranslocator) {
                ((TileTranslocator) tile).handlePacket(packet);
            }
            break;
        case 4:
            int windowId = packet.readUByte();
            GuiTranslocator gui = new GuiTranslocator(new ContainerItemTranslocator(new InventorySimple(9, packet.readUShort(), packet.readString()), mc.player.inventory));
            ClientUtils.openSMPGui(windowId, gui);
            break;
        case 5:
            mc.player.openContainer.putStackInSlot(packet.readUByte(), packet.readItemStack());
            break;
        }
    }
}
