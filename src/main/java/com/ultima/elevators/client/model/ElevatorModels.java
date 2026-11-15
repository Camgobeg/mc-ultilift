package com.ultima.elevators.client.model;

import com.ultima.elevators.client.renderer.ElevatorCarRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Cab geometry: floor slab (0.5 high collision slab), walls with door aperture, ceiling,
 * animated car doors, counterweight and ropes.
 */
public final class ElevatorModels {
    private ElevatorModels() {}

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // floor slab: 16 x 8 x 16 (0.5 block thick), top at y=0 (feet level)
        root.addOrReplaceChild("floor", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 8.0F, 16.0F),
                PartPose.ZERO);

        // ceiling
        root.addOrReplaceChild("ceiling", CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-8.0F, -34.0F, -8.0F, 16.0F, 2.0F, 16.0F),
                PartPose.ZERO);

        // back wall (south side, +Z)
        root.addOrReplaceChild("back", CubeListBuilder.create()
                        .texOffs(0, 44).addBox(-8.0F, -32.0F, 6.0F, 16.0F, 32.0F, 2.0F),
                PartPose.ZERO);

        // side walls
        root.addOrReplaceChild("left", CubeListBuilder.create()
                        .texOffs(0, 44).addBox(-10.0F, -32.0F, -8.0F, 2.0F, 32.0F, 16.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("right", CubeListBuilder.create()
                        .texOffs(0, 44).addBox(8.0F, -32.0F, -8.0F, 2.0F, 32.0F, 16.0F),
                PartPose.ZERO);

        // front posts (door jambs of the cab)
        root.addOrReplaceChild("postL", CubeListBuilder.create()
                        .texOffs(38, 44).addBox(-8.0F, -32.0F, -10.0F, 3.0F, 32.0F, 2.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("postR", CubeListBuilder.create()
                        .texOffs(38, 44).addBox(5.0F, -32.0F, -10.0F, 3.0F, 32.0F, 2.0F),
                PartPose.ZERO);

        // header above the car door
        root.addOrReplaceChild("header", CubeListBuilder.create()
                        .texOffs(0, 80).addBox(-5.0F, -32.0F, -10.0F, 10.0F, 8.0F, 2.0F),
                PartPose.ZERO);

        // car door panels (animated by progress): 5 wide, 24 high (1.5 blocks)
        root.addOrReplaceChild("carDoorL", CubeListBuilder.create()
                        .texOffs(28, 80).addBox(-2.5F, -24.5F, -9.0F, 5.0F, 24.0F, 1.0F),
                PartPose.offset(-2.5F, 0.0F, 0.0F));
        root.addOrReplaceChild("carDoorR", CubeListBuilder.create()
                        .texOffs(28, 80).addBox(-2.5F, -24.5F, -9.0F, 5.0F, 24.0F, 1.0F),
                PartPose.offset(2.5F, 0.0F, 0.0F));

        // counterweight block (moves opposite to the cab)
        root.addOrReplaceChild("cw", CubeListBuilder.create()
                        .texOffs(44, 80).addBox(-3.0F, 0.0F, 4.0F, 6.0F, 12.0F, 3.0F),
                PartPose.ZERO);

        // ropes (1 x 32 x 1, scaled dynamically)
        root.addOrReplaceChild("ropeA", CubeListBuilder.create()
                        .texOffs(50, 110).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 32.0F, 1.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("ropeB", CubeListBuilder.create()
                        .texOffs(50, 110).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 32.0F, 1.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 160);
    }

    public static ModelPart child(ModelPart root, String name) {
        ModelPart p = root.getChild(name);
        if (p == null) {
            throw new IllegalStateException("Missing model part: " + name);
        }
        return p;
    }

    /** helper used by {@link ElevatorCarRenderer} */
    public static String[] partNames() {
        return new String[]{"floor", "ceiling", "back", "left", "right", "postL", "postR", "header"};
    }
}
