package codechicken.translocator.tile;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.inventory.InventoryRange;
import codechicken.lib.inventory.InventorySimple;
import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.math.MathHelper;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.util.ArrayUtils;
import codechicken.lib.util.ItemUtils;
import codechicken.lib.util.ServerUtils;
import codechicken.translocator.container.ContainerItemTranslocator;
import codechicken.translocator.handler.ConfigurationHandler;
import codechicken.translocator.network.TranslocatorSPH;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.LinkedList;

public class TileItemTranslocator extends TileTranslocator {

    public class ItemAttachment extends Attachment {

        boolean regulate = false;
        boolean a_powering = false;
        boolean signal = false;

        ItemStack[] filters;

        public ItemAttachment(int side) {

            super(side);
            filters = new ItemStack[9];
            ArrayUtils.fillArray(filters, ItemStack.EMPTY);
        }

        public void setPowering(boolean b) {

            if ((signal || !b) && b != a_powering) {
                a_powering = b;
                BlockPos pos = new BlockPos(TileItemTranslocator.this.getPos());
                world.notifyNeighborsOfStateChange(pos, Blocks.REDSTONE_WIRE, true);
                pos.offset(EnumFacing.VALUES[side]);
                world.notifyNeighborsOfStateChange(pos, Blocks.REDSTONE_WIRE, true);
                markUpdate();
            }
        }

        @Override
        public boolean activate(EntityPlayer player, int subPart) {

            ItemStack held = player.inventory.getCurrentItem();
            if (held.isEmpty()) {
                return super.activate(player, subPart);
            } else if (ItemUtils.areStacksSameType(held, ConfigurationHandler.nugget) && !regulate) {
                regulate = true;

                if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
                markUpdate();
                return true;
            } else if (held.getItem() == Items.IRON_INGOT && !signal) {
                signal = true;

                if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
                markUpdate();
                return true;
            } else {
                return super.activate(player, subPart);
            }
        }

        @Override
        public void stripModifiers() {

            super.stripModifiers();
            if (regulate) {
                regulate = false;
                dropItem(ItemUtils.copyStack(ConfigurationHandler.nugget, 1));
            }
            if (signal) {
                setPowering(false);
                signal = false;
                dropItem(new ItemStack(Items.IRON_INGOT));
            }
        }

        @Override
        public Collection<ItemStack> getDrops(IBlockState state) {

            Collection<ItemStack> stuff = super.getDrops(state);
            if (regulate) {
                stuff.add(ItemUtils.copyStack(ConfigurationHandler.nugget, 1));
            }
            if (signal) {
                stuff.add(new ItemStack(Items.IRON_INGOT));
            }
            return stuff;
        }

        @Override
        public int getIconIndex() {

            int i = super.getIconIndex();
            if (regulate) {
                i |= 8;
            }
            if (signal) {
                i |= a_powering ? 32 : 16;
            }
            return i;
        }

        @Override
        public void openGui(EntityPlayer player) {

            openItemGui(player, filters, regulate ? "translocator.regulate" : "translocator.filter");
        }

        private void openItemGui(EntityPlayer player, ItemStack[] filters, final String string) {

            ServerUtils.openSMPContainer((EntityPlayerMP) player, new ContainerItemTranslocator(new InventorySimple(filters, filterStackLimit()) {
                @Override
                public void markDirty() {

                    markUpdate();
                }
            }, player.inventory), (player1, windowId) -> {
                PacketCustom packet = new PacketCustom(TranslocatorSPH.channel, 4);
                packet.writeByte(windowId);
                packet.writeShort(filterStackLimit());
                packet.writeString(string);

                packet.sendToPlayer(player1);
            });
        }

        private int filterStackLimit() {

            if (regulate) {
                return 65535;
            }
            if (fast) {
                return 64;
            }
            return 1;
        }

        @Override
        public void read(NBTTagCompound tag) {

            super.read(tag);
            regulate = tag.getBoolean("regulate");
            signal = tag.getBoolean("signal");
            a_powering = tag.getBoolean("powering");
            InventoryUtils.readItemStacksFromTag(filters, tag.getTagList("filters", 10));
        }

