package com.ultima.elevators.client.screen;

import com.ultima.elevators.core.CabStyle;
import com.ultima.elevators.core.ElevatorBrand;
import com.ultima.elevators.core.MachineMode;
import com.ultima.elevators.network.C2SControllerActionPacket;
import com.ultima.elevators.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Controller GUI: brand, cab frame style, contract speed, door dwell, dispatch group, recalibrate.
 */
public class ControllerScreen extends Screen {
    private final BlockPos pos;
    private final int floorCount;
    private final int minY;
    private final int maxY;
    private final MachineMode mode;
    private final String statusKeyRaw;

    private int maxSpeedIdx;

    private ElevatorBrand brand;
    private CabStyle style;
    private int speedIdx;
    private int dwellIdx;
    private String group;

    private EditBox groupBox;
    private Button brandBtn;
    private Button styleBtn;
    private Button speedBtn;
    private Button dwellBtn;

    private static final Component[] DWELL_NAMES = {
            Component.literal("1s"), Component.literal("1.5s"), Component.literal("2s"),
            Component.literal("3s"), Component.literal("4s"), Component.literal("5s"), Component.literal("6s")};

    public ControllerScreen(BlockPos pos, ElevatorBrand brand, CabStyle style, int speedIdx, int dwellIdx,
                            MachineMode mode, int floorCount, int minY, int maxY,
                            String statusKey, String group, int maxSpeedIdx) {
        super(Component.translatable("gui.ultimaelevators.controller"));
        this.pos = pos;
        this.brand = brand;
        this.style = style;
        this.speedIdx = speedIdx;
        this.dwellIdx = dwellIdx;
        this.mode = mode;
        this.floorCount = floorCount;
        this.minY = minY;
        this.maxY = maxY;
        this.statusKeyRaw = statusKey == null || statusKey.isEmpty() ? "status.ultimaelevators.ok" : statusKey;
        this.group = group;
        this.maxSpeedIdx = maxSpeedIdx;
    }

    private static final float[] SPEEDS = {0.5F, 1.0F, 1.6F, 2.5F, 4.0F, 6.0F};

    private void send(int action, int value, String text) {
        ModNetwork.sendToServer(new C2SControllerActionPacket(pos, action, value, text));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int x0 = cx - 100;
        int y = 40;

        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> cycleBrand(-1))
                .bounds(x0, y, 20, 20).build());
        brandBtn = this.addRenderableWidget(Button.builder(brandLabel(), b -> { })
                .bounds(x0 + 22, y, 116, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> cycleBrand(1))
                .bounds(x0 + 140, y, 20, 20).build());
        y += 24;

        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> cycleStyle(-1))
                .bounds(x0, y, 20, 20).build());
        styleBtn = this.addRenderableWidget(Button.builder(style.label(), b -> { })
                .bounds(x0 + 22, y, 116, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> cycleStyle(1))
                .bounds(x0 + 140, y, 20, 20).build());
        y += 24;

        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> cycleSpeed(-1))
                .bounds(x0, y, 20, 20).build());
        speedBtn = this.addRenderableWidget(Button.builder(speedLabel(), b -> { })
                .bounds(x0 + 22, y, 116, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> cycleSpeed(1))
                .bounds(x0 + 140, y, 20, 20).build());
        y += 24;

        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> cycleDwell(-1))
                .bounds(x0, y, 20, 20).build());
        dwellBtn = this.addRenderableWidget(Button.builder(dwellLabel(), b -> { })
                .bounds(x0 + 22, y, 116, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> cycleDwell(1))
                .bounds(x0 + 140, y, 20, 20).build());
        y += 26;

        groupBox = new EditBox(this.font, x0 + 20, y, 120, 20, Component.translatable("gui.ultimaelevators.group"));
        groupBox.setMaxLength(32);
        groupBox.setValue(group);
        groupBox.setFocused(true);
        this.addRenderableWidget(groupBox);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.ultimaelevators.group_set"), b -> {
            send(9, 0, groupBox.getValue());
        }).bounds(x0 + 142, y, 18, 20).build());
        y += 26;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.ultimaelevators.recalibrate"), b -> {
            send(10, 0, "");
            send(0, 0, "");
            this.onClose();
        }).bounds(cx - 60, y, 120, 20).build());
    }

    private Component brandLabel() {
        return brand.label();
    }

    private Component styleLabel() {
        return style.label();
    }

    private Component speedLabel() {
        return Component.literal(SPEEDS[speedIdx] + " m/s").append("  ")
                .append(mode.displayName);
    }

    private Component dwellLabel() {
        return Component.literal("Door dwell: ").append(DWELL_NAMES[dwellIdx]);
    }

    private void cycleBrand(int dir) {
        send(dir > 0 ? 1 : 2, dir, "");
        brand = dir > 0 ? brand.next() : brand.prev();
        brandBtn.setMessage(brandLabel());
    }

    private void cycleStyle(int dir) {
        send(dir > 0 ? 3 : 4, dir, "");
        style = dir > 0 ? style.next() : style.prev();
        styleBtn.setMessage(styleLabel());
    }

    private void cycleSpeed(int dir) {
        int ni = Math.max(0, Math.min(maxSpeedIdx, speedIdx + dir));
        send(dir > 0 ? 5 : 6, dir, "");
        speedIdx = ni;
        speedBtn.setMessage(speedLabel());
    }

    private void cycleDwell(int dir) {
        send(dir > 0 ? 7 : 8, dir, "");
        dwellIdx = Math.max(0, Math.min(DWELL_NAMES.length - 1, dwellIdx + dir));
        dwellBtn.setMessage(dwellLabel());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);

        g.drawString(this.font, Component.translatable("gui.ultimaelevators.brand"), this.width / 2 - 100, 34, 0xA0A0A0, false);
        int y = 40;
        y += 24;
        g.drawString(this.font, Component.translatable("gui.ultimaelevators.style"), this.width / 2 - 100, y - 6, 0xA0A0A0, false);
        y += 24;
        g.drawString(this.font, Component.translatable("gui.ultimaelevators.speed"), this.width / 2 - 100, y - 6, 0xA0A0A0, false);
        y += 24;
        g.drawString(this.font, Component.translatable("gui.ultimaelevators.dwell"), this.width / 2 - 100, y - 6, 0xA0A0A0, false);

        g.drawString(this.font,
                Component.translatable("gui.ultimaelevators.floors", floorCount, minY, maxY),
                this.width / 2 - 100, this.height - 40, 0x80FF80, false);

        int color = statusKeyRaw.endsWith(".ok") ? 0x80FF80 : 0xFF6060;
        g.drawString(this.font, Component.translatable(statusKeyRaw), this.width / 2 - 100, this.height - 26, color, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
