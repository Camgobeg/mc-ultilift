package com.ultima.elevators.block.entity;

import com.ultima.elevators.block.DispatchManager;
import com.ultima.elevators.core.ElevatorBrand;
import com.ultima.elevators.core.FloorLabels;
import com.ultima.elevators.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;

/**
 * KONE Polaris style destination kiosk.
 */
public class KioskBlockEntity extends BlockEntity {
    private String group = "";
    // last assignment shown on screen
    private String assignedCar = "";
    private int assignedPickupFloor = -1;
    private int assignedDestFloor = -1;
    private long expiresAt = 0;

    private int tick = 0;
    private ControllerBlockEntity linkedController = null;

    public KioskBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DESTINATION_KIOSK.get(), pos, state);
    }

    public String getGroup() { return group; }

    public void setGroup(String g) {
        this.group = g == null ? "" : g.trim();
        this.linkedController = null;
        setChanged();
    }

    public int getFloorCount() {
        if (linkedController != null && !linkedController.isRemoved()) {
            return linkedController.getFloorCount();
        }
        return 0;
    }

    public String getAssignedCar() { return assignedCar; }
    public int getAssignedPickupFloor() { return assignedPickupFloor; }
    public int getAssignedDestFloor() { return assignedDestFloor; }

    @SuppressWarnings("unused")
    public static void serverTick(Level level, BlockPos pos, BlockState state, KioskBlockEntity be) {
        be.tick++;
        if (be.tick % 20 != 0) return;

        // resolve controller
        List<ControllerBlockEntity> candidates = DispatchManager.controllersInGroup(level, be.group);
        if (candidates.isEmpty()) {
            be.linkedController = DispatchManager.nearest(level, be.getBlockPos(), 48);
        } else {
            be.linkedController = candidates.get(0);
        }

        if (be.expiresAt != 0 && level.getGameTime() > be.expiresAt) {
            be.assignedCar = "";
            be.assignedPickupFloor = -1;
            be.assignedDestFloor = -1;
            be.expiresAt = 0;
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    /** Player selected destination floor in the GUI. Returns assignment text or null. */
    public String assignDestination(ServerPlayer player, int destFloorIdx) {
        Level level = this.level;
        if (!(level instanceof ServerLevel sl) || linkedController == null || linkedController.isRemoved()) {
            // try resolve now
            List<ControllerBlockEntity> candidates = DispatchManager.controllersInGroup(level, group);
            linkedController = candidates.isEmpty() ? DispatchManager.nearest(level, getBlockPos(), 48) : candidates.get(0);
            if (linkedController == null) return null;
        }
        int pickupFloor = linkedController.floorIndexForY(this.getBlockPos().getY());
        if (pickupFloor < 0) return null;

        UUID playerId = player.getUUID();
        boolean ok = linkedController.addPickup(playerId, pickupFloor, destFloorIdx);
        if (!ok) return null;

        this.assignedCar = linkedController.getCarName();
        this.assignedPickupFloor = pickupFloor;
        this.assignedDestFloor = destFloorIdx;
        this.expiresAt = level.getGameTime() + 2400;
        this.setChanged();
        level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);

        ElevatorBrand brand = linkedController.getBrand();
        player.sendSystemMessage(Component.translatable("message.ultimaelevators.dispatch.assigned",
                brand.label(), Component.literal(assignedCar).withStyle(brand.chatColor),
                Component.literal(FloorLabels.label(pickupFloor))));
        return assignedCar;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("group", group);
        tag.putString("car", assignedCar);
        tag.putInt("pickup", assignedPickupFloor);
        tag.putInt("dest", assignedDestFloor);
        tag.putLong("expires", expiresAt);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        group = tag.getString("group");
        assignedCar = tag.getString("car");
        assignedPickupFloor = tag.getInt("pickup");
        assignedDestFloor = tag.getInt("dest");
        expiresAt = tag.getLong("expires");
        this.linkedController = null;
    }
}
