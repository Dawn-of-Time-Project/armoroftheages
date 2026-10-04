package org.dawnoftime.armoroftheages;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.dawnoftime.armoroftheages.item.HumanoidArmorItem;
import org.dawnoftime.armoroftheages.loot.AmorOfTheAgesLootModifiersFabric;
import org.dawnoftime.armoroftheages.networking.FabricConfigSyncNetworkHandler;
import org.dawnoftime.armoroftheages.patreon.PatronSyncHandler;
import org.dawnoftime.armoroftheages.registry.ItemRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.dawnoftime.armoroftheages.Constants.MOD_ID;

public class ArmorOfTheAgesFabric implements ModInitializer {

    private static final CreativeModeTab CREATIVE_MODE_TAB = FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MOD_ID))
            .icon(() -> ItemRegistry.REGISTRY.TAB_ICON.get().getDefaultInstance())
            .displayItems((params, output) -> output.acceptAll(ItemRegistryImpl.ITEMS.stream().filter(item -> item != ItemRegistry.REGISTRY.TAB_ICON.get()).map(Item::getDefaultInstance).toList()))
            .build();

    @Override
    public void onInitialize() {
        Constants.CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
        CommonClass.CONFIG_SYNC_HANDLER = new FabricConfigSyncNetworkHandler();
        CommonClass.init();

        // Items init
        ItemRegistryImpl.REGISTRY = new ItemRegistryImpl();

        // Creative inventory init
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, MOD_ID), CREATIVE_MODE_TAB);
        AmorOfTheAgesLootModifiersFabric.modifyLootTables();

        // Armor set effects — iterate all online players at the end of each server tick
        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getPlayerList().getPlayers().forEach(ArmorSetEffectHandler::onPlayerTick));

        // Send patron tier to player on login
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                PatronSyncHandler.onPlayerLogin(handler.player));
    }

    public static class ItemRegistryImpl extends ItemRegistry {
        public static final List<Item> ITEMS = new ArrayList<>();
        public static final Map<String, List<Identifier>> ARMORS_LOCATION_FROM_NAME = new Object2ObjectOpenHashMap<>();

        @Override
        public void register(String armorSetName, ArmorMaterial material, ArmorType slot) {
            Identifier armorLocation = Identifier.fromNamespaceAndPath(MOD_ID, armorSetName + "_" + slot.getSlot().getName());
            Item item = new HumanoidArmorItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, armorLocation)), armorSetName, material, slot);
            Registry.register(BuiltInRegistries.ITEM, armorLocation, item);
            ARMORS_LOCATION_FROM_NAME.computeIfAbsent(armorSetName, s -> new ObjectArrayList<>()).add(armorLocation);
            ITEMS.add(item);
        }

        @Override
        public Supplier<Item> register(String name, Function<Item.Properties, Item> itemFactory) {
            Identifier location = Identifier.fromNamespaceAndPath(MOD_ID, name);
            Item item = itemFactory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, location)));
            Registry.register(BuiltInRegistries.ITEM, location, item);
            ARMORS_LOCATION_FROM_NAME.computeIfAbsent(name, s -> new ObjectArrayList<>()).add(location);
            ITEMS.add(item);
            return () -> item;
        }
    }
}