        @Override
        public NBTTagCompound write(NBTTagCompound tag) {

            tag.setBoolean("regulate", regulate);
            tag.setBoolean("signal", signal);
            tag.setBoolean("powering", a_powering);
            tag.setTag("filters", InventoryUtils.writeItemStacksToTag(filters, 65536));
            return super.write(tag);
        }

        @Override
        public void read(MCDataInput packet, boolean described) {

            super.read(packet, described);
            regulate = packet.readBoolean();
            signal = packet.readBoolean();
            a_powering = packet.readBoolean();
        }

        @Override
        public void write(MCDataOutput packet) {

            super.write(packet);
            packet.writeBoolean(regulate);
            packet.writeBoolean(signal);
            packet.writeBoolean(a_powering);
        }

        @Override
        public boolean canConnectRedstone() {
            return super.canConnectRedstone() || signal;
        }
    }

    public class MovingItem {

        public int src;
        public int dst;
        public ItemStack stack;

        public double a_progress;
        public double b_progress;

        public MovingItem(PacketCustom packet) {

            int b = packet.readUByte();
            src = b >> 4;
            dst = b & 0xF;
            stack = packet.readItemStack();
        }

        public boolean update() {

            if (a_progress >= 1) {
                return true;
            }

            b_progress = a_progress;
            a_progress = MathHelper.approachLinear(a_progress, 1, 0.2);
            return false;
        }
    }

    public LinkedList<MovingItem> movingItems = new LinkedList<>();

    @Override
    public void readFromNBT(NBTTagCompound tag) {

        super.readFromNBT(tag);
        if (tag.hasKey("items")) {
            for (Attachment a : attachments) {
                if (a != null) {
                    InventoryUtils.readItemStacksFromTag(((ItemAttachment) a).filters, tag.getTagList("items", 10));
                }
            }
        }
    }

    @Override
    public void createAttachment(int side) {

        attachments[side] = new ItemAttachment(side);
    }

    @Override
    public void update() {

        super.update();
        if (world.isRemote) {
            movingItems.removeIf(MovingItem::update);
        } else {
            //move those items
            InventoryRange[] attached = new InventoryRange[6];

            for (int i = 0; i < 6; i++) {
                Attachment a = attachments[i];
                if (a == null) {
                    continue;
                }

                BlockPos invpos = pos.offset(EnumFacing.VALUES[i]);
                IInventory inv = InventoryUtils.getInventory(world, invpos);
                if (inv == null) {
                    harvestPart(i, true);
                    continue;
                }
                attached[i] = new InventoryRange(inv, i ^ 1);
            }

            for (int i = 0; i < 6; i++) {
                ItemAttachment ia = (ItemAttachment) attachments[i];
                if (ia == null || !ia.a_eject) {
                    continue;
                }

                int largestQuantity = 0;
                int largestSlot = 0;
                InventoryRange access = attached[i];
                //Find the largest stack and slot.
                for (int slot : access.slots) {
                    ItemStack stack = access.inv.getStackInSlot(slot);
                    if (stack.isEmpty() || !access.canExtractItem(slot, stack) || stack.getCount() == 0)//stack size 0 hack
                    {
                        continue;
                    }

                    int quantity = ia.fast ? stack.getCount() : 1;
                    if (quantity <= largestQuantity) {
                        continue;
                    }

                    quantity = Math.min(quantity, extractableAmount(stack, ia, access));
                    if (quantity <= largestQuantity) {
                        continue;
                    }

                    quantity = Math.min(quantity, insertAmount(stack, attached));
                    if (quantity <= largestQuantity) {
                        continue;
                    }

                    largestSlot = slot;
                    largestQuantity = quantity;
                }

                //Move stuff from the largest slot.
                if (largestQuantity > 0) {
                    //Copy and mutate the stack.
                    ItemStack move = ItemUtils.copyStack(access.inv.getStackInSlot(largestSlot), largestQuantity);
                    //Spread, prioritizing non redstone.
                    spreadOutput(move, i, false, attached);
                    spreadOutput(move, i, true, attached);

                    //Decrement the actual slot.
                    InventoryUtils.decrStackSize(access.inv, largestSlot, largestQuantity - move.getCount());
                }
            }

            //Set power levels.
            boolean allSatisfied = true;
            for (int i = 0; i < 6; i++) {
                ItemAttachment ia = (ItemAttachment) attachments[i];
                if (ia != null && !ia.a_eject) {
                    boolean b = isSatsified(ia, attached[i]);
                    ia.setPowering(b);
                    if (!b) {
                        allSatisfied = false;
                    }
                }
            }

            for (int i = 0; i < 6; i++) {
                ItemAttachment ia = (ItemAttachment) attachments[i];
                if (ia != null && ia.signal && ia.a_eject) {
                    ia.setPowering(allSatisfied || !canTransferFilter(ia, attached[i], attached));
                }
            }
        }
    }

