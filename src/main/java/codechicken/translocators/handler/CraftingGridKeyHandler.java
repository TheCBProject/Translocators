package codechicken.translocators.handler;

import codechicken.lib.packet.PacketCustom;
import codechicken.translocators.init.ModBlocks;
import codechicken.translocators.network.TranslocatorCPH;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class CraftingGridKeyHandler extends KeyBinding {

    public static final CraftingGridKeyHandler instance = new CraftingGridKeyHandler();
    private boolean wasPressed = false;

    private CraftingGridKeyHandler() {
        super("key.craftingGrid", Keyboard.KEY_C, "key.categories.gameplay");
    }

    @SubscribeEvent
    @SideOnly (Side.CLIENT)
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        boolean pressed = isKeyDown();
        if (pressed != wasPressed) {
            wasPressed = pressed;
            if (pressed) {
                onKeyPressed();
            }
        }
    }

    private void onKeyPressed() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) {
            return;
        }

        //place the grid
        RayTraceResult hit = mc.objectMouseOver;
        if (hit == null || hit.typeOfHit != Type.BLOCK) {
            return;
        }
        IBlockState state = mc.world.getBlockState(hit.getBlockPos());
        if (state.getBlock() == ModBlocks.blockCraftingGrid) {
            PacketCustom packet = new PacketCustom(TranslocatorCPH.channel, 2);
            packet.writePos(hit.getBlockPos());
            packet.sendToServer();

            mc.player.swingArm(EnumHand.MAIN_HAND);
        } else if (ModBlocks.blockCraftingGrid.placeBlock(mc.world, mc.player, hit.getBlockPos(), hit.sideHit)) {
            PacketCustom packet = new PacketCustom(TranslocatorCPH.channel, 1);
            packet.writePos(hit.getBlockPos());
            packet.writeByte(hit.sideHit.ordinal());
            packet.sendToServer();
        }
    }
}
