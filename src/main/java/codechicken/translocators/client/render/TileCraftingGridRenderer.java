package codechicken.translocators.client.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.translocators.tile.TileCraftingGrid;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class TileCraftingGridRenderer extends TileEntityRenderer<TileCraftingGrid> {

    public TileCraftingGridRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(TileCraftingGrid tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

    }

    //    @Override
//    public void render(TileCraftingGrid tcraft, double x, double y, double z, float partialTicks, int destroyStage) {
//        TextureUtils.bindBlockTexture();
//        TextureUtils.dissableBlockMipmap();
//        TextureAtlasSprite icon = TextureUtils.getBlockTexture("translocators:crafting_grid");
//
//        CCRenderState ccrs = CCRenderState.instance();
//        BufferBuilder buffer = ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
//        buffer.setTranslation(x, y + 0.001, z);
//        buffer.pos(1, 0, 0).tex(icon.getMinU(), icon.getMinV()).normal(0, 1, 0).endVertex();
//        buffer.pos(0, 0, 0).tex(icon.getMinU(), icon.getMaxV()).normal(0, 1, 0).endVertex();
//        buffer.pos(0, 0, 1).tex(icon.getMaxU(), icon.getMaxV()).normal(0, 1, 0).endVertex();
//        buffer.pos(1, 0, 1).tex(icon.getMaxU(), icon.getMinV()).normal(0, 1, 0).endVertex();
//        buffer.setTranslation(0, 0, 0);
//        ccrs.draw();
//        TextureUtils.restoreBlockMipmap();
//
//        GlStateManager.enableRescaleNormal();
//        GlStateManager.pushMatrix();
//        GlStateManager.translated(x + 0.5, y, z + 0.5);
//        Transformation orient = Rotation.quarterRotations[tcraft.rotation];
//
//        for (int i = 0; i < 9; i++) {
//            ItemStack item = tcraft.items[i];
//            if (item.isEmpty()) {
//                continue;
//            }
//
//            int row = i / 3;
//            int col = i % 3;
//
//            Vector3 pos = new Vector3((col - 1) * 5 / 16D, 0.1 + 0.01 * Math.sin(i * 1.7 + ClientUtils.getRenderTime() / 5), (row - 1) * 5 / 16D).apply(orient);
//            GlStateManager.pushMatrix();
//            GlStateManager.translated(pos.x, pos.y, pos.z);
//            GlStateManager.scaled(0.5, 0.5, 0.5);
//
//            RenderUtils.renderItemUniform(item);
//
//            GlStateManager.popMatrix();
//        }
//
//        if (!tcraft.result.isEmpty()) {
//            GlStateManager.pushMatrix();
//            GlStateManager.translated(0, 0.6 + 0.02 * Math.sin(ClientUtils.getRenderTime() / 10), 0);
//            GlStateManager.scaled(0.8, 0.8, 0.8);
//
//            RenderUtils.renderItemUniform(tcraft.result, ClientUtils.getRenderTime());
//
//            GlStateManager.popMatrix();
//        }
//
//        GlStateManager.popMatrix();
//        GlStateManager.disableRescaleNormal();
//    }
}
