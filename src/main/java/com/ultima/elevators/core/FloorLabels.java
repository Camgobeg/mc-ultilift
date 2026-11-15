package com.ultima.elevators.core;

/**
 * Human readable floor labels. Bottom shaft floor = "G", then 1..n.
 */
public final class FloorLabels {
    private FloorLabels() {}

    public static String label(int index, int count) {
        if (index <= 0) return "G";
        int above = count - 1 - index;
        // Reverse numbering would complicate; keep simple: 0=G, 1=1, 2=2 ...
        return String.valueOf(index);
    }

    public static String label(int index) {
        return index <= 0 ? "G" : String.valueOf(index);
    }
}
