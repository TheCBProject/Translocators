package codechicken.translocators.client.render;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Created by covers1624 on 18/11/2017.
 */
public class RenderTranslocatorItem implements IItemRenderer {

    private final int type;

    public RenderTranslocatorItem(int type) {
        this.type = type;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        TranslocatorPartRenderer.renderItem(type, pStack, ctx, buffers, packedLight, packedOverlay);
    }

    @Override
    public PerspectiveModelState getModelState() {
        return TransformUtils.DEFAULT_BLOCK;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }
}
