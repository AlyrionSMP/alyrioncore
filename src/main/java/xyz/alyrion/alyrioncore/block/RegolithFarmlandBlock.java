package xyz.alyrion.alyrioncore.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import xyz.alyrion.alyrioncore.registry.ModBlocks;

import javax.annotation.Nullable;

public class RegolithFarmlandBlock extends Block {

    public static final MapCodec<RegolithFarmlandBlock> CODEC = simpleCodec(RegolithFarmlandBlock::new);
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    public static final int MAX_MOISTURE = 7;

    public RegolithFarmlandBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, 0));
    }

    @Override
    public MapCodec<RegolithFarmlandBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.UP && !state.canSurvive(level, currentPos)) {
            level.scheduleTick(currentPos, this, 1);
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return !above.isSolid() || above.getBlock() instanceof FenceGateBlock || above.getBlock() instanceof MovingPistonBlock;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return !this.defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())
                ? ModBlocks.MARTIAN_REGOLITH.get().defaultBlockState()
                : super.getStateForPlacement(context);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            turnToRegolith(null, state, level, pos);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (!isNearWater(level, pos)) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            } else if (!shouldMaintainFarmland(level, pos)) {
                turnToRegolith(null, state, level, pos);
            }
        } else if (moisture < MAX_MOISTURE) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!level.isClientSide
                && level.random.nextFloat() < fallDistance - 0.5F
                && entity instanceof LivingEntity
                && (entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
                && entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512F) {
            turnToRegolith(entity, state, level, pos);
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    public static void turnToRegolith(@Nullable Entity entity, BlockState state, Level level, BlockPos pos) {
        BlockState regolithState = pushEntitiesUp(state, ModBlocks.MARTIAN_REGOLITH.get().defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, regolithState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, regolithState));
    }

    private static boolean shouldMaintainFarmland(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.above()).getBlock() instanceof MartianPotatoCropBlock;
    }

    private static boolean isNearWater(LevelReader level, BlockPos pos) {
        for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (level.getFluidState(blockpos).is(FluidTags.WATER)) {
                return true;
            }
        }
        return net.neoforged.neoforge.common.FarmlandWaterManager.hasBlockWaterTicket(level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
