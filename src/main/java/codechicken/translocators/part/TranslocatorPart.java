package codechicken.translocators.part;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.math.MathHelper;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.*;
import codechicken.translocators.client.render.RenderTranslocator;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by covers1624 on 10/11/2017.
 */
public abstract class TranslocatorPart extends TMultiPart implements TCuboidPart, TFacePart, TNormalOcclusionPart, ITickable, TDynamicRenderPart, TFastRenderPart {

    public static Cuboid6 base = new Cuboid6(3 / 16D, 0, 3 / 16D, 13 / 16D, 2 / 16D, 13 / 16D);
    public static Cuboid6[] boxes = new Cuboid6[6];
    public static Cuboid6[][] base_parts = new Cuboid6[6][4];

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

            Transformation rt = Rotation.sideRotations[i].at(Vector3.center);

            boxes[i] = base.copy().apply(rt);
            base_parts[i] = new Cuboid6[] {//
                    new Cuboid6(x1, y1, z1, x1 + d1, y2, z2).apply(rt),//
                    new Cuboid6(x2 - d1, y1, z1, x2, y2, z2).apply(rt),//
                    new Cuboid6(x1 + d1, y1, z1, x2 - d1, y2, z1 + d1).apply(rt),//
                    new Cuboid6(x1 + d1, y1, z2 - d1, x2 - d1, y2, z2).apply(rt)//
            };
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
    public void save(NBTTagCompound tag) {
        tag.setByte("side", side);
        tag.setBoolean("invert_redstone", invert_redstone);
        tag.setBoolean("redstone", redstone);
        tag.setBoolean("fast", fast);
    }

    @Override
    public void load(NBTTagCompound tag) {
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
        packet.writeBoolean(a_eject);
        packet.writeBoolean(redstone);
        packet.writeBoolean(fast);
    }

    @Override
    public void readDesc(MCDataInput packet) {
        side = packet.readByte();
        a_eject = packet.readBoolean();
        redstone = packet.readBoolean();
        fast = packet.readBoolean();
    }
    //endregion

    //region Inc Updating
    //Overriden so we can append a boolean to the write stream.
    @Override
    public void sendDescUpdate() {
        writeDesc(getWriteStream().writeBoolean(true));
    }

    //Overriden so we can split off inc updates and description packets.
    @Override
    public void read(MCDataInput packet) {
        boolean desc = packet.readBoolean();
        if (desc) {
            super.read(packet);
            onPartChanged(this);
        } else {
            readIncUpdate(packet);
        }
    }

    /**
     * Gets the stream to write incremental updates for this part.
     * Batched at the end of the tick, bitbanged to a large blob update packet.
     *
     * @return The stream to write data to.
     */
    public MCDataOutput getIncStream() {
        return getWriteStream().writeBoolean(false);
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
            onNeighborChanged();
        }
        super.onPartChanged(part);
    }

    @Override
    public boolean solid(int side) {
        return false;
    }

    @Override
    public int redstoneConductionMap() {
        return 0;
    }

    @Override
    public int getSlotMask() {
        return 1 << side;
    }

    @Override
    public Cuboid6 getBounds() {
        return boxes[side];
    }

    public Cuboid6 getInsertBounds() {
        return new Cuboid6(6 / 16D, 0, 6 / 16D, 10 / 16D, a_insertpos * 2 / 16D + 1 / 16D, 10 / 16D).apply(Rotation.sideRotations[side].at(Vector3.center));
    }

    @Override
    public Iterable<Cuboid6> getOcclusionBoxes() {
        return Arrays.asList(boxes[side], getInsertBounds());
    }

    @Override
    public Iterable<Cuboid6> getCollisionBoxes() {
        List<Cuboid6> cuboids = Lists.newArrayList(boxes[side]);
        cuboids.add(getInsertBounds().copy());
        return cuboids;
    }

    @Override
    public Iterable<IndexedCuboid6> getSubParts() {
        List<IndexedCuboid6> parts = Lists.newArrayList(base_parts[side]).stream()//
                .map(b -> new IndexedCuboid6(HIT_BASE, b))//
                .collect(Collectors.toList());
        parts.add(new IndexedCuboid6(HIT_INSERT, getInsertBounds()));
        return parts;
    }

    @Override
    public void onNeighborChanged() {
        if (!dropIfCantStay()) {
            sendDescUpdate();
        }
    }

    @Override
    public void update() {
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
    public boolean activate(EntityPlayer player, CuboidRayTraceResult hit, ItemStack held, EnumHand hand) {
        if (world().isRemote) {
            return true;
        }
        if (held.isEmpty() && player.isSneaking()) {
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
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
            }
            if (world().isBlockPowered(pos()) == invert_redstone == a_eject) {
                invert_redstone = !invert_redstone;
            }
            markUpdate();
        } else if (held.getItem() == Items.GLOWSTONE_DUST && !fast) {
            fast = true;
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
            }
            markUpdate();
        } else {
            openGui(player);
        }
        return true;
    }

    /**
     * Called to open any gui that the translocator wishes to display.
     *
     * @param player The player.
     */
    public void openGui(EntityPlayer player) {

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
    public ItemStack pickItem(CuboidRayTraceResult hit) {
        return getItem();
    }

    public abstract ItemStack getItem();

    public abstract int getTType();

    public abstract boolean canStay();

    public TranslocatorPart setupPlacement(EntityPlayer player, int side) {
        this.side = (byte) (side ^ 1);
        return this;
    }

    public void markUpdate() {
        tile().markDirty();
        tile().notifyPartChange(this);
        sendDescUpdate();
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
    @SideOnly (Side.CLIENT)
    public boolean renderStatic(Vector3 pos, BlockRenderLayer layer, CCRenderState ccrs) {
        if (layer == BlockRenderLayer.SOLID) {
            RenderTranslocator.renderStatic(ccrs, this, pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean drawHighlight(EntityPlayer player, CuboidRayTraceResult hit, float frame) {
        if (hit.subHit == HIT_BASE) {
            RenderUtils.renderHitBox(player, boxes[side].copy().add(hit.getBlockPos()), frame);
            return true;
        }
        return false;
    }

    @Override
    public Cuboid6 getRenderBounds() {
        return getBounds();
    }

    @Override
    public void renderDynamic(Vector3 pos, int pass, float frame) {
        RenderTranslocator.renderDynamic(this, pos, frame);
    }

    @Override
    public boolean canRenderDynamic(int pass) {
        return pass == 0 && a_eject;
    }

    @Override
    public void renderFast(CCRenderState ccrs, Vector3 pos, int pass, float frameDelta) {
        RenderTranslocator.renderFast(ccrs, this, pos, frameDelta);
    }

    @Override
    public boolean canRenderFast(int pass) {
        return pass == 0;
    }
}
