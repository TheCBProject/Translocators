package codechicken.translocators.tile;

import codechicken.lib.data.MCDataByteBuf;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.inventory.InventorySimple;
import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.util.ArrayUtils;
import codechicken.lib.util.ItemUtils;
import codechicken.lib.vec.Vector3;
import codechicken.translocators.init.TranslocatorsModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TileCraftingGrid extends BlockEntity {

    public ItemStack[] items = ArrayUtils.fill(new ItemStack[9], ItemStack.EMPTY);
    public ItemStack result = ItemStack.EMPTY;
    public int rotation = 0;

    public int timeout = 400;//20 seconds

    public TileCraftingGrid(BlockPos pos, BlockState state) {
        super(TranslocatorsModContent.tileCraftingGridType.get(), pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("items", InventoryUtils.writeItemStacksToTag(items));
        tag.putInt("timeout", timeout);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        InventoryUtils.readItemStacksFromTag(items, tag.getList("items", 10));
        timeout = tag.getInt("timeout");
    }

    public void tickServer() {
        timeout--;
        if (timeout == 0) {
            dropItems();
            level.removeBlock(getBlockPos(), false);
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
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, e -> {
            MCDataByteBuf packet = new MCDataByteBuf();
            writeToPacket(packet);
            CompoundTag tag = new CompoundTag();
            packet.writeToNBT(tag, "data");
            return tag;
        });
    }

    @Override
    public CompoundTag getUpdateTag() {
        MCDataByteBuf packet = new MCDataByteBuf();
        writeToPacket(packet);
        return packet.writeToNBT(super.getUpdateTag(), "data");
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        readFromPacket(MCDataByteBuf.readFromNBT(pkt.getTag(), "data"));
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
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

    public void activate(int subHit, Player player) {
        ItemStack held = player.getInventory().getSelected();
        if (held.isEmpty()) {
            if (!items[subHit].isEmpty()) {
                giveOrDropItem(items[subHit], player);
            }
            items[subHit] = ItemStack.EMPTY;
        } else {
            if (!InventoryUtils.areStacksIdentical(held, items[subHit])) {
                ItemStack old = items[subHit];
                items[subHit] = ItemUtils.copyStack(held, 1);
                player.getInventory().removeItem(player.getInventory().selected, 1);

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
        CraftingContainer craftMatrix = getCraftMatrix();

        for (int i = 0; i < 4; i++) {
            Optional<CraftingRecipe> mresult = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftMatrix, level);
            if (mresult.isPresent()) {
                result = mresult.get().assemble(craftMatrix, level.registryAccess());
                return;
            }

            rotateItems(craftMatrix);
        }

        result = ItemStack.EMPTY;
    }

    private void giveOrDropItem(ItemStack stack, Player player) {
        if (player.getInventory().add(stack)) {
            player.inventoryMenu.broadcastChanges();
        } else {
            ItemUtils.dropItem(stack, level, Vector3.fromTileCenter(this));
        }
    }

    public void craft(ServerPlayer player) {
        CraftingContainer craftMatrix = getCraftMatrix();

        for (int i = 0; i < 4; i++) {
            Optional<CraftingRecipe> mresult = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftMatrix, level);
            if (mresult.isPresent()) {
                CraftingRecipe recipe = mresult.get();
                if (recipe.isSpecial() || !level.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) || player.getRecipeBook().contains(recipe)) {
                    doCraft(recipe, recipe.assemble(craftMatrix, level.registryAccess()), craftMatrix, player);
                    break;
                }
            }

            rotateItems(craftMatrix);
        }
        player.swing(InteractionHand.MAIN_HAND);
        dropItems();
        level.removeBlock(getBlockPos(), false);
    }

    private CraftingContainer getCraftMatrix() {
        GridCraftingInventory craftMatrix = new GridCraftingInventory();

        for (int i = 0; i < 9; i++) {
            craftMatrix.setItem(i, items[i]);
        }

        return craftMatrix;
    }

    private void doCraft(CraftingRecipe recipe, ItemStack mresult, CraftingContainer craftMatrix, Player player) {
        giveOrDropItem(mresult, player);

        mresult.onCraftedBy(level, player, mresult.getCount());
        ForgeEventFactory.firePlayerCraftingEvent(player, mresult, craftMatrix);
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
                } else if (ItemStack.isSameItemSameTags(invStack, remStack)) {
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

    private void rotateItems(CraftingContainer inv) {
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
        rotation = (int) (entity.getYRot() * 4 / 360 + 0.5D) & 3;
    }

    private static class GridCraftingInventory extends InventorySimple implements CraftingContainer {

        public GridCraftingInventory() {
            super(3 * 3);
        }

        @Override
        public int getWidth() {
            return 3;
        }

        @Override
        public int getHeight() {
            return 3;
        }

        @Override
        public List<ItemStack> getItems() {
            return Arrays.asList(items);
        }

        @Override
        public void fillStackedContents(StackedContents pContents) {
            for (ItemStack item : items) {
                pContents.accountSimpleStack(item);
            }
        }
    }
}
