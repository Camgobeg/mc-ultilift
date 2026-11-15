package com.ultima.elevators.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Elevator OEM brands supported by the Ultima modular traction system.
 * Each brand changes the cab tint, the arrival chime pitch and the displayed logo.
 */
public enum ElevatorBrand {
    SCHINDLER("Schindler", 0xFFC8102E, 0.95F, ChatFormatting.RED),
    KONE("KONE", 0xFF005AA0, 1.15F, ChatFormatting.AQUA),
    OTIS("Otis", 0xFF003DA5, 1.05F, ChatFormatting.BLUE),
    MITSUBISHI("Mitsubishi", 0xFFE60012, 1.30F, ChatFormatting.LIGHT_PURPLE),
    TK_ELEVATOR("TKE", 0xFF00A94F, 0.85F, ChatFormatting.GREEN),
    ULTIMA("Ultima", 0xFFF0B000, 1.00F, ChatFormatting.GOLD);

    public final String displayName;
    /** ARGB tint applied to cab/panel rendering. */
    public final int tint;
    /** Arrival chime pitch. */
    public final float chimePitch;
    public final ChatFormatting chatColor;

    ElevatorBrand(String displayName, int tint, float chimePitch, ChatFormatting chatColor) {
        this.displayName = displayName;
        this.tint = tint;
        this.chimePitch = chimePitch;
        this.chatColor = chatColor;
    }

    public float red() { return ((tint >> 16) & 0xFF) / 255.0F; }
    public float green() { return ((tint >> 8) & 0xFF) / 255.0F; }
    public float blue() { return (tint & 0xFF) / 255.0F; }

    public Component label() {
        return Component.literal(displayName).withStyle(chatColor);
    }

    public ElevatorBrand next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public ElevatorBrand prev() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public static ElevatorBrand byIndex(int i) {
        ElevatorBrand[] v = values();
        return v[Math.floorMod(i, v.length)];
    }
}
