package com.ultima.elevators.block;

import net.minecraft.world.level.block.Block;

public class SpringBufferBlock extends Block implements BufferBlock {
    public SpringBufferBlock(Properties props) {
        super(props);
    }

    @Override
    public boolean isOil() {
        return false;
    }
}
