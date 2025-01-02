package codechicken.translocators.part;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.inventory.InventorySimple;
import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.inventory.container.CCLMenuType;
import codechicken.lib.math.MathHelper;
import codechicken.lib.util.ArrayUtils;
import codechicken.lib.util.ItemUtils;
import codechicken.lib.util.ServerUtils;
import codechicken.multipart.api.MultipartType;
import codechicken.multipart.api.part.MultiPart;
import codechicken.multipart.api.part.redstone.RedstonePart;
import codechicken.multipart.util.PartRayTraceResult;
import codechicken.translocators.container.ContainerItemTranslocator;
import codechicken.translocators.handler.ConfigHandler;
import codechicken.translocators.init.TranslocatorsModContent;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by covers1624 on 10/11/2017.
 */
public class ItemTranslocatorPart extends TranslocatorPart implements RedstonePart {

    public boolean regulate;
    public boolean signal;
    public boolean a_powering;

    public ItemStack regulateStack = ItemStack.EMPTY;
    public ItemStack[] filters = ArrayUtils.fill(new ItemStack[9], ItemStack.EMPTY);
    public List<MovingItem> movingItems = new LinkedList<>();

    @Override
    public MultipartType<?> getType() {
        return TranslocatorsModContent.itemTranslocatorPartType.get();
    }

    @Override
    public int getTType() {
        return 0;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(TranslocatorsModContent.itemTranslocatorItem.get(), 1);
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
        return capCache().getCapability(Capabilities.ItemHandler.BLOCK, Direction.BY_3D_DATA[side]) != null;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            movingItems.removeIf(MovingItem::update);
        } else {
            if (a_eject) {
                IItemHandler[] handlers = new IItemHandler[6];
                for (int i = 0; i < 6; i++) {
                    //Fill with empty if the translocator doesnt exist or is the incorrect type.
                    if (canInsert(i) || i == side) {
                        handlers[i] = capCache().getCapabilityOr(Capabilities.ItemHandler.BLOCK, Direction.BY_3D_DATA[i], EmptyItemHandler.INSTANCE);
                    } else {
                        handlers[i] = EmptyItemHandler.INSTANCE;
                    }
                }
                IItemHandler myHandler = handlers[side];
                //Find the largest insertable stack in the inventory.
                int largestSize = 0;
                int largestSlot = 0;
                for (int slot = 0; slot < myHandler.getSlots(); slot++) {
                    ItemStack stack = myHandler.getStackInSlot(slot);
                    if (stack.isEmpty() || !InventoryUtils.canExtractStack(myHandler, slot)) {
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

                    size = Math.min(size, getInsertableAmount(stack, this, handlers));
                    if (size <= largestSize) {
                        continue;
                    }

                    largestSlot = slot;
                    largestSize = size;
                }
                //Is it worth continuing?
                if (largestSize > 0) {
                    ItemStack move = myHandler.extractItem(largestSlot, largestSize, true);
                    move = move.copy();
                    int initialCount = move.getCount();
                    List<MovingItem> transfers = new ArrayList<>();
                    spreadOutput(move, false, handlers, transfers);
                    spreadOutput(move, true, handlers, transfers);
                    myHandler.extractItem(largestSlot, initialCount - move.getCount(), false);
                    sendTransferPacket(transfers);
                }
            }
            if (signal) {
                IItemHandler[] handlers = new IItemHandler[6];
                for (int i = 0; i < 6; i++) {
                    handlers[i] = capCache().getCapabilityOr(Capabilities.ItemHandler.BLOCK, Direction.BY_3D_DATA[side], EmptyItemHandler.INSTANCE);
                }
                if (a_eject) {
                    boolean allSatisfied = true;
                    for (int i = 0; i < 6; i++) {
                        MultiPart other = tile().getSlottedPart(i);
                        if (other instanceof ItemTranslocatorPart otherPart) {
                            if (!otherPart.a_eject) {
                                if (!otherPart.isSatisfied(handlers[i])) {
                                    allSatisfied = false;
                                }
                            }
                        }
                    }
                    setPowering(allSatisfied);
                } else {
                    setPowering(!canTransferFilter(handlers[side], handlers));
                }
            }
        }
    }

    private void sendTransferPacket(List<MovingItem> transfers) {
        sendIncUpdate(packet -> {
            packet.writeVarInt(transfers.size());
            for (MovingItem transfer : transfers) {
                packet.writeByte(transfer.dst);
                packet.writeItemStack(transfer.stack);
            }
        });
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
            MultiPart p = tile().getSlottedPart(i);
            if (p instanceof ItemTranslocatorPart part) {
                if (!part.canEject() && part.redstone == rspass && i != side) {
                    outputQuantities[i] = getInsertableAmount(move, part, attached[i]);
                    if (outputQuantities[i] > 0) {
                        outputCount++;
                    }
                }
            }
        }