    private boolean matches(ItemStack stack, ItemStack filter) {

        return stack.getItem() == filter.getItem() && (!filter.getHasSubtypes() || filter.getItemDamage() == stack.getItemDamage()) && ItemStack.areItemStackTagsEqual(filter, stack);
    }

    private boolean canTransferFilter(ItemAttachment ia, InventoryRange access, InventoryRange[] attached) {

        boolean filterSet = false;
        for (ItemStack filter : ia.filters) {
            if (!filter.isEmpty()) {
                filterSet = true;
                if ((!ia.regulate || countMatchingStacks(access, filter, false) > filterCount(ia, filter)) && insertAmount(filter, attached) > 0) {
                    return true;
                }
            }
        }

        return !filterSet;
    }

    private boolean isSatsified(ItemAttachment ia, InventoryRange access) {

        boolean filterSet = false;
        for (ItemStack filter : ia.filters) {
            if (!filter.isEmpty()) {
                filterSet = true;
                if (ia.regulate) {
                    if (countMatchingStacks(access, filter, !ia.a_eject) < filterCount(ia, filter)) {
                        return false;
                    }
                } else {
                    if (InventoryUtils.getInsertibleQuantity(access, filter) > 0) {
                        return false;
                    }
                }
            }
        }

        return filterSet || !hasEmptySpace(access);
    }

