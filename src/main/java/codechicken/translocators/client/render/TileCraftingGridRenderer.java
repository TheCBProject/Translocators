package codechicken.translocators.client.render;

import codechicken.lib.render.CCRenderEventHandler;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.translocators.init.TranslocatorTextures;
import codechicken.translocators.tile.TileCraftingGrid;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class TileCraftingGridRenderer implements BlockEntityRenderer<TileCraftingGrid> {

    public TileCraftingGridRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(TileCraftingGrid tile, float partialTicks, PoseStack mStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;

        TextureAtlasSprite icon = TranslocatorTextures.CRAFTING_GRID;

        Matrix4 mat = new Matrix4(mStack);
        mat.translate(0, 0.001, 0);

        ccrs.bind(RenderType.cutout(), buffers, mat);
        ccrs.normal.set(0, 1, 0);
        ccrs.vert.set(1, 0, 0, icon.getU0(), icon.getV0());
        ccrs.writeVert();
        ccrs.vert.set(0, 0, 0, icon.getU0(), icon.getV1());
        ccrs.writeVert();
        ccrs.vert.set(0, 0, 1, icon.getU1(), icon.getV1());
        ccrs.writeVert();
        ccrs.vert.set(1, 0, 1, icon.getU1(), icon.getV0());
        ccrs.writeVert();

        mStack.pushPose();
        mStack.translate(0.5, 0, 0.5);

        Transformation orient = Rotation.quarterRotations[tile.rotation];
        for (int i = 0; i < 9; i++) {
            ItemStack stack = tile.items[i];

            if (stack.isEmpty()) continue;

            int row = i / 3;
            int col = i % 3;

            Vector3 pos = new Vector3((col - 1) * 5 / 16D, 0.1 + 0.01 * Math.sin(i * 1.7 + ClientUtils.getRenderTime() / 5), (row - 1) * 5 / 16D).apply(orient);
            mStack.pushPose();
            mStack.translate(pos.x, pos.y, pos.z);
            mStack.scale(0.35f, 0.35f, 0.35f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, mStack, buffers, tile.getLevel(), (int) tile.getBlockPos().asLong());
            mStack.popPose();
        }

        if (!tile.result.isEmpty()) {
            mStack.pushPose();
            mStack.translate(0, 0.6 + 0.02 * Math.sin(ClientUtils.getRenderTime() / 10), 0);
            mStack.scale(0.8F, 0.8F, 0.8F);
            float spin = (float) (ClientUtils.getRenderTime() * 9 / Math.PI);
            float bob = (float) (Math.sin(((float) CCRenderEventHandler.renderTime + spin) / 20.0F) * 0.1F + 0.1F);
            mStack.translate(0, bob + 0.25, 0);
            mStack.mulPose(Axis.YP.rotation(((float) CCRenderEventHandler.renderTime + spin) / 30.0F));
            itemRenderer.renderStatic(tile.result, ItemDisplayContext.FIXED, packedLight, packedOverlay, mStack, buffers, tile.getLevel(), (int) tile.getBlockPos().asLong());
            mStack.popPose();
        }

        mStack.popPose();
    }
}
