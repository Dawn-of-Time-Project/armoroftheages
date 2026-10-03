package org.dawnoftime.armoroftheages.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.dawnoftime.armoroftheages.client.ArmorModelProvider;
import org.dawnoftime.armoroftheages.client.models.ArmorModel;
import org.dawnoftime.armoroftheages.item.HumanoidArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class MixinHumanoidArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends RenderLayer<S, M> {

    public MixinHumanoidArmorLayer(RenderLayerParent<S, M> parentLayer) {
        super(parentLayer);
    }

    /**
     * The AotA armors have no equipment asset, so vanilla renders nothing for them.
     * Their model is submitted here, the animation is computed from the render state when the model is rendered.
     */
    @Inject(method = "submit", at = @At("HEAD"))
    private void armorOfTheAges$submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, S state, float yRot, float xRot, CallbackInfo ci) {
        this.armorOfTheAges$submitCustomArmor(poseStack, collector, packedLight, state, state.headEquipment, EquipmentSlot.HEAD);
        this.armorOfTheAges$submitCustomArmor(poseStack, collector, packedLight, state, state.chestEquipment, EquipmentSlot.CHEST);
        this.armorOfTheAges$submitCustomArmor(poseStack, collector, packedLight, state, state.legsEquipment, EquipmentSlot.LEGS);
        this.armorOfTheAges$submitCustomArmor(poseStack, collector, packedLight, state, state.feetEquipment, EquipmentSlot.FEET);
    }

    @Unique
    private void armorOfTheAges$submitCustomArmor(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, S state, ItemStack itemStack, EquipmentSlot slot) {
        if (itemStack.getItem() instanceof HumanoidArmorItem armorItem && armorItem.getArmorSlot() == slot) {
            ArmorModelProvider provider = armorItem.getModelProvider();
            if (provider != null) {
                ArmorModel model = provider.getArmorModel(state);
                collector.submitModel(model, state, poseStack, RenderTypes.armorCutoutNoCull(provider.getTexture(state)),
                        packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
                if (itemStack.hasFoil()) {
                    collector.submitModel(model, state, poseStack, RenderTypes.armorEntityGlint(),
                            packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
                }
            }
        }
    }
}
