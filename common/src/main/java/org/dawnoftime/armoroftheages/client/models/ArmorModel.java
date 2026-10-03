package org.dawnoftime.armoroftheages.client.models;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.dawnoftime.armoroftheages.client.ArmorModelSupplier;
import org.jetbrains.annotations.NotNull;

public abstract class ArmorModel extends HumanoidModel<HumanoidRenderState> implements ArmorModelSupplier {
    public final boolean isSlim;
    /** Pose flags copied from the render state before each animation. */
    protected boolean riding;
    protected boolean crouching;

    public ArmorModel(ModelPart root, boolean isSlim) {
        super(root);
        this.isSlim = isSlim;
    }

    /**
     * Override this function to animate the model, instead of overriding {@link ArmorModel#setupAnim}.
     */
    protected abstract void setupArmorPartAnim(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch);

    /**
     * This function must be called before adding the parts in the other models !!!
     *
     * @return A minimal mesh with all the part of the player model and their appropriate rotation positions.
     */
    public static MeshDefinition templateLayerDefinition(float scale) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F + scale, 0.0F))
                .addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F + scale, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F + scale, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F + scale, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F + scale, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F + scale, 0.0F));
        return mesh;
    }

    /**
     * The vanilla humanoid pose is computed from the render state first, so the armor follows the body exactly.
     * Then the custom animation of the armor part is applied on top of it.
     */
    /**
     * Since 1.21 the vanilla humanoid model reads "hat" as a child of "head".
     * A head model that replaces "head" erases it, so every layer definition goes through this method.
     */
    public static MeshDefinition ensureHat(MeshDefinition mesh) {
        PartDefinition head = mesh.getRoot().getChild("head");
        if (head != null && head.getChild("hat") == null) {
            head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        }
        return mesh;
    }

    @Override
    public void setupAnim(@NotNull HumanoidRenderState state) {
        super.setupAnim(state);
        this.riding = state.isPassenger;
        this.crouching = state.isCrouching;
        if (state instanceof ArmorStandRenderState stand) {
            // Fix the "breathing" and wrong head rotation on ArmorStands
            float f = (float) Math.PI / 180F;
            this.head.xRot = f * stand.headPose.x();
            this.head.yRot = f * stand.headPose.y();
            this.head.zRot = f * stand.headPose.z();
            this.body.xRot = f * stand.bodyPose.x();
            this.body.yRot = f * stand.bodyPose.y();
            this.body.zRot = f * stand.bodyPose.z();
            this.leftArm.xRot = f * stand.leftArmPose.x();
            this.leftArm.yRot = f * stand.leftArmPose.y();
            this.leftArm.zRot = f * stand.leftArmPose.z();
            this.rightArm.xRot = f * stand.rightArmPose.x();
            this.rightArm.yRot = f * stand.rightArmPose.y();
            this.rightArm.zRot = f * stand.rightArmPose.z();
            this.leftLeg.xRot = f * stand.leftLegPose.x();
            this.leftLeg.yRot = f * stand.leftLegPose.y();
            this.leftLeg.zRot = f * stand.leftLegPose.z();
            this.rightLeg.xRot = f * stand.rightLegPose.x();
            this.rightLeg.yRot = f * stand.rightLegPose.y();
            this.rightLeg.zRot = f * stand.rightLegPose.z();
        } else {
            this.setupArmorPartAnim(state.walkAnimationPos, state.walkAnimationSpeed, state.ageInTicks, state.yRot, state.xRot);
        }
    }

    public static float sinPI(float f) { return Mth.sin(f * (float) Math.PI); }

    public static float cosPI(float f) { return Mth.cos(f * (float) Math.PI); }
}
