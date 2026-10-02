package org.dawnoftime.armoroftheages.client.models.anubis_armor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import org.dawnoftime.armoroftheages.client.models.ArmorModel;

public class FeetAnubisArmorModel<T extends LivingEntity> extends ArmorModel<T> {

    public FeetAnubisArmorModel(ModelPart root, boolean isSlim) {
        super(root, isSlim);
    }

    @Override
    public <E extends LivingEntity> ArmorModel<E> create(ModelPart root, boolean isSlim) {
        return new FeetAnubisArmorModel<>(root, isSlim);
    }

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition meshdefinition = templateLayerDefinition(1.0F);
        PartDefinition root = meshdefinition.getRoot();
        // The legs need their real position, Epic Fight reads the initial pose of the parts instead of copying the player one
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror()
                        .texOffs(93, 14).addBox(-2.5F, 7.4F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-0.2F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

       root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(93, 14).addBox(-2.5F, 7.4F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-0.2F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    protected void setupArmorPartAnim(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}
}
