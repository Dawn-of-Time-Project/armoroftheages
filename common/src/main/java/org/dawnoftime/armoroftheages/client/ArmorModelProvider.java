package org.dawnoftime.armoroftheages.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import org.dawnoftime.armoroftheages.CommonClass;
import org.dawnoftime.armoroftheages.client.models.ArmorModel;
import org.dawnoftime.armoroftheages.config.AOTAConfig;
import org.dawnoftime.armoroftheages.config.PreferredModel;
import org.dawnoftime.armoroftheages.config.SkinSyncState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.dawnoftime.armoroftheages.Constants.MOD_ID;

// Client side
public class ArmorModelProvider {
    protected static final Identifier PLAYER_RESOURCE_LOCATION = Identifier.withDefaultNamespace("player");

    public interface SkinVariant {
        String getTexturePrefix();
    }

    public static ArmorModelProvider create(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier){
        return new ArmorModelProvider(armorName, slot, modelSupplier, layerDefinitionSupplier);
    }

    public static ArmorModelProvider create(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier, Supplier<LayerDefinition> slimLayerDefinitionSupplier){
        return new MixedArmorModelProvider(armorName, slot, modelSupplier, layerDefinitionSupplier, slimLayerDefinitionSupplier);
    }

    public static <E extends Enum<E> & SkinVariant> ArmorModelProvider create(
            String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier,
            Supplier<LayerDefinition> layerDefinitionSupplier, Supplier<LayerDefinition> slimLayerDefinitionSupplier,
            Supplier<E> skinSupplier, Class<E> enumClass, Function<SkinSyncState, E> syncStateExtractor) {
        return new SkinnedMixedArmorModelProvider<>(armorName, slot, modelSupplier, layerDefinitionSupplier, slimLayerDefinitionSupplier, skinSupplier, enumClass, syncStateExtractor);
    }

    private final Supplier<LayerDefinition> layerDefinitionSupplier;
    protected final ArmorModelSupplier modelSupplier;
    private ArmorModel armorModel;
    private final ModelLayerLocation modelLayerLocation;
    private final Identifier resourceLocation;

    protected ArmorModelProvider(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier){
        this.layerDefinitionSupplier = layerDefinitionSupplier;
        this.modelSupplier = modelSupplier;
        this.modelLayerLocation = new ModelLayerLocation(PLAYER_RESOURCE_LOCATION, armorName + "_" + slot.name().toLowerCase());
        this.resourceLocation = Identifier.fromNamespaceAndPath(MOD_ID, "textures/models/armor/" + armorName + ".png");
    }

    @NotNull
    public Identifier getTexture(HumanoidRenderState state) {
        return this.resourceLocation;
    }

    @NotNull
    public ModelLayerLocation getLayerLocation() {
        return this.modelLayerLocation;
    }

    public LayerDefinition createLayer(){
        return this.layerDefinitionSupplier.get();
    }

    /**
     * The render state does not hold the entity, so it is found back from the entity id of the avatar state.
     * Only players have an avatar state, other humanoids (zombies, armor stands...) have no custom skin.
     */
    @Nullable
    protected static Entity entityOf(HumanoidRenderState state) {
        if (state instanceof AvatarRenderState avatar && Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.getEntity(avatar.id);
        }
        return null;
    }

    public static boolean isSlim(HumanoidRenderState state) {
        if (state instanceof AvatarRenderState avatar) {
            Entity entity = entityOf(state);
            if (entity != null && entity == Minecraft.getInstance().player) {
                return AOTAConfig.get().preferredModel == PreferredModel.FEMALE;
            }
            if (entity != null && CommonClass.CURRENT_PREFERRED_MODEL_MAP.containsKey(entity.getUUID())) {
                return CommonClass.CURRENT_PREFERRED_MODEL_MAP.get(entity.getUUID()) == PreferredModel.FEMALE;
            }
            return avatar.skin.model() == PlayerModelType.SLIM;
        }
        return false;
    }

    public ArmorModel getArmorModel(HumanoidRenderState state) {
        if(this.armorModel == null){
            this.armorModel = this.modelSupplier.create(Minecraft.getInstance().getEntityModels().bakeLayer(this.modelLayerLocation), false);
        }
        return this.armorModel;
    }

