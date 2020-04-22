package codechicken.translocators.container;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.inventory.InventorySimple;
import codechicken.lib.inventory.container.ContainerExtended;
import codechicken.lib.inventory.container.SlotDummy;
import codechicken.lib.packet.PacketCustom;
import codechicken.translocators.init.ModContent;
import codechicken.translocators.network.TranslocatorNetwork;
import codechicken.translocators.network.TranslocatorSPH;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

import static codechicken.translocators.network.TranslocatorNetwork.C_FILTER_GUI_SET_SLOT;
import static codechicken.translocators.network.TranslocatorNetwork.NET_CHANNEL;

public class ContainerItemTranslocator extends ContainerExtended {

    private final IInventory inv;

    public ContainerItemTranslocator(int windowId, PlayerInventory playerInv, MCDataInput packet) {
        this(windowId, playerInv, new InventorySimple(packet.readUShort()));
    }

    public ContainerItemTranslocator(int windowId, PlayerInventory playerInv, InventorySimple inv) {
        super(ModContent.containerItemTranslocator, windowId);
        this.inv = inv;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                this.addSlot(new SlotDummy(inv, y + x * 3, 62 + y * 18, 17 + x * 18, inv.limit));
            }
        }

        bindPlayerInventory(playerInv);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return !(slot instanceof SlotDummy);
    }

    @Override
    public void sendLargeStack(ItemStack stack, int slot, List<ServerPlayerEntity> players) {
        PacketCustom packet = new PacketCustom(NET_CHANNEL, C_FILTER_GUI_SET_SLOT);
        packet.writeByte(slot);
        packet.writeItemStack(stack);
        players.forEach(packet::sendToPlayer);
    }
}
