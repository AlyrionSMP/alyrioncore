package xyz.alyrion.alyrioncore.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;
import xyz.alyrion.alyrioncore.registry.ModBlockEntities;

/**
 * The Oxygen Generator: the heart of every pressurized habitat. The machine holds
 * a charge of Forge Energy (FE) and, while it has power, keeps any sealed room it
 * sits inside breathable. Let the buffer run dry and the habitat depressurizes —
 * you drown unless a new generator (or a fresh charge) comes online.
 *
 * The block is a solid, airtight full cube (so it can be built into walls and
 * roofs) with a custom 3D model: a meteoric-iron chassis, a teal coolant tank on
 * top, a front dial and a recessed vent whose fan is animated by the block entity
 * renderer. While running (energy &gt; 0) it emits light level 8 and the model
 * switches to the "active" variant (lit dial / glowing tank).
 */
public class OxygenGeneratorBlock extends Block implements EntityBlock {

    public static final MapCodec<OxygenGeneratorBlock> CODEC = simpleCodec(OxygenGeneratorBlock::new);

    /** True while the machine has FE stored and is producing oxygen. */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public OxygenGeneratorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    public static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(4.0F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.NETHERITE_BLOCK)
                .noOcclusion()
                .lightLevel(state -> state.getValue(ACTIVE) ? 8 : 0);
    }

    @Override
    public MapCodec<OxygenGeneratorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OxygenGeneratorBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.OXYGEN_GENERATOR.get()) {
            return (lvl, pos, st, be) -> OxygenGeneratorBlockEntity.tick(lvl, pos, st, (OxygenGeneratorBlockEntity) be);
        }
        return null;
    }
}
