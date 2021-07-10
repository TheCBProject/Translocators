package codechicken.translocators.block;

import codechicken.translocators.handler.ConfigHandler;
import codechicken.translocators.tile.TileCraftingGrid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockCraftingGrid extends Block {

    public BlockCraftingGrid() {
        super(Block.Properties.of(Material.WOOD));
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

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos beneath = pos.below();
        return world.getBlockState(beneath).isFaceSturdy(world, beneath, Direction.UP);
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

//    @Override
//    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
//        return willHarvest || super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
//    }
//
//    @Override
//    public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack stack) {
//        super.harvestBlock(world, player, pos, state, te, stack);
//        world.removeBlock(pos, false);
//    }

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
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (world.isClientSide()) {
            return ActionResultType.PASS;
        }

        TileCraftingGrid tcraft = (TileCraftingGrid) world.getBlockEntity(pos);

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
        if (side != Direction.UP && block != Blocks.SNOW) {
            return false;
        }

        //TODO, block.isReplaceable requires ItemStack
//        if (block != Blocks.VINE && block != Blocks.TALL_GRASS && block != Blocks.DEAD_BUSH && !block.isReplaceable(world, pos)) {
//            pos = pos.offset(Direction.UP);
//        }

        if (!world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP)) {
            return false;
        }
//
//        if (!world.mayPlace(this, pos, false, EnumFacing.UP, null)) {
//            return false;
//        }

        player.swing(Hand.MAIN_HAND);
        if (!world.setBlockAndUpdate(pos, defaultBlockState())) {
            return false;
        }

        setPlacedBy(world, pos, defaultBlockState(), player, null);
        return true;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        ((TileCraftingGrid) world.getBlockEntity(pos)).onPlaced(placer);
    }
}
