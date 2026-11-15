package com.ultima.elevators.block.entity;

import com.ultima.elevators.block.BufferBlock;
import com.ultima.elevators.block.CounterweightRailBlock;
import com.ultima.elevators.block.DispatchManager;
import com.ultima.elevators.block.ElevatorDoorBlock;
import com.ultima.elevators.block.MrlDriveBlock;
import com.ultima.elevators.block.ShaftBlock;
import com.ultima.elevators.block.ShaftHeadBlock;
import com.ultima.elevators.block.TractionMachineBlock;
import com.ultima.elevators.core.CabStyle;
import com.ultima.elevators.core.ElevatorBrand;
import com.ultima.elevators.core.MachineMode;
import com.ultima.elevators.entity.ElevatorCarEntity;
import com.ultima.elevators.registry.ModBlockEntities;
import com.ultima.elevators.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

/**
 * The brain of an elevator: validates the shaft column (counterweight rail, buffers, machine head,
 * landing doors), detects MR / MRL mode, stores brand / style / speed contract and commands the cab.
 */
public class ControllerBlockEntity extends BlockEntity {
    public static final float[] SPEEDS = {0.5F, 1.0F, 1.6F, 2.5F, 4.0F, 6.0F};
    public static final int[] DWELL_TICKS = {20, 30, 40, 60, 80, 100, 120};

    private ElevatorBrand brand = ElevatorBrand.SCHINDLER;
    private CabStyle style = CabStyle.STAINLESS;
    private int speedIndex = 1;
    private int dwellIndex = 2;
    private String group = "";

    private MachineMode mode = MachineMode.MRL;
    private List<Integer> floorYs = new ArrayList<>();
    private int columnBottomY = 0;
    private int columnTopY = 0;
    private String statusKey = "status.ultimaelevators.building";

    private UUID cabId = null;
    private String carName = "";
    private final TreeSet<Integer> hallUp = new TreeSet<>();
    private final TreeSet<Integer> hallDown = new TreeSet<>();
    private final List<BlockPos> litButtons = new ArrayList<>();

    private boolean dirty = true;
    private int tickCounter = 0;

