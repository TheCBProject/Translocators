package codechicken.translocators.network;

import codechicken.lib.packet.ICustomPacketHandler.IServerPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.tile.TileCraftingGrid;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import static codechicken.translocators.network.TranslocatorNetwork.S_CRAFTING_GRID_EXECUTE;
import static codechicken.translocators.network.TranslocatorNetwork.S_CRAFTING_GRID_PLACE;

public class TranslocatorSPH implements IServerPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, ServerPlayer sender, ServerGamePacketListenerImpl handler) {
        switch (packet.getType()) {
            case S_CRAFTING_GRID_PLACE -> TranslocatorsModContent.blockCraftingGrid.get().placeBlock(sender.level(), sender, packet.readPos(), packet.readDirection());
            case S_CRAFTING_GRID_EXECUTE -> {
                if (sender.level().getBlockEntity(packet.readPos()) instanceof TileCraftingGrid tile) {
                    tile.craft(sender);
                }
            }
        }
    }
}
