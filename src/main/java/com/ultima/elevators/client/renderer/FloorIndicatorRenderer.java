package com.ultima.elevators.client.renderer;

import com.ultima.elevators.block.FloorIndicatorBlock;
import com.ultima.elevators.block.entity.FloorIndicatorBlockEntity;
import com.ultima.elevators.core.ElevatorBrand;
import com.ultima.elevators.core.FloorLabels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/**
 * Landing indicator: current floor number + travel arrow, tinted by brand color.
 */
public class FloorIndicatorRenderer implements BlockEntityRenderer<FloorIndicatorBlockEntity> {
    public FloorIndicatorRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(FloorIndicatorBlockEntity be, float partialTicks, PoseStack ps,
                       MultiBufferSource buf, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;
        Direction facing = be.getBlockState().getValue(FloorIndicatorBlock.FACING);
        int floor = be.getFloorIndex();
        int dir = be.getDir();

        ps.pushPose();
        ps.translate(0.5F, 0.0F, 0.5F);
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        ps.translate(0.0F, 0.75F, -0.19F);
        ps.mulPose(Axis.YP.rotationDegrees(180.0F));
        ps.scale(0.035F, 0.035F, 0.035F);

        Font font = Minecraft.getInstance().font;
        String label = floor >= 0 ? FloorLabels.label(floor) : "--";
        int color = floor >= 0 ? 0xFFD868 : 0x909090;

        float w = font.width(label);
        font.drawInBatch(label, -w / 2.0F, -4.0F, color, false,
                ps.last().pose(), buf, Font.DisplayMode.NORMAL, 0, packedLight);

        if (dir != 0) {
            String arrow = dir > 0 ? "^" : "v";
            int arrowColor = dir > 0 ? 0x40FF60 : 0xFFA000;
            font.drawInBatch(arrow, w / 2.0F + 2.0F, -4.0F, arrowColor, false,
                    ps.last().pose(), buf, Font.DisplayMode.NORMAL, 0, packedLight);
        }
        ps.popPose();
    }
}
