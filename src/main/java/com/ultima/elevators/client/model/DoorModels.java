package com.ultima.elevators.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Landing door panels (center opening, animated by the door block entity). */
public final class DoorModels {
    private DoorModels() {}

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // panel: 8 wide x 32 high (2 blocks) x 2 deep, top at y=-32 (y-down model space)
        root.addOrReplaceChild("panelL", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -32.0F, -1.0F, 8.0F, 32.0F, 2.0F),
                PartPose.ZERO);
        root.addOrReplaceChild("panelR", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -32.0F, -1.0F, 8.0F, 32.0F, 2.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 40);
    }
}
