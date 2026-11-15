package com.ultima.elevators.block;

import com.ultima.elevators.block.entity.DoorBlockEntity;
import com.ultima.elevators.registry.ModBlockEntities;
import com.ultima.elevators.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Landing door (Schindler style center opening). FACING = direction the door faces (hallway side).
 * Panels are animated by DoorBlockEntityRenderer; the upper half is ELEVATOR_DOOR_UPPER.
 */
public class ElevatorDoorBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public ElevatorDoorBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.SOUTH)
                .setValue(OPEN, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Front (hallway side) faces the player.
        Direction front = ctx.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, front).setValue(OPEN, Boolean.FALSE);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (aboveState.isAir()) {
                level.setBlock(above, ModBlocks.ELEVATOR_DOOR_UPPER.get().defaultBlockState()
                        .setValue(ElevatorDoorUpperBlock.FACING, state.getValue(FACING))
                        .setValue(ElevatorDoorUpperBlock.OPEN, state.getValue(OPEN)), 3);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        BlockPos above = pos.above();
        if (level.getBlockState(above).is(ModBlocks.ELEVATOR_DOOR_UPPER.get())) {
            level.removeBlock(above, false);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(OPEN) ? Shapes.empty() : Shapes.block();
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(OPEN) ? Shapes.empty() : Shapes.block();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(OPEN);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DoorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) return null;
        return TickerHelper.createTickerHelper(type, ModBlockEntities.DOOR.get(), DoorBlockEntity::clientTick);
    }

    /** Where the shaft column is, relative to this door. */
    public static BlockPos shaftPosOf(BlockPos doorPos, BlockState doorState) {
        return doorPos.relative(doorState.getValue(FACING).getOpposite());
    }
}
