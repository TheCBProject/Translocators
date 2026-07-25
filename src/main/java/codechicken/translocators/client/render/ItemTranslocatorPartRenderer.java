package codechicken.translocators.client.render;

import codechicken.lib.math.MathHelper;
import codechicken.lib.vec.Vector3;
import codechicken.translocators.client.render.ItemTranslocatorPartRenderer.ItemTranslocatorRenderState;
import codechicken.translocators.part.ItemTranslocatorPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 4/5/23.
 */
public class ItemTranslocatorPartRenderer extends TranslocatorPartRenderer<ItemTranslocatorPart, ItemTranslocatorRenderState> {

    @Override
    public ItemTranslocatorRenderState createDynamicState() {
        return new ItemTranslocatorRenderState();
    }

    @Override
    public void extractDynamicState(BEState beState, ItemTranslocatorRenderState state, ItemTranslocatorPart part, float partialTick, Vec3 camera, @Nullable CrumblingOverlay breakProgress) {
        super.extractDynamicState(beState, state, part, partialTick, camera, breakProgress);

        var itemModelResolver = Minecraft.getInstance().getItemModelResolver();
        int posSeed = (int) part.pos().asLong();

        state.items = new ArrayList<>(part.movingItems.size());
        for (var m : part.movingItems) {
            var itemState = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(itemState, m.stack, ItemDisplayContext.FIXED, part.level(), null, posSeed);

            state.items.add(new MovingItemSnapshot(
                    itemState,
                    m.dst,
                    MathHelper.interpolate(m.b_progress, m.a_progress, partialTick)
            ));
            posSeed++;
        }
    }

    @Override
    public void submitDynamic(BEState beState, ItemTranslocatorRenderState state, PoseStack pStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        super.submitDynamic(beState, state, pStack, collector, cameraState);
        if (state.items != null) {
            for (var m : state.items) {
                pStack.pushPose();
                var path = getPath(state.side, m.dst, m.progress).add(itemFloat(state.side, m.dst, m.progress));
                pStack.translate(path.x, path.y, path.z);
                pStack.scale(0.5f, 0.5f, 0.5f);
                pStack.scale(0.35f, 0.35f, 0.35f);
                m.itemRenderState.submit(pStack, collector, beState.lightCoords(), OverlayTexture.NO_OVERLAY, 0);
                pStack.popPose();
            }
        }
    }

    private static Vector3 itemFloat(int src, int dst, double d) {
        return getPerp(src, dst).multiply(0.01 * Math.sin(d * 4 * Math.PI));
    }

    public static class ItemTranslocatorRenderState extends TranslocatorRenderState {

        public @Nullable List<MovingItemSnapshot> items;
    }

    public record MovingItemSnapshot(ItemStackRenderState itemRenderState, int dst, double progress) { }
}
