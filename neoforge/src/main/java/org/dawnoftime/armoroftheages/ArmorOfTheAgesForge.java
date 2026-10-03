package org.dawnoftime.armoroftheages;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dawnoftime.armoroftheages.client.ArmorOfTheAgesClientForge;
import org.dawnoftime.armoroftheages.config.AOTAConfig;
import org.dawnoftime.armoroftheages.item.HumanoidArmorItem;
import org.dawnoftime.armoroftheages.loot.LootModifierProvider;
import org.dawnoftime.armoroftheages.loot.LootModifiers;
import org.dawnoftime.armoroftheages.networking.ForgeConfigSyncNetworkHandler;
import org.dawnoftime.armoroftheages.networking.NeoForgePayloadRegistry;
import org.dawnoftime.armoroftheages.patreon.PatronSyncHandler;
import org.dawnoftime.armoroftheages.registry.ItemRegistry;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.dawnoftime.armoroftheages.Constants.MOD_ID;

@Mod(MOD_ID)
public class ArmorOfTheAgesForge {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public ArmorOfTheAgesForge(IEventBus modEventBus, ModContainer modContainer) {
        // Correct Forge path: .minecraft/config/<modid>.json (DO NOT prefix with "config/")
        Constants.CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(MOD_ID + ".json");

        /*
         * ================================================================
         *  TODO: REMOVE THIS BLOCK IN A FUTURE VERSION
         *  TEMPORARY CONFIG MIGRATION FIX (Forge only)
         *
         *  Objective:
         *   - If an old file exists in ".minecraft/config/config/<modid>.json",
         *     copy it to ".minecraft/config/<modid>.json" IF IT DOESN'T ALREADY EXIST.
         *   - Delete the old file, then attempt to delete the
         *     ".minecraft/config/config" folder ONLY if it is empty.
         *
         *  Safety:
         *   - We do NOT overwrite an existing file at the new destination.
         *   - Everything is logged to help with debugging for players.
         * ================================================================
         */
        migrateForgeConfigIfNeeded();
        // ========================== END TEMPORARY FIX ==========================

        CommonClass.CONFIG_SYNC_HANDLER = new ForgeConfigSyncNetworkHandler();
        CommonClass.init();

        // Items init
        ItemRegistryImpl.REGISTRY = new ItemRegistryImpl();
        ItemRegistryImpl.DEFERRED_REGISTER.register(modEventBus);

        // Creative inventory init
        CREATIVE_MODE_TAB.register(modEventBus);
        CREATIVE_MODE_TAB.register(MOD_ID, () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + MOD_ID))
                .icon(() -> ItemRegistry.REGISTRY.TAB_ICON.get().getDefaultInstance())
                .displayItems((params, output) -> output.acceptAll(
                        ItemRegistryImpl.DEFERRED_REGISTER.getEntries().stream()
                                .filter(holder -> holder != ItemRegistry.REGISTRY.TAB_ICON)
                                .map(holder -> holder.get().getDefaultInstance())
                                .toList()))
                .build());

        LootModifiers.register(modEventBus);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(NeoForgePayloadRegistry::register);

        // Armor set effects — run at the end of each player tick, server-side only
        NeoForge.EVENT_BUS.addListener(ArmorOfTheAgesForge::onPlayerTick);

        // Send patron tier to player on login
        NeoForge.EVENT_BUS.addListener(ArmorOfTheAgesForge::onPlayerLoggedIn);

        // Client init
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, (modContainer2, parent) -> AOTAConfig.createScreen().generateScreen(parent));
            modEventBus.addListener(ArmorOfTheAgesClientForge::registerLayerDefinitions);
            NeoForge.EVENT_BUS.addListener(ArmorOfTheAgesClientForge::playerLoggedInEvent);
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        ArmorSetEffectHandler.onPlayerTick(event.getEntity());
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            PatronSyncHandler.onPlayerLogin(serverPlayer);
        }
    }

    public void gatherData(GatherDataEvent.Server event) {
        event.createProvider(LootModifierProvider::new);
    }

    // ===== TEMPORARY CONFIG MIGRATION FIX (see TODO above) =====
    private static void migrateForgeConfigIfNeeded() {
        final Path configDir = FMLPaths.CONFIGDIR.get();        // .../.minecraft/config
        final Path oldDir    = configDir.resolve("config");      // .../.minecraft/config/config
        final Path oldFile   = oldDir.resolve(MOD_ID + ".json"); // old location
        final Path newFile   = configDir.resolve(MOD_ID + ".json"); // new correct location

        try {
            if (Files.exists(oldFile)) {
                // Copy only if the new file doesn't exist
                if (!Files.exists(newFile)) {
                    try {
                        Files.createDirectories(newFile.getParent());
                        Files.copy(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);
                        LOGGER.info("[{}] Configuration migrated from '{}' to '{}'.", MOD_ID, oldFile, newFile);
                    } catch (IOException e) {
                        LOGGER.warn("[{}] Failed to copy old configuration '{}' to '{}': {}",
                                MOD_ID, oldFile, newFile, e.getMessage());
                    }
                } else {
                    LOGGER.info("[{}] New configuration file already present: '{}'. " +
                            "The old one will be cleaned up if possible.", MOD_ID, newFile);
                }

                // Delete old file (best-effort)
                try {
                    Files.deleteIfExists(oldFile);
                } catch (IOException e) {
                    LOGGER.warn("[{}] Failed to delete old configuration file '{}': {}",
                            MOD_ID, oldFile, e.getMessage());
                }

                // Try to delete the '.../config/config' folder if it's empty
                try {
                    if (Files.isDirectory(oldDir)) {
                        try (DirectoryStream<Path> ds = Files.newDirectoryStream(oldDir)) {
                            Iterator<Path> it = ds.iterator();
                            if (!it.hasNext()) {
                                Files.delete(oldDir);
                                LOGGER.info("[{}] Old folder '{}' deleted (was empty).", MOD_ID, oldDir);
                            } else {
                                LOGGER.info("[{}] Old folder '{}' kept (contents detected).", MOD_ID, oldDir);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.warn("[{}] Failed to clean up old folder '{}': {}",
                            MOD_ID, oldDir, e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[{}] Problem during configuration migration: {}", MOD_ID, e.getMessage());
        }
    }
    // ===================== END TEMPORARY MIGRATION FIX =====================

    public static class ItemRegistryImpl extends ItemRegistry {
        public static final DeferredRegister.Items DEFERRED_REGISTER = DeferredRegister.createItems(MOD_ID);
        public static final Map<String, List<Identifier>> ARMORS_LOCATION_FROM_NAME = new Object2ObjectOpenHashMap<>();

        @Override
        public void register(String armorSetName, ArmorMaterial material, ArmorType slot) {
            ARMORS_LOCATION_FROM_NAME
                    .computeIfAbsent(armorSetName, s -> new ObjectArrayList<>())
                    .add(Identifier.fromNamespaceAndPath(MOD_ID, armorSetName + "_" + slot.getSlot().getName()));
            DEFERRED_REGISTER.registerItem(armorSetName + "_" + slot.getSlot().getName(),
                    properties -> new HumanoidArmorItem(properties, armorSetName, material, slot));
        }

        @Override
        public Supplier<Item> register(String name, Function<Item.Properties, Item> itemFactory) {
            ARMORS_LOCATION_FROM_NAME
                    .computeIfAbsent(name, s -> new ObjectArrayList<>())
                    .add(Identifier.fromNamespaceAndPath(MOD_ID, name));
            return DEFERRED_REGISTER.registerItem(name, itemFactory);
        }
    }
}
