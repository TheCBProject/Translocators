package codechicken.translocators.handler;

import codechicken.lib.packet.PacketCustom;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.network.TranslocatorNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.lwjgl.glfw.GLFW;

public class CraftingGridKeyHandler {

    public static final KeyMapping CRAFTING_KEY = new KeyMapping("key.craftingGrid", GLFW.GLFW_KEY_C, "key.categories.gameplay");
    private static boolean wasPressed = false;

    public static void init(IEventBus modBus) {
        modBus.addListener(CraftingGridKeyHandler::registerKeyBinding);
        MinecraftForge.EVENT_BUS.addListener(CraftingGridKeyHandler::onClientTick);
    }

    private static void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(CRAFTING_KEY);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        boolean pressed = CRAFTING_KEY.isDown();
        if (pressed != wasPressed) {
            wasPressed = pressed;
            if (pressed) {
                onKeyPressed();
            }
        }
    }

    private static void onKeyPressed() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return;
        }

        //place the grid
        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos aboveHit = blockHit.getBlockPos().above();
        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
        if (state.getBlock() == TranslocatorsModContent.blockCraftingGrid.get()) {
            PacketCustom packet = new PacketCustom(TranslocatorNetwork.NET_CHANNEL, TranslocatorNetwork.S_CRAFTING_GRID_EXECUTE);
            packet.writePos(blockHit.getBlockPos());
            packet.sendToServer();

            mc.player.swing(InteractionHand.MAIN_HAND);
        } else if (TranslocatorsModContent.blockCraftingGrid.get().placeBlock(mc.level, mc.player, aboveHit, blockHit.getDirection())) {
            PacketCustom packet = new PacketCustom(TranslocatorNetwork.NET_CHANNEL, TranslocatorNetwork.S_CRAFTING_GRID_PLACE);
            packet.writePos(aboveHit);
            packet.writeDirection(blockHit.getDirection());
            packet.sendToServer();
        }
    }
}
