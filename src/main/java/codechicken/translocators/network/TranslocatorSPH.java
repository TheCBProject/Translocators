package codechicken.translocators.network;

import codechicken.lib.packet.ICustomPacketHandler.IServerPacketHandler;
import codechicken.lib.packet.PacketCustom;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.tile.TileCraftingGrid;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

import static codechicken.translocators.network.TranslocatorNetwork.S_CRAFTING_GRID_EXECUTE;
import static codechicken.translocators.network.TranslocatorNetwork.S_CRAFTING_GRID_PLACE;

public class TranslocatorSPH implements IServerPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, ServerPlayerEntity sender, IServerPlayNetHandler handler) {
        switch (packet.getType()) {
            case S_CRAFTING_GRID_PLACE:
                TranslocatorsModContent.blockCraftingGrid.get().placeBlock(sender.level, sender, packet.readPos(), packet.readDirection());
                break;
            case S_CRAFTING_GRID_EXECUTE:
                TileEntity tile = sender.level.getBlockEntity(packet.readPos());
                if (tile instanceof TileCraftingGrid) {
                    ((TileCraftingGrid) tile).craft(sender);
                }
                break;
        }
    }
}
