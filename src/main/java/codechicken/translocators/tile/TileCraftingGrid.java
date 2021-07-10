package codechicken.translocators.tile;

import codechicken.lib.data.MCDataByteBuf;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.util.ArrayUtils;
import codechicken.lib.util.ItemUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.translocators.init.TranslocatorsModContent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static codechicken.lib.vec.Vector3.CENTER;

public class TileCraftingGrid extends TileEntity implements ITickableTileEntity {

    private static final Cuboid6[][] BOXES = new Cuboid6[4][6];

    public ItemStack[] items;
    public ItemStack result;
    public int rotation = 0;

    public int timeout = 400;//20 seconds

    public TileCraftingGrid() {
        super(TranslocatorsModContent.tileCraftingGridType);
        items = ArrayUtils.fill(new ItemStack[9], ItemStack.EMPTY);
        result = ItemStack.EMPTY;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.put("items", InventoryUtils.writeItemStacksToTag(items));
        tag.putInt("timeout", timeout);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        InventoryUtils.readItemStacksFromTag(items, tag.getList("items", 10));
        timeout = tag.getInt("timeout");
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            timeout--;
            if (timeout == 0) {
                dropItems();
                world.removeBlock(getPos(), false);
            }
        }
    }

    public void dropItems() {
        Vector3 drop = Vector3.fromTileCenter(this);
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                ItemUtils.dropItem(item, world, drop);
            }
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        MCDataByteBuf packet = new MCDataByteBuf();
        writeToPacket(packet);
        return packet.toTilePacket(getPos());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        MCDataByteBuf packet = new MCDataByteBuf();
        writeToPacket(packet);
        return packet.writeToNBT(super.getUpdateTag(), "data");
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readFromPacket(MCDataByteBuf.fromTilePacket(pkt));
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        readFromPacket(MCDataByteBuf.readFromNBT(tag, "data"));
    }

    public void writeToPacket(MCDataOutput packet) {
        packet.writeByte(rotation);
        for (ItemStack item : items) {
            packet.writeItemStack(item);
        }
    }

    public void readFromPacket(MCDataInput packet) {
        rotation = packet.readUByte();

        for (int i = 0; i < 9; i++) {
            items[i] = packet.readItemStack();
        }

        updateResult();
    }

    public void activate(int subHit, PlayerEntity player) {
        ItemStack held = player.inventory.getCurrentItem();
        if (held.isEmpty()) {
            if (!items[subHit].isEmpty()) {
                giveOrDropItem(items[subHit], player);
            }
            items[subHit] = ItemStack.EMPTY;
        } else {
            if (!InventoryUtils.areStacksIdentical(held, items[subHit])) {
                ItemStack old = items[subHit];
                items[subHit] = ItemUtils.copyStack(held, 1);
                player.inventory.decrStackSize(player.inventory.currentItem, 1);

                if (!old.isEmpty()) {
                    giveOrDropItem(old, player);
                }
            }
        }

        timeout = 2400;
        BlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 3);
        markDirty();
    }

    private void updateResult() {
        CraftingInventory craftMatrix = getCraftMatrix();

        for (int i = 0; i < 4; i++) {
            Optional<ICraftingRecipe> mresult = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftMatrix, world);
            if (mresult.isPresent()) {
                //TODO, IRecipeHolder.canUseRecipe.
                result = mresult.get().getCraftingResult(craftMatrix);
                return;
            }

            rotateItems(craftMatrix);
        }

        result = ItemStack.EMPTY;
    }

    private void giveOrDropItem(ItemStack stack, PlayerEntity player) {
        if (player.inventory.addItemStackToInventory(stack)) {
            player.container.detectAndSendChanges();
        } else {
            ItemUtils.dropItem(stack, world, Vector3.fromTileCenter(this));
        }
    }

    public void craft(ServerPlayerEntity player) {
        CraftingInventory craftMatrix = getCraftMatrix();

        for (int i = 0; i < 4; i++) {
            Optional<ICraftingRecipe> mresult = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftMatrix, world);
            if (mresult.isPresent()) {
                ICraftingRecipe recipe = mresult.get();
                if (recipe.isDynamic() || !world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) || player.getRecipeBook().isUnlocked(recipe)) {
                    doCraft(recipe.getCraftingResult(craftMatrix), craftMatrix, player);
                    break;
                }
            }

            rotateItems(craftMatrix);
        }
        player.swingArm(Hand.MAIN_HAND);
        dropItems();
        world.removeBlock(getPos(), false);
    }

    private CraftingInventory getCraftMatrix() {
        CraftingInventory craftMatrix = new CraftingInventory(new Container(null, 0) {
            @Override
            public boolean canInteractWith(PlayerEntity player) {
                return true;
            }
        }, 3, 3);

        for (int i = 0; i < 9; i++) {
            craftMatrix.setInventorySlotContents(i, items[i]);
        }

        return craftMatrix;
    }

    private void doCraft(ItemStack mresult, CraftingInventory craftMatrix, PlayerEntity player) {
        giveOrDropItem(mresult, player);

        //FMLCommonHandler.instance().firePlayerCraftingEvent(player, mresult, craftMatrix);
        mresult.onCrafting(world, player, mresult.getCount());

        for (int slot = 0; slot < 9; ++slot) {
            ItemStack stack = craftMatrix.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            craftMatrix.decrStackSize(slot, 1);
            if (stack.getItem().hasContainerItem(stack)) {
                ItemStack container = stack.getItem().getContainerItem(stack);

                if (!container.isEmpty()) {
                    if (container.isDamageable() && container.getDamage() > container.getMaxDamage()) {
                        container = ItemStack.EMPTY;
                    }

                    craftMatrix.setInventorySlotContents(slot, container);
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            items[i] = craftMatrix.getStackInSlot(i);
        }
    }

    private void rotateItems(CraftingInventory inv) {
        int[] slots = new int[] { 0, 1, 2, 5, 8, 7, 6, 3 };
        ItemStack[] arrangement = new ItemStack[9];
        arrangement[4] = inv.getStackInSlot(4);

        for (int i = 0; i < 8; i++) {
            arrangement[slots[(i + 2) % 8]] = inv.getStackInSlot(slots[i]);
        }

        for (int i = 0; i < 9; i++) {
            inv.setInventorySlotContents(i, arrangement[i]);
        }
    }

    public void onPlaced(LivingEntity entity) {
        rotation = (int) (entity.rotationYaw * 4 / 360 + 0.5D) & 3;
    }

    public List<IndexedCuboid6> getIndexedCuboids() {
        LinkedList<IndexedCuboid6> parts = new LinkedList<>();

        parts.add(new IndexedCuboid6(0, new Cuboid6(0, 0, 0, 1, 0.005, 1)));

        for (int i = 0; i < 9; i++) {
            Cuboid6 box = new Cuboid6(1 / 16D, 0, 1 / 16D, 5 / 16D, 0.01, 5 / 16D).apply(new Translation((i % 3) * 5 / 16D, 0, (i / 3) * 5 / 16D).with(Rotation.quarterRotations[rotation].at(CENTER)));

            parts.add(new IndexedCuboid6(i + 1, box));
        }

        return parts;
    }
}
