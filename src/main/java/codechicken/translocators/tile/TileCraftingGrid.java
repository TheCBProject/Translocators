package codechicken.translocators.tile;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.packet.ICustomPacketTile;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.raytracer.ICuboidProvider;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.util.ArrayUtils;
import codechicken.lib.util.ItemUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.translocators.network.TranslocatorSPH;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.LinkedList;
import java.util.List;

import static codechicken.lib.vec.Vector3.center;

public class TileCraftingGrid extends TileEntity implements ICustomPacketTile, ITickable, ICuboidProvider {
    public ItemStack[] items;
    public ItemStack result;
    public int rotation = 0;

    public int timeout = 400;//20 seconds

    public TileCraftingGrid() {

        items = new ItemStack[9];
        ArrayUtils.fillArray(items, ItemStack.EMPTY);
        result = ItemStack.EMPTY;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("items", InventoryUtils.writeItemStacksToTag(items));
        tag.setInteger("timeout", timeout);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        InventoryUtils.readItemStacksFromTag(items, tag.getTagList("items", 10));
        timeout = tag.getInteger("timeout");
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            timeout--;
            if (timeout == 0) {
                dropItems();
                world.setBlockToAir(getPos());
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
    public SPacketUpdateTileEntity getUpdatePacket() {
        PacketCustom packet = new PacketCustom(TranslocatorSPH.channel, 3);
        writeToPacket(packet);
        return packet.toTilePacket(getPos());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        PacketCustom packet = new PacketCustom(TranslocatorSPH.channel, 3);
        writeToPacket(packet);
        return packet.toNBTTag(super.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromPacket(PacketCustom.fromTilePacket(pkt));
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromPacket(PacketCustom.fromNBTTag(tag));
    }

    @Override
    public void writeToPacket(MCDataOutput packet) {
        packet.writeByte(rotation);
        for (ItemStack item : items) {
            packet.writeItemStack(item);
        }
    }

    @Override
    public void readFromPacket(MCDataInput packet) {
        rotation = packet.readUByte();

        for (int i = 0; i < 9; i++) {
            items[i] = packet.readItemStack();
        }

        updateResult();
    }

    public void activate(int subHit, EntityPlayer player) {
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
        IBlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 3);
        markDirty();
    }

    private void updateResult() {
        InventoryCrafting craftMatrix = getCraftMatrix();

        for (int i = 0; i < 4; i++) {
            IRecipe mresult = CraftingManager.findMatchingRecipe(craftMatrix, world);
            if (mresult != null) {
                result = mresult.getCraftingResult(craftMatrix);
                return;
            }

            rotateItems(craftMatrix);
        }

        result = ItemStack.EMPTY;
    }

    private void giveOrDropItem(ItemStack stack, EntityPlayer player) {
        if (player.inventory.addItemStackToInventory(stack)) {
            player.inventoryContainer.detectAndSendChanges();
        } else {
            ItemUtils.dropItem(stack, world, Vector3.fromTileCenter(this));
        }
    }

    public void craft(EntityPlayer player) {
        InventoryCrafting craftMatrix = getCraftMatrix();

        for (int i = 0; i < 4; i++) {
            IRecipe mresult = CraftingManager.findMatchingRecipe(craftMatrix, world);
            if (mresult != null) {
                doCraft(mresult.getCraftingResult(craftMatrix), craftMatrix, player);
                break;
            }

            rotateItems(craftMatrix);
        }
        player.swingArm(EnumHand.MAIN_HAND);
        dropItems();
        world.setBlockToAir(getPos());
    }

    private InventoryCrafting getCraftMatrix() {
        InventoryCrafting craftMatrix = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer entityplayer) {
                return true;
            }
        }, 3, 3);

        for (int i = 0; i < 9; i++) {
            craftMatrix.setInventorySlotContents(i, items[i]);
        }

        return craftMatrix;
    }

    private void doCraft(ItemStack mresult, InventoryCrafting craftMatrix, EntityPlayer player) {
        giveOrDropItem(mresult, player);

        FMLCommonHandler.instance().firePlayerCraftingEvent(player, mresult, craftMatrix);
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
                    if (container.isItemStackDamageable() && container.getItemDamage() > container.getMaxDamage()) {
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

    private void rotateItems(InventoryCrafting inv) {
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

    public void onPlaced(EntityLivingBase entity) {
        rotation = (int) (entity.rotationYaw * 4 / 360 + 0.5D) & 3;
    }

    @Override
    public List<IndexedCuboid6> getIndexedCuboids() {
        LinkedList<IndexedCuboid6> parts = new LinkedList<>();

        parts.add(new IndexedCuboid6(0, new Cuboid6(0, 0, 0, 1, 0.005, 1)));

        for (int i = 0; i < 9; i++) {
            Cuboid6 box = new Cuboid6(1 / 16D, 0, 1 / 16D, 5 / 16D, 0.01, 5 / 16D).apply(new Translation((i % 3) * 5 / 16D, 0, (i / 3) * 5 / 16D).with(Rotation.quarterRotations[rotation].at(center)));

            parts.add(new IndexedCuboid6(i + 1, box));
        }

        return parts;
    }
}
