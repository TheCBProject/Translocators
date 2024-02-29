package codechicken.translocators.client.render;

import codechicken.lib.math.MathHelper;
import codechicken.lib.vec.Vector3;
import codechicken.translocators.part.ItemTranslocatorPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;

/**
 * Created by covers1624 on 4/5/23.
 */
public class ItemTranslocatorPartRenderer extends TranslocatorPartRenderer<ItemTranslocatorPart> {

    @Override
    public void renderDynamic(ItemTranslocatorPart part, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, float partialTicks) {
        super.renderDynamic(part, pStack, buffers, packedLight, packedOverlay, partialTicks);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        for (ItemTranslocatorPart.MovingItem m : part.movingItems) {
            pStack.pushPose();
            double d = MathHelper.interpolate(m.b_progress, m.a_progress, partialTicks);
            Vector3 path = getPath(part.side, m.dst, d).add(itemFloat(part.side, m.dst, d));
            pStack.translate(path.x, path.y, path.z);
            pStack.scale(0.5f, 0.5f, 0.5f);
            pStack.scale(0.35f, 0.35f, 0.35f);
            itemRenderer.renderStatic(m.stack, ItemTransforms.TransformType.FIXED, packedLight, packedOverlay, pStack, buffers, (int) part.pos().asLong());
            pStack.popPose();
        }
    }

    private static Vector3 itemFloat(int src, int dst, double d) {
        return getPerp(src, dst).multiply(0.01 * Math.sin(d * 4 * Math.PI));
    }
}
