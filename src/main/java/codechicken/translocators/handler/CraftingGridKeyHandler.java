package codechicken.translocators.handler;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
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
        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
        //        if (state.getBlock() == ModBlocks.blockCraftingGrid) {
        //            PacketCustom packet = new PacketCustom(TranslocatorCPH.channel, 2);
        //            packet.writePos(hit.getBlockPos());
        //            packet.sendToServer();
        //
        //            mc.player.swingArm(EnumHand.MAIN_HAND);
        //        } else if (ModBlocks.blockCraftingGrid.placeBlock(mc.world, mc.player, blockHit.getPos(), blockHit.getFace())) {
        //            PacketCustom packet = new PacketCustom(TranslocatorCPH.channel, 1);
        //            packet.writePos(hit.getBlockPos());
        //            packet.writeByte(hit.sideHit.ordinal());
        //            packet.sendToServer();
        //        }
    }
}
