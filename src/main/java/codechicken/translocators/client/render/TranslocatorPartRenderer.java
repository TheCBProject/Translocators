package codechicken.translocators.client.render;

import codechicken.lib.colour.CustomGradient;
import codechicken.lib.math.MathHelper;
import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.ExtentsConsumer;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Quat;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.multipart.api.part.render.PartRenderer;
import codechicken.multipart.util.PartRayTraceResult;
import codechicken.translocators.client.TranslocatorModelData;
import codechicken.translocators.client.render.TranslocatorPartRenderer.TranslocatorRenderState;
import codechicken.translocators.part.TranslocatorPart;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.model.data.ModelData;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static codechicken.lib.vec.Rotation.sideRotations;
import static codechicken.translocators.Translocators.MOD_ID;
import static codechicken.translocators.init.TranslocatorTextures.TEXTURES;

/**
 * Created by covers1624 on 4/5/23.
 */
public abstract class TranslocatorPartRenderer<T extends TranslocatorPart, S extends TranslocatorRenderState> implements PartRenderer<T, S> {

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

    private static final CustomGradient gradient = new CustomGradient(Identifier.fromNamespaceAndPath(MOD_ID, "textures/fx/grad.png"));

    //    private static final RenderType ITEM_RENDER_TYPE = RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS);
    private static final RenderType PARTICLE_TYPE = RenderType.create("translocator_link", RenderSetup.builder(RenderPipelines.TRANSLUCENT_PARTICLE)
            .useLightmap()
            .withTexture("Sampler0", Identifier.fromNamespaceAndPath(MOD_ID, "textures/fx/particle.png"))
            .createRenderSetup()
    );

    private static final CCModel[] plates = new CCModel[6];
    private static final CCModel insert;

    // TODO this should become non-static.
    // TODO PartRenderer instances should not be registered statically, we should re-load them on model bake.
    private static final Map<TranslocatorModelData, List<BlockModelPart>> BAKED = new ConcurrentHashMap<>();

    static {
        Map<String, CCModel> models = new OBJParser(Identifier.fromNamespaceAndPath(MOD_ID, "models/model.obj"))
                .ignoreMtl()
                .quads()
                .swapYZ()
                .parse();
        plates[0] = models.get("Plate");
        insert = models.get("Insert");
        CCModel.generateSidedModels(plates, 0, new Vector3());
    }

    @Override
    public void collectParts(ModelData modelData, BlockAndTintGetter level, BlockPos pos, RandomSource rand, List<BlockModelPart> parts) {
        var data = modelData.get(TranslocatorModelData.TYPE);
        if (data == null) return;

        var existing = BAKED.get(data);
        if (existing != null) {
            parts.addAll(existing);
            return;
        }

        synchronized (BAKED) {
            // TODO double latch here is yucky
            existing = BAKED.get(data);
            if (existing != null) {
                parts.addAll(existing);
                return;
            }

            var ccrs = CCRenderState.instance();
            ccrs.reset();
            var builder = ccrs.bindBaking();
            plates[data.side()].render(
                    ccrs,
                    Vector3.CENTER.translation(),
                    new IconTransformation(TEXTURES[data.type()][data.iconIndex()])
            );
            var quads = new QuadCollection.Builder();
            builder.bake().forEach(quads::addUnculledFace);
            List<BlockModelPart> baked = List.of(new SimpleModelWrapper(
                    quads.build(),
                    true,
                    TEXTURES[data.type()][data.iconIndex()],
                    ChunkSectionLayer.CUTOUT
            ));
            BAKED.put(data, baked);
            parts.addAll(baked);
        }
    }

    @Override
    public abstract S createDynamicState();

    @Override
    public void extractDynamicState(BEState beState, S state, T part, float partialTick, Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        state.side = part.side;
        state.type = part.getTType();
        state.iconIndex = part.getIconIndex();
        state.insertPos = MathHelper.interpolate(part.b_insertpos, part.a_insertpos, partialTick);
        state.renderLinksTo = buildLinkMask(part);
    }

    @Override
    public void submitDynamic(BEState beState, S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        var ccrs = CCRenderState.instance();

        IconTransformation i_trans = new IconTransformation(TEXTURES[state.type][state.iconIndex]);
        collector.submitCustomGeometry(poseStack, RenderTypes.solidMovingBlock(), (pose, consumer) -> {
            ccrs.reset();
            ccrs.bind(consumer, RenderTypes.solidMovingBlock().format());
            ccrs.brightness = beState.lightCoords();
            ccrs.overlay = OverlayTexture.NO_OVERLAY;
            var mat = new Matrix4(pose)
                    .translate(Vector3.CENTER)
                    .apply(sideRotations[state.side])
                    .translate(new Vector3(0, -0.5, 0))
                    .scale(1, state.insertPos * 2 / 3 + 1 / 3D, 1);
            insert.render(ccrs, mat, i_trans);
        });

        if (state.renderLinksTo != 0) {
            var cameraRot = new Quat(cameraState.orientation);

            collector.submitCustomGeometry(poseStack, PARTICLE_TYPE, (pose, consumer) -> {
                double time = ClientUtils.getRenderTime();
                for (int dst = 0; dst < 6; dst++) {
                    if (dst == state.side) continue;
                    if ((state.renderLinksTo & (1 << dst)) == 0) continue;
                    renderLink(consumer, pose, cameraRot, beState.lightCoords(), state.side, dst, time);
                }
            });
        }
    }

