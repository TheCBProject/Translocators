package codechicken.translocators.part;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.math.MathHelper;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.SubHitVoxelShape;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.*;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.api.part.ITickablePart;
import codechicken.multipart.api.part.TFacePart;
import codechicken.multipart.api.part.TMultiPart;
import codechicken.multipart.api.part.TNormalOcclusionPart;
import codechicken.multipart.util.PartRayTraceResult;
import codechicken.translocators.client.render.RenderTranslocator;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by covers1624 on 10/11/2017.
 */
public abstract class TranslocatorPart extends TMultiPart implements TFacePart, TNormalOcclusionPart, ITickablePart {

    public static final SoundType PLACEMENT_SOUND = new SoundType(1.0F, 1.0F, null, null, SoundEvents.BLOCK_STONE_STEP, null, null);

    public static Cuboid6 base = new Cuboid6(3 / 16D, 0, 3 / 16D, 13 / 16D, 2 / 16D, 13 / 16D);
    public static Cuboid6[] boxes = new Cuboid6[6];
    public static Cuboid6[][] base_parts = new Cuboid6[6][4];

    public static VoxelShape baseShape = base.shape();
    public static VoxelShape[] boxShapes = new VoxelShape[6];
    public static VoxelShape[][] basePartShapes = new VoxelShape[6][4];
    public static VoxelShape[] basePartsJoined = new VoxelShape[6];

    public static int HIT_BASE = 0;
    public static int HIT_INSERT = 1;

    static {
        for (int i = 0; i < 6; i++) {

            double d1 = 3D / 16D;
            double x1 = base.min.x;
            double y1 = base.min.y;
            double z1 = base.min.z;
            double x2 = base.max.x;
            double y2 = base.max.y;
            double z2 = base.max.z;

            Transformation rt = Rotation.sideRotations[i].at(Vector3.CENTER);

            boxes[i] = base.copy().apply(rt);
            base_parts[i] = new Cuboid6[] {//
                    new Cuboid6(x1, y1, z1, x1 + d1, y2, z2).apply(rt),//
                    new Cuboid6(x2 - d1, y1, z1, x2, y2, z2).apply(rt),//
                    new Cuboid6(x1 + d1, y1, z1, x2 - d1, y2, z1 + d1).apply(rt),//
                    new Cuboid6(x1 + d1, y1, z2 - d1, x2 - d1, y2, z2).apply(rt)//
            };
            boxShapes[i] = boxes[i].shape();
            basePartShapes[i] = Arrays.stream(base_parts[i]).map(Cuboid6::shape).toArray(VoxelShape[]::new);
            basePartsJoined[i] = Arrays.stream(basePartShapes[i]).reduce(VoxelShapes.empty(), VoxelShapes::or);
        }
    }

    public byte side;
    //Use canEject to check another parts state.
    public boolean a_eject;
    public boolean b_eject;

    public boolean redstone;
    public boolean invert_redstone;
    public boolean fast;

    public double a_insertpos;
    public double b_insertpos;

    public TranslocatorPart() {
        a_eject = b_eject = invert_redstone = true;
        a_insertpos = b_insertpos = 1;
    }

    //region Data
    //region NBT
    @Override
    public void save(CompoundNBT tag) {
        tag.putByte("side", side);
        tag.putBoolean("invert_redstone", invert_redstone);
        tag.putBoolean("redstone", redstone);
        tag.putBoolean("fast", fast);
    }

    @Override
    public void load(CompoundNBT tag) {
        side = tag.getByte("side");
        invert_redstone = tag.getBoolean("invert_redstone");
        redstone = tag.getBoolean("redstone");
        fast = tag.getBoolean("fast");
    }
    //endregion

    //region Desc
    @Override
    public void writeDesc(MCDataOutput packet) {
        packet.writeByte(side);
        packet.writeByte(writeFlags());
    }

    @Override
    public void readDesc(MCDataInput packet) {
        side = packet.readByte();
        readFlags(packet.readByte());
    }

    //Writes flags, IFFF FFFF
    //7 - Incremental
    //0 - Eject
    //1 - Restone
    //2 - Fast
    //ItemTranslocator
    //3 - Regulate
    //4 - Signal
    //5 - Powering
    protected int writeFlags() {
        int flags = 0;
        flags |= (a_eject ? 1 : 0)/* << 0*/;
        flags |= (redstone ? 1 : 0) << 1;
        flags |= (fast ? 1 : 0) << 2;
        return flags;
    }

