package com.ultima.elevators.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Machine-room traction machine. GEARED=false -> gearless (high speed, needs oil buffer).
 */
public class TractionMachineBlock extends Block implements ShaftBlock, ShaftHeadBlock {
    public static final BooleanProperty GEARED = BooleanProperty.create("geared");

    public TractionMachineBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(GEARED, Boolean.TRUE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GEARED);
    }
}
