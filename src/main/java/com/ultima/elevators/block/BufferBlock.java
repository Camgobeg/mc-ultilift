package com.ultima.elevators.block;

/** Marker: oil/spring shaft buffer (kuyu yayi). */
public interface BufferBlock extends ShaftBlock {
    /** true = oil buffer, required for MR high speed. */
    boolean isOil();
}
