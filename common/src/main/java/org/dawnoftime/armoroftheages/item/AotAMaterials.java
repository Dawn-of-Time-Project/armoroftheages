package org.dawnoftime.armoroftheages.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.dawnoftime.armoroftheages.config.AOTAConfig;

import java.util.Map;

import static org.dawnoftime.armoroftheages.Constants.MOD_ID;

/**
 * One material per armor set. Durability is the per set multiplier from the config (applied on next launch).
 * Repair items come from the item tags {@code armoroftheages:repairs_<set>}.
 */
public class AotAMaterials {

	private static ArmorMaterial create(String name, int durability, int helmet, int chestplate, int leggings, int boots,
			int enchantability, Holder<SoundEvent> equipSound, float toughness, float knockbackResistance) {
		Map<ArmorType, Integer> defense = Map.of(
			ArmorType.HELMET, helmet,
			ArmorType.CHESTPLATE, chestplate,
			ArmorType.LEGGINGS, leggings,
			ArmorType.BOOTS, boots
		);
		TagKey<Item> repairTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "repairs_" + name));
		ResourceKey<EquipmentAsset> assetId = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(MOD_ID, name));
		return new ArmorMaterial(durability, defense, enchantability, equipSound, toughness, knockbackResistance, repairTag, assetId);
	}

	public static final ArmorMaterial IRON_PLATE = create("iron_plate",
		AOTAConfig.get().ironPlateDurability,
		AOTAConfig.get().ironPlateHelmetDef,
		AOTAConfig.get().ironPlateChestDef,
		AOTAConfig.get().ironPlateLegsDef,
		AOTAConfig.get().ironPlateFeetDef,
		AOTAConfig.get().ironPlateEnchantability,
		SoundEvents.ARMOR_EQUIP_IRON,
		AOTAConfig.get().ironPlateToughness,
		0.0F);

	public static final ArmorMaterial HOLY = create("holy",
		AOTAConfig.get().holyDurability,
		AOTAConfig.get().holyHelmetDef,
		AOTAConfig.get().holyChestDef,
		AOTAConfig.get().holyLegsDef,
		AOTAConfig.get().holyFeetDef,
		AOTAConfig.get().holyEnchantability,
		SoundEvents.ARMOR_EQUIP_DIAMOND,
		AOTAConfig.get().holyToughness,
		AOTAConfig.get().holyKnockbackResistance);

	public static final ArmorMaterial EXALTED_AURUM = create("exalted_aurum",
		AOTAConfig.get().exaltedAurumDurability,
		AOTAConfig.get().exaltedAurumHelmetDef,
		AOTAConfig.get().exaltedAurumChestDef,
		AOTAConfig.get().exaltedAurumLegsDef,
		AOTAConfig.get().exaltedAurumFeetDef,
		AOTAConfig.get().exaltedAurumEnchantability,
		SoundEvents.ARMOR_EQUIP_IRON,
		AOTAConfig.get().exaltedAurumToughness,
		AOTAConfig.get().exaltedAurumKnockbackResistance);

	public static final ArmorMaterial JAPANESE_LIGHT = create("japanese_light",
		AOTAConfig.get().japaneseLightDurability,
		AOTAConfig.get().japaneseLightHelmetDef,
		AOTAConfig.get().japaneseLightChestDef,
		AOTAConfig.get().japaneseLightLegsDef,
		AOTAConfig.get().japaneseLightFeetDef,
		AOTAConfig.get().japaneseLightEnchantability,
		SoundEvents.ARMOR_EQUIP_LEATHER,
		AOTAConfig.get().japaneseLightToughness,
		0.0F);

	public static final ArmorMaterial O_YOROI = create("o_yoroi",
		AOTAConfig.get().oYoroiDurability,
		AOTAConfig.get().oYoroiHelmetDef,
		AOTAConfig.get().oYoroiChestDef,
		AOTAConfig.get().oYoroiLegsDef,
		AOTAConfig.get().oYoroiFeetDef,
		AOTAConfig.get().oYoroiEnchantability,
		SoundEvents.ARMOR_EQUIP_IRON,
		AOTAConfig.get().oYoroiToughness,
		0.0F);

	public static final ArmorMaterial RAIJIN = create("raijin",
		AOTAConfig.get().raijinDurability,
		AOTAConfig.get().raijinHelmetDef,
		AOTAConfig.get().raijinChestDef,
		AOTAConfig.get().raijinLegsDef,
		AOTAConfig.get().raijinFeetDef,
		AOTAConfig.get().raijinEnchantability,
		SoundEvents.ARMOR_EQUIP_LEATHER,
		AOTAConfig.get().raijinToughness,
		AOTAConfig.get().raijinKnockbackResistance);

	public static final ArmorMaterial PHARAOH = create("pharaoh",
		AOTAConfig.get().pharaohDurability,
		AOTAConfig.get().pharaohHelmetDef,
		AOTAConfig.get().pharaohChestDef,
		AOTAConfig.get().pharaohLegsDef,
		AOTAConfig.get().pharaohFeetDef,
		AOTAConfig.get().pharaohEnchantability,
		SoundEvents.ARMOR_EQUIP_GOLD,
		AOTAConfig.get().pharaohToughness,
		0.0F);

	public static final ArmorMaterial ANUBIS = create("anubis",
		AOTAConfig.get().anubisDurability,
		AOTAConfig.get().anubisHelmetDef,
		AOTAConfig.get().anubisChestDef,
		AOTAConfig.get().anubisLegsDef,
		AOTAConfig.get().anubisFeetDef,
		AOTAConfig.get().anubisEnchantability,
		SoundEvents.ARMOR_EQUIP_GOLD,
		AOTAConfig.get().anubisToughness,
		AOTAConfig.get().anubisKnockbackResistance);

	public static final ArmorMaterial CENTURION = create("centurion",
		AOTAConfig.get().centurionDurability,
		AOTAConfig.get().centurionHelmetDef,
		AOTAConfig.get().centurionChestDef,
		AOTAConfig.get().centurionLegsDef,
		AOTAConfig.get().centurionFeetDef,
		AOTAConfig.get().centurionEnchantability,
		SoundEvents.ARMOR_EQUIP_CHAIN,
		AOTAConfig.get().centurionToughness,
		0.0F);

	public static final ArmorMaterial QUETZALCOATL = create("quetzalcoatl",
		AOTAConfig.get().quetzalcoatlDurability,
		AOTAConfig.get().quetzalcoatlHelmetDef,
		AOTAConfig.get().quetzalcoatlChestDef,
		AOTAConfig.get().quetzalcoatlLegsDef,
		AOTAConfig.get().quetzalcoatlFeetDef,
		AOTAConfig.get().quetzalcoatlEnchantability,
		SoundEvents.ARMOR_EQUIP_TURTLE,
		AOTAConfig.get().quetzalcoatlToughness,
		AOTAConfig.get().quetzalcoatlKnockbackResistance);
}
