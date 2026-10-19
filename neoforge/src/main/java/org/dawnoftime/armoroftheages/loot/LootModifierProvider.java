package org.dawnoftime.armoroftheages.loot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.dawnoftime.armoroftheages.Constants;

import java.util.concurrent.CompletableFuture;

public class LootModifierProvider extends GlobalLootModifierProvider {

    public LootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Constants.MOD_ID);
    }

    @Override
    protected void start() {
        add("bamboo_hat_in_mineshaft",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.ABANDONED_MINESHAFT)),
                                LootItemRandomChanceCondition.randomChance(0.35f)
                        ),
                        Constants.BAMBOO_HAT_NAME,
                        1.0f
                )
        );

        add("bamboo_hat_in_shipwreck_supply",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.SHIPWRECK_SUPPLY)),
                                LootItemRandomChanceCondition.randomChance(0.35f)
                        ),
                        Constants.BAMBOO_HAT_NAME,
                        1.0f
                )
        );

        add("bamboo_hat_in_village_taiga_house",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.VILLAGE_TAIGA_HOUSE)),
                                LootItemRandomChanceCondition.randomChance(0.35f)
                        ),
                        Constants.BAMBOO_HAT_NAME,
                        1.0f
                )
        );

        add("japanese_light_in_simple_dungeon",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.SIMPLE_DUNGEON)),
                                LootItemRandomChanceCondition.randomChance(0.5f)
                        ),
                        Constants.JAPANESE_LIGHT_ARMOR_NAME,
                        0.5f
                )
        );

        add("iron_plate_in_simple_dungeon",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.SIMPLE_DUNGEON)),
                                LootItemRandomChanceCondition.randomChance(0.35f)
                        ),
                        Constants.IRON_PLATE_ARMOR_NAME,
                        0.25f
                )
        );

        add("japanese_light_in_village_weaponsmith",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.VILLAGE_WEAPONSMITH)),
                                LootItemRandomChanceCondition.randomChance(1.0f)
                        ),
                        Constants.JAPANESE_LIGHT_ARMOR_NAME,
                        0.7f
                )
        );

        add("centurion_in_ruined_portal",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.RUINED_PORTAL)),
                                LootItemRandomChanceCondition.randomChance(1.0f)
                        ),
                        Constants.CENTURION_ARMOR_NAME,
                        0.15f
                )
        );

        add("iron_plate_in_village_armorer",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.VILLAGE_ARMORER)),
                                LootItemRandomChanceCondition.randomChance(0.5f)
                        ),
                        Constants.IRON_PLATE_ARMOR_NAME,
                        0.15f
                )
        );

        add("o_yoroi_in_shipwreck_treasure",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.SHIPWRECK_TREASURE)),
                                LootItemRandomChanceCondition.randomChance(1.0f)
                        ),
                        Constants.O_YOROI_ARMOR_NAME,
                        0.15f
                )
        );

        add("pharaoh_in_desert_pyramid",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.DESERT_PYRAMID)),
                                LootItemRandomChanceCondition.randomChance(0.7f)
                        ),
                        Constants.PHARAOH_ARMOR_NAME,
                        0.15f
                )
        );

        add("exalted_aurum_in_ancient_city",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.ANCIENT_CITY)),
                                LootItemRandomChanceCondition.randomChance(0.35f)
                        ),
                        Constants.EXALTED_AURUM_ARMOR_NAME,
                        0.7f
                )
        );

        add("holy_in_village_temple",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.VILLAGE_TEMPLE)),
                                LootItemRandomChanceCondition.randomChance(0.5f)
                        ),
                        Constants.HOLY_ARMOR_NAME,
                        0.15f
                )
        );

        add("quetzalcoatl_in_jungle_temple",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.JUNGLE_TEMPLE)),
                                LootItemRandomChanceCondition.randomChance(1.0f)
                        ),
                        Constants.QUETZALCOATL_ARMOR_NAME,
                        0.15f
                )
        );

        add("raijin_in_bastion_treasure",
                new ArmorOfTheAgesLootModifier(
                        ArmorOfTheAgesLootModifier.allOf(
                                LootTableIdCondition.builder(Identifier.parse(LootTablesToModify.BASTION_TREASURE)),
                                LootItemRandomChanceCondition.randomChance(1.0f)
                        ),
                        Constants.RAIJIN_ARMOR_NAME,
                        0.7f
                )
        );
    }
}