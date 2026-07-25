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
import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;

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
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        InventoryUtils.writeItemStacksToOutput(output.child("items"), items);
        output.putInt("timeout", timeout);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        InventoryUtils.readItemStacksFromInput(input.childOrEmpty("items"), items);
        timeout = input.getIntOr("timeout", 400);
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
        return ClientboundBlockEntityDataPacket.create(this, (e, r) -> {
            MCDataByteBuf packet = new MCDataByteBuf(r);
            writeToPacket(packet);
            CompoundTag tag = new CompoundTag();
            packet.writeToNBT(tag, "data");
            return tag;
        });
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryAccess) {
        MCDataByteBuf packet = new MCDataByteBuf(getLevel().registryAccess());
        writeToPacket(packet);
        return packet.writeToNBT(super.getUpdateTag(registryAccess), "data");
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        var buffer = input.read("data", Codec.BYTE_BUFFER).orElseThrow();
        readFromPacket(new MCDataByteBuf(Unpooled.wrappedBuffer(buffer), getLevel().registryAccess()));
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        var buffer = input.read("data", Codec.BYTE_BUFFER).orElseThrow();
        readFromPacket(new MCDataByteBuf(Unpooled.wrappedBuffer(buffer), getLevel().registryAccess()));
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
        ItemStack held = player.getInventory().getSelectedItem();
        if (held.isEmpty()) {
            if (!items[subHit].isEmpty()) {
                giveOrDropItem(items[subHit], player);
            }
            items[subHit] = ItemStack.EMPTY;
        } else {
            if (!InventoryUtils.areStacksIdentical(held, items[subHit])) {
                ItemStack old = items[subHit];
                items[subHit] = ItemUtils.copyStack(held, 1);
                player.getInventory().removeItem(player.getInventory().getSelectedSlot(), 1);

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
        CraftingInput input = craftMatrix.asCraftInput();

        for (int i = 0; i < 4; i++) {
            // TODO rewrite, we need to do recipes on the server.
//            Optional<RecipeHolder<CraftingRecipe>> mresult = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
            Optional<RecipeHolder<CraftingRecipe>> mresult = Optional.empty();
            if (mresult.isPresent()) {
                result = mresult.get().value().assemble(input, level.registryAccess());
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
        CraftingInput.Positioned positionedInput = craftMatrix.asPositionedCraftInput();

        for (int i = 0; i < 4; i++) {
            // TODO recipes need to be server-side
//            Optional<RecipeHolder<CraftingRecipe>> mresult = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, positionedInput.input(), level);
            Optional<RecipeHolder<CraftingRecipe>> mresult = Optional.empty();
            if (mresult.isPresent()) {
                RecipeHolder<CraftingRecipe> holder = mresult.get();
                CraftingRecipe recipe = holder.value();
                if (recipe.isSpecial() || !player.level().getGameRules().get(GameRules.LIMITED_CRAFTING) || player.getRecipeBook().contains(holder.id())) {
                    doCraft(holder, recipe.assemble(positionedInput.input(), level.registryAccess()), craftMatrix, positionedInput, player);
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

    private void doCraft(RecipeHolder<CraftingRecipe> holder, ItemStack mresult, CraftingContainer craftMatrix, CraftingInput.Positioned positionedInput, Player player) {
        giveOrDropItem(mresult, player);

        mresult.onCraftedBy(player, mresult.getCount());
        EventHooks.firePlayerCraftingEvent(player, mresult, craftMatrix);
        player.triggerRecipeCrafted(holder, craftMatrix.getItems());

        CraftingRecipe recipe = holder.value();
        if (!recipe.isSpecial()) {
            player.awardRecipes(Collections.singleton(holder));
        }

        CraftingInput input = positionedInput.input();
        CommonHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(input);
        CommonHooks.setCraftingPlayer(null);

        for (int rx = 0; rx < input.width(); rx++) {
            for (int ry = 0; ry < input.height(); ry++) {
                int x = rx + positionedInput.left();
                int y = ry + positionedInput.top();
                int i = x + y * craftMatrix.getWidth();

                ItemStack invStack = craftMatrix.getItem(i);
                ItemStack remStack = remaining.get(rx + ry * input.width());
                if (!invStack.isEmpty()) {
                    craftMatrix.removeItem(i, 1);
                    invStack = craftMatrix.getItem(i);
                }

                if (!remStack.isEmpty()) {
                    if (invStack.isEmpty()) {
                        craftMatrix.setItem(i, remStack);
                    } else if (ItemStack.isSameItemSameComponents(invStack, remStack)) {
                        remStack.grow(invStack.getCount());
                        craftMatrix.setItem(i, remStack);
                    } else {
                        giveOrDropItem(remStack, player);
                    }
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
        public void fillStackedContents(StackedItemContents pContents) {
            for (ItemStack item : items) {
                pContents.accountSimpleStack(item);
            }
        }
    }
}
