package codechicken.translocators.network;

import codechicken.lib.inventory.InventorySimple;
import codechicken.lib.packet.ICustomPacketHandler.IClientPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.ClientUtils;
import codechicken.translocators.Translocator;
import codechicken.translocators.client.gui.GuiTranslocator;
import codechicken.translocators.container.ContainerItemTranslocator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.INetHandlerPlayClient;

public class TranslocatorCPH implements IClientPacketHandler {

    public static Object channel = Translocator.instance;

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, INetHandlerPlayClient handler) {
        switch (packet.getType()) {
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
