package codechicken.translocators.network;

import codechicken.lib.packet.ICustomPacketHandler.IServerPacketHandler;
import codechicken.lib.packet.PacketCustom;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.IServerPlayNetHandler;

import static codechicken.translocators.network.TranslocatorNetwork.S_CRAFTING_GRID_EXECUTE;
import static codechicken.translocators.network.TranslocatorNetwork.S_CRAFTING_GRID_PLACE;

public class TranslocatorSPH implements IServerPacketHandler {

    @Override
    public void handlePacket(PacketCustom packet, ServerPlayerEntity sender, IServerPlayNetHandler handler) {
        switch (packet.getType()) {
            case S_CRAFTING_GRID_PLACE:
                //ModBlocks.blockCraftingGrid.placeBlock(sender.world, sender, packet.readPos(), EnumFacing.VALUES[packet.readUByte()]);
                break;
            case S_CRAFTING_GRID_EXECUTE:
                //                TileEntity tile = sender.world.getTileEntity(packet.readPos());
                //                if (tile instanceof TileCraftingGrid) {
                //                    ((TileCraftingGrid) tile).craft(sender);
                //                }
                break;
        }
    }
}
