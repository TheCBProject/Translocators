package codechicken.translocators.network;

import codechicken.lib.packet.ICustomPacketHandler.IClientPacketHandler;
import codechicken.lib.packet.PacketCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import static codechicken.translocators.network.TranslocatorNetwork.C_CRAFTING_GRID_UPDATE;
import static codechicken.translocators.network.TranslocatorNetwork.C_FILTER_GUI_SET_SLOT;

public class TranslocatorCPH implements IClientPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, ClientPacketListener handler) {
        switch (packet.getType()) {
            case C_FILTER_GUI_SET_SLOT:
                mc.player.containerMenu.getSlot(packet.readUByte()).set(packet.readItemStack());
                break;
            case C_CRAFTING_GRID_UPDATE:
                break;
        }
    }
}
