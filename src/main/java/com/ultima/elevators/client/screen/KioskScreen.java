package com.ultima.elevators.client.screen;

import com.ultima.elevators.core.FloorLabels;
import com.ultima.elevators.network.C2SKioskSelectPacket;
import com.ultima.elevators.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * KONE Polaris destination selection screen.
 */
public class KioskScreen extends Screen {
    private final BlockPos pos;
    private final int floorCount;

    public KioskScreen(BlockPos pos, int floorCount, String group) {
        super(Component.translatable("gui.ultimaelevators.kiosk"));
        this.pos = pos;
        this.floorCount = floorCount;
    }

    @Override
    protected void init() {
        if (floorCount < 2) {
            return;
        }
        int perRow = Math.min(6, floorCount);
        int rows = (floorCount + perRow - 1) / perRow;
        int bw = 30, bh = 20, gap = 4;
        int totalW = perRow * bw + (perRow - 1) * gap;
        int x0 = (this.width - totalW) / 2;
        int y0 = Math.max(50, this.height / 2 - rows * (bh + gap) / 2);

        for (int i = 0; i < floorCount; i++) {
            final int floor = i;
            int r = i / perRow;
            int c = i % perRow;
            Button b = Button.builder(Component.literal(FloorLabels.label(i)), btn -> select(floor))
                    .bounds(x0 + c * (bw + gap), y0 + r * (bh + gap), bw, bh)
                    .build();
            this.addRenderableWidget(b);
        }
    }

    private void select(int floor) {
        ModNetwork.sendToServer(new C2SKioskSelectPacket(pos, floor));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, 18, 0xFFFFFF);
        if (floorCount < 2) {
            g.drawCenteredString(this.font, Component.translatable("gui.ultimaelevators.kiosk.no_service"),
                    this.width / 2, this.height / 2 - 4, 0xFF8080);
        } else {
            g.drawCenteredString(this.font, Component.translatable("gui.ultimaelevators.kiosk.hint"),
                    this.width / 2, 34, 0xAAAAAA);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
