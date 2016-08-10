package codechicken.translocator.network;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustom.IServerPacketHandler;
import codechicken.translocator.Translocator;
import codechicken.translocator.init.ModBlocks;
import codechicken.translocator.tile.TileCraftingGrid;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TranslocatorSPH implements IServerPacketHandler {
    public static Object channel = Translocator.instance;

    @Override
    public void handlePacket(PacketCustom packet, EntityPlayerMP sender, INetHandlerPlayServer handler) {
        switch (packet.getType()) {
        case 1:
            ModBlocks.blockCraftingGrid.placeBlock(sender.worldObj, sender, packet.readBlockPos(), EnumFacing.VALUES[packet.readUByte()]);
            break;
        case 2:
            TileEntity tile = sender.worldObj.getTileEntity(packet.readBlockPos());
            if (tile instanceof TileCraftingGrid) {
                ((TileCraftingGrid) tile).craft(sender);
            }
            break;
        }
    }
}
