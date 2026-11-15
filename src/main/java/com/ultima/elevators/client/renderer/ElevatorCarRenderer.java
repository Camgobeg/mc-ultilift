package com.ultima.elevators.client.renderer;

import com.ultima.elevators.UltimaElevators;
import com.ultima.elevators.client.model.ElevatorModels;
import com.ultima.elevators.entity.ElevatorCarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the elevator cab, its car doors, the counterweight (moving opposite) and ropes.
 */
public class ElevatorCarRenderer extends EntityRenderer<ElevatorCarEntity> {
    private static final ResourceLocation TEX =
            new ResourceLocation(UltimaElevators.MODID, "textures/entity/elevator/cab.png");

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(UltimaElevators.MODID, "elevator_car"), "main");

    private final ModelPart floor;
    private final ModelPart ceiling;
    private final ModelPart back;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart postL;
    private final ModelPart postR;
    private final ModelPart header;
    private final ModelPart carDoorL;
    private final ModelPart carDoorR;
    private final ModelPart cw;
    private final ModelPart ropeA;
    private final ModelPart ropeB;

    public ElevatorCarRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        ModelPart root = ctx.bakeLayer(LAYER);
        this.floor = ElevatorModels.child(root, "floor");
        this.ceiling = ElevatorModels.child(root, "ceiling");
        this.back = ElevatorModels.child(root, "back");
        this.left = ElevatorModels.child(root, "left");
        this.right = ElevatorModels.child(root, "right");
        this.postL = ElevatorModels.child(root, "postL");
        this.postR = ElevatorModels.child(root, "postR");
        this.header = ElevatorModels.child(root, "header");
        this.carDoorL = ElevatorModels.child(root, "carDoorL");
        this.carDoorR = ElevatorModels.child(root, "carDoorR");
        this.cw = ElevatorModels.child(root, "cw");
        this.ropeA = ElevatorModels.child(root, "ropeA");
        this.ropeB = ElevatorModels.child(root, "ropeB");
    }

    @Override
    public void render(ElevatorCarEntity entity, float yaw, float partialTicks,
                       PoseStack ps, MultiBufferSource buf, int packedLight) {
        ps.pushPose();

        Direction facing = Direction.from2DDataValue(entityFacing(entity));
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));

        float r = entity.getBrand().red() * 0.8F + 0.2F;
        float g = entity.getBrand().green() * 0.8F + 0.2F;
        float b = entity.getBrand().blue() * 0.8F + 0.2F;
        var buffer = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        int light = packedLight;
        int overlay = OverlayTexture.NO_OVERLAY;

        floor.render(ps, buffer, light, overlay, r, g, b, 1.0F);
        ceiling.render(ps, buffer, light, overlay, r, g, b, 1.0F);
        back.render(ps, buffer, light, overlay, r, g, b, 1.0F);
        left.render(ps, buffer, light, overlay, r, g, b, 1.0F);
        right.render(ps, buffer, light, overlay, r, g, b, 1.0F);
        postL.render(ps, buffer, light, overlay, r, g, b, 1.0F);
        postR.render(ps, buffer, light, overlay, r, g, b, 1.0F);
        header.render(ps, buffer, light, overlay, r, g, b, 1.0F);

        float p = entity.doorProgress();
        carDoorL.x = -2.5F - 5.0F * p;
        carDoorR.x = 2.5F + 5.0F * p;
        carDoorL.render(ps, buffer, light, overlay, r, g, b, 1.0F);
        carDoorR.render(ps, buffer, light, overlay, r, g, b, 1.0F);

        renderCounterweightAndRopes(entity, ps, buf, light, overlay, r, g, b);

        ps.popPose();
        super.render(entity, yaw, partialTicks, ps, buf, packedLight);
    }

    private int entityFacing(ElevatorCarEntity entity) {
        // synced by the controller when the cab spawns; 2D data value
        return entity.facingIndex();
    }

    private void renderCounterweightAndRopes(ElevatorCarEntity entity, PoseStack ps, MultiBufferSource buf,
                                             int light, int overlay, float r, float g, float b) {
        double cabY = entity.getY();
        double bottom = entity.shaftBottom();
        double top = entity.shaftTop();
        if (top - bottom < 1.0) return;

        // counterweight rides opposite the cab between pit and head
        double cwY = (bottom + top) - cabY;
        cwY = Math.max(bottom + 0.75, Math.min(top - 0.75, cwY));
        double cwOffsetUnits = (cabY - cwY) * 16.0; // model units (positive = down)

        var buffer = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        ps.pushPose();
        ps.translate(0.0F, (float) cwOffsetUnits / 16.0F, 0.0F);
        cw.render(ps, buffer, light, overlay, r, g, b, 1.0F);
        ps.popPose();

        double cabRoofModel = -34.0;                      // y=+2.125 blocks above cab origin
        double sheaveModel = (cabY - (top + 1.25)) * 16.0; // sheave above the shaft head
        double cwTopModel = cwOffsetUnits;                 // top of the cw block

        // rope A: cab roof -> sheave
        double lenA = Math.max(1.0, cabRoofModel - sheaveModel);
        ps.pushPose();
        ps.translate(4.0F / 16.0F, (float) cabRoofModel / 16.0F, 5.5F / 16.0F);
        ps.scale(1.0F, (float) (lenA / 32.0), 1.0F);
        ropeA.render(ps, buffer, light, overlay, r, g, b, 1.0F);
        ps.popPose();

        // rope B: sheave -> counterweight top
        double lenB = Math.max(1.0, cwTopModel - sheaveModel);
        ps.pushPose();
        ps.translate(-4.0F / 16.0F, (float) sheaveModel / 16.0F, 5.5F / 16.0F);
        ps.scale(1.0F, (float) (lenB / 32.0), 1.0F);
        ropeB.render(ps, buffer, light, overlay, r, g, b, 1.0F);
        ps.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ElevatorCarEntity entity) {
        return TEX;
    }
}
