package codechicken.translocators.block;

import codechicken.lib.raytracer.IndexedVoxelShape;
import codechicken.lib.raytracer.MultiIndexedVoxelShape;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.raytracer.SubHitBlockHitResult;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Translation;
import codechicken.translocators.handler.ConfigHandler;
import codechicken.translocators.init.TranslocatorsModContent;
import codechicken.translocators.tile.TileCraftingGrid;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static codechicken.lib.vec.Vector3.CENTER;

public class BlockCraftingGrid extends Block implements EntityBlock {

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
        super(Block.Properties.of().mapColor(MapColor.WOOD).noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileCraftingGrid(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != TranslocatorsModContent.tileCraftingGridType.get()) return null;
        return (level1, pos, state1, t) -> {
            if (!level1.isClientSide) {
                ((TileCraftingGrid) t).tickServer();
            }
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        VoxelShape shape = BASE;
        if (level.getBlockEntity(pos) instanceof TileCraftingGrid tile) {
            shape = SHAPES[tile.rotation];
        }
        return shape;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos beneath = pos.below();
        return world.getBlockState(beneath).isFaceSturdy(world, beneath, Direction.UP);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return willHarvest || super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(worldIn, player, pos, state, te, stack);
        worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> stacks = new ArrayList<>();
        TileCraftingGrid tcraft = (TileCraftingGrid) builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHit) {
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        TileCraftingGrid tcraft = (TileCraftingGrid) level.getBlockEntity(pos);

        if (RayTracer.retrace(player) instanceof SubHitBlockHitResult hit) {
            if (hit.subHit > 0) {
                tcraft.activate(hit.subHit - 1, player);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.FAIL;
    }

    public boolean placeBlock(Level world, Player player, BlockPos pos, Direction side) {
        if (ConfigHandler.disableCraftingGrid || side != Direction.UP) {
            return false;
        }

        BlockState state = world.getBlockState(pos);

        BlockHitResult hit = RayTracer.retrace(player);
        BlockPlaceContext ctx = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, hit);

        if (!state.canBeReplaced(ctx)) {
            pos = pos.above();
        }

        if (!world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP)) {
            return false;
        }

        player.swing(InteractionHand.MAIN_HAND);
        if (!world.setBlockAndUpdate(pos, defaultBlockState())) {
            return false;
        }

        setPlacedBy(world, pos, defaultBlockState(), player, null);
        return true;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        return !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        ((TileCraftingGrid) world.getBlockEntity(pos)).onPlaced(placer);
    }
}
