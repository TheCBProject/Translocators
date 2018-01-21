package codechicken.translocator.part;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.inventory.InventorySimple;
import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.math.MathHelper;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.util.ArrayUtils;
import codechicken.lib.util.ItemUtils;
import codechicken.lib.util.ServerUtils;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.translocator.client.render.RenderTranslocator;
import codechicken.translocator.container.ContainerItemTranslocator;
import codechicken.translocator.handler.ConfigurationHandler;
import codechicken.translocator.init.ModItems;
import codechicken.translocator.network.TranslocatorSPH;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by covers1624 on 10/11/2017.
 */
public class ItemTranslocatorPart extends TranslocatorPart {

    public boolean regulate;
    public boolean signal;
    public boolean a_powering;

    public ItemStack[] filters;
    public List<MovingItem> movingItems = new LinkedList<>();

    public ItemTranslocatorPart() {
        filters = ArrayUtils.fill(new ItemStack[9], ItemStack.EMPTY);
    }

    @Override
    public ResourceLocation getType() {
        return new ResourceLocation("translocators", "item_translocator");
    }

    @Override
    public int getTType() {
        return 0;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.translocatorPart, 1, 0);
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
    public boolean canStay() {
        BlockPos pos = pos().offset(EnumFacing.VALUES[side]);
        TileEntity tile = world().getTileEntity(pos);
        return tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
    }

    @Override
    public void update() {
        super.update();
        if (world().isRemote) {
            movingItems.removeIf(MovingItem::update);
        } else {
            if (a_eject) {
                IItemHandler[] handlers = new IItemHandler[6];
                IItemHandler myHandler = InventoryUtils.getItemHandlerOrEmpty(world(), pos().offset(EnumFacing.VALUES[side]), side ^ 1);
                //Find the largest stack in the inventory.
                int largestSize = 0;
                int largestSlot = 0;
                for (int slot = 0; slot < myHandler.getSlots(); slot++) {
                    ItemStack stack = myHandler.getStackInSlot(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    int size = fast ? stack.getCount() : 1;
                    if (size <= largestSize) {
                        continue;
                    }
                    size = Math.min(size, getExtractableAmount(stack, this, myHandler));
                    if (size <= largestSize) {
                        continue;
                    }

                    largestSlot = slot;
                    largestSize = size;
                }
                //Is it worth continuing?
                if (largestSize > 0) {
                    //Grab all the adjacent handlers.
                    for (int i = 0; i < 6; i++) {
                        if (i == side) {
                            handlers[i] = myHandler;
                            continue;
                        }
                        handlers[i] = InventoryUtils.getItemHandlerOrEmpty(world(), pos().offset(EnumFacing.VALUES[i]), i ^ 1);
                    }
                    ItemStack move = myHandler.extractItem(largestSlot, largestSize, true);
                    move = move.copy();
                    List<MovingItem> transfers = new ArrayList<>();
                    spreadOutput(move, false, handlers, transfers);
                    spreadOutput(move, true, handlers, transfers);
                    myHandler.extractItem(largestSlot, largestSize - move.getCount(), false);
                    sendTransferPacket(transfers);
                }
            }

            //TODO, This needs to either go away or be heavily optimized.
//            if (!a_eject) {
//                setPowering(isSatisfied(handle));
//            } else if (signal) {
//                IItemHandler[] handlers = new IItemHandler[6];
//                IItemHandler myHandler = InventoryUtils.getItemHandlerOrEmpty(world(), pos().offset(EnumFacing.VALUES[side]), side ^ 1);
//                for (int i = 0; i < 6; i++) {
//                    if (i == side) {
//                        handlers[i] = myHandler;
//                        continue;
//                    }
//                    handlers[i] = InventoryUtils.getItemHandlerOrEmpty(world(), pos().offset(EnumFacing.VALUES[i]), i ^ 1);
//                }
//                setPowering(isSatisfied(myHandler) || !canTransferFilter(myHandler, handlers));
//            }
        }
    }

    private void sendTransferPacket(List<MovingItem> transfers) {
        MCDataOutput packet = getIncStream();
        packet.writeVarInt(transfers.size());
        for (MovingItem transfer : transfers) {
            packet.writeByte(transfer.dst);
            packet.writeItemStack(transfer.stack);
        }
    }

    private boolean canTransferFilter(IItemHandler access, IItemHandler[] attached) {

        boolean filterSet = false;
        for (ItemStack filter : filters) {
            if (!filter.isEmpty()) {
                filterSet = true;
                if ((!regulate || InventoryUtils.countMatchingStacks(access, filter, false) > filterCount(this, filter)) && getInsertableAmount(filter, this, attached) > 0) {
                    return true;
                }
            }
        }

        return !filterSet;
    }

    private boolean isSatisfied(IItemHandler handler) {
        boolean filterSet = false;
        for (ItemStack filter : filters) {
            if (!filter.isEmpty()) {
                filterSet = true;
                if (regulate) {
                    if (InventoryUtils.countMatchingStacks(handler, filter, !a_eject) < filterCount(this, filter)) {
                        return false;
                    }
                } else {
                    if (InventoryUtils.getInsertableQuantity(handler, filter) > 0) {
                        return false;
                    }
                }
            }
        }
        return filterSet || !hasEmptySpace(handler);
    }

    private boolean hasEmptySpace(IItemHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            boolean b = stack.isEmpty() || stack.isStackable() && stack.getCount() < Math.min(stack.getMaxStackSize(), handler.getSlotLimit(slot));
            if (InventoryUtils.canInsertStack(handler, slot, new ItemStack(Items.DIAMOND)) && b) {
                return true;
            }
        }
        return false;
    }

