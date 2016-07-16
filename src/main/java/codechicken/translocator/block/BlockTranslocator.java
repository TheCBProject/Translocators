package codechicken.translocator.block;

import codechicken.lib.block.property.PropertyString;
import codechicken.lib.math.MathHelper;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.translocator.tile.TileItemTranslocator;
import codechicken.translocator.tile.TileLiquidTranslocator;
import codechicken.translocator.tile.TileTranslocator;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.common.FMLLog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static codechicken.translocator.reference.VariantReference.translocatorNamesList;

public class BlockTranslocator extends Block {
    private RayTracer rayTracer = new RayTracer();
    private static final PropertyString VARIANTS = new PropertyString("type", translocatorNamesList);

    public BlockTranslocator() {
        super(Material.IRON);
        setHardness(1.5F);
        setResistance(10.0F);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return getMetaFromState(state) < 2;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        switch (getMetaFromState(state)) {
        case 0:
            return new TileItemTranslocator();
        case 1:
            return new TileLiquidTranslocator();
        }

        return null;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    public static boolean canExistOnSide(World world, BlockPos pos, EnumFacing side, int meta) {
        switch (meta) {
        case 0:
            return world.getTileEntity(pos.offset(side)) instanceof IInventory;
        case 1:
            return world.getTileEntity(pos.offset(side)) instanceof IFluidHandler;
        }
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
        TileTranslocator ttrans = (TileTranslocator) world.getTileEntity(pos);
        int meta = getMetaFromState(state);

        for (EnumFacing facing : EnumFacing.VALUES) {
            if (ttrans.attachments[facing.ordinal()] != null && !canExistOnSide(world, pos, facing, meta)) {
                if (ttrans.harvestPart(facing.ordinal(), true)) {
                    break;
                }
            }
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        RayTraceResult hit = RayTracer.retraceBlock(world, player, pos);
        if (hit == null) {
            return false;
        }

        TileTranslocator ttrans = (TileTranslocator) world.getTileEntity(pos);
        return ttrans.harvestPart(hit.subHit % 6, !player.capabilities.isCreativeMode);
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return 0;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        ArrayList<ItemStack> ai = new ArrayList<ItemStack>();
        TileTranslocator ttrans = (TileTranslocator) world.getTileEntity(pos);
        if (ttrans != null) {
            for (TileTranslocator.Attachment a : ttrans.attachments) {
                if (a != null) {
                    ai.addAll(a.getDrops(state));
                }
            }
        }

        return ai;
    }

    //@Override
    //public IIcon getIcon(int par1, int par2) {
    //    return Blocks.obsidian.getIcon(par1, par2);
    //}

    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d start, Vec3d end) {
        return rayTracer.rayTraceCuboids(new Vector3(start), new Vector3(end), getParts(world, pos), new BlockCoord(pos));
    }

    public List<IndexedCuboid6> getParts(World world, BlockPos pos) {
        TileTranslocator tile = (TileTranslocator) world.getTileEntity(pos);
        if (tile == null) {
            return null;
        }

        List<IndexedCuboid6> cuboids = new LinkedList<IndexedCuboid6>();
        tile.addTraceableCuboids(cuboids);
        return cuboids;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
        List<IndexedCuboid6> cuboids = getParts(world, pos);
        for (IndexedCuboid6 cb : cuboids) {
            AxisAlignedBB aabb = cb.aabb();
            if (aabb.intersectsWith(entityBox)) {
                collidingBoxes.add(aabb);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        RayTraceResult hit = RayTracer.retraceBlock(world, player, pos);
        TileTranslocator ttrans = (TileTranslocator) world.getTileEntity(pos);

        if (hit != null) {
            if (hit.subHit < 6) {
                Vector3 vhit = new Vector3(hit.hitVec);
                vhit.add(-pos.getX() - 0.5, -pos.getY() - 0.5, -pos.getZ() - 0.5);
                vhit.apply(Rotation.sideRotations[hit.subHit % 6].inverse());
                if (MathHelper.between(-2 / 16D, vhit.x, 2 / 16D) && MathHelper.between(-2 / 16D, vhit.z, 2 / 16D)) {
                    hit.subHit += 6;
                }
            }

            return ttrans.attachments[hit.subHit % 6].activate(player, hit.subHit / 6);
        }

        return false;
    }

    //@SideOnly(Side.CLIENT)
    //@SubscribeEvent
    //public void onBlockHighlight(DrawBlockHighlightEvent event) {
    //    if (event.target.typeOfHit == MovingObjectType.BLOCK && event.player.worldObj.getBlock(event.target.blockX, event.target.blockY, event.target.blockZ) == this) {
    //        RayTracer.retraceBlock(event.player.worldObj, event.player, event.target.blockX, event.target.blockY, event.target.blockZ);
    //    }
    //}

    @Override
    public void getSubBlocks(Item item, CreativeTabs creativeTab, List<ItemStack> list) {
        list.add(new ItemStack(this, 1, 0));
        list.add(new ItemStack(this, 1, 1));
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileTranslocator ttrans = (TileTranslocator) world.getTileEntity(pos);
        return ttrans.connectRedstone();
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileTranslocator ttrans = (TileTranslocator) world.getTileEntity(pos);
        return ttrans.strongPowerLevel(side);
    }

    //@Override
    //@SideOnly(Side.CLIENT)
    //public void registerBlockIcons(IIconRegister par1IconRegister) {
    //}

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANTS);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return translocatorNamesList.indexOf(String.valueOf(state.getValue(VARIANTS)));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getBlockState().getBaseState().withProperty(VARIANTS, translocatorNamesList.get(meta));
    }
}
