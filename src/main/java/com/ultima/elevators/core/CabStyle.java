package com.ultima.elevators.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Cab interior / frame finish styles ("cerceve" options).
 */
public enum CabStyle {
    STAINLESS("Brushed Stainless", 0xFFB8BDC4),
    BLACK_STEEL("Black Steel", 0xFF2A2D32),
    GOLD("Champagne Gold", 0xFFC9A227),
    PANORAMIC("Panoramic Glass", 0xFF9FD8E8);

    public final String displayName;
    public final int tint;

    CabStyle(String displayName, int tint) {
        this.displayName = displayName;
        this.tint = tint;
    }

    public float red() { return ((tint >> 16) & 0xFF) / 255.0F; }
    public float green() { return ((tint >> 8) & 0xFF) / 255.0F; }
    public float blue() { return (tint & 0xFF) / 255.0F; }

    public Component label() {
        return Component.literal(displayName).withStyle(ChatFormatting.GRAY);
    }

    public CabStyle next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public CabStyle prev() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public static CabStyle byIndex(int i) {
        CabStyle[] v = values();
        return v[Math.floorMod(i, v.length)];
    }
}
