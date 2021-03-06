package codechicken.translocators.handler;

import codechicken.lib.packet.PacketCustom;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.network.TranslocatorNetwork;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class CraftingGridKeyHandler extends KeyBinding {

    public static final CraftingGridKeyHandler instance = new CraftingGridKeyHandler();
    private boolean wasPressed = false;

    private CraftingGridKeyHandler() {
        super("key.craftingGrid", GLFW.GLFW_KEY_C, "key.categories.gameplay");
    }

    @SubscribeEvent
    @OnlyIn (Dist.CLIENT)
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        boolean pressed = isDown();
        if (pressed != wasPressed) {
            wasPressed = pressed;
            if (pressed) {
                onKeyPressed();
            }
        }
    }

    private void onKeyPressed() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return;
        }

        //place the grid
        RayTraceResult hit = mc.hitResult;
        if (hit == null || hit.getType() != Type.BLOCK) {
            return;
        }
        BlockRayTraceResult blockHit = (BlockRayTraceResult) hit;
        BlockPos aboveHit = blockHit.getBlockPos().above();
        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
        if (state.getBlock() == TranslocatorsModContent.blockCraftingGrid.get()) {
            PacketCustom packet = new PacketCustom(TranslocatorNetwork.NET_CHANNEL, TranslocatorNetwork.S_CRAFTING_GRID_EXECUTE);
            packet.writePos(blockHit.getBlockPos());
            packet.sendToServer();

            mc.player.swing(Hand.MAIN_HAND);
        } else if (TranslocatorsModContent.blockCraftingGrid.get().placeBlock(mc.level, mc.player, aboveHit, blockHit.getDirection())) {
            PacketCustom packet = new PacketCustom(TranslocatorNetwork.NET_CHANNEL, TranslocatorNetwork.S_CRAFTING_GRID_PLACE);
            packet.writePos(aboveHit);
            packet.writeDirection(blockHit.getDirection());
            packet.sendToServer();
        }
    }
}
