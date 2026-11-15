package com.ultima.elevators.block.entity;

import com.ultima.elevators.block.DispatchManager;
import com.ultima.elevators.block.entity.ControllerBlockEntity;
import com.ultima.elevators.entity.ElevatorCarEntity;
import com.ultima.elevators.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Landing indicator: shows current floor number and travel direction of the car in the shaft behind it.
 */
public class FloorIndicatorBlockEntity extends BlockEntity {
    private int floorIndex = -1;
    private int dir = 0; // 0 none, 1 up, -1 down
    private int brandIndex = 0;

    private int tick = 0;

    public FloorIndicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLOOR_INDICATOR.get(), pos, state);
    }

    public int getFloorIndex() { return floorIndex; }
    public int getDir() { return dir; }
    public int getBrandIndex() { return brandIndex; }

    @SuppressWarnings("unused")
    public static void serverTick(Level level, BlockPos pos, BlockState state, FloorIndicatorBlockEntity be) {
        be.tick++;
        if (be.tick % 5 != 0) return;

        BlockPos shaft = pos.relative(state.getValue(com.ultima.elevators.block.FloorIndicatorBlock.FACING).getOpposite());
        ControllerBlockEntity controller = null;
        for (ControllerBlockEntity c : DispatchManager.controllersOf(level)) {
            if (c.columnContains(shaft)) {
                controller = c;
                break;
            }
        }
        int newFloor = -1;
        int newDir = 0;
        int newBrand = 0;
        if (controller != null) {
            newBrand = controller.getBrand().ordinal();
            ElevatorCarEntity cab = controller.getCab();
            if (cab != null) {
                newFloor = cab.currentFloorIndex();
                newDir = cab.motionDir();
            }
        }
        if (newFloor != be.floorIndex || newDir != be.dir || newBrand != be.brandIndex) {
            be.floorIndex = newFloor;
            be.dir = newDir;
            be.brandIndex = newBrand;
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("floor", floorIndex);
        tag.putInt("dir", dir);
        tag.putInt("brand", brandIndex);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        floorIndex = tag.getInt("floor");
        dir = tag.getInt("dir");
        brandIndex = tag.getInt("brand");
    }
}
