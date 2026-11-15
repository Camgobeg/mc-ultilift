package com.ultima.elevators.block.entity;

import com.ultima.elevators.block.ElevatorDoorBlock;
import com.ultima.elevators.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Landing door state: shaft anchor + animated open progress for the renderer.
 */
public class DoorBlockEntity extends BlockEntity {
    private BlockPos shaftPos;
    /** 0 = fully closed, 1 = fully open (client animation). */
    private float openProgress = 0.0F;

    public DoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DOOR.get(), pos, state);
        this.shaftPos = pos.relative(state.getValue(ElevatorDoorBlock.FACING).getOpposite());
    }

    public BlockPos getShaftPos() {
        if (shaftPos == null && level != null) {
            shaftPos = getBlockPos().relative(getBlockState().getValue(ElevatorDoorBlock.FACING).getOpposite());
        }
        return shaftPos;
    }

    public float getOpenProgress() {
        return openProgress;
    }

    @SuppressWarnings("unused")
    public static void clientTick(net.minecraft.world.level.Level level, BlockPos pos,
                                  BlockState state, DoorBlockEntity be) {
        boolean open = state.getValue(ElevatorDoorBlock.OPEN);
        float target = open ? 1.0F : 0.0F;
        float speed = 1.0F / 14.0F;
        if (be.openProgress < target) be.openProgress = Math.min(target, be.openProgress + speed);
        else if (be.openProgress > target) be.openProgress = Math.max(target, be.openProgress - speed);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (shaftPos != null) {
            tag.putLong("shaft", shaftPos.asLong());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("shaft")) {
            this.shaftPos = BlockPos.of(tag.getLong("shaft"));
        }
    }
}