    public ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTROLLER.get(), pos, state);
    }

    // ---------------------------------------------------------------- registry

    @Override
    public void onLoad() {
        super.onLoad();
        Level lvl = this.level;
        if (lvl == null || lvl.isClientSide) return;
        DispatchManager.register(this);
        assignCarName();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        DispatchManager.unregister(this);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        DispatchManager.unregister(this);
    }

    private void assignCarName() {
        if (!carName.isEmpty()) return;
        if (this.level == null) {
            carName = "A";
            return;
        }
        int n = DispatchManager.controllersOf(this.level).size();
        char c = (char) ('A' + Math.min(n, 25));
        carName = String.valueOf(c);
    }

    // ---------------------------------------------------------------- config

    public ElevatorBrand getBrand() { return brand; }
    public CabStyle getStyle() { return style; }
    public MachineMode getMode() { return mode; }
    public String getGroup() { return group; }
    public String getStatusKey() { return statusKey; }
    public List<Integer> getFloorYs() { return Collections.unmodifiableList(floorYs); }
    public int getFloorCount() { return floorYs.size(); }
    public int getColumnTopY() { return columnTopY; }
    public int getSpeedIndex() { return speedIndex; }
    public int getDwellIndex() { return dwellIndex; }
    public float getContractSpeed() { return SPEEDS[Mth.clamp(speedIndex, 0, SPEEDS.length - 1)]; }
    public int getDwellTicks() { return DWELL_TICKS[Mth.clamp(dwellIndex, 0, DWELL_TICKS.length - 1)]; }
    public String getCarName() { return carName; }

    public void cycleBrand(int dir) {
        brand = dir >= 0 ? brand.next() : brand.prev();
        pushToCabConfig();
        setChanged();
    }

    public void cycleStyle(int dir) {
        style = dir >= 0 ? style.next() : style.prev();
        pushToCabConfig();
        setChanged();
    }

    public void cycleSpeed(int dir) {
        int maxIdx = 0;
        for (int i = 0; i < SPEEDS.length; i++) if (SPEEDS[i] <= mode.maxSpeed) maxIdx = i;
        speedIndex = Mth.clamp(speedIndex + dir, 0, maxIdx);
        pushToCabConfig();
        setChanged();
    }

    public void cycleDwell(int dir) {
        dwellIndex = Mth.clamp(dwellIndex + dir, 0, DWELL_TICKS.length - 1);
        pushToCabConfig();
        setChanged();
    }

    public void setGroup(String g) {
        this.group = g == null ? "" : g.trim();
        setChanged();
    }

    public void markDirty() {
        this.dirty = true;
    }

    // ---------------------------------------------------------------- calls / dispatch

    public void queueHallCall(int floorIdx, boolean up) {
        if (floorIdx < 0 || floorIdx >= floorYs.size()) return;
        if (up) hallUp.add(floorIdx); else hallDown.add(floorIdx);
        lightButtons(floorIdx, up);
        ElevatorCarEntity cab = getCab();
        if (cab != null) cab.addHallStop(floorIdx, up);
    }

    /** Kiosk destination dispatch: assign a pickup+destination to this controller's car. */
    public boolean addPickup(UUID playerId, int pickupFloor, int destFloor) {
        ElevatorCarEntity cab = getCab();
        if (cab == null) return false;
        cab.addPickup(playerId, pickupFloor, destFloor);
        return true;
    }

    public void onFloorServed(int floorIdx) {
        hallUp.remove(floorIdx);
        hallDown.remove(floorIdx);
        clearButtonsAt(floorIdx);
    }

    private void lightButtons(int floorIdx, boolean up) {
        if (level == null || floorIdx < 0 || floorIdx >= floorYs.size()) return;
        int fy = floorYs.get(floorIdx);
        BlockPos anchor = getAnchorPos();
        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos p = new BlockPos(anchor.getX() + dx, fy + dy, anchor.getZ() + dz);
                    BlockState st = level.getBlockState(p);
                    if (st.getBlock() instanceof com.ultima.elevators.block.CallButtonBlock b) {
                        if (st.getValue(com.ultima.elevators.block.CallButtonBlock.CALL_UP) == up) {
                            com.ultima.elevators.block.CallButtonBlock.setLit(level, p, true);
                            if (!litButtons.contains(p)) litButtons.add(p);
                        }
                    }
                }
            }
        }
    }

    private void clearButtonsAt(int floorIdx) {
        if (level == null) return;
        for (BlockPos p : new ArrayList<>(litButtons)) {
            if (Math.abs(p.getY() - floorYFor(floorIdx)) <= 2) {
                com.ultima.elevators.block.CallButtonBlock.setLit(level, p, false);
                litButtons.remove(p);
            }
        }
    }

    private int floorYFor(int idx) {
        return idx >= 0 && idx < floorYs.size() ? floorYs.get(idx) : Integer.MIN_VALUE;
    }

    // ---------------------------------------------------------------- geometry queries

    public BlockPos getAnchorPos() {
        // Bottom-most shaft block of the validated column (stored via columnBottomY).
        BlockPos side = this.getBlockPos().relative(this.getBlockState().getValue(
                com.ultima.elevators.block.ElevatorControllerBlock.FACING));
        return new BlockPos(side.getX(), columnBottomY, side.getZ());
    }

    public boolean columnContains(BlockPos p) {
        BlockPos side = this.getBlockPos().relative(this.getBlockState().getValue(
                com.ultima.elevators.block.ElevatorControllerBlock.FACING));
        return p.getX() == side.getX() && p.getZ() == side.getZ()
                && p.getY() >= columnBottomY && p.getY() <= columnTopY;
    }

    /** Nearest floor index for a world Y, or -1 if farther than 2 blocks. */
    public int floorIndexForY(int y) {
        int best = -1;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < floorYs.size(); i++) {
            int d = Math.abs(floorYs.get(i) - y);
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return bestDist <= 2 ? best : -1;
    }

    // ---------------------------------------------------------------- validation

    private boolean isShaftFamily(Level lvl, BlockPos p) {
        return lvl.getBlockState(p).getBlock() instanceof ShaftBlock;
    }

    public void validate() {
        Level lvl = this.level;
        if (lvl == null) return;
        BlockPos side = this.getBlockPos().relative(this.getBlockState().getValue(
                com.ultima.elevators.block.ElevatorControllerBlock.FACING));

        if (!(lvl.getBlockState(side).getBlock() instanceof ShaftBlock)) {
            fail("no_shaft", 0, 0, List.of());
            return;
        }

        int bottom = side.getY();
        while (isShaftFamily(lvl, new BlockPos(side.getX(), bottom - 1, side.getZ()))) bottom--;
        int top = side.getY();
        while (isShaftFamily(lvl, new BlockPos(side.getX(), top + 1, side.getZ()))) top++;

        int height = top - bottom + 1;
        if (height < 5) {
            fail("shaft_too_short", bottom, top, List.of());
            return;
        }

        // Counterweight rail mandatory at the very bottom (karşi agirlik rayi).
        BlockState bottomState = lvl.getBlockState(new BlockPos(side.getX(), bottom, side.getZ()));
        if (!(bottomState.getBlock() instanceof CounterweightRailBlock)) {
            fail("no_counterweight", bottom, top, List.of());
            return;
        }

        // Buffers (kuyu yaylari) mandatory directly above the counterweight rail.
        int y = bottom + 1;
        boolean oil = false;
        int buffers = 0;
        while (y <= top) {
            BlockState bs = lvl.getBlockState(new BlockPos(side.getX(), y, side.getZ()));
            if (bs.getBlock() instanceof BufferBlock buf) {
                oil |= buf.isOil();
                buffers++;
                y++;
            } else break;
        }
        if (buffers == 0) {
            fail("no_buffer", bottom, top, List.of());
            return;
        }

        // Machine head at the top of the shaft.
        BlockState headState = lvl.getBlockState(new BlockPos(side.getX(), top, side.getZ()));
        MachineMode newMode;
        if (headState.getBlock() instanceof TractionMachineBlock) {
            boolean geared = headState.getValue(TractionMachineBlock.GEARED);
            newMode = geared ? MachineMode.MR_GEARED : MachineMode.MR_GEARLESS;
            if (newMode == MachineMode.MR_GEARLESS && !oil) {
                fail("oil_buffer_required", bottom, top, List.of());
                return;
            }
            // Machine room must have free space above the machine.
            if (!lvl.getBlockState(new BlockPos(side.getX(), top + 1, side.getZ())).isAir()) {
                fail("no_machine_room", bottom, top, List.of());
                return;
            }
        } else if (headState.getBlock() instanceof MrlDriveBlock) {
            newMode = MachineMode.MRL;
            if (oil && newMode.maxSpeed < SPEEDS[2]) {
                // Oil buffer on MRL is allowed but pointless; no failure.
            }
        } else {
            fail("no_machine", bottom, top, List.of());
            return;
        }

        this.mode = newMode;

        // Landing doors define floors.
        List<Integer> floors = new ArrayList<>();
        for (int fy = bottom + buffers; fy <= top - 1; fy++) {
            if (hasDoorAt(lvl, side.getX(), fy, side.getZ())) floors.add(fy);
        }
        if (floors.size() < 2) {
            fail("no_floors", bottom, top, floors);
            return;
        }

        this.columnBottomY = bottom;
        this.columnTopY = top;
        this.floorYs = floors;
        this.statusKey = "";

        // Clamp speed to machine capability.
        int maxIdx = 0;
        for (int i = 0; i < SPEEDS.length; i++) if (SPEEDS[i] <= mode.maxSpeed) maxIdx = i;
        if (speedIndex > maxIdx) {
            speedIndex = maxIdx;
        }

        ensureCab(lvl);
        // orient cab towards the first detected landing door side
        Direction doorSide = firstDoorSide(lvl, side.getX(), side.getZ());
        if (doorSide != null) {
            ElevatorCarEntity cab = getCab();
            if (cab != null) cab.setFacing(doorSide);
        }
        pushToCabConfig();
        setChanged();
    }

    @Nullable
    private Direction firstDoorSide(Level lvl, int x, int z) {
        for (int fy : this.floorYs) {
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos p = new BlockPos(x, fy, z).relative(d);
                BlockState st = lvl.getBlockState(p);
                if (st.getBlock() instanceof ElevatorDoorBlock) {
                    BlockEntity be = lvl.getBlockEntity(p);
                    if (be instanceof DoorBlockEntity door && door.getShaftPos().equals(new BlockPos(x, fy, z))) {
                        return d;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasDoorAt(Level lvl, int x, int y, int z) {
        BlockPos col = new BlockPos(x, y, z);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos p = col.relative(d);
            BlockState st = lvl.getBlockState(p);
            if (st.getBlock() instanceof ElevatorDoorBlock) {
                BlockEntity be = lvl.getBlockEntity(p);
                if (be instanceof DoorBlockEntity door && door.getShaftPos().equals(col)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void fail(String key, int bottom, int top, List<Integer> floors) {
        this.statusKey = "status.ultimaelevators." + key;
        this.columnBottomY = bottom;
        this.columnTopY = top;
        this.floorYs = new ArrayList<>(floors);
        this.dirty = true;
        setChanged();
    }

    // ---------------------------------------------------------------- cab management

    public void ensureCab(Level lvl) {
        if (!(lvl instanceof ServerLevel sl)) return;
        ElevatorCarEntity cab = getCab();
        if (cab == null || !cab.isAlive()) {
            cab = new ElevatorCarEntity(ModEntities.ELEVATOR_CAR.get(), sl);
            double x = getAnchorPos().getX() + 0.5;
            double z = getAnchorPos().getZ() + 0.5;
            double y = floorYs.get(0);
            cab.setPos(x, y, z);
            cab.setController(this.getBlockPos());
            cab.setCarName(carName);
            sl.addFreshEntity(cab);
            this.cabId = cab.getUUID();
            setChanged();
        }
    }

    public ElevatorCarEntity getCab() {
        if (cabId == null || !(level instanceof ServerLevel sl)) return null;
        Entity e = sl.getEntity(cabId);
        return e instanceof ElevatorCarEntity car ? car : null;
    }

    public void bindCab(ElevatorCarEntity cab) {
        this.cabId = cab.getUUID();
    }

    public void pushToCabConfig() {
        ElevatorCarEntity cab = getCab();
        if (cab != null) {
            cab.applyContract(brand, style, getContractSpeed(), getDwellTicks(),
                    floorYs.stream().mapToInt(Integer::intValue).toArray());
        }
    }

    // ---------------------------------------------------------------- ticking

    @SuppressWarnings("unused")
    public static void serverTick(Level lvl, BlockPos pos, BlockState state, ControllerBlockEntity be) {
        be.tickCounter++;
        if (be.dirty || be.tickCounter % 40 == 0) {
            be.dirty = false;
            be.validate();
        }
    }

    // ---------------------------------------------------------------- nbt

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("brand", brand.ordinal());
        tag.putInt("style", style.ordinal());
        tag.putInt("speed", speedIndex);
        tag.putInt("dwell", dwellIndex);
        tag.putString("group", group);
        tag.putString("status", statusKey);
        tag.putString("carName", carName);
        tag.putInt("mode", mode.ordinal());
        tag.putInt("bottom", columnBottomY);
        tag.putInt("top", columnTopY);
        long[] ys = new long[floorYs.size()];
        for (int i = 0; i < ys.length; i++) ys[i] = floorYs.get(i);
        tag.put("floors", new LongArrayTag(ys));
        if (cabId != null) tag.putUUID("cab", cabId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        brand = ElevatorBrand.byIndex(tag.getInt("brand"));
        style = CabStyle.byIndex(tag.getInt("style"));
        speedIndex = Mth.clamp(tag.getInt("speed"), 0, SPEEDS.length - 1);
        dwellIndex = Mth.clamp(tag.getInt("dwell"), 0, DWELL_TICKS.length - 1);
        group = tag.getString("group");
        statusKey = tag.contains("status") ? tag.getString("status") : "status.ultimaelevators.building";
        carName = tag.getString("carName");
        mode = MachineMode.values()[Mth.clamp(tag.getInt("mode"), 0, MachineMode.values().length - 1)];
        columnBottomY = tag.getInt("bottom");
        columnTopY = tag.getInt("top");
        floorYs = new ArrayList<>();
        if (tag.contains("floors")) {
            long[] ys = tag.getLongArray("floors");
            for (long l : ys) floorYs.add((int) l);
        }
        if (tag.hasUUID("cab")) cabId = tag.getUUID("cab");
        this.dirty = true;
    }

    public Optional<BlockPos> getControllerPos() {
        return Optional.of(this.getBlockPos());
    }

    public Component statusComponent() {
        return Component.translatable(statusKey.isEmpty() ? "status.ultimaelevators.ok" : statusKey);
    }
}
