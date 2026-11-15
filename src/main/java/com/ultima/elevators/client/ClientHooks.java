package com.ultima.elevators.client;

import com.ultima.elevators.client.screen.ControllerScreen;
import com.ultima.elevators.client.screen.CopScreen;
import com.ultima.elevators.client.screen.KioskScreen;
import com.ultima.elevators.core.CabStyle;
import com.ultima.elevators.core.ElevatorBrand;
import com.ultima.elevators.core.MachineMode;
import com.ultima.elevators.network.S2COpenControllerScreenPacket;
import com.ultima.elevators.network.S2COpenCopScreenPacket;
import com.ultima.elevators.network.S2COpenKioskScreenPacket;
import net.minecraft.client.Minecraft;

/** Client-only packet handlers (guarded by DistExecutor). */
public final class ClientHooks {
    private ClientHooks() {}

    public static void openControllerScreen(S2COpenControllerScreenPacket msg) {
        MachineMode mode = MachineMode.values()[Math.floorMod(msg.modeOrdinal, MachineMode.values().length)];
        Minecraft.getInstance().setScreen(new ControllerScreen(
                msg.pos,
                ElevatorBrand.byIndex(msg.brandIdx),
                CabStyle.byIndex(msg.styleIdx),
                msg.speedIdx,
                msg.dwellIdx,
                mode,
                msg.floorCount,
                msg.minY,
                msg.maxY,
                msg.statusKey,
                msg.group,
                msg.maxSpeedIdx));
    }

    public static void openKioskScreen(S2COpenKioskScreenPacket msg) {
        Minecraft.getInstance().setScreen(new KioskScreen(msg.pos, msg.floorCount, msg.group));
    }

    public static void openCopScreen(S2COpenCopScreenPacket msg) {
        Minecraft.getInstance().setScreen(new CopScreen(
                msg.cabId,
                msg.currentFloor,
                msg.floorCount,
                ElevatorBrand.byIndex(msg.brandIdx),
                msg.doorsOpen));
    }
}