    private void spreadOutput(ItemStack move, boolean rspass, IItemHandler[] attached, List<MovingItem> transfers) {

        //If there is nothing don't do the do.
        if (move.getCount() == 0) {
            return;
        }

        int outputCount = 0;
        int[] outputQuantities = new int[6];
        for (int i = 0; i < 6; i++) {
            TMultiPart p = tile().partMap(i);
            if (p instanceof ItemTranslocatorPart) {
                ItemTranslocatorPart part = (ItemTranslocatorPart) p;
                if (!part.canEject() && part.redstone == rspass && i != side) {
                    outputQuantities[i] = getInsertableAmount(move, part, attached[i]);
                    if (outputQuantities[i] > 0) {
                        outputCount ++;
                    }
                }
            }
        }

        for (int dst = 0; dst < 6 && move.getCount() > 0; dst++) {
            int qty = outputQuantities[dst];
            if (qty <= 0) {
                continue;
            }

            qty = Math.min(qty, move.getCount() / outputCount + world().rand.nextInt(move.getCount() % outputCount + 1));
            outputCount--;

            if (qty == 0) {
                continue;
            }

            IItemHandler handler = attached[dst];
            ItemStack add = ItemUtils.copyStack(move, qty);
            ItemStack ret = InventoryUtils.insertItem(handler, add, false);
            int consumed = ret.isEmpty() ? move.getCount() : ret.getCount();
            move.shrink(consumed);

            transfers.add(new MovingItem(dst, add));
        }
    }

    /**
     * Grabs the amount specified in the filter for the stack provided.
     *
     * @param stack The stack to match against.
     * @return The requested amount, -1 if none, 0 means there is a filter but the item doesnt match.
     */
    private static int filterCount(ItemTranslocatorPart part, ItemStack stack) {

        boolean filterSet = false;
        int match = 0;
        for (ItemStack filter : part.filters) {
            if (!filter.isEmpty()) {
                //k, we are filtering.
                filterSet = true;
                if (ItemUtils.areStacksSameType(stack, filter)) {
                    //Ok, inc match.
                    match += filter.getCount();
                }
            }
        }

        return filterSet ? match : -1;
    }

    private static int getInsertableAmount(ItemStack stack, ItemTranslocatorPart me, IItemHandler[] handler) {
        int insertableAmount = 0;

        for (int i = 0; i < 6; i++) {
            TMultiPart p = me.tile().partMap(i);
            if (p instanceof ItemTranslocatorPart) {
                ItemTranslocatorPart part = (ItemTranslocatorPart) p;
                if (!part.canEject()) {
                    insertableAmount += getInsertableAmount(stack, part, handler[i]);
                }
            }
        }

        return insertableAmount;
    }

    private static int getInsertableAmount(ItemStack stack, ItemTranslocatorPart me, IItemHandler range) {

        int filter = filterCount(me, stack);
        if (filter == 0) {
            return 0;
        }

        int fit = InventoryUtils.getInsertableQuantity(range, stack);
        if (fit == 0) {
            return 0;
        }

        if (me.regulate && filter > 0) {
            fit = Math.min(fit, filter - InventoryUtils.countMatchingStacks(range, stack, true));
        }

        return fit > 0 ? fit : 0;
    }

