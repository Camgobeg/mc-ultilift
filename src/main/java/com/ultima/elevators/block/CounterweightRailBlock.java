package com.ultima.elevators.block;

import net.minecraft.world.level.block.Block;

/**
 * Mandatory counterweight rail at the very bottom of every shaft column.
 * Without this block (the counterweight assembly) the controller refuses to run.
 */
public class CounterweightRailBlock extends Block implements ShaftBlock {
    public CounterweightRailBlock(Properties props) {
        super(props);
    }
}
