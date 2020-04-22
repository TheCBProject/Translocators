package codechicken.translocators.network;

import codechicken.lib.packet.ICustomPacketHandler.IClientPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.translocators.Translocators;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.IClientPlayNetHandler;

import static codechicken.translocators.network.TranslocatorNetwork.*;

public class TranslocatorCPH implements IClientPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, IClientPlayNetHandler handler) {
        switch (packet.getType()) {
            case C_FILTER_GUI_SET_SLOT:
                mc.player.openContainer.putStackInSlot(packet.readUByte(), packet.readItemStack());
                break;
            case C_CRAFTING_GRID_UPDATE:
                break;
        }
    }
}
