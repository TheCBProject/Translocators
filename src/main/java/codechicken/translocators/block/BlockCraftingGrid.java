package codechicken.translocators.block;

import codechicken.lib.raytracer.IndexedVoxelShape;
import codechicken.lib.raytracer.MultiIndexedVoxelShape;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Translation;
import codechicken.translocators.handler.ConfigHandler;
import codechicken.translocators.tile.TileCraftingGrid;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static codechicken.lib.vec.Vector3.CENTER;

public class BlockCraftingGrid extends Block {

    private static final IndexedVoxelShape BASE = new IndexedVoxelShape(new Cuboid6(0, 0, 0, 1, 0.005, 1).shape(), 0);
    private static final IndexedVoxelShape[][] BUTTONS = new IndexedVoxelShape[4][9];
    private static final VoxelShape[] SHAPES = new VoxelShape[4];

    static {
        for (int rot = 0; rot < 4; rot++) {
            for (int b = 0; b < 9; b++) {
                Cuboid6 box = new Cuboid6(1 / 16D, 0, 1 / 16D, 5 / 16D, 0.01, 5 / 16D)
                        .apply(new Translation((b % 3) * 5 / 16D, 0, (b / 3) * 5 / 16D).with(Rotation.quarterRotations[rot].at(CENTER)));
                BUTTONS[rot][b] = new IndexedVoxelShape(box.shape(), b + 1);
            }
            ImmutableSet.Builder<IndexedVoxelShape> builder = ImmutableSet.builder();
            builder.add(BASE);
            builder.add(BUTTONS[rot]);
            SHAPES[rot] = new MultiIndexedVoxelShape(BASE, builder.build());
        }
    }

    public BlockCraftingGrid() {
        super(Block.Properties.of(Material.WOOD).noOcclusion());
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
        VoxelShape shape = BASE;
        TileEntity t = worldIn.getBlockEntity(pos);
        if (t instanceof TileCraftingGrid) {
            TileCraftingGrid tile = (TileCraftingGrid) t;
            shape = SHAPES[tile.rotation];
        }
        return shape;
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

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public void playerDestroy(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.playerDestroy(worldIn, player, pos, state, te, stack);
        worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> stacks = new ArrayList<>();
        TileCraftingGrid tcraft = (TileCraftingGrid) builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
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
        if (ConfigHandler.disableCraftingGrid || side != Direction.UP) {
            return false;
        }

        BlockState state = world.getBlockState(pos);

        BlockRayTraceResult hit = RayTracer.retrace(player);
        BlockItemUseContext ctx = new BlockItemUseContext(player, Hand.MAIN_HAND, ItemStack.EMPTY, hit);

        if (!state.canBeReplaced(ctx)) {
            pos = pos.above();
        }

        if (!world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP)) {
            return false;
        }

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
