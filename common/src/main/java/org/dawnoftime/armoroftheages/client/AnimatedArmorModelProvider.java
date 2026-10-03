package org.dawnoftime.armoroftheages.client;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.dawnoftime.armoroftheages.Constants.MOD_ID;

// Client side
public class AnimatedArmorModelProvider extends ArmorModelProvider {
    public static ArmorModelProvider create(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier, int numberOfFrames, int ticksPerFrame){
        return new AnimatedArmorModelProvider(armorName, slot, modelSupplier, layerDefinitionSupplier, numberOfFrames, ticksPerFrame);
    }

    public static ArmorModelProvider create(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier, Supplier<LayerDefinition> slimLayerDefinitionSupplier, int numberOfFrames, int ticksPerFrame){
        return new AnimatedMixedArmorModelProvider(armorName, slot, modelSupplier, layerDefinitionSupplier, slimLayerDefinitionSupplier, numberOfFrames, ticksPerFrame);
    }

    private final Identifier[] resourceLocations;
    private final int ticksPerFrame;

    private AnimatedArmorModelProvider(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier, int numberOfFrames, int ticksPerFrame){
        super(armorName, slot, modelSupplier, layerDefinitionSupplier);
        this.resourceLocations = IntStream.range(0, numberOfFrames)
                .mapToObj(n -> Identifier.fromNamespaceAndPath(MOD_ID, "textures/models/armor/" + armorName + "_" + (n + 1) + ".png"))
                .toArray(Identifier[]::new);
        this.ticksPerFrame = ticksPerFrame;
    }

    @NotNull
    public Identifier getTexture(HumanoidRenderState state) {
        return this.resourceLocations[((int) state.ageInTicks / this.ticksPerFrame) % this.resourceLocations.length];
    }

    public static class AnimatedMixedArmorModelProvider extends MixedArmorModelProvider {
        private final Identifier[] resourceLocations;
        private final Identifier[] slimResourceLocations;
        private final int ticksPerFrame;

        private AnimatedMixedArmorModelProvider(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier, Supplier<LayerDefinition> slimLayerDefinitionSupplier, int numberOfFrames, int ticksPerFrame){
            super(armorName, slot, modelSupplier, layerDefinitionSupplier, slimLayerDefinitionSupplier);
            this.resourceLocations = IntStream.range(0, numberOfFrames)
                    .mapToObj(n -> Identifier.fromNamespaceAndPath(MOD_ID, "textures/models/armor/" + armorName + "_" + (n + 1) + ".png"))
                    .toArray(Identifier[]::new);
            this.slimResourceLocations = IntStream.range(0, numberOfFrames)
                    .mapToObj(n -> Identifier.fromNamespaceAndPath(MOD_ID, "textures/models/armor/" + armorName + "_slim_" + (n + 1) + ".png"))
                    .toArray(Identifier[]::new);
            this.ticksPerFrame = ticksPerFrame;
        }

        @Override
        public @NotNull Identifier getTexture(HumanoidRenderState state) {
            if(isSlim(state)){
                return this.slimResourceLocations[((int) state.ageInTicks / this.ticksPerFrame) % this.slimResourceLocations.length];
            }else{
                return this.resourceLocations[((int) state.ageInTicks / this.ticksPerFrame) % this.resourceLocations.length];
            }
        }
    }
}