package org.dawnoftime.armoroftheages.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.Minecraft;
import org.dawnoftime.armoroftheages.CommonClass;
import org.dawnoftime.armoroftheages.registry.ModelProviderRegistry;

public class ArmorOfTheAgesClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CommonClass.LOCAL_PLAYER_SUPPLIER = () -> Minecraft.getInstance().player;
        ArmorOfTheAgesClientFabric.registerLayerDefinitions();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            CommonClass.CONFIG_SYNC_HANDLER.syncConfig();
        });
    }

    /**
     * Registers the LayerDefinitions. Must be client side only !
     */
    public static void registerLayerDefinitions() {
        ModelProviderRegistry.REGISTRY.forEach((name, provider) -> {
            ModelLayerRegistry.registerModelLayer(provider.getLayerLocation(), provider::createLayer);
            if(provider instanceof ArmorModelProvider.MixedArmorModelProvider slimProvide){
                ModelLayerRegistry.registerModelLayer(slimProvide.getSlimLayerLocation(), slimProvide::createSlimLayer);
            }
        });
    }
}
