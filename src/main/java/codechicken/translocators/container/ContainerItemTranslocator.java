package codechicken.translocators.container;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.inventory.InventorySimple;
import codechicken.lib.inventory.container.ContainerExtended;
import codechicken.lib.inventory.container.SlotDummy;
import codechicken.lib.packet.PacketCustom;
import codechicken.translocators.init.TranslocatorsModContent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static codechicken.translocators.network.TranslocatorNetwork.C_FILTER_GUI_SET_SLOT;
import static codechicken.translocators.network.TranslocatorNetwork.NET_CHANNEL;

public class ContainerItemTranslocator extends ContainerExtended {

    public ContainerItemTranslocator(int windowId, Inventory playerInv, MCDataInput packet) {
        this(windowId, playerInv, new InventorySimple(9, packet.readUShort()));
    }

    public ContainerItemTranslocator(int windowId, Inventory playerInv, InventorySimple inv) {
        super(TranslocatorsModContent.containerItemTranslocator.get(), windowId, playerInv);

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                this.addSlot(new SlotDummy(inv, y + x * 3, 62 + y * 18, 17 + x * 18, inv.limit));
            }
        }

        bindPlayerInventory(playerInv);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof SlotDummy);
    }

    @Override
    public void sendLargeStack(ItemStack stack, int slot, ServerPlayer player) {
        PacketCustom packet = new PacketCustom(NET_CHANNEL, C_FILTER_GUI_SET_SLOT);
        packet.writeByte(slot);
        packet.writeItemStack(stack);
        packet.sendToPlayer(player);
    }
}
