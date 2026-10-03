package org.dawnoftime.armoroftheages.compat.epicfight;

import net.neoforged.fml.ModList;

/**
 * Entry point of the Epic Fight compatibility. This class must never reference an Epic Fight class directly,
 * so it can be safely loaded when Epic Fight is not installed.
 */
public final class EpicFightCompat {
    private static final String EPIC_FIGHT_MOD_ID = "epicfight";

    private EpicFightCompat() {
    }

    /**
     * Registers our armor mesh transformer in Epic Fight. Must be called on the client, after the mod loading phase.
     */
    public static void init() {
        if (ModList.get().isLoaded(EPIC_FIGHT_MOD_ID)) {
            EpicFightArmorMeshes.register();
        }
    }
}