    protected void readFlags(int flags) {
        a_eject = (flags & (1 /*<< 0*/)) != 0;
        redstone = (flags & (1 << 1)) != 0;
        fast = (flags & (1 << 2)) != 0;
    }
    //endregion

    //region Inc Updating
    @Override
    public final void readUpdate(MCDataInput packet) {
        int flags = packet.readByte();
        readFlags(flags);
        if ((flags & (1 << 7)) != 0) {
            readIncUpdate(packet);
        } else {
            onPartChanged(this);
            tile().markRender();
        }
    }

    //Sends just this Translocators flags
    public void sendFlagsUpdate() {
        sendUpdate(packet -> packet.writeByte(writeFlags()));
    }

    //Sends the flags, and extra data.
    public void sendIncUpdate(Consumer<MCDataOutput> func) {
        sendUpdate(packet -> {
            packet.writeByte(writeFlags() | (1 << 7));
            func.accept(packet);
        });
    }

    /**
     * Called when our Incremental update packet arrives.
     *
     * @param packet The packet to read data from.
     */
    public void readIncUpdate(MCDataInput packet) {
    }
    //endregion
    //endregion

    @Override
    public void onPartChanged(TMultiPart part) {
        if (!world().isRemote) {
            onNeighborBlockChanged(pos());
        }
        super.onPartChanged(part);
    }

    @Override
    public boolean solid(int side) {
        return false;
    }

    @Override
    public SoundType getPlacementSound(ItemStack stack, PlayerEntity player) {
        return PLACEMENT_SOUND;
    }

    @Override
    public int redstoneConductionMap() {
        return 0;
    }

    @Override
    public int getSlotMask() {
        return 1 << side;
    }

    public Cuboid6 getInsertBounds() {
        return new Cuboid6(6 / 16D, 0, 6 / 16D, 10 / 16D, a_insertpos * 2 / 16D + 1 / 16D, 10 / 16D).apply(Rotation.sideRotations[side].at(Vector3.CENTER));
    }

    @Override
    public VoxelShape getOutlineShape() {
        return boxShapes[side];
    }

    @Override
    public VoxelShape getOcclusionShape() {
        return boxShapes[side];
    }

    @Override
    public VoxelShape getCollisionShape() {
        //TODO add merging to VoxelShapeCache.
        return VoxelShapes.or(boxShapes[side], getInsertBounds().shape());
    }

    @Override
    public VoxelShape getRayTraceShape() {
        Cuboid6 insert = getInsertBounds();
        VoxelShape insertShape = insert.shape();
        List<IndexedCuboid6> parts = Lists.newArrayList(base_parts[side]).stream()//
                .map(b -> new IndexedCuboid6(HIT_BASE, b))//
                .collect(Collectors.toList());
        parts.add(new IndexedCuboid6(HIT_INSERT, getInsertBounds()));
        return new SubHitVoxelShape(VoxelShapes.or(basePartsJoined[side], insertShape), parts);
    }

    @Override
    public void onNeighborBlockChanged(BlockPos from) {
        if (!dropIfCantStay()) {
            sendFlagsUpdate();
        }
    }

    @Override
    public void tick() {
        b_insertpos = a_insertpos;
        a_insertpos = MathHelper.approachExp(a_insertpos, a_eject ? 1 : 0, 0.5, 0.1);
        if (!world().isRemote) {
            b_eject = a_eject;
            a_eject = (redstone && world().isBlockPowered(pos())) != invert_redstone;
            if (a_eject != b_eject) {
                markUpdate();
            }
        }
    }

    @Override
    public ActionResultType activate(PlayerEntity player, PartRayTraceResult hit, ItemStack held, Hand hand) {
        if (world().isRemote) {
            return ActionResultType.SUCCESS;
        }
        if (held.isEmpty() && player.isCrouching()) {
            stripModifiers();
            markUpdate();
        } else if (held.isEmpty()) {
            if (hit.subHit == HIT_INSERT) {
                invert_redstone = !invert_redstone;
            } else {
                openGui(player);
            }
        } else if (held.getItem() == Items.REDSTONE && !redstone) {
            redstone = true;
            if (!player.abilities.isCreativeMode) {
                held.shrink(1);
            }
            if (world().isBlockPowered(pos()) == invert_redstone == a_eject) {
                invert_redstone = !invert_redstone;
            }
            markUpdate();
        } else if (held.getItem() == Items.GLOWSTONE_DUST && !fast) {
            fast = true;
            if (!player.abilities.isCreativeMode) {
                held.shrink(1);
            }
            markUpdate();
        } else {
            openGui(player);
        }
        return ActionResultType.SUCCESS;
    }

