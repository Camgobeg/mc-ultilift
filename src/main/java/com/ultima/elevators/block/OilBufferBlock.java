package com.ultima.elevators.block;

import net.minecraft.world.level.block.Block;

public class OilBufferBlock extends Block implements BufferBlock {
    public OilBufferBlock(Properties props) {
        super(props);
    }

    @Override
    public boolean isOil() {
        return true;
    }
}
