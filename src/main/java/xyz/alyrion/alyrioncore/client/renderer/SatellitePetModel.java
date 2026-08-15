package xyz.alyrion.alyrioncore.client.renderer;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import xyz.alyrion.alyrioncore.AlyrionCore;

/**
 * Box-geometry 3D model of the Satellite pet: a gold research satellite with
 * twin blue solar wings, a tilted antenna dish on a mast and a blinking beacon
 * light. Texture atlas lives at {@code textures/pets/satellite.png} (128x32).
 */
public class SatellitePetModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "satellite_pet"), "main");

    /** 1 texture pixel = 1 model unit on the 128x32 atlas. */
    private static final int TEX_WIDTH = 128;
    private static final int TEX_HEIGHT = 32;

    private SatellitePetModel() {
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition satellite = root.addOrReplaceChild("satellite", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        // Central body (8x8x8, centered)
        satellite.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Twin solar wings (right side, mirrored left side)
        satellite.addOrReplaceChild("panel_right",
                CubeListBuilder.create().texOffs(32, 0).addBox(4.0F, -0.5F, -2.5F, 12.0F, 1.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        satellite.addOrReplaceChild("panel_left",
                CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-16.0F, -0.5F, -2.5F, 12.0F, 1.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Antenna mast with a tilted parabolic dish
        PartDefinition mast = satellite.addOrReplaceChild("mast",
                CubeListBuilder.create().texOffs(80, 8).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        mast.addOrReplaceChild("dish",
                CubeListBuilder.create().texOffs(80, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, -0.35F, 0.0F, 0.0F));

        // Blinking beacon light, kept OUTSIDE the satellite tree so the renderer
        // can draw it with a blinking tint without re-drawing the whole model.
        root.addOrReplaceChild("light",
                CubeListBuilder.create().texOffs(110, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, TEX_WIDTH, TEX_HEIGHT);
    }
}
