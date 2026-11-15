package com.ultima.elevators.block;

import com.ultima.elevators.block.entity.ControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime registry of all controllers per dimension, plus dispatch helpers
 * (hall calls and KONE Polaris style destination assignment).
 */
public final class DispatchManager {
    private DispatchManager() {}

    private static final Map<String, List<ControllerBlockEntity>> CONTROLLERS = new ConcurrentHashMap<>();

    public static String keyOf(Level level) {
        return level.dimension().location().toString();
    }

    public static void register(ControllerBlockEntity controller) {
        Level level = controller.getLevel();
        if (level == null) return;
        CONTROLLERS.computeIfAbsent(keyOf(level), k -> new ArrayList<>()).add(controller);
    }

    public static void unregister(ControllerBlockEntity controller) {
        Level level = controller.getLevel();
        if (level == null) return;
        List<ControllerBlockEntity> list = CONTROLLERS.get(keyOf(level));
        if (list != null) list.remove(controller);
    }

    public static List<ControllerBlockEntity> controllersOf(Level level) {
        List<ControllerBlockEntity> list = CONTROLLERS.get(keyOf(level));
        return list == null ? List.of() : list;
    }

    public static List<ControllerBlockEntity> controllersInGroup(Level level, String group) {
        List<ControllerBlockEntity> all = controllersOf(level);
        List<ControllerBlockEntity> out = new ArrayList<>();
        for (ControllerBlockEntity c : all) {
            if (c.isRemoved()) continue;
            if (group.isEmpty() || c.getGroup().equalsIgnoreCase(group)) out.add(c);
        }
        return out;
    }

    public static ControllerBlockEntity nearest(Level level, BlockPos pos, double maxDist) {
        ControllerBlockEntity best = null;
        double bestDist = maxDist * maxDist;
        for (ControllerBlockEntity c : controllersOf(level)) {
            if (c.isRemoved()) continue;
            double d = c.getBlockPos().distSqr(pos);
            if (d < bestDist) {
                bestDist = d;
                best = c;
            }
        }
        return best;
    }

    /** Hall call from a call button. Returns true if queued. */
    public static boolean queueHallCall(Level level, BlockPos buttonPos, boolean up) {
        ControllerBlockEntity c = nearest(level, buttonPos, 48);
        if (c == null) return false;
        int floor = c.floorIndexForY(buttonPos.getY());
        if (floor < 0) return false;
        c.queueHallCall(floor, up);
        return true;
    }
}
