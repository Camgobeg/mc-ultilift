package com.ultima.elevators.client.screen;

import com.ultima.elevators.core.ElevatorBrand;
import com.ultima.elevators.core.FloorLabels;
import com.ultima.elevators.network.C2SCopActionPacket;
import com.ultima.elevators.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Car Operating Panel (COP): floor buttons + door open/hold/close, shown while riding.
 */
public class CopScreen extends Screen {
    private final int cabId;
    private final int currentFloor;
    private final int floorCount;
    private final ElevatorBrand brand;
    private final boolean doorsOpen;

    public CopScreen(int cabId, int currentFloor, int floorCount, ElevatorBrand brand, boolean doorsOpen) {
        super(Component.translatable("gui.ultimaelevators.cop"));
        this.cabId = cabId;
        this.currentFloor = currentFloor;
        this.floorCount = floorCount;
        this.brand = brand;
        this.doorsOpen = doorsOpen;
    }

    @Override
    protected void init() {
        int perRow = Math.min(8, Math.max(1, floorCount));
        int rows = (floorCount + perRow - 1) / perRow;
        int bw = 28, bh = 20, gap = 4;
        int totalW = perRow * bw + (perRow - 1) * gap;
        int x0 = (this.width - totalW) / 2;
        int y0 = 56;

        for (int i = 0; i < floorCount; i++) {
            final int floor = i;
            int r = i / perRow;
            int c = i % perRow;
            Button b = Button.builder(Component.literal(FloorLabels.label(i)), btn -> {
                        ModNetwork.sendToServer(new C2SCopActionPacket(cabId, 0, floor));
                        btn.active = false;
                    })
                    .bounds(x0 + c * (bw + gap), y0 + r * (bh + gap), bw, bh)
                    .build();
            if (i == currentFloor) b.active = false;
            this.addRenderableWidget(b);
        }

        int by = y0 + rows * (bh + gap) + 8;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.ultimaelevators.cop.door_open"), b ->
                        ModNetwork.sendToServer(new C2SCopActionPacket(cabId, 1, -1)))
                .bounds(this.width / 2 - 80, by, 75, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.ultimaelevators.cop.door_close"), b ->
                        ModNetwork.sendToServer(new C2SCopActionPacket(cabId, 2, -1)))
                .bounds(this.width / 2 + 5, by, 75, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        g.drawCenteredString(this.font, Component.translatable("gui.ultimaelevators.cop.status",
                        brand.label(), Component.literal(FloorLabels.label(currentFloor, floorCount))),
                this.width / 2, 30, 0xCCCCCC);
        g.drawCenteredString(this.font,
                Component.translatable(doorsOpen ? "gui.ultimaelevators.cop.doors.open" : "gui.ultimaelevators.cop.doors.closed"),
                this.width / 2, 42, doorsOpen ? 0x80FF80 : 0xFFB000);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
