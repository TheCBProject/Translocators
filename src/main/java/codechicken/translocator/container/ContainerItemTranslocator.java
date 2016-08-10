package codechicken.translocator.container;

import codechicken.core.inventory.ContainerExtended;
import codechicken.core.inventory.SlotDummy;
import codechicken.lib.inventory.InventorySimple;
import codechicken.lib.packet.PacketCustom;
import codechicken.translocator.network.TranslocatorSPH;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ContainerItemTranslocator extends ContainerExtended {
    IInventory inv;

    public ContainerItemTranslocator(InventorySimple inv, InventoryPlayer playerInv) {
        this.inv = inv;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                this.addSlotToContainer(new SlotDummy(inv, y + x * 3, 62 + y * 18, 17 + x * 18, inv.limit));
            }
        }

        bindPlayerInventory(playerInv);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return !(slot instanceof SlotDummy);
    }

    public String getName() {
        return inv.getName();
    }

    @Override
    public void sendLargeStack(ItemStack stack, int slot, List<EntityPlayerMP> players) {
        PacketCustom packet = new PacketCustom(TranslocatorSPH.channel, 5);
        packet.writeByte(slot);
        packet.writeItemStack(stack);

        for (EntityPlayerMP player : players) {
            packet.sendToPlayer(player);
        }
    }
}