    /**
     * Called to open any gui that the translocator wishes to display.
     *
     * @param player The player.
     */
    public void openGui(PlayerEntity player) {

    }

    /**
     * Strips all modifiers from the Translocator and drops the items in world.
     */
    public void stripModifiers() {
        if (redstone) {
            redstone = false;
            dropItem(new ItemStack(Items.REDSTONE));

            if (invert_redstone != a_eject) {
                invert_redstone = !invert_redstone;
            }
        }
        if (fast) {
            fast = false;
            dropItem(new ItemStack(Items.GLOWSTONE_DUST));
        }
    }

    @Override
    public Iterable<ItemStack> getDrops() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(getItem());
        if (redstone) {
            stacks.add(new ItemStack(Items.REDSTONE));
        }
        if (fast) {
            stacks.add(new ItemStack(Items.GLOWSTONE_DUST));
        }
        return stacks;
    }

    /**
     * Called to check if the part can't stay anymore.
     * I.E, chest removed, Translocator needs to drop.
     * This method will drop the part in world and notify the tile that it was removed.
     *
     * @return True if the translocator was dropped, false if it wasn't.
     */
    public boolean dropIfCantStay() {
        if (!canStay()) {
            drop();
            return true;
        }
        return false;
    }

    /**
     * Drops the part in world, removes all modifiers.
     * Also notifies the tile to remove this part.
     */
    public void drop() {
        getDrops().forEach(this::dropItem);
        tile().remPart(this);
    }

    @Override
    public ItemStack pickItem(PartRayTraceResult hit) {
        return getItem();
    }

    public abstract ItemStack getItem();

    public abstract int getTType();

    public abstract boolean canStay();

    public TranslocatorPart setupPlacement(PlayerEntity player, int side) {
        this.side = (byte) (side ^ 1);
        return this;
    }

    public void markUpdate() {
        tile().markDirty();
        tile().notifyPartChange(this);
        sendFlagsUpdate();
    }

    /**
     * Use this to check if another part, not yourself can eject.
     * Mainly to bypass ticking issues, as the part state may actually change next tick,
     * But hasn't had the chance to tick in the space yet.
     *
     * @return same as a_eject but with tick look forward.
     */
    @SuppressWarnings ("BooleanMethodIsAlwaysInverted")
    public boolean canEject() {
        if (!world().isRemote) {
            boolean b = (redstone && world().isBlockPowered(pos())) != invert_redstone;
            if (b != a_eject) {
                return b;
            }
        }
        return a_eject;
    }

    public boolean canConnect(int side) {
        TMultiPart other = tile().partMap(side);
        return other instanceof TranslocatorPart && getTType() == ((TranslocatorPart) other).getTType();
    }

    public boolean canInsert(int side) {
        return canConnect(side) && !((TranslocatorPart) tile().partMap(side)).canEject();
    }

    @SuppressWarnings ("unchecked")
    public <T> T getOther(int side) {
        return (T) tile().partMap(side);
    }

    protected void dropItem(ItemStack stack) {
        TileMultipart.dropItem(stack, world(), Vector3.fromTileCenter(tile()));
    }

    public int getIconIndex() {
        int i = 0;
        if (redstone) {
            i |= world().isBlockPowered(pos()) ? 0x02 : 0x01;
        }
        if (fast) {
            i |= 0x04;
        }
        return i;
    }

    @Override
    public boolean renderStatic(Vector3 pos, RenderType layer, CCRenderState ccrs) {
        if (layer == RenderType.getSolid()) {
            ccrs.reset();
            RenderTranslocator.renderStatic(ccrs, this);
        }
        return false;
    }

    @Override
    public void renderDynamic(MatrixStack mStack, IRenderTypeBuffer buffers, int packedLight, int packedOverlay, float partialTicks) {
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        RenderTranslocator.renderInsert(this, ccrs, mStack, buffers, packedLight, packedOverlay, partialTicks);
        RenderTranslocator.renderLinks(this, ccrs, mStack, buffers);
    }

    @Override
    public boolean drawHighlight(PartRayTraceResult hit, ActiveRenderInfo info, MatrixStack mStack, IRenderTypeBuffer getter, float partialTicks) {
        if (hit.subHit == HIT_INSERT) {
            RenderUtils.bufferHitbox(new Matrix4(mStack).translate(hit.getPos()), getter, info, getInsertBounds());
            return true;
        }
        return false;
    }

    @Override
    public Cuboid6 getRenderBounds() {
        return boxes[side];
    }
}
