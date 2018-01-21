package codechicken.translocator.client.render;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.CustomGradient;
import codechicken.lib.lighting.LightModel;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.OBJParser;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.SwapYZ;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.multipart.TileMultipart;
import codechicken.translocator.part.FluidTranslocatorPart;
import codechicken.translocator.part.ItemTranslocatorPart;
import codechicken.translocator.part.TranslocatorPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.vecmath.Matrix4f;
import java.util.Map;

import static codechicken.lib.vec.Rotation.sideRotations;
import static codechicken.translocator.init.TranslocatorTextures.TEXTURES;

/**
 * Created by covers1624 on 14/11/2017.
 */
public class RenderTranslocator {

    //@formatter:off
    public static Vector3[] sidePos = new Vector3[] {
            new Vector3(0.5, 0, 0.5),
            new Vector3(0.5, 1, 0.5),
            new Vector3(0.5, 0.5, 0),
            new Vector3(0.5, 0.5, 1),
            new Vector3(0, 0.5, 0.5),
            new Vector3(1, 0.5, 0.5)
    };
    public static Vector3[] sideVec = new Vector3[] {
            new Vector3(0, -1, 0),
            new Vector3(0, 1, 0),
            new Vector3(0, 0, -1),
            new Vector3(0, 0, 1),
            new Vector3(-1, 0, 0),
            new Vector3(1, 0, 0)
    };
    //@formatter:on

    private static CCModel[] plates = new CCModel[6];
    private static CCModel insert;

    private static CustomGradient gradient = new CustomGradient(new ResourceLocation("translocator", "textures/fx/grad.png"));

    static {
        Map<String, CCModel> models = OBJParser.parseModels(new ResourceLocation("translocator", "models/model_new.obj"), 0x07, new SwapYZ());
        plates[0] = models.get("Plate");
        insert = models.get("Insert");
        CCModel.generateSidedModels(plates, 0, new Vector3());
        for (int i = 0; i < 6; i++) {
            plates[i].computeLighting(LightModel.standardLightModel);
        }
    }

    public static void renderWorld(CCRenderState ccrs, TranslocatorPart p, Vector3 pos) {
        Vector3 trans = pos.copy().add(Vector3.center);
        IconTransformation i_trans = new IconTransformation(TEXTURES[p.getTType()][p.getIconIndex()]);
        ccrs.reset();
        ccrs.pullLightmap();
        plates[p.side].render(ccrs, trans.translation(), i_trans);
    }

