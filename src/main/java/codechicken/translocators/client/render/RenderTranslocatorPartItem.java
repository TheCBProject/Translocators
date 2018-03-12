package codechicken.translocators.client.render;

import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.model.IModelState;

/**
 * Created by covers1624 on 18/11/2017.
 */
public class RenderTranslocatorPartItem implements IItemRenderer {

    @Override
    public void renderItem(ItemStack stack, TransformType transformType) {
        RenderTranslocator.renderItem(stack);
    }

    @Override
    public IModelState getTransforms() {
        return TransformUtils.DEFAULT_BLOCK;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }
}
