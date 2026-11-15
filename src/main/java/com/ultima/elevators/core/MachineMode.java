package com.ultima.elevators.core;

/**
 * Machine room configuration detected from the shaft head block.
 */
public enum MachineMode {
    MRL("Machine-Room-Less", 1.6F),
    MR_GEARED("MR Geared Traction", 2.5F),
    MR_GEARLESS("MR Gearless Traction", 6.0F);

    public final String displayName;
    /** Maximum contract speed allowed by this machine configuration, blocks per second. */
    public final float maxSpeed;

    MachineMode(String displayName, float maxSpeed) {
        this.displayName = displayName;
        this.maxSpeed = maxSpeed;
    }

    public boolean isMachineRoom() {
        return this != MRL;
    }
}