    public static void renderItem(ItemStack stack) {
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.startDrawing(0x07, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        IconTransformation i_trans = new IconTransformation(TEXTURES[stack.getMetadata()][0]);
        Vector3 v_trans = Vector3.center.copy().add(0, 0, 0.5);
        Transformation trans = v_trans.translation();
        Matrix4 i_matrix = new Matrix4().translate(v_trans).apply(sideRotations[2]).translate(new Vector3(0, -0.5, 0)).scale(new Vector3(1, 1D * 2 / 3 + 1 / 3D, 1));
        plates[2].render(ccrs, trans, i_trans);
        insert.render(ccrs, i_matrix, i_trans);
        ccrs.draw();
    }

    public static void renderDynamic(TranslocatorPart p, Vector3 pos, float delta) {
        CCRenderState ccrs = CCRenderState.instance();
        double time = ClientUtils.getRenderTime();

        //Render the insert.
        TextureUtils.bindBlockTexture();
        double insertpos = MathHelper.interpolate(p.b_insertpos, p.a_insertpos, delta);
        IconTransformation i_trans = new IconTransformation(TEXTURES[p.getTType()][p.getIconIndex()]);
        Matrix4 matrix = new Matrix4().translate(pos.copy().add(Vector3.center)).apply(sideRotations[p.side]).translate(new Vector3(0, -0.5, 0)).scale(new Vector3(1, insertpos * 2 / 3 + 1 / 3D, 1));
        ccrs.reset();
        ccrs.pullLightmap();
        ccrs.startDrawing(0x07, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        insert.render(ccrs, matrix, i_trans);
        ccrs.draw();

        //Render the particles.
        TileMultipart tile = p.tile();
        if (p.a_eject) {
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            TextureUtils.changeTexture("translocator:textures/fx/particle.png");
            ccrs.startDrawing(0x07, DefaultVertexFormats.POSITION_TEX_COLOR);
            for (int dst = 0; dst < 6; dst++) {
                if (dst == p.side) {
                    continue;
                }
                if (p.canConnect(dst)) {
                    TranslocatorPart p_dst = (TranslocatorPart) tile.partMap(dst);
                    if (!p_dst.canEject()) {
                        renderLink(ccrs, p.side, dst, time, Vector3.fromBlockPos(p.pos()));
                    }
                }
            }
            ccrs.draw();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();

        }
    }

    public static void renderFluid(FluidTranslocatorPart p, Vector3 pos, float delta) {
        CCRenderState ccrs = CCRenderState.instance();
        double time = ClientUtils.getRenderTime();
        for (FluidTranslocatorPart.MovingLiquid m : p.getMovingLiquids()) {
            double start = MathHelper.interpolate(m.b_start, m.a_start, delta);
            double end = MathHelper.interpolate(m.b_end, m.a_end, delta);

            drawLiquidSpiral(ccrs, p.side, m.dst, m.liquid, start, end, time, 0, pos.x, pos.y, pos.z);
            if (p.fast) {
                drawLiquidSpiral(ccrs, p.side, m.dst, m.liquid, start, end, time, 0.5, pos.x, pos.y, pos.z);
            }
        }
    }

    public static void renderItem(ItemTranslocatorPart p, Vector3 pos, float delta) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        for (ItemTranslocatorPart.MovingItem m : p.movingItems) {
            GlStateManager.pushMatrix();
            double d = MathHelper.interpolate(m.b_progress, m.a_progress, delta);
            Vector3 path = getPath(p.side, m.dst, d).add(itemFloat(p.side, m.dst, d)).add(pos);
            GlStateManager.translate(path.x, path.y, path.z);
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.scale(0.35, 0.35, 0.35);
            renderItem.renderItem(m.stack, TransformType.FIXED);
            //RenderUtils.renderItemUniform(m.stack);
            GlStateManager.popMatrix();
        }
    }

    private static void renderLink(CCRenderState ccrs, int src, int dst, double time, Vector3 tPos) {

        double d = ((time + src + dst * 2) % 10) / 6;
        //0 is head
        for (int n = 0; n < 20; n++) {
            double dn = d - n * 0.1;
            int spriteX = (int) (7 - n * 1.5 - d * 2);
            if (!MathHelper.between(0, dn, 1) || spriteX < 0) {
                continue;
            }

            Vector3 pos = getPath(src, dst, dn).add(tPos);
            double b = 1;//d*0.6+0.4;
            double s = 1;//d*0.6+0.4;

            double u1 = spriteX / 8D;
            double u2 = u1 + 1 / 8D;
            double v1 = 0;
            double v2 = 1;

            renderParticle(ccrs, pos, gradient.getColour((dn - 0.5) * 1.2 + 0.5).multiplyC(b), s * 0.12, u1, v1, u2, v2);
        }
    }

    private static void drawLiquidSpiral(CCRenderState ccrs, int src, int dst, FluidStack stack, double start, double end, double time, double theta0, double x, double y, double z) {

        RenderUtils.preFluidRender();
        TextureAtlasSprite tex = RenderUtils.prepareFluidRender(stack, 255);

        BufferBuilder vertexBuffer = ccrs.startDrawing(7, DefaultVertexFormats.POSITION_TEX);
        vertexBuffer.setTranslation(x, y, z);

        Vector3[] last = new Vector3[] { new Vector3(), new Vector3(), new Vector3(), new Vector3() };
        Vector3[] next = new Vector3[] { new Vector3(), new Vector3(), new Vector3(), new Vector3() };
        double tess = 0.05;

        Vector3 a = getPerp(src, dst);
        boolean rev = sum(a.copy().crossProduct(getPathNormal(src, dst, 0))) != sum(sideVec[src]);

        for (double di = end; di <= start; di += tess) {
            Vector3 b = getPathNormal(src, dst, di);
            Vector3 c = getPath(src, dst, di);

            if (rev) {
                b.negate();
            }

            double r = (2 * di - time / 10 + theta0 + dst / 6) * 2 * Math.PI;
            double sz = 0.1;
            Vector3 p = c.add(a.copy().multiply(MathHelper.sin(r) * sz)).add(b.copy().multiply(MathHelper.cos(r) * sz));

            double s1 = 0.02;
            double s2 = -0.02;
            next[0].set(p).add(a.x * s1 + b.x * s1, a.y * s1 + b.y * s1, a.z * s1 + b.z * s1);
            next[1].set(p).add(a.x * s2 + b.x * s1, a.y * s2 + b.y * s1, a.z * s2 + b.z * s1);
            next[2].set(p).add(a.x * s2 + b.x * s2, a.y * s2 + b.y * s2, a.z * s2 + b.z * s2);
            next[3].set(p).add(a.x * s1 + b.x * s2, a.y * s1 + b.y * s2, a.z * s1 + b.z * s2);

            if (di > end) {
                double u1 = tex.getInterpolatedU(Math.abs(di) * 16);
                double u2 = tex.getInterpolatedU(Math.abs(di - tess) * 16);
                for (int i = 0; i < 4; i++) {
                    int j = (i + 1) % 4;
                    Vector3 axis = next[j].copy().subtract(next[i]);
                    double v1 = tex.getInterpolatedV(Math.abs(next[i].scalarProject(axis)) * 16);
                    double v2 = tex.getInterpolatedV(Math.abs(next[j].scalarProject(axis)) * 16);

                    vertexBuffer.pos(next[i].x, next[i].y, next[i].z).tex(u1, v1).endVertex();
                    vertexBuffer.pos(next[j].x, next[j].y, next[j].z).tex(u1, v2).endVertex();
                    vertexBuffer.pos(last[j].x, last[j].y, last[j].z).tex(u2, v2).endVertex();
                    vertexBuffer.pos(last[i].x, last[i].y, last[i].z).tex(u2, v1).endVertex();
                }
            }

            Vector3[] tmp = last;
            last = next;
            next = tmp;
        }

        ccrs.draw();
        vertexBuffer.setTranslation(0, 0, 0);

        RenderUtils.postFluidRender();
    }

    public static void renderParticle(CCRenderState ccrs, Vector3 pos, Colour colour, double s, double u1, double v1, double u2, double v2) {
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;
        x -= Particle.interpPosX;
        y -= Particle.interpPosY;
        z -= Particle.interpPosZ;

        float par3 = ActiveRenderInfo.getRotationX();
        float par4 = ActiveRenderInfo.getRotationXZ();
        float par5 = ActiveRenderInfo.getRotationZ();
        float par6 = ActiveRenderInfo.getRotationYZ();
        float par7 = ActiveRenderInfo.getRotationXY();

        BufferBuilder b = ccrs.getBuffer();
        b.pos(x - par3 * s - par6 * s, y - par4 * s, z - par5 * s - par7 * s).tex(u2, v2).color(colour.r & 0xFF, colour.g & 0xFF, colour.b & 0xFF, colour.a & 0xFF).endVertex();
        b.pos(x - par3 * s + par6 * s, y + par4 * s, z - par5 * s + par7 * s).tex(u2, v1).color(colour.r & 0xFF, colour.g & 0xFF, colour.b & 0xFF, colour.a & 0xFF).endVertex();
        b.pos(x + par3 * s + par6 * s, y + par4 * s, z + par5 * s + par7 * s).tex(u1, v1).color(colour.r & 0xFF, colour.g & 0xFF, colour.b & 0xFF, colour.a & 0xFF).endVertex();
        b.pos(x + par3 * s - par6 * s, y - par4 * s, z + par5 * s - par7 * s).tex(u1, v2).color(colour.r & 0xFF, colour.g & 0xFF, colour.b & 0xFF, colour.a & 0xFF).endVertex();
    }

    public static Vector3 getPath(int src, int dst, double d) {

        Vector3 v;
        if ((src ^ 1) == dst)//opposite
        {
            v = sideVec[src ^ 1].copy().multiply(d);
        } else {
            Vector3 vsrc = sideVec[src ^ 1];
            Vector3 vdst = sideVec[dst ^ 1];
            Vector3 a = vsrc.copy().multiply(5 / 16D);
            Vector3 b = vdst.copy().multiply(6 / 16D);
            double sind = MathHelper.sin(d * Math.PI / 2);
            double cosd = MathHelper.cos(d * Math.PI / 2);
            v = a.multiply(sind).add(b.multiply(cosd - 1)).add(vsrc.copy().multiply(3 / 16D));
        }
        return v.add(sidePos[src]);
    }

    private static Vector3 getPathNormal(int srcSide, int dstSide, double d) {

        if ((srcSide ^ 1) == dstSide) {
            return sideVec[(srcSide + 4) % 6].copy();
        }

        double sind = MathHelper.sin(d * Math.PI / 2);
        double cosd = MathHelper.cos(d * Math.PI / 2);

        Vector3 vsrc = sideVec[srcSide ^ 1].copy();
        Vector3 vdst = sideVec[dstSide ^ 1].copy();

        return vsrc.multiply(sind).add(vdst.multiply(cosd)).normalize();
    }

    private static Vector3 itemFloat(int src, int dst, double d) {

        return getPerp(src, dst).multiply(0.01 * MathHelper.sin(d * 4 * Math.PI));
    }

    public static Vector3 getPerp(int src, int dst) {

        if ((src ^ 1) == dst) {
            return sideVec[(src + 2) % 6].copy();
        }

        for (int i = 0; i < 3; i++) {
            if (i != src / 2 && i != dst / 2) {
                return sideVec[i * 2].copy();
            }
        }

        return null;
    }

    private static double sum(Vector3 v) {

        return v.x + v.y + v.z;
    }

}