    /**
     * Gets the amount able to be extracted from the specified inventory.
     *
     * @param stack   The stack we are counting.
     * @param handler The inventory we are extracting from.
     * @return The total extractable amount for the specified ItemStack.
     */
    private static int getExtractableAmount(ItemStack stack, ItemTranslocatorPart me, IItemHandler handler) {

        //Grab the filter.
        int filter = filterCount(me, stack);
        //If we have a filter but this doesnt match.
        if (filter == 0) {
            //we are regulating, let it through, otherwise nope.
            return me.regulate ? stack.getMaxStackSize() : 0;
        }

        //If the filter doesnt match, max otherwise filter.
        int qty = filter < 0 ? stack.getMaxStackSize() : filter;

        //If we are regulating and have a filter.
        if (me.regulate && filter > 0) {
            //count how many things we can extract.
            qty = Math.min(qty, InventoryUtils.countMatchingStacks(handler, stack, false) - filter);
        }

        //Clamp at 0.
        return qty > 0 ? qty : 0;
    }

    @Override
    public boolean activate(EntityPlayer player, CuboidRayTraceResult hit, ItemStack held, EnumHand hand) {
        if (world().isRemote) {
            return true;
        }
        ItemStack stack = player.getHeldItem(hand);
        if (ItemUtils.areStacksSameType(stack, ConfigurationHandler.nugget) && !regulate) {
            regulate = true;
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            markUpdate();
            return true;
        } else if (stack.getItem() == Items.IRON_INGOT && !signal) {
            signal = true;
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            markUpdate();
            return true;
        }
        return super.activate(player, hit, stack, hand);
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
    public void openGui(EntityPlayer player) {
        openItemGui(player, filters, regulate ? "translocator.regulate" : "translocator.filter");
    }

    private void openItemGui(EntityPlayer player, ItemStack[] filters, String string) {

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

    public void setPowering(boolean b) {

        if ((signal || !b) && b != a_powering) {
            a_powering = b;
            world().notifyNeighborsOfStateChange(pos(), Blocks.REDSTONE_WIRE, true);
            world().notifyNeighborsOfStateChange(pos().offset(EnumFacing.VALUES[side]), Blocks.REDSTONE_WIRE, true);
            markUpdate();
        }
    }

    @Override
    public void readIncUpdate(MCDataInput packet) {
        int expected = packet.readVarInt();
        for (int i = 0; i < expected; i++) {
            int dst = packet.readByte();
            ItemStack stack = packet.readItemStack();
            movingItems.add(new MovingItem(dst, stack));
        }
    }

    @Override
    public void save(NBTTagCompound tag) {
        super.save(tag);
        tag.setBoolean("regulate", regulate);
        tag.setBoolean("signal", signal);
        tag.setBoolean("powering", a_powering);
        tag.setTag("filters", InventoryUtils.writeItemStacksToTag(filters, 65536));
    }

    @Override
    public void load(NBTTagCompound tag) {
        super.load(tag);
        regulate = tag.getBoolean("regulate");
        signal = tag.getBoolean("signal");
        a_powering = tag.getBoolean("powering");
        InventoryUtils.readItemStacksFromTag(filters, tag.getTagList("filters", 10));
    }

    @Override
    public void writeDesc(MCDataOutput packet) {
        super.writeDesc(packet);
        packet.writeBoolean(regulate);
        packet.writeBoolean(signal);
        packet.writeBoolean(a_powering);
    }

    @Override
    public void readDesc(MCDataInput packet) {
        super.readDesc(packet);
        regulate = packet.readBoolean();
        signal = packet.readBoolean();
        a_powering = packet.readBoolean();
    }

    @Override
    public void renderDynamic(Vector3 pos, int pass, float frame) {
        RenderTranslocator.renderItem(this, pos, frame);
        super.renderDynamic(pos, pass, frame);
    }

    public class MovingItem {

        public int dst;
        public ItemStack stack;

        public double a_progress;
        public double b_progress;

        public MovingItem(int dst, ItemStack stack) {
            this.dst = dst;
            this.stack = stack;
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
}
