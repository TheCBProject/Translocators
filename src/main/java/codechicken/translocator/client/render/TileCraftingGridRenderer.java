package codechicken.translocator.client.render;

import codechicken.core.ClientUtils;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.TextureUtils;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.translocator.tile.TileCraftingGrid;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class TileCraftingGridRenderer extends TileEntitySpecialRenderer<TileCraftingGrid> {

    @Override
    public void renderTileEntityAt(TileCraftingGrid tcraft, double x, double y, double z, float partialTicks, int destroyStage) {
        TextureUtils.bindBlockTexture();
        TextureUtils.dissableBlockMipmap();
        TextureAtlasSprite icon = TextureUtils.getBlockTexture("translocator:craftingGrid");

        CCRenderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        VertexBuffer buffer = CCRenderState.pullBuffer();
        buffer.setTranslation(x, y + 0.001, z);
        buffer.pos(1, 0, 0).tex(icon.getMinU(), icon.getMinV()).normal(0, 1, 0).endVertex();
        buffer.pos(0, 0, 0).tex(icon.getMinU(), icon.getMaxV()).normal(0, 1, 0).endVertex();
        buffer.pos(0, 0, 1).tex(icon.getMaxU(), icon.getMaxV()).normal(0, 1, 0).endVertex();
        buffer.pos(1, 0, 1).tex(icon.getMaxU(), icon.getMinV()).normal(0, 1, 0).endVertex();
        buffer.setTranslation(0, 0, 0);
        CCRenderState.draw();
        TextureUtils.restoreBlockMipmap();


        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        Transformation orient = Rotation.quarterRotations[tcraft.rotation];

        for (int i = 0; i < 9; i++) {
            ItemStack item = tcraft.items[i];
            if (item == null) {
                continue;
            }

            int row = i / 3;
            int col = i % 3;

            Vector3 pos = new Vector3((col - 1) * 5 / 16D, 0.1 + 0.01 * Math.sin(i * 1.7 + ClientUtils.getRenderTime() / 5), (row - 1) * 5 / 16D).apply(orient);
            GlStateManager.pushMatrix();
            GlStateManager.translate(pos.x, pos.y, pos.z);
            GlStateManager.scale(0.5, 0.5, 0.5);

            RenderUtils.renderItemUniform(item);

            GlStateManager.popMatrix();
        }

        if (tcraft.result != null) {
            GlStateManager.pushMatrix(); //GL11.glPushMatrix();
            GlStateManager.translate(0, 0.6 + 0.02 * Math.sin(ClientUtils.getRenderTime() / 10), 0);
            GlStateManager.scale(0.8, 0.8, 0.8);

            RenderUtils.renderItemUniform(tcraft.result, ClientUtils.getRenderTime());

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();

    }
}
