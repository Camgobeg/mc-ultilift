package com.ultima.elevators.block;

import com.ultima.elevators.block.DispatchManager;
import com.ultima.elevators.core.FloorLabels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Hall call button (up / down), Schindler style. LIT while the call is queued.
 */
public class CallButtonBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty CALL_UP = BooleanProperty.create("call_up");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public CallButtonBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.SOUTH)
                .setValue(CALL_UP, Boolean.TRUE)
                .setValue(LIT, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CALL_UP, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction front = ctx.getHorizontalDirection().getOpposite();
        Player p = ctx.getPlayer();
        boolean up = p == null || !p.isShiftKeyDown();
        return this.defaultBlockState().setValue(FACING, front).setValue(CALL_UP, up);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            boolean up = state.getValue(CALL_UP);
            boolean queued = DispatchManager.queueHallCall(level, pos, up);
            if (queued) {
                level.setBlock(pos, state.setValue(LIT, Boolean.TRUE), 3);
            } else {
                player.displayClientMessage(Component.translatable("message.ultimaelevators.no_controller"), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static void setLit(Level level, BlockPos pos, boolean lit) {
        BlockState st = level.getBlockState(pos);
        if (st.getBlock() instanceof CallButtonBlock && st.getValue(LIT) != lit) {
            level.setBlock(pos, st.setValue(LIT, lit), 3);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        Direction f = state.getValue(FACING);
        return switch (f) {
            case NORTH -> Block.box(2, 2, 0, 14, 14, 3);
            case SOUTH -> Block.box(2, 2, 13, 14, 14, 16);
            case WEST -> Block.box(0, 2, 2, 3, 14, 14);
            case EAST -> Block.box(13, 2, 2, 16, 14, 14);
            default -> Block.box(2, 2, 0, 14, 14, 3);
        };
    }
}
