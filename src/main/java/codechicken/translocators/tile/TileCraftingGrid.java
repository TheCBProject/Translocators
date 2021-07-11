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
import net.minecraft.util.NonNullList;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.hooks.BasicEventHooks;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static codechicken.lib.vec.Vector3.CENTER;

public class TileCraftingGrid extends TileEntity implements ITickableTileEntity {

    public ItemStack[] items = ArrayUtils.fill(new ItemStack[9], ItemStack.EMPTY);
    public ItemStack result = ItemStack.EMPTY;
    public int rotation = 0;

    public int timeout = 400;//20 seconds

    public TileCraftingGrid() {
        super(TranslocatorsModContent.tileCraftingGridType.get());
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.put("items", InventoryUtils.writeItemStacksToTag(items));
        tag.putInt("timeout", timeout);
        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        InventoryUtils.readItemStacksFromTag(items, tag.getList("items", 10));
        timeout = tag.getInt("timeout");
    }

    @Override
    public void tick() {
        if (!level.isClientSide()) {
            timeout--;
            if (timeout == 0) {
                dropItems();
                level.removeBlock(getBlockPos(), false);
            }
        }
    }

    public void dropItems() {
        Vector3 drop = Vector3.fromTileCenter(this);
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                ItemUtils.dropItem(item, level, drop);
            }
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        MCDataByteBuf packet = new MCDataByteBuf();
        writeToPacket(packet);
        return packet.toTilePacket(getBlockPos());
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
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
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
        ItemStack held = player.inventory.getSelected();
        if (held.isEmpty()) {
            if (!items[subHit].isEmpty()) {
                giveOrDropItem(items[subHit], player);
            }
            items[subHit] = ItemStack.EMPTY;
        } else {
            if (!InventoryUtils.areStacksIdentical(held, items[subHit])) {
                ItemStack old = items[subHit];
                items[subHit] = ItemUtils.copyStack(held, 1);
                player.inventory.removeItem(player.inventory.selected, 1);

                if (!old.isEmpty()) {
                    giveOrDropItem(old, player);
                }
            }
        }

        timeout = 2400;
        BlockState state = level.getBlockState(getBlockPos());
        level.sendBlockUpdated(getBlockPos(), state, state, 3);
        setChanged();
    }

    private void updateResult() {
        CraftingInventory craftMatrix = getCraftMatrix();

        for (int i = 0; i < 4; i++) {
            Optional<ICraftingRecipe> mresult = level.getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, craftMatrix, level);
            if (mresult.isPresent()) {
                result = mresult.get().assemble(craftMatrix);
                return;
            }

            rotateItems(craftMatrix);
        }

        result = ItemStack.EMPTY;
    }

    private void giveOrDropItem(ItemStack stack, PlayerEntity player) {
        if (player.inventory.add(stack)) {
            player.inventoryMenu.broadcastChanges();
        } else {
            ItemUtils.dropItem(stack, level, Vector3.fromTileCenter(this));
        }
    }

    public void craft(ServerPlayerEntity player) {
        CraftingInventory craftMatrix = getCraftMatrix();

        for (int i = 0; i < 4; i++) {
            Optional<ICraftingRecipe> mresult = level.getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, craftMatrix, level);
            if (mresult.isPresent()) {
                ICraftingRecipe recipe = mresult.get();
                if (recipe.isSpecial() || !level.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) || player.getRecipeBook().contains(recipe)) {
                    doCraft(recipe,recipe.assemble(craftMatrix), craftMatrix, player);
                    break;
                }
            }

            rotateItems(craftMatrix);
        }
        player.swing(Hand.MAIN_HAND);
        dropItems();
        level.removeBlock(getBlockPos(), false);
    }

    private CraftingInventory getCraftMatrix() {
        CraftingInventory craftMatrix = new CraftingInventory(new Container(null, 0) {
            @Override
            public boolean stillValid(PlayerEntity player) {
                return true;
            }
        }, 3, 3);

        for (int i = 0; i < 9; i++) {
            craftMatrix.setItem(i, items[i]);
        }

        return craftMatrix;
    }

    private void doCraft(ICraftingRecipe recipe, ItemStack mresult, CraftingInventory craftMatrix, PlayerEntity player) {
        giveOrDropItem(mresult, player);

        mresult.onCraftedBy(level, player, mresult.getCount());
        BasicEventHooks.firePlayerCraftingEvent(player, mresult, craftMatrix);
        if (!recipe.isSpecial()) {
            player.awardRecipes(Collections.singleton(recipe));
        }

        ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(craftMatrix);
        ForgeHooks.setCraftingPlayer(null);

        for (int i = 0; i < remaining.size(); i++) {
            ItemStack invStack = craftMatrix.getItem(i);
            ItemStack remStack = remaining.get(i);
            if (!invStack.isEmpty()) {
                craftMatrix.removeItem(i, 1);
                invStack = craftMatrix.getItem(i);
            }

            if (!remStack.isEmpty()) {
                if (invStack.isEmpty()) {
                    craftMatrix.setItem(i, remStack);
                } else if (ItemStack.isSame(invStack, remStack) && ItemStack.tagMatches(invStack, remStack)) {
                    remStack.grow(invStack.getCount());
                    craftMatrix.setItem(i, remStack);
                } else {
                    giveOrDropItem(remStack, player);
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            items[i] = craftMatrix.getItem(i);
        }
    }

    private void rotateItems(CraftingInventory inv) {
        int[] slots = new int[] { 0, 1, 2, 5, 8, 7, 6, 3 };
        ItemStack[] arrangement = new ItemStack[9];
        arrangement[4] = inv.getItem(4);

        for (int i = 0; i < 8; i++) {
            arrangement[slots[(i + 2) % 8]] = inv.getItem(slots[i]);
        }

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, arrangement[i]);
        }
    }

    public void onPlaced(LivingEntity entity) {
        rotation = (int) (entity.yRot * 4 / 360 + 0.5D) & 3;
    }
}
