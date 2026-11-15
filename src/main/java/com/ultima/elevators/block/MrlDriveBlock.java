package com.ultima.elevators.block;

import net.minecraft.world.level.block.Block;

/** Slim machine-room-less drive mounted at the top of the shaft. */
public class MrlDriveBlock extends Block implements ShaftBlock, ShaftHeadBlock {
    public MrlDriveBlock(Properties props) {
        super(props);
    }
}