    public static class SkinnedMixedArmorModelProvider<E extends Enum<E> & SkinVariant> extends MixedArmorModelProvider {
        private final java.util.Map<E, Identifier> skinTextures;
        private final java.util.Map<E, Identifier> slimSkinTextures;
        private final Supplier<E> skinSupplier;
        private final Function<SkinSyncState, E> syncStateExtractor;

        protected SkinnedMixedArmorModelProvider(
                String armorName,
                EquipmentSlot slot,
                ArmorModelSupplier modelSupplier,
                Supplier<LayerDefinition> layerDefinitionSupplier,
                Supplier<LayerDefinition> slimLayerDefinitionSupplier,
                Supplier<E> skinSupplier,
                Class<E> enumClass,
                Function<SkinSyncState, E> syncStateExtractor) {
            super(armorName, slot, modelSupplier, layerDefinitionSupplier, slimLayerDefinitionSupplier);
            this.skinSupplier = skinSupplier;
            this.syncStateExtractor = syncStateExtractor;
            this.skinTextures = new java.util.EnumMap<>(enumClass);
            this.slimSkinTextures = new java.util.EnumMap<>(enumClass);
            for (E skin : enumClass.getEnumConstants()) {
                String prefix = skin.getTexturePrefix();
                this.skinTextures.put(skin, Identifier.fromNamespaceAndPath(MOD_ID, "textures/models/armor/" + prefix + armorName + ".png"));
                this.slimSkinTextures.put(skin, Identifier.fromNamespaceAndPath(MOD_ID, "textures/models/armor/" + prefix + armorName + "_slim.png"));
            }
        }

        @Override
        public @NotNull Identifier getTexture(HumanoidRenderState state) {
            E skin;
            Entity entity = entityOf(state);
            if (entity == null || entity == Minecraft.getInstance().player) {
                skin = skinSupplier.get();
            } else {
                SkinSyncState syncState = CommonClass.CURRENT_SKIN_MAP.get(entity.getUUID());
                skin = (syncState != null) ? syncStateExtractor.apply(syncState) : skinSupplier.get();
            }
            return isSlim(state) ? slimSkinTextures.get(skin) : skinTextures.get(skin);
        }
    }

    public static class MixedArmorModelProvider extends ArmorModelProvider{
        private final Supplier<LayerDefinition> slimLayerDefinitionSupplier;
        private final ModelLayerLocation slimModelLayerLocation;
        private final Identifier slimResourceLocation;
        private ArmorModel slimArmorModel;

        protected MixedArmorModelProvider(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier, Supplier<LayerDefinition> slimLayerDefinitionSupplier){
            super(armorName, slot, modelSupplier, layerDefinitionSupplier);
            this.slimLayerDefinitionSupplier = slimLayerDefinitionSupplier;
            this.slimModelLayerLocation = new ModelLayerLocation(PLAYER_RESOURCE_LOCATION, armorName + "_" + slot.name().toLowerCase() + "_slim");
            this.slimResourceLocation = Identifier.fromNamespaceAndPath(MOD_ID, "textures/models/armor/" + armorName + "_slim.png");
        }

        @NotNull
        public ModelLayerLocation getSlimLayerLocation() {
            return this.slimModelLayerLocation;
        }

        public LayerDefinition createSlimLayer(){
            return this.slimLayerDefinitionSupplier.get();
        }

        @Override
        public @NotNull Identifier getTexture(HumanoidRenderState state) {
            return isSlim(state) ? this.slimResourceLocation : super.getTexture(state);
        }

        @Override
        public ArmorModel getArmorModel(HumanoidRenderState state) {
            if(ArmorModelProvider.isSlim(state)){
                if(this.slimArmorModel == null){
                    this.slimArmorModel = this.modelSupplier.create(Minecraft.getInstance().getEntityModels().bakeLayer(this.slimModelLayerLocation), true);
                }
                return this.slimArmorModel;
            }else{
                return super.getArmorModel(state);
            }
        }
    }
}