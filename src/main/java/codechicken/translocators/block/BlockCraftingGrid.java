package codechicken.translocators.block;

import codechicken.lib.raytracer.ICuboidProvider;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import codechicken.translocators.handler.ConfigHandler;
import codechicken.translocators.tile.TileCraftingGrid;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockCraftingGrid extends Block {

    public BlockCraftingGrid() {
        super(Material.WOOD);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileCraftingGrid();
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        ICuboidProvider provider = (ICuboidProvider) source.getTileEntity(pos);
        if (provider != null && !provider.getIndexedCuboids().isEmpty()) {
            return provider.getIndexedCuboids().get(0).aabb();
        }
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.setBlockToAir(pos);
    }

    @Override
    public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        TileCraftingGrid tcraft = (TileCraftingGrid) world.getTileEntity(pos);
        if (tcraft != null) {
            for (ItemStack item : tcraft.items) {
                if (item != null) {
                    stacks.add(item.copy());
                }
            }
        }

        return stacks;
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        ICuboidProvider provider = (ICuboidProvider) worldIn.getTileEntity(pos);
        List<IndexedCuboid6> cuboids = provider.getIndexedCuboids();
        return RayTracer.rayTraceCuboidsClosest(start, end, pos, cuboids);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        RayTraceResult hit = RayTracer.retraceBlock(world, player, pos);
        TileCraftingGrid tcraft = (TileCraftingGrid) world.getTileEntity(pos);

        if (hit != null) {
            if (hit.subHit > 0) {
                tcraft.activate(hit.subHit - 1, player);
            }
            return true;
        }
        return false;
    }

    public boolean placeBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing side) {
        if (ConfigHandler.disableCraftingGrid) {
            return false;
        }

        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (side != EnumFacing.UP && block != Blocks.SNOW_LAYER) {
            return false;
        }

        if (block != Blocks.VINE && block != Blocks.TALLGRASS && block != Blocks.DEADBUSH && !block.isReplaceable(world, pos)) {
            pos = pos.offset(EnumFacing.UP);
        }

        if (!world.isSideSolid(pos.offset(EnumFacing.DOWN), EnumFacing.UP)) {
            return false;
        }

        if (!world.mayPlace(this, pos, false, EnumFacing.UP, null)) {
            return false;
        }

        player.swingArm(EnumHand.MAIN_HAND);
        if (!world.setBlockState(pos, getDefaultState())) {
            return false;
        }

        onBlockPlacedBy(world, pos, getDefaultState(), player, null);
        return true;
    }

    ThreadLocal<BlockPos> replaceCheck = new ThreadLocal<>();

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        replaceCheck.set(pos);
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        BlockPos beneath = pos.down();
        if (!world.isSideSolid(beneath, EnumFacing.UP, false)) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack item) {
        ((TileCraftingGrid) world.getTileEntity(pos)).onPlaced(entity);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null;
    }

    /*@Override//TODO Wot..
    public void onBlockPreDestroy(World world, BlockPos pos, int meta) {
        BlockPos c = replaceCheck.get();
        if (!world.isRemote && c != null && c.equals(new BlockCoord(x, y, z))) {
            dropBlockAsItem(world, x, y, z, meta, 0);
        }
        replaceCheck.set(null);
    }*/

    /*@Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        super.registerBlockIcons(register);
        gridIcon = register.registerIcon("translocator:craftingGrid");
    }*/
}
