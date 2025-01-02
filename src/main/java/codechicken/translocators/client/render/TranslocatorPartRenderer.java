package codechicken.translocators.client.render;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.CustomGradient;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.render.pipeline.attribute.LightCoordAttribute;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.multipart.api.part.render.PartRenderer;
import codechicken.multipart.block.TileMultipart;
import codechicken.multipart.util.PartRayTraceResult;
import codechicken.translocators.part.TranslocatorPart;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static codechicken.lib.vec.Rotation.sideRotations;
import static codechicken.translocators.Translocators.MOD_ID;
import static codechicken.translocators.init.TranslocatorTextures.TEXTURES;

/**
 * Created by covers1624 on 4/5/23.
 */
public class TranslocatorPartRenderer<T extends TranslocatorPart> implements PartRenderer<T> {

    protected static final Vector3[] sidePos = new Vector3[] {
            new Vector3(0.5, 0, 0.5),
            new Vector3(0.5, 1, 0.5),
            new Vector3(0.5, 0.5, 0),
            new Vector3(0.5, 0.5, 1),
            new Vector3(0, 0.5, 0.5),
            new Vector3(1, 0.5, 0.5)
    };
    protected static final Vector3[] sideVec = new Vector3[] {
            new Vector3(0, -1, 0),
            new Vector3(0, 1, 0),
            new Vector3(0, 0, -1),
            new Vector3(0, 0, 1),
            new Vector3(-1, 0, 0),
            new Vector3(1, 0, 0)
    };

    private static final CustomGradient gradient = new CustomGradient(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/fx/grad.png"));

    private static final RenderType ITEM_RENDER_TYPE = RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS);
    private static final RenderType particleType = RenderType.create("translocator_link", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 255, RenderType.CompositeState.builder()
            .setShaderState(RenderType.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/fx/particle.png"), false, false))
            .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
            .setLightmapState(RenderType.LIGHTMAP)
            .createCompositeState(false)
    );

    private static final CCModel[] plates = new CCModel[6];
    private static final CCModel insert;

    static {
        Map<String, CCModel> models = new OBJParser(ResourceLocation.fromNamespaceAndPath(MOD_ID, "models/model.obj"))
                .ignoreMtl()
                .quads()
                .swapYZ()
                .parse();
        plates[0] = models.get("Plate");
        insert = models.get("Insert");
        CCModel.generateSidedModels(plates, 0, new Vector3());
    }

    @Override
    public void renderStatic(T part, @Nullable RenderType layer, CCRenderState ccrs) {
        Vector3 trans = Vector3.CENTER;
        IconTransformation i_trans = new IconTransformation(TEXTURES[part.getTType()][part.getIconIndex()]);
        ccrs.reset();
        ccrs.setBrightness(part.level(), part.pos());
        plates[part.side].render(ccrs, trans.translation(), i_trans, ccrs.lightMatrix);
    }

    @Override
    public void renderDynamic(T part, PoseStack pStack, MultiBufferSource buffers, int packedLight, int packedOverlay, float partialTicks) {
        BlockPos pos = part.pos();
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        double insertpos = MathHelper.interpolate(part.b_insertpos, part.a_insertpos, partialTicks);
        IconTransformation i_trans = new IconTransformation(TEXTURES[part.getTType()][part.getIconIndex()]);

        ccrs.reset();
        ccrs.bind(RenderType.solid(), buffers, pStack);
        ccrs.lightMatrix.locate(part.level(), pos);
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        Matrix4 mat = new Matrix4()
                .translate(Vector3.CENTER)
                .apply(sideRotations[part.side])
                .translate(new Vector3(0, -0.5, 0))
                .scale(1, insertpos * 2 / 3 + 1 / 3D, 1);
        insert.render(ccrs, mat, i_trans, ccrs.lightMatrix);

        ccrs.reset();
        renderLinks(part, ccrs, pStack, buffers);
    }

    @Override
    public boolean drawHighlight(T part, PartRayTraceResult hit, Camera camera, PoseStack pStack, MultiBufferSource buffers, float partialTicks) {
        if (hit.subHit != TranslocatorPart.HIT_INSERT) return false;

        RenderUtils.bufferHitbox(new Matrix4(pStack).translate(hit.getBlockPos()), buffers, camera, part.getInsertBounds());
        return true;
    }

    public static void renderItem(int type, PoseStack pStack, ItemDisplayContext ctx, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.bind(ITEM_RENDER_TYPE, buffers);
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;
        IconTransformation i_trans = new IconTransformation(TEXTURES[type][0]);
        Vector3 v_trans = Vector3.CENTER.copy().add(0, 0, 0.5);
        if (ctx == ItemDisplayContext.GROUND) {
            v_trans.subtract(0, 0.5, 0);
        }
        Matrix4 mat = new Matrix4(pStack);
        Matrix4 i_matrix = mat.copy()
                .translate(v_trans)
                .apply(sideRotations[2])
                .translate(new Vector3(0, -0.5, 0))
                .scale(new Vector3(1, 1D * 2 / 3 + 1 / 3D, 1));
        mat.translate(v_trans);
        plates[2].render(ccrs, mat, i_trans);
        insert.render(ccrs, i_matrix, i_trans);
        ccrs.reset();
    }

    private static void renderLinks(TranslocatorPart p, CCRenderState ccrs, PoseStack pStack, MultiBufferSource buffers) {
        double time = ClientUtils.getRenderTime();
        //Render the particles.
        TileMultipart tile = p.tile();
        if (p.a_eject) {
            Matrix4 mat = new Matrix4(pStack);
            ccrs.bind(particleType, buffers);
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

    public static void renderParticle(CCRenderState ccrs, Matrix4 mat, Colour colour, double s, double u1, double v1, double u2, double v2) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        double rotationX = Math.cos(camera.getYRot() * MathHelper.torad);
        double rotationZ = Math.sin(camera.getYRot() * MathHelper.torad);
        double rotationYZ = -rotationZ * Math.sin(camera.getXRot() * MathHelper.torad);
        double rotationXY = rotationX * Math.sin(camera.getXRot() * MathHelper.torad);
        double rotationXZ = Math.cos(camera.getXRot() * MathHelper.torad);

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
        if ((src ^ 1) == dst) { //opposite
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
}