    @Override
    public boolean extractBlockHighlight(T part, PartRayTraceResult hit, LevelRenderer levelRenderer, LevelRenderState levelRenderState, Camera camera, CollisionContext collisionCtx, boolean isInTranslucentPass, boolean isHighContrast, Consumer<CustomBlockOutlineRenderer> cons) {
        if (!(hit.hit instanceof VoxelShapeBlockHitResult voxHit)) return false;
        if (voxHit.subHit != TranslocatorPart.HIT_INSERT) return false;

        levelRenderState.blockOutlineRenderState = new BlockOutlineRenderState(
                part.pos(),
                isInTranslucentPass,
                isHighContrast,
                voxHit.shape,
                List.of()
        );
        return true;
    }

    // TODO we can statically compute this into a Cuboid6 and just give it the min/max..
    public static void itemExtents(Consumer<Vector3fc> cons) {
        var ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.bind(new ExtentsConsumer(cons), DefaultVertexFormat.POSITION);

        Matrix4 mat = new Matrix4();
        IconTransformation i_trans = new IconTransformation(TEXTURES[0][0]);
        mat.translate(Vector3.CENTER);
        mat.translate(0, 0, 0.5);
        plates[2].render(ccrs, mat, i_trans);

        mat.apply(sideRotations[2]);
        mat.translate(new Vector3(0, -0.5, 0));
        mat.scale(new Vector3(1, 1D * 2 / 3 + 1 / 3D, 1));
        insert.render(ccrs, mat, i_trans);
    }

    public static void submitItem(int type, PoseStack pStack, ItemDisplayContext ctx, SubmitNodeCollector collector, int packedLight, int packedOverlay) {
        collector.cc$submitCCRS(pStack, Sheets.cutoutBlockSheet(), (mat, ccrs) -> {
            ccrs.brightness = packedLight;
            ccrs.overlay = packedOverlay;

            IconTransformation i_trans = new IconTransformation(TEXTURES[type][0]);
            mat.translate(Vector3.CENTER);
            mat.translate(0, 0, 0.5);
            if (ctx == ItemDisplayContext.GROUND) {
                mat.translate(0, 0.5, 0);
            }
            plates[2].render(ccrs, mat, i_trans);

            mat.apply(sideRotations[2]);
            mat.translate(new Vector3(0, -0.5, 0));
            mat.scale(new Vector3(1, 1D * 2 / 3 + 1 / 3D, 1));
            insert.render(ccrs, mat, i_trans);
        });
    }

    private static int buildLinkMask(TranslocatorPart p) {
        if (!p.a_eject) return 0;

        int mask = 0;
        for (int dst = 0; dst < 6; dst++) {
            if (!p.canConnect(dst)) continue;
            var p_dst = p.getOther(dst);
            if (p_dst.canEject()) continue;

            mask |= (1 << dst);
        }
        return mask;
    }

    private static void renderLink(VertexConsumer cons, PoseStack.Pose pose, Quat rot, int light, int src, int dst, double time) {
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

            var color = gradient.getColour((dn - 0.5) * 1.2 + 0.5).multiplyC(b).argb();
            var size = s * 0.12;

            Vector3 vec = new Vector3();

            addParticleVertex(cons, pose, vec.set(1, -1, 0).rotate(rot).multiply(size).add(pos), u2, v2, color, light);
            addParticleVertex(cons, pose, vec.set(1, 1, 0).rotate(rot).multiply(size).add(pos), u2, v1, color, light);
            addParticleVertex(cons, pose, vec.set(-1, 1, 0).rotate(rot).multiply(size).add(pos), u1, v1, color, light);
            addParticleVertex(cons, pose, vec.set(-1, -1, 0).rotate(rot).multiply(size).add(pos), u1, v2, color, light);
        }
    }

    private static void addParticleVertex(VertexConsumer cons, PoseStack.Pose pose, Vector3 vec, double u, double v, int color, int packedLight) {
        cons.addVertex(pose, (float) vec.x, (float) vec.y, (float) vec.z).setUv((float) u, (float) v).setColor(color).setLight(packedLight);
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

        throw new IllegalStateException();
    }

    public static class TranslocatorRenderState {

        public int side;
        public int type;
        public int iconIndex;
        public double insertPos;
        public int renderLinksTo;
    }
}
