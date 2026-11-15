package com.ultima.elevators.entity;

import com.ultima.elevators.block.entity.ControllerBlockEntity;
import com.ultima.elevators.core.CabStyle;
import com.ultima.elevators.core.ElevatorBrand;
import com.ultima.elevators.core.FloorLabels;
import com.ultima.elevators.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

/**
 * The moving elevator cab entity. Travels inside the validated shaft column, animates doors,
 * boards passengers by walking (auto-step onto the slab), carries them with a light-curtain
 * safety, and executes SCAN dispatch with KONE Polaris pickups.
 */
public class ElevatorCarEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_DOOR_PROGRESS =
            SynchedEntityData.defineId(ElevatorCarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_DOORS_OPEN =
            SynchedEntityData.defineId(ElevatorCarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_MOTION_DIR =
            SynchedEntityData.defineId(ElevatorCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BRAND =
            SynchedEntityData.defineId(ElevatorCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STYLE =
            SynchedEntityData.defineId(ElevatorCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CURRENT_FLOOR =
            SynchedEntityData.defineId(ElevatorCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLOOR_COUNT =
            SynchedEntityData.defineId(ElevatorCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_CAR_NAME =
            SynchedEntityData.defineId(ElevatorCarEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_FACING =
            SynchedEntityData.defineId(ElevatorCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SHAFT_BOTTOM =
            SynchedEntityData.defineId(ElevatorCarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SHAFT_TOP =
            SynchedEntityData.defineId(ElevatorCarEntity.class, EntityDataSerializers.INT);

    private enum DoorState { CLOSED, OPENING, OPEN, CLOSING }

    public record Pickup(int pickupFloor, int destFloor, long createdAt) {}

    private static final float ACCEL = 0.004F;        // blocks per tick^2
    private static final float DOOR_SPEED = 1.0F / 14.0F;

    private BlockPos controllerPos;
    private final Map<UUID, Integer> playerTargets = new HashMap<>();
    private final Map<UUID, Pickup> pickups = new HashMap<>();
    private final TreeSet<Integer> hallUp = new TreeSet<>();
    private final TreeSet<Integer> hallDown = new TreeSet<>();
    /** aboard passengers: uuid -> {dx, dy, dz} offsets relative to cab position */
    private final Map<UUID, double[]> aboard = new HashMap<>();

    private int[] floors = new int[0];
    private ElevatorBrand brand = ElevatorBrand.SCHINDLER;
    private CabStyle style = CabStyle.STAINLESS;
    private float contractSpeed = 1.0F; // blocks per second
    private int dwellTicks = 40;

    private double vy = 0.0; // blocks per tick
    private int motionDir = 0;
    private int targetFloor = -1;
    private int dockedFloor = -1;

    private DoorState doorState = DoorState.CLOSED;
    private boolean doorHold = false;
    private int doorTimer = 0;
    private int lightCurtainCooldown = 0;
    private boolean landingDoorsOpen = false;
    private int landingDoorsSyncFloor = -1;
    private int missingControllerTicks = 0;
    private int doorSoundCooldown = 0;

    public ElevatorCarEntity(EntityType<? extends ElevatorCarEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = false;
        this.setInvulnerable(true);
    }

    // ---------------------------------------------------------------- sync data

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_DOOR_PROGRESS, 0.0F);
        this.entityData.define(DATA_DOORS_OPEN, false);
        this.entityData.define(DATA_MOTION_DIR, 0);
        this.entityData.define(DATA_BRAND, 0);
        this.entityData.define(DATA_STYLE, 0);
        this.entityData.define(DATA_CURRENT_FLOOR, -1);
        this.entityData.define(DATA_FLOOR_COUNT, 0);
        this.entityData.define(DATA_CAR_NAME, "A");
        this.entityData.define(DATA_FACING, 2);
        this.entityData.define(DATA_SHAFT_BOTTOM, 0);
        this.entityData.define(DATA_SHAFT_TOP, 0);
    }

    public float doorProgress() { return this.entityData.get(DATA_DOOR_PROGRESS); }
    public boolean doorOpenTarget() { return this.entityData.get(DATA_DOORS_OPEN); }
    public int motionDir() { return this.entityData.get(DATA_MOTION_DIR); }
    public ElevatorBrand getBrand() { return ElevatorBrand.byIndex(this.entityData.get(DATA_BRAND)); }
    public CabStyle getStyle() { return CabStyle.byIndex(this.entityData.get(DATA_STYLE)); }
    public int currentFloorIndex() { return this.entityData.get(DATA_CURRENT_FLOOR); }
    public int floorCount() { return this.entityData.get(DATA_FLOOR_COUNT); }
    public String getCarName() { return this.entityData.get(DATA_CAR_NAME); }

    public void setCarName(String name) {
        this.entityData.set(DATA_CAR_NAME, name == null || name.isEmpty() ? "A" : name);
    }

    public int facingIndex() { return this.entityData.get(DATA_FACING); }

    public void setFacing(Direction dir) {
        this.entityData.set(DATA_FACING, dir.get2DDataValue());
    }

    public double shaftBottom() { return this.entityData.get(DATA_SHAFT_BOTTOM); }
    public double shaftTop() { return this.entityData.get(DATA_SHAFT_TOP); }

    // ---------------------------------------------------------------- contract

    public void applyContract(ElevatorBrand brand, CabStyle style, float speedBps, int dwellTicks, int[] floors) {
        this.brand = brand;
        this.style = style;
        this.contractSpeed = Math.max(0.2F, speedBps);
        this.dwellTicks = Math.max(20, dwellTicks);
        if (floors != null && floors.length >= 2) {
            this.floors = floors.clone();
        }
        this.entityData.set(DATA_BRAND, brand.ordinal());
        this.entityData.set(DATA_STYLE, style.ordinal());
        this.entityData.set(DATA_FLOOR_COUNT, this.floors.length);
        if (this.floors.length >= 2) {
            this.entityData.set(DATA_SHAFT_BOTTOM, this.floors[0]);
            this.entityData.set(DATA_SHAFT_TOP, this.floors[this.floors.length - 1]);
        }
    }

    public void setController(BlockPos pos) {
        this.controllerPos = pos;
    }

    private ControllerBlockEntity controller() {
        if (controllerPos == null || !(level() instanceof ServerLevel sl)) return null;
        if (!sl.hasChunkAt(controllerPos)) {
            missingControllerTicks = 0;
            return null;
        }
        if (!(sl.getBlockEntity(controllerPos) instanceof ControllerBlockEntity c)) {
            return null;
        }
        c.bindCab(this);
        return c;
    }

    // ---------------------------------------------------------------- dispatch inputs

    public void addHallStop(int floorIdx, boolean up) {
        if (up) hallUp.add(floorIdx); else hallDown.add(floorIdx);
    }

    public void addPickup(UUID playerId, int pickupFloor, int destFloor) {
        if (pickupFloor != destFloor) {
            pickups.put(playerId, new Pickup(pickupFloor, destFloor, level().getGameTime()));
        }
    }

    public void setPlayerDestination(UUID playerId, int floorIdx) {
        if (floorIdx < 0 || floorIdx >= floors.length) return;
        playerTargets.put(playerId, floorIdx);
    }

    public void holdDoorsOpen() {
        if (doorState == DoorState.OPEN || doorState == DoorState.OPENING) {
            doorHold = true;
        }
    }

    public void closeDoorsNow() {
        if (doorState == DoorState.OPEN) {
            doorHold = false;
            doorTimer = 0;
        }
    }

    public boolean isAboard(Player player) {
        return aboard.containsKey(player.getUUID());
    }

    // ---------------------------------------------------------------- helpers

    public int nearestFloorIndex() {
        if (floors.length == 0) return -1;
        int y = Mth.floor(this.getY() + 0.5);
        int best = 0;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < floors.length; i++) {
            int d = Math.abs(floors[i] - y);
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return best;
    }

    private int columnX() { return Mth.floor(this.getX()); }
    private int columnZ() { return Mth.floor(this.getZ()); }

    /** Region where a passenger counts as aboard (standing on the cab slab). */
    private AABB platformBox() {
        return new AABB(getX() - 0.49, getY() + 0.3, getZ() - 0.49, getX() + 0.49, getY() + 2.6, getZ() + 0.49);
    }

    /** Doorway cells of any landing door adjacent to the column at a floor. */
    private List<AABB> doorwayBoxes(int floorY) {
        List<AABB> list = new ArrayList<>();
        if (!(level() instanceof ServerLevel sl)) return list;
        BlockPos col = new BlockPos(columnX(), floorY, columnZ());
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos p = col.relative(dir);
            var st = sl.getBlockState(p);
            if (st.getBlock() instanceof com.ultima.elevators.block.ElevatorDoorBlock
                    || st.getBlock() instanceof com.ultima.elevators.block.ElevatorDoorUpperBlock) {
                list.add(new AABB(p));
            }
        }
        return list;
    }

    private void setLandingDoors(boolean open) {
        if (!(level() instanceof ServerLevel sl)) return;
        int fy = dockedFloor >= 0 && dockedFloor < floors.length ? floors[dockedFloor] : -1;
        if (fy < 0) return;
        boolean sameFloor = landingDoorsSyncFloor == fy;
        if (sameFloor && landingDoorsOpen == open) return;
        // close stale doors left open at another floor
        if (landingDoorsOpen && !sameFloor && landingDoorsSyncFloor >= 0) {
            applyDoorState(sl, landingDoorsSyncFloor, false);
        }
        applyDoorState(sl, fy, open);
        landingDoorsOpen = open;
        landingDoorsSyncFloor = fy;
    }

    private void applyDoorState(ServerLevel sl, int fy, boolean open) {
        BlockPos col = new BlockPos(columnX(), fy, columnZ());
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos base = col.relative(dir);
            var lowerState = sl.getBlockState(base);
            if (lowerState.getBlock() instanceof com.ultima.elevators.block.ElevatorDoorBlock
                    && lowerState.getValue(com.ultima.elevators.block.ElevatorDoorBlock.OPEN) != open) {
                sl.setBlock(base, lowerState.setValue(com.ultima.elevators.block.ElevatorDoorBlock.OPEN, open), 3);
            }
            BlockPos upper = base.above();
            var upperState = sl.getBlockState(upper);
            if (upperState.getBlock() instanceof com.ultima.elevators.block.ElevatorDoorUpperBlock
                    && upperState.getValue(com.ultima.elevators.block.ElevatorDoorUpperBlock.OPEN) != open) {
                sl.setBlock(upper, upperState.setValue(com.ultima.elevators.block.ElevatorDoorUpperBlock.OPEN, open), 3);
            }
        }
    }

    // ---------------------------------------------------------------- sounds

    private void play(Object sound, float pitch) {
        net.minecraft.sounds.SoundEvent event;
        if (sound instanceof net.minecraft.core.Holder<?> h) {
            event = (net.minecraft.sounds.SoundEvent) h.value();
        } else {
            event = (net.minecraft.sounds.SoundEvent) sound;
        }
        if (level() instanceof ServerLevel sl) {
            sl.playSound(null, getX(), getY(), getZ(), event, SoundSource.BLOCKS, 0.8F, pitch);
        }
    }

    private void playDoorOpen() {
        play(SoundEvents.IRON_DOOR_OPEN, 0.9F + 0.2F * level().random.nextFloat());
    }

    private void playDoorClose() {
        play(SoundEvents.IRON_DOOR_CLOSE, 0.9F + 0.2F * level().random.nextFloat());
    }

    private void playChime() {
        play(SoundEvents.NOTE_BLOCK_BELL, brand.chimePitch);
    }

    private void playStart() {
        play(SoundEvents.MINECART_RIDING, 0.6F);
    }

    // ---------------------------------------------------------------- main tick

    private void serverTick() {
        ControllerBlockEntity ctrl = controller();

        if (ctrl == null) {
            if (controllerPos != null && level() instanceof ServerLevel sl && sl.hasChunkAt(controllerPos)) {
                missingControllerTicks++;
                if (missingControllerTicks > 60) {
                    this.discard();
                }
            }
            return;
        }
        missingControllerTicks = 0;

        // refresh contract every half second
        if ((tickCount & 15) == 0) {
            int[] fs = ctrl.getFloorYs().stream().mapToInt(Integer::intValue).toArray();
            applyContract(ctrl.getBrand(), ctrl.getStyle(), ctrl.getContractSpeed(), ctrl.getDwellTicks(), fs);
            setCarName(ctrl.getCarName());
        }

        if (floors.length < 2) {
            entityData.set(DATA_FLOOR_COUNT, floors.length);
            return;
        }

        // clamp inside shaft
        double clampedY = Mth.clamp(getY(), floors[0], floors[floors.length - 1]);
        if (clampedY != getY()) {
            setPos(getX(), clampedY, getZ());
        }

        int cf = nearestFloorIndex();
        entityData.set(DATA_CURRENT_FLOOR, cf);
        entityData.set(DATA_FLOOR_COUNT, floors.length);

        // expire stale pickups
        long now = level().getGameTime();
        pickups.values().removeIf(p -> now - p.createdAt() > 2400);

        // choose next target when idle
        if (targetFloor < 0 && doorState == DoorState.CLOSED) {
            int next = nextStop(cf);
            if (next >= 0 && next != cf) {
                targetFloor = next;
                motionDir = next > cf ? 1 : -1;
                entityData.set(DATA_MOTION_DIR, motionDir);
                playStart();
            } else if (next == cf) {
                // stop needed at the current floor (hall call / pickup / destination here)
                serveCurrentFloor(cf);
            } else {
                motionDir = 0;
                entityData.set(DATA_MOTION_DIR, 0);
            }
        }

        // movement
        if (targetFloor >= 0 && doorState == DoorState.CLOSED) {
            double targetY = floors[targetFloor];
            double dy = targetY - getY();
            float maxV = Math.max(0.02F, contractSpeed / 20.0F);
            double brakeDist = (vy * vy) / (2.0 * ACCEL);
            if (Math.abs(dy) <= brakeDist + 0.06) {
                vy = Math.max(0.01F, (float) Math.sqrt(2.0 * ACCEL * Math.abs(dy)));
            } else {
                vy = Math.min(maxV, vy + ACCEL);
            }
            double step = Math.signum(dy) * Math.min(Math.abs(dy), vy);
            setPos(getX(), getY() + step, getZ());
            movePassengers(true);

            if (Math.abs(targetY - getY()) < 0.02) {
                setPos(getX(), targetY, getZ());
                vy = 0.0;
                arrive(targetFloor);
            }
        } else if (vy != 0.0) {
            movePassengers(true);
        } else {
            movePassengers(false);
        }

        tickDoorMachine();

        if (lightCurtainCooldown > 0) lightCurtainCooldown--;
        if (doorSoundCooldown > 0) doorSoundCooldown--;
    }

    /** SCAN dispatch: honour current travel direction, then serve the other side. */
    private int nextStop(int cf) {
        TreeSet<Integer> all = new TreeSet<>(hallUp);
        all.addAll(hallDown);
        all.addAll(playerTargets.values());
        for (Pickup p : pickups.values()) all.add(p.pickupFloor());
        if (all.isEmpty()) return -1;

        Integer above = all.ceiling(cf);
        Integer below = all.floor(cf);
        if (above != null && above == cf) {
            boolean needStopHere = hallUp.contains(cf) || hallDown.contains(cf)
                    || playerTargets.containsValue(cf)
                    || anyPickupAt(cf);
            if (!needStopHere) above = all.higher(cf);
        }
        int dirPref = motionDir != 0 ? motionDir : 1;
        Integer pick = dirPref > 0 ? above : below;
        if (pick == null) pick = dirPref > 0 ? below : above;
        return pick == null ? -1 : pick;
    }

    private boolean anyPickupAt(int floor) {
        for (Pickup p : pickups.values()) {
            if (p.pickupFloor() == floor) return true;
        }
        return false;
    }

    private void arrive(int floorIdx) {
        targetFloor = -1;
        dockedFloor = floorIdx;
        motionDir = 0;
        entityData.set(DATA_MOTION_DIR, 0);
        playChime();
        openDoors();
        ControllerBlockEntity ctrl = controller();
        if (ctrl != null) ctrl.onFloorServed(floorIdx);
        hallUp.remove(floorIdx);
        hallDown.remove(floorIdx);
        // consume arrived player targets so they can leave
        playerTargets.values().removeIf(f -> f == floorIdx);
    }

    /** Serve a stop at the floor the cab is already docked at (hall call / pickup here). */
    private void serveCurrentFloor(int floorIdx) {
        dockedFloor = floorIdx;
        openDoors();
        ControllerBlockEntity ctrl = controller();
        if (ctrl != null) ctrl.onFloorServed(floorIdx);
        hallUp.remove(floorIdx);
        hallDown.remove(floorIdx);
        playerTargets.values().removeIf(f -> f == floorIdx);
    }

    // ---------------------------------------------------------------- doors

    private void openDoors() {
        if (doorState == DoorState.CLOSED) {
            doorState = DoorState.OPENING;
            entityData.set(DATA_DOORS_OPEN, true);
            setLandingDoors(true);
            if (doorSoundCooldown <= 0) {
                playDoorOpen();
                doorSoundCooldown = 10;
            }
        }
    }

    private void tickDoorMachine() {
        switch (doorState) {
            case OPENING -> {
                float p = doorProgress() + DOOR_SPEED;
                if (p >= 1.0F) {
                    entityData.set(DATA_DOOR_PROGRESS, 1.0F);
                    doorState = DoorState.OPEN;
                    doorTimer = dwellTicks;
                } else {
                    entityData.set(DATA_DOOR_PROGRESS, p);
                }
            }
            case OPEN -> {
                boardEntities();
                if (doorHold) {
                    doorTimer = Math.max(doorTimer, 20);
                }
                if (--doorTimer <= 0) {
                    doorState = DoorState.CLOSING;
                }
            }
            case CLOSING -> {
                if (lightCurtainCooldown <= 0 && isLightCurtainBlocked()) {
                    lightCurtainCooldown = 30;
                    doorState = DoorState.OPENING;
                    if (doorSoundCooldown <= 0) {
                        playDoorOpen();
                        doorSoundCooldown = 10;
                    }
                    break;
                }
                float p = doorProgress() - DOOR_SPEED;
                if (p <= 0.0F) {
                    entityData.set(DATA_DOOR_PROGRESS, 0.0F);
                    doorState = DoorState.CLOSED;
                    doorHold = false;
                    entityData.set(DATA_DOORS_OPEN, false);
                    setLandingDoors(false);
                    if (doorSoundCooldown <= 0) {
                        playDoorClose();
                        doorSoundCooldown = 10;
                    }
                } else {
                    entityData.set(DATA_DOOR_PROGRESS, p);
                }
            }
            case CLOSED -> { }
        }
    }

    private boolean isLightCurtainBlocked() {
        if (dockedFloor < 0 || dockedFloor >= floors.length) return false;
        int fy = floors[dockedFloor];
        for (AABB doorway : doorwayBoxes(fy)) {
            List<Entity> blocked = level().getEntities(this, doorway, e -> e != this && e.isAlive());
            if (!blocked.isEmpty()) return true;
        }
        return false;
    }

    // ---------------------------------------------------------------- passengers

    private void boardEntities() {
        AABB box = platformBox();
        List<Entity> found = level().getEntities(this, box,
                e -> e != this && e.isAlive() && e.canChangeDimensions() && !e.isPassenger() && !e.isVehicle()
                        && !(e instanceof ElevatorCarEntity));
        for (Entity e : found) {
            UUID id = e.getUUID();
            if (!aboard.containsKey(id)) {
                aboard.put(id, new double[]{e.getX() - getX(), e.getY() - getY(), e.getZ() - getZ()});
                Pickup p = pickups.get(id);
                if (p != null && p.pickupFloor() == nearestFloorIndex()) {
                    playerTargets.put(id, p.destFloor());
                    pickups.remove(id);
                    if (e instanceof Player pl) {
                        pl.displayClientMessage(Component.translatable(
                                "message.ultimaelevators.onboard",
                                Component.literal(FloorLabels.label(p.destFloor(), floors.length))), true);
                    }
                }
            }
        }
    }

    /**
     * @param force true while the cab is physically moving (passengers are locked to their offsets),
     *              false while docked (passengers walk freely and offsets are observed).
     */
    private void movePassengers(boolean force) {
        if (aboard.isEmpty()) return;
        AABB keep = platformBox().inflate(0.25);
        List<UUID> gone = new ArrayList<>();
        for (Map.Entry<UUID, double[]> en : aboard.entrySet()) {
            Entity e = findEntity(en.getKey());
            if (e == null) {
                continue;
            }
            double[] off = en.getValue();
            if (force) {
                e.setPos(getX() + off[0], getY() + off[1], getZ() + off[2]);
                e.hasImpulse = true;
            } else {
                // observe walking inside the cab
                off[0] = e.getX() - getX();
                off[1] = e.getY() - getY();
                off[2] = e.getZ() - getZ();
                if (e.onGround() && doorState == DoorState.OPEN && !keep.intersects(e.getBoundingBox())) {
                    gone.add(en.getKey());
                }
            }
        }
        for (UUID id : gone) {
            aboard.remove(id);
        }
    }

    private Entity findEntity(UUID id) {
        if (level() instanceof ServerLevel sl) {
            return sl.getEntity(id);
        }
        return null;
    }

    // ---------------------------------------------------------------- entity base

    @Override
    public void tick() {
        this.baseTick();
        if (!level().isClientSide) {
            serverTick();
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    /** Acts as the 0.5-high cab slab: players walk onto it and stand inside the cab. */
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    // ---------------------------------------------------------------- nbt

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        if (controllerPos != null) tag.putLong("ctrl", controllerPos.asLong());
        tag.putString("carName", getCarName());
        tag.putFloat("speed", contractSpeed);
        tag.putInt("dwell", dwellTicks);
        long[] fs = new long[floors.length];
        for (int i = 0; i < fs.length; i++) fs[i] = floors[i];
        tag.putLongArray("floors", fs);
        ListTag aboardList = new ListTag();
        for (Map.Entry<UUID, double[]> en : aboard.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("id", en.getKey());
            t.putDouble("dx", en.getValue()[0]);
            t.putDouble("dy", en.getValue()[1]);
            t.putDouble("dz", en.getValue()[2]);
            aboardList.add(t);
        }
        tag.put("aboard", aboardList);
        ListTag pickupList = new ListTag();
        for (Map.Entry<UUID, Pickup> en : pickups.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("id", en.getKey());
            t.putInt("pickup", en.getValue().pickupFloor());
            t.putInt("dest", en.getValue().destFloor());
            pickupList.add(t);
        }
        tag.put("pickups", pickupList);
        ListTag targetList = new ListTag();
        for (Map.Entry<UUID, Integer> en : playerTargets.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("id", en.getKey());
            t.putInt("floor", en.getValue());
            targetList.add(t);
        }
        tag.put("targets", targetList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("ctrl")) controllerPos = BlockPos.of(tag.getLong("ctrl"));
        setCarName(tag.contains("carName") ? tag.getString("carName") : "A");
        contractSpeed = Math.max(0.2F, tag.getFloat("speed"));
        dwellTicks = Math.max(20, tag.getInt("dwell"));
        if (tag.contains("floors")) {
            long[] fs = tag.getLongArray("floors");
            floors = new int[fs.length];
            for (int i = 0; i < fs.length; i++) floors[i] = (int) fs[i];
        }
        aboard.clear();
        if (tag.contains("aboard")) {
            ListTag list = tag.getList("aboard", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag t = list.getCompound(i);
                aboard.put(t.getUUID("id"), new double[]{
                        t.getDouble("dx"), t.getDouble("dy"), t.getDouble("dz")});
            }
        }
        pickups.clear();
        if (tag.contains("pickups")) {
            ListTag list = tag.getList("pickups", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag t = list.getCompound(i);
                pickups.put(t.getUUID("id"), new Pickup(t.getInt("pickup"), t.getInt("dest"), level().getGameTime()));
            }
        }
        playerTargets.clear();
        if (tag.contains("targets")) {
            ListTag list = tag.getList("targets", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag t = list.getCompound(i);
                playerTargets.put(t.getUUID("id"), t.getInt("floor"));
            }
        }
        entityData.set(DATA_FLOOR_COUNT, floors.length);
        entityData.set(DATA_CAR_NAME, getCarName());
    }

    public static EntityType<ElevatorCarEntity> type() {
        return ModEntities.ELEVATOR_CAR.get();
    }
}
