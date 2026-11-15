package com.ultima.elevators.client.renderer;

import com.ultima.elevators.block.DestinationKioskBlock;
import com.ultima.elevators.block.entity.KioskBlockEntity;
import com.ultima.elevators.core.FloorLabels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * Kiosk screen: shows the last destination assignment (KONE Polaris style).
 */
public class KioskRenderer implements BlockEntityRenderer<KioskBlockEntity> {
    public KioskRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(KioskBlockEntity be, float partialTicks, PoseStack ps,
                       MultiBufferSource buf, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;
        Direction facing = be.getBlockState().getValue(DestinationKioskBlock.FACING);

        ps.pushPose();
        ps.translate(0.5F, 0.0F, 0.5F);
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        ps.translate(0.0F, 1.45F, -0.22F);
        ps.mulPose(Axis.YP.rotationDegrees(180.0F));
        ps.scale(0.026F, 0.026F, 0.026F);

        Font font = Minecraft.getInstance().font;
        if (!be.getAssignedCar().isEmpty()) {
            String line1 = "Car " + be.getAssignedCar();
            String line2 = "-> " + FloorLabels.label(be.getAssignedDestFloor());
            float w1 = font.width(line1);
            float w2 = font.width(line2);
            font.drawInBatch(line1, -w1 / 2.0F, -4.0F, 0x7FE3FF, false,
                    ps.last().pose(), buf, Font.DisplayMode.NORMAL, 0, packedLight);
            font.drawInBatch(line2, -w2 / 2.0F, 5.0F, 0xFFFFFF, false,
                    ps.last().pose(), buf, Font.DisplayMode.NORMAL, 0, packedLight);
        } else if (be.getFloorCount() > 0) {
            String line = "Polaris";
            float w = font.width(line);
            font.drawInBatch(line, -w / 2.0F, 0.0F, 0x35C8F0, false,
                    ps.last().pose(), buf, Font.DisplayMode.NORMAL, 0, packedLight);
        } else {
            String line = "no service";
            float w = font.width(line);
            font.drawInBatch(line, -w / 2.0F, 0.0F, 0xFF6060, false,
                    ps.last().pose(), buf, Font.DisplayMode.NORMAL, 0, packedLight);
        }
        ps.popPose();
    }
}
