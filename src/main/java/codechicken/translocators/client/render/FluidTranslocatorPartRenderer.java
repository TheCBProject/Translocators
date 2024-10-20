package codechicken.translocators.client.render;

import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import codechicken.translocators.part.FluidTranslocatorPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Created by covers1624 on 4/5/23.
 */
public class FluidTranslocatorPartRenderer extends TranslocatorPartRenderer<FluidTranslocatorPart> {

    @Override
    public void renderDynamic(FluidTranslocatorPart part, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, float partialTicks) {
        super.renderDynamic(part, pStack, buffers, packedLight, packedOverlay, partialTicks);
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.bind(RenderUtils.getFluidRenderType(), buffers);
        double time = ClientUtils.getRenderTime();
        Matrix4 mat = new Matrix4(pStack);
        for (FluidTranslocatorPart.MovingLiquid m : part.getMovingLiquids()) {
            double start = MathHelper.interpolate(m.b_start, m.a_start, partialTicks);
            double end = MathHelper.interpolate(m.b_end, m.a_end, partialTicks);

            drawLiquidSpiral(ccrs, mat, part.side, m.dst, m.liquid, start, end, time, 0);
            if (part.fast) {
                drawLiquidSpiral(ccrs, mat, part.side, m.dst, m.liquid, start, end, time, 0.5);
            }
        }
    }

    private static void drawLiquidSpiral(CCRenderState ccrs, Matrix4 mat, int src, int dst, FluidStack stack, double start, double end, double time, double theta0) {
        if (stack.isEmpty()) {
            return;
        }

        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(stack.getFluid());
        Material material = ClientHooks.getBlockMaterial(props.getStillTexture(stack));
        TextureAtlasSprite tex = material.sprite();
        ccrs.colour = props.getTintColor(stack) << 8 | 255;//Set ccrs.colour opposed to baseColour as we call writeVert manually bellow.

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
                double u1 = tex.getU((float) Math.abs(di));
                double u2 = tex.getU((float) Math.abs(di - tess));
                for (int i = 0; i < 4; i++) {
                    int j = (i + 1) % 4;
                    Vector3 axis = next[j].copy().subtract(next[i]);
                    double v1 = tex.getV((float) Math.abs(next[i].scalarProject(axis)));
                    double v2 = tex.getV((float) Math.abs(next[j].scalarProject(axis)));

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

    private static double sum(Vector3 v) {
        return v.x + v.y + v.z;
    }
}
