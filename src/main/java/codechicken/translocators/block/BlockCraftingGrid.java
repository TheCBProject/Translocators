package codechicken.translocators.block;

import codechicken.translocators.handler.ConfigHandler;
import codechicken.translocators.tile.TileCraftingGrid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockCraftingGrid extends Block {

    public BlockCraftingGrid() {
        super(Block.Properties.create(Material.WOOD));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileCraftingGrid();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    //    @Override
    //    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
    //        ICuboidProvider provider = (ICuboidProvider) source.getTileEntity(pos);
    //        if (provider != null && !provider.getIndexedCuboids().isEmpty()) {
    //            return provider.getIndexedCuboids().get(0).aabb();
    //        }
    //        return super.getBoundingBox(state, source, pos);
    //    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.removeBlock(pos, false);
    }

    //    @Override
    //    public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    //        ArrayList<ItemStack> stacks = new ArrayList<>();
    //        TileCraftingGrid tcraft = (TileCraftingGrid) world.getTileEntity(pos);
    //        if (tcraft != null) {
    //            for (ItemStack item : tcraft.items) {
    //                if (item != null) {
    //                    stacks.add(item.copy());
    //                }
    //            }
    //        }
    //
    //        return stacks;
    //    }

    //    @Override
    //    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
    //        ICuboidProvider provider = (ICuboidProvider) worldIn.getTileEntity(pos);
    //        List<IndexedCuboid6> cuboids = provider.getIndexedCuboids();
    //        return RayTracer.rayTraceCuboidsClosest(start, end, pos, cuboids);
    //    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return ActionResultType.PASS;
        }

        TileCraftingGrid tcraft = (TileCraftingGrid) world.getTileEntity(pos);

        if (hit != null) {
            if (hit.subHit > 0) {
                tcraft.activate(hit.subHit - 1, player);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    public boolean placeBlock(World world, PlayerEntity player, BlockPos pos, Direction side) {
        if (ConfigHandler.disableCraftingGrid) {
            return false;
        }

        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        //        if (side != EnumFacing.UP && block != Blocks.SNOW_LAYER) {
        //            return false;
        //        }
        //
        //        if (block != Blocks.VINE && block != Blocks.TALLGRASS && block != Blocks.DEADBUSH && !block.isReplaceable(world, pos)) {
        //            pos = pos.offset(EnumFacing.UP);
        //        }
        //
        //        if (!world.isSideSolid(pos.offset(EnumFacing.DOWN), EnumFacing.UP)) {
        //            return false;
        //        }
        //
        //        if (!world.mayPlace(this, pos, false, EnumFacing.UP, null)) {
        //            return false;
        //        }

        player.swingArm(Hand.MAIN_HAND);
        if (!world.setBlockState(pos, getDefaultState())) {
            return false;
        }

        onBlockPlacedBy(world, pos, getDefaultState(), player, null);
        return true;
    }

    //    ThreadLocal<BlockPos> replaceCheck = new ThreadLocal<>();
    //
    //    @Override
    //    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
    //        replaceCheck.set(useContext.getPos());
    //        return true;
    //    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
        BlockPos beneath = pos.down();
        //        if (!world.isSideSolid(beneath, EnumFacing.UP, false)) {
        //            dropBlockAsItem(world, pos, state, 0);
        //            world.setBlockToAir(pos);
        //        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        ((TileCraftingGrid) world.getTileEntity(pos)).onPlaced(placer);
    }

    //    @Override
    //    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    //        return null;
    //    }

    /*@Override//TODO Wot..
    public void onBlockPreDestroy(World world, BlockPos pos, int meta) {
        BlockPos c = replaceCheck.get();
        if (!world.isRemote && c != null && c.equals(new BlockCoord(x, y, z))) {
            dropBlockAsItem(world, x, y, z, meta, 0);
        }
        replaceCheck.set(null);
    }*/
}