        for (int dst = 0; dst < 6 && move.getCount() > 0; dst++) {
            int qty = outputQuantities[dst];
            if (qty <= 0) {
                continue;
            }

            qty = Math.min(qty, move.getCount() / outputCount + level().random.nextInt(move.getCount() % outputCount + 1));
            outputCount--;

            if (qty == 0) {
                continue;
            }

            IItemHandler handler = attached[dst];
            ItemStack add = ItemUtils.copyStack(move, qty);
            ItemStack remain = InventoryUtils.insertItem(handler, add, false);
            move.shrink(qty - remain.getCount());
            add.shrink(remain.getCount());
            if (!add.isEmpty()) {
                transfers.add(new MovingItem(dst, add));
            }
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
            if (me.canInsert(i)) {
                insertableAmount += getInsertableAmount(stack, me.getOther(i), handler[i]);
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

        return Math.max(fit, 0);
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
        return Math.max(qty, 0);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, Player player, PartRayTraceResult hit, InteractionHand hand) {
        if (level().isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        if (stack.is(ConfigHandler.regulateTag) && !regulate) {
            regulateStack = ItemUtils.copyStack(stack, 1);
            regulate = true;
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            markUpdate();
            return ItemInteractionResult.SUCCESS;
        } else if (stack.getItem() == Items.IRON_INGOT && !signal) {
            signal = true;
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            markUpdate();
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, player, hit, hand);
    }

    @Override
    public void stripModifiers() {
        super.stripModifiers();
        if (regulate) {
            regulate = false;
            dropItem(regulateStack);
            regulateStack = ItemStack.EMPTY;
        }
        if (signal) {
            setPowering(false);
            signal = false;
            dropItem(new ItemStack(Items.IRON_INGOT));
        }
    }

    @Override
    public void openGui(Player player) {
        openItemGui(player, filters, regulate ? "gui.translocators.regulate" : "gui.translocators.filter");
    }

    private void openItemGui(Player player, ItemStack[] filters, String name) {
        class Inv extends InventorySimple {

            public Inv(ItemStack[] items, int limit) {
                super(items, limit);
            }

            @Override
            public void setChanged() {
                markUpdate();
            }
        }
        MenuProvider provider = new SimpleMenuProvider(
                (id, inv, p) -> new ContainerItemTranslocator(id, inv, new Inv(filters, filterStackLimit())),
                Component.translatable(name)
        );
        CCLMenuType.openMenu((ServerPlayer) player, provider, p -> {
            p.writeShort(filterStackLimit());
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
            level().updateNeighborsAt(pos(), Blocks.REDSTONE_WIRE);
            level().updateNeighborsAt(pos().relative(Direction.BY_3D_DATA[side]), Blocks.REDSTONE_WIRE);
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
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        super.save(tag, registries);
        tag.putBoolean("regulate", regulate);
        tag.putBoolean("signal", signal);
        tag.putBoolean("powering", a_powering);
        tag.put("filters", InventoryUtils.writeItemStacksToTag(registries, filters, 65536));
        tag.put("regulateStack", regulateStack.saveOptional(registries));
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        super.load(tag, registries);
        regulate = tag.getBoolean("regulate");
        signal = tag.getBoolean("signal");
        a_powering = tag.getBoolean("powering");
        InventoryUtils.readItemStacksFromTag(registries, filters, tag.getList("filters", 10));
        regulateStack = ItemStack.parseOptional(registries, tag.getCompound("regulateStack"));
    }

    @Override
    protected int writeFlags() {
        int flags = super.writeFlags();
        flags |= (regulate ? 1 : 0) << 3;
        flags |= (signal ? 1 : 0) << 4;
        flags |= (a_powering ? 1 : 0) << 5;
        return flags;
    }

    @Override
    protected void readFlags(int flags) {
        super.readFlags(flags);
        regulate = (flags & (1 << 3)) != 0;
        signal = (flags & (1 << 4)) != 0;
        a_powering = (flags & (1 << 5)) != 0;
    }

    @Override
    public int strongPowerLevel(int side) {
        return a_powering && side == this.side ? 15 : 0;
    }

    @Override
    public int weakPowerLevel(int side) {
        return 0;
    }

    @Override
    public boolean canConnectRedstone(int side) {
        return redstone;
    }

    public static class MovingItem {

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
