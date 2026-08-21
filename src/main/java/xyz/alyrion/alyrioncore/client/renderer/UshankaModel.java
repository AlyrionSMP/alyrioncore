package xyz.alyrion.alyrioncore.client.renderer;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Worn model of the Soviet Ushanka. Replaces the vanilla armor "helmet box"
 * with a fur crown, a brow flap over the forehead, two ear flaps hanging down
 * to the chin and a neck flap at the back. All parts hang off the humanoid
 * head part, so the hat follows head rotation.
 *
 * <p>UV layout on the 64x64 sheet (see {@code generate_ushanka.py}):
 * <pre>
 * crown (9x5x9)       @ (0,0)   top (9,0) bottom (18,0) sides (0..36, 9..14)
 * back flap (8x8x1)   @ (0,16)  outer face (10,17) 8x8
 * front flap (8x3x1)  @ (20,16) outer face (21,17) 8x3  <- red star + band
 * ear flaps (1x8x6)   @ (40,0)  outer face (40,6) 6x8 (shared, left mirrored)
 * </pre>
 */
@OnlyIn(Dist.CLIENT)
public class UshankaModel extends HumanoidArmorModel<LivingEntity> {

    private static UshankaModel instance;

    private UshankaModel(ModelPart root) {
        super(root);
    }

    public static UshankaModel getInstance() {
        if (instance == null) {
            instance = new UshankaModel(createBodyLayer().bakeRoot());
        }
        return instance;
    }

    private static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        // Crown: a fur dome over the top half of the head, slightly oversized.
        // Replaces the vanilla head box so no plain "helmet cube" remains.
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.5F, -9.5F, -4.5F, 9.0F, 5.0F, 9.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // The hat part must exist (the armor layer toggles it) but stays empty.
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        // Brow flap on the forehead, protruding and tilted slightly forward.
        head.addOrReplaceChild("front_flap",
                CubeListBuilder.create().texOffs(20, 16)
                        .addBox(-4.2F, -3.0F, -1.0F, 8.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -5.5F, -4.1F, -0.10F, 0.0F, 0.0F));

        // Ear flaps hanging past the chin, tilted slightly outward.
        head.addOrReplaceChild("ear_flap_right",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-1.1F, -0.5F, -3.0F, 1.0F, 8.0F, 6.0F),
                PartPose.offsetAndRotation(-4.1F, -4.5F, -0.8F, 0.0F, 0.0F, 0.10F));
        head.addOrReplaceChild("ear_flap_left",
                CubeListBuilder.create().texOffs(40, 0).mirror()
                        .addBox(0.1F, -0.5F, -3.0F, 1.0F, 8.0F, 6.0F),
                PartPose.offsetAndRotation(4.1F, -4.5F, -0.8F, 0.0F, 0.0F, -0.10F));

        // Neck flap at the back, tilted slightly backwards.
        head.addOrReplaceChild("back_flap",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-4.2F, -0.5F, 0.0F, 8.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -4.5F, 3.9F, 0.10F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
