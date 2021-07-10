package codechicken.translocators.client.render;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.CustomGradient;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.OBJParser;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.lighting.LightModel;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.SwapYZ;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.multipart.block.TileMultiPart;
import codechicken.translocators.part.FluidTranslocatorPart;
import codechicken.translocators.part.ItemTranslocatorPart;
import codechicken.translocators.part.TranslocatorPart;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.Map;

import static codechicken.lib.util.SneakyUtils.none;
import static codechicken.lib.vec.Rotation.sideRotations;
import static codechicken.translocators.init.TranslocatorTextures.TEXTURES;
import static net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;

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

    private static final CCModel[] plates = new CCModel[6];
    private static final CCModel insert;

    private static final CustomGradient gradient = new CustomGradient(new ResourceLocation("translocators", "textures/fx/grad.png"));
    private static final RenderType particleType = RenderType.makeType("translocator_link", DefaultVertexFormats.POSITION_TEX_COLOR, GL11.GL_QUADS, 255, RenderType.State.getBuilder()//
            .texture(new RenderState.TextureState(new ResourceLocation("translocators:textures/fx/particle.png"), false, false))//
            .transparency(RenderType.TRANSLUCENT_TRANSPARENCY)//
            .writeMask(RenderType.COLOR_WRITE)//
            .texturing(new RenderState.TexturingState("lighting", RenderSystem::disableLighting, none()))//
            .build(false)//
    );

    static {
        Map<String, CCModel> models = OBJParser.parseModels(new ResourceLocation("translocators", "models/model_new.obj"), 0x07, new SwapYZ());
        plates[0] = models.get("Plate");
        insert = models.get("Insert");
        CCModel.generateSidedModels(plates, 0, new Vector3());
        for (int i = 0; i < 6; i++) {
            plates[i].computeLighting(LightModel.standardLightModel);
        }
    }

    public static void renderStatic(CCRenderState ccrs, TranslocatorPart p) {
        Vector3 trans = Vector3.CENTER;
        IconTransformation i_trans = new IconTransformation(TEXTURES[p.getTType()][p.getIconIndex()]);
        ccrs.reset();
        ccrs.setBrightness(p.world(), p.pos());
        plates[p.side].render(ccrs, trans.translation(), i_trans);
    }

    public static void renderInsert(TranslocatorPart p, CCRenderState ccrs, MatrixStack mStack, IRenderTypeBuffer getter, int packedLight, int packedOverlay, float delta) {
        Matrix4 mat = new Matrix4(mStack);
        double insertpos = MathHelper.interpolate(p.b_insertpos, p.a_insertpos, delta);
        IconTransformation i_trans = new IconTransformation(TEXTURES[p.getTType()][p.getIconIndex()]);
        mat.translate(Vector3.CENTER);
        mat.apply(sideRotations[p.side]);
        mat.translate(new Vector3(0, -0.5, 0));
        mat.scale(1, insertpos * 2 / 3 + 1 / 3D, 1);
        ccrs.reset();
        ccrs.bind(RenderType.getSolid(), getter);
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        insert.render(ccrs, mat, i_trans);

    }

    public static void renderItem(int type, MatrixStack mStack, TransformType transformType, IRenderTypeBuffer buffers, int packedLight, int packedOverlay) {
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.bind(RenderType.getSolid(), buffers);
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        IconTransformation i_trans = new IconTransformation(TEXTURES[type][0]);
        Vector3 v_trans = Vector3.CENTER.copy().add(0, 0, 0.5);
        if (transformType == TransformType.GROUND) {
            v_trans.subtract(0, 0.5, 0);
        }
        Matrix4 mat = new Matrix4(mStack);
        Matrix4 i_matrix = mat.copy()
                .translate(v_trans)
                .apply(sideRotations[2])
                .translate(new Vector3(0, -0.5, 0))
                .scale(new Vector3(1, 1D * 2 / 3 + 1 / 3D, 1));
        mat.translate(v_trans);
        plates[2].render(ccrs, mat, i_trans);
        insert.render(ccrs, i_matrix, i_trans);
    }

    public static void renderLinks(TranslocatorPart p, CCRenderState ccrs, MatrixStack mStack, IRenderTypeBuffer getter) {
        double time = ClientUtils.getRenderTime();
        //Render the particles.
        TileMultiPart tile = p.tile();
        if (p.a_eject) {
            Matrix4 mat = new Matrix4(mStack);
            ccrs.bind(particleType, getter);
            for (int dst = 0; dst < 6; dst++) {
                if (dst == p.side) {
                    continue;
                }
                if (p.canConnect(dst)) {
                    TranslocatorPart p_dst = (TranslocatorPart) tile.getSlottedPart(dst);
                    if (!p_dst.canEject()) {
                        renderLink(ccrs, mat, p.side, dst, time);
                    }
                }
            }
        }
    }

    public static void renderFluid(FluidTranslocatorPart p, MatrixStack mStack, IRenderTypeBuffer getter, float delta) {
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.bind(RenderUtils.getFluidRenderType(), getter);
        double time = ClientUtils.getRenderTime();
        Matrix4 mat = new Matrix4(mStack);
        for (FluidTranslocatorPart.MovingLiquid m : p.getMovingLiquids()) {
            double start = MathHelper.interpolate(m.b_start, m.a_start, delta);
            double end = MathHelper.interpolate(m.b_end, m.a_end, delta);

            drawLiquidSpiral(ccrs, mat, p.side, m.dst, m.liquid, start, end, time, 0);
            if (p.fast) {
                drawLiquidSpiral(ccrs, mat, p.side, m.dst, m.liquid, start, end, time, 0.5);
            }
        }
    }

    public static void renderItem(ItemTranslocatorPart p, MatrixStack mStack, IRenderTypeBuffer getter, int packedLight, int packedOverlay, float delta) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        for (ItemTranslocatorPart.MovingItem m : p.movingItems) {
            mStack.push();
            double d = MathHelper.interpolate(m.b_progress, m.a_progress, delta);
            Vector3 path = getPath(p.side, m.dst, d).add(itemFloat(p.side, m.dst, d));
            mStack.translate(path.x, path.y, path.z);
            mStack.scale(0.5f, 0.5f, 0.5f);
            mStack.scale(0.35f, 0.35f, 0.35f);
            itemRenderer.renderItem(m.stack, TransformType.FIXED, packedLight, packedOverlay, mStack, getter);
            mStack.pop();
        }
    }

    private static void renderLink(CCRenderState ccrs, Matrix4 mat, int src, int dst, double time) {

        double d = ((time + src + dst * 2) % 10) / 6;
        //0 is head
        for (int n = 0; n < 20; n++) {
            double dn = d - n * 0.1;
            int spriteX = (int) (7 - n * 1.5 - d * 2);
            if (!MathHelper.between(0, dn, 1) || spriteX < 0) {
                continue;
            }

            Vector3 pos = getPath(src, dst, dn);
            double b = 1;//d*0.6+0.4;
            double s = 1;//d*0.6+0.4;

            double u1 = spriteX / 8D;
            double u2 = u1 + 1 / 8D;
            double v1 = 0;
            double v2 = 1;

            renderParticle(ccrs, mat.copy().translate(pos), gradient.getColour((dn - 0.5) * 1.2 + 0.5).multiplyC(b), s * 0.12, u1, v1, u2, v2);
        }
    }

    private static void drawLiquidSpiral(CCRenderState ccrs, Matrix4 mat, int src, int dst, FluidStack stack, double start, double end, double time, double theta0) {
        if (stack.isEmpty()) {
            return;
        }

        FluidAttributes attribs = stack.getFluid().getAttributes();
        Material material = ForgeHooksClient.getBlockMaterial(attribs.getStillTexture(stack));
        TextureAtlasSprite tex = material.getSprite();
        ccrs.colour = attribs.getColor(stack) << 8 | 255;//Set ccrs.colour opposed to baseColour as we call writeVert manually bellow.

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
            Vector3 p = c.add(a.copy().multiply(Math.sin(r) * sz)).add(b.copy().multiply(Math.cos(r) * sz));

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

                    ccrs.vert.set(next[i], u1, v1).apply(mat);
                    ccrs.writeVert();
                    ccrs.vert.set(next[j], u1, v2).apply(mat);
                    ccrs.writeVert();
                    ccrs.vert.set(last[j], u2, v2).apply(mat);
                    ccrs.writeVert();
                    ccrs.vert.set(last[i], u2, v1).apply(mat);
                    ccrs.writeVert();
                }
            }

            Vector3[] tmp = last;
            last = next;
            next = tmp;
        }
    }

    public static void renderParticle(CCRenderState ccrs, Matrix4 mat, Colour colour, double s, double u1, double v1, double u2, double v2) {
        ActiveRenderInfo info = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();

        double rotationX = Math.cos(info.getYaw() * MathHelper.torad);
        double rotationZ = Math.sin(info.getYaw() * MathHelper.torad);
        double rotationYZ = -rotationZ * Math.sin(info.getPitch() * MathHelper.torad);
        double rotationXY = rotationX * Math.sin(info.getPitch() * MathHelper.torad);
        double rotationXZ = Math.cos(info.getPitch() * MathHelper.torad);

        ccrs.colour = colour.rgba();
        ccrs.vert.set(-rotationX * s - rotationYZ * s, -rotationXZ * s, -rotationZ * s - rotationXY * s, u2, v2).apply(mat);
        ccrs.writeVert();
        ccrs.vert.set(-rotationX * s + rotationYZ * s, rotationXZ * s, -rotationZ * s + rotationXY * s, u2, v1).apply(mat);
        ccrs.writeVert();
        ccrs.vert.set(rotationX * s + rotationYZ * s, rotationXZ * s, rotationZ * s + rotationXY * s, u1, v1).apply(mat);
        ccrs.writeVert();
        ccrs.vert.set(rotationX * s - rotationYZ * s, -rotationXZ * s, rotationZ * s - rotationXY * s, u1, v2).apply(mat);
        ccrs.writeVert();
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
            double sind = Math.sin(d * Math.PI / 2);
            double cosd = Math.cos(d * Math.PI / 2);
            v = a.multiply(sind).add(b.multiply(cosd - 1)).add(vsrc.copy().multiply(3 / 16D));
        }
        return v.add(sidePos[src]);
    }

    private static Vector3 getPathNormal(int srcSide, int dstSide, double d) {

        if ((srcSide ^ 1) == dstSide) {
            return sideVec[(srcSide + 4) % 6].copy();
        }

        double sind = Math.sin(d * Math.PI / 2);
        double cosd = Math.cos(d * Math.PI / 2);

        Vector3 vsrc = sideVec[srcSide ^ 1].copy();
        Vector3 vdst = sideVec[dstSide ^ 1].copy();

        return vsrc.multiply(sind).add(vdst.multiply(cosd)).normalize();
    }

    private static Vector3 itemFloat(int src, int dst, double d) {

        return getPerp(src, dst).multiply(0.01 * Math.sin(d * 4 * Math.PI));
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
