package com.ultima.elevators.client.renderer;

import com.ultima.elevators.UltimaElevators;
import com.ultima.elevators.block.ElevatorDoorBlock;
import com.ultima.elevators.block.entity.DoorBlockEntity;
import com.ultima.elevators.client.model.DoorModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Sliding center-opening landing doors, rendered across two block heights.
 */
public class DoorRenderer implements BlockEntityRenderer<DoorBlockEntity> {
    private static final ResourceLocation TEX =
            new ResourceLocation(UltimaElevators.MODID, "textures/entity/elevator/door.png");

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(UltimaElevators.MODID, "elevator_door"), "main");

    private final ModelPart panelL;
    private final ModelPart panelR;

    public DoorRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(LAYER);
        this.panelL = root.getChild("panelL");
        this.panelR = root.getChild("panelR");
    }

    @Override
    public void render(DoorBlockEntity be, float partialTicks, PoseStack ps,
                       MultiBufferSource buf, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;
        Direction facing = be.getBlockState().getValue(ElevatorDoorBlock.FACING);
        float progress = be.getOpenProgress();

        ps.pushPose();
        ps.translate(0.5F, 0.0F, 0.5F);
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));

        panelL.x = -4.0F - 8.0F * progress;
        panelR.x = 4.0F + 8.0F * progress;
        var buffer = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        int overlay = OverlayTexture.NO_OVERLAY;
        panelL.render(ps, buffer, packedLight, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        panelR.render(ps, buffer, packedLight, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        ps.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(DoorBlockEntity be) {
        return true; // panels extend into the block above
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