    private boolean hasEmptySpace(InventoryRange inv) {

        for (int slot : inv.slots) {
            ItemStack stack = inv.inv.getStackInSlot(slot);
            if (inv.canInsertItem(slot, new ItemStack(Items.DIAMOND)) && (stack.isEmpty() || stack.isStackable() && stack.getCount() < Math.min(stack.getMaxStackSize(), inv.inv.getInventoryStackLimit()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Grabs the amount specified in the filter for the stack provided.
     *
     * @param ia The attachment to check the filter of.
     * @param stack The stack to match against.
     * @return The requested amount, -1 if none, 0 means there is a filter but the item doesnt match.
     */
    private int filterCount(ItemAttachment ia, ItemStack stack) {

        boolean filterSet = false;
        int match = 0;
        for (ItemStack filter : ia.filters) {
            if (!filter.isEmpty()) {
                //k, we are filtering.
                filterSet = true;
                if (matches(stack, filter)) {
                    //Ok, inc match.
                    match += filter.getCount();
                }
            }
        }

        return filterSet ? match : -1;
    }

    private void spreadOutput(ItemStack move, int src, boolean rspass, InventoryRange[] attached) {

        //If there is nothing don't do the do.
        if (move.getCount() == 0) {
            return;
        }

        int outputCount = 0;
        int[] outputQuantities = new int[6];
        for (int i = 0; i < 6; i++) {
            ItemAttachment ia = (ItemAttachment) attachments[i];
            if (ia != null && !ia.a_eject && ia.redstone == rspass) {
                outputQuantities[i] = insertAmount(move, ia, attached[i]);
                if (outputQuantities[i] > 0) {
                    outputCount++;
                }
            }
        }

        for (int dst = 0; dst < 6 && move.getCount() > 0; dst++) {
            int qty = outputQuantities[dst];
            if (qty <= 0) {
                continue;
            }

            qty = Math.min(qty, move.getCount() / outputCount + world.rand.nextInt(move.getCount() % outputCount + 1));
            outputCount--;

            if (qty == 0) {
                continue;
            }

            InventoryRange range = attached[dst];
            ItemStack add = ItemUtils.copyStack(move, qty);
            InventoryUtils.insertItem(range, add, false);
            move.shrink(qty);

            sendTransferPacket(src, dst, add);
        }
    }

    /**
     * Counts the matching stacks.
     * Checks for insertion or extraction.
     *
     * @param inv The inventory.
     * @param filter What we are checking for.
     * @param insert If we are checking for insertion or extraction.
     * @return The total number of items of the specified filter type.
     */
    private int countMatchingStacks(InventoryRange inv, ItemStack filter, boolean insert) {

        int c = 0;
        //For all the slots in the inventory.
        for (int slot : inv.slots) {
            //Get the stack.
            ItemStack stack = inv.inv.getStackInSlot(slot);
            if (!stack.isEmpty() && matches(filter, stack) && (insert ? inv.canInsertItem(slot, stack) : inv.canExtractItem(slot, stack))) {
                c += stack.getCount();
            }
        }
        return c;
    }

    private void sendTransferPacket(int i, int j, ItemStack add) {

        PacketCustom packet = new PacketCustom(TranslocatorSPH.channel, 2);
        packet.writePos(getPos());
        packet.writeByte(i << 4 | j);
        packet.writeItemStack(add);
        packet.sendToChunk(world, getPos().getX() >> 4, getPos().getZ() >> 4);
    }

    private int insertAmount(ItemStack stack, InventoryRange[] attached) {

        int insertAmount = 0;
        for (int i = 0; i < 6; i++) {
            ItemAttachment ia = (ItemAttachment) attachments[i];
            if (ia == null || ia.a_eject) {
                continue;
            }

            insertAmount += insertAmount(stack, ia, attached[i]);
        }
        return insertAmount;
    }

    private int insertAmount(ItemStack stack, ItemAttachment ia, InventoryRange range) {

        int filter = filterCount(ia, stack);
        if (filter == 0) {
            return 0;
        }

        int fit = InventoryUtils.getInsertibleQuantity(range, stack);
        if (fit == 0) {
            return 0;
        }

        if (ia.regulate && filter > 0) {
            fit = Math.min(fit, filter - countMatchingStacks(range, stack, true));
        }

        return fit > 0 ? fit : 0;
    }

    /**
     * Gets the amount able to be extracted from the specified inventory.
     *
     * @param stack The stack we are counting.
     * @param ia The attachment we are.
     * @param range The inventory we are extracting from.
     * @return The total extractable amount for the specified ItemStack.
     */
    private int extractableAmount(ItemStack stack, ItemAttachment ia, InventoryRange range) {

        //Grab the filter.
        int filter = filterCount(ia, stack);
        //If we have a filter but this doesnt match.
        if (filter == 0) {
            //we are regulating, let it through, otherwise nope.
            return ia.regulate ? stack.getMaxStackSize() : 0;
        }

        //If the filter doesnt match, max otherwise filter.
        int qty = filter < 0 ? stack.getMaxStackSize() : filter;

        //If we are regulating and have a filter.
        if (ia.regulate && filter > 0) {
            //count how many things we can extract.
            qty = Math.min(qty, countMatchingStacks(range, stack, false) - filter);
        }

        //Clamp at 0.
        return qty > 0 ? qty : 0;
    }

    @Override
    public void handlePacket(PacketCustom packet) {

        if (packet.getType() == 2) {
            movingItems.add(new MovingItem(packet));
        } else {
            super.handlePacket(packet);
        }
    }

    @Override
    public int strongPowerLevel(EnumFacing facing) {

        ItemAttachment ia = (ItemAttachment) attachments[facing.ordinal()];
        if (ia != null && ia.a_powering) {
            return 15;
        }
        return 0;
    }
}
