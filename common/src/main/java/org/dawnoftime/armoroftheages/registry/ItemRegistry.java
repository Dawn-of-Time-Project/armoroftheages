package org.dawnoftime.armoroftheages.registry;

import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;
import org.dawnoftime.armoroftheages.config.AOTAConfig;
import org.dawnoftime.armoroftheages.item.AotAMaterials;
import org.dawnoftime.armoroftheages.item.HatItem;

import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraft.world.item.equipment.ArmorType.*;
import static org.dawnoftime.armoroftheages.Constants.*;
import static org.dawnoftime.armoroftheages.Constants.RAIJIN_ARMOR_NAME;

public abstract class ItemRegistry {
    public static ItemRegistry REGISTRY;

    public final Supplier<Item> TAB_ICON = register(MOD_ID, Item::new);

    public ItemRegistry(){
        register(BAMBOO_HAT_NAME, properties -> new HatItem(
                properties,
                AOTAConfig.get().bambooHatDurability,
                AOTAConfig.get().bambooHatHelmetDef,
                AOTAConfig.get().bambooHatToughness,
                AOTAConfig.get().bambooHatEnchantability));
        register(ANUBIS_ARMOR_NAME, AotAMaterials.ANUBIS, HELMET);
        register(ANUBIS_ARMOR_NAME, AotAMaterials.ANUBIS, CHESTPLATE);
        register(ANUBIS_ARMOR_NAME, AotAMaterials.ANUBIS, LEGGINGS);
        register(ANUBIS_ARMOR_NAME, AotAMaterials.ANUBIS, BOOTS);
        register(CENTURION_ARMOR_NAME, AotAMaterials.CENTURION, HELMET);
        register(CENTURION_ARMOR_NAME, AotAMaterials.CENTURION, CHESTPLATE);
        register(CENTURION_ARMOR_NAME, AotAMaterials.CENTURION, LEGGINGS);
        register(CENTURION_ARMOR_NAME, AotAMaterials.CENTURION, BOOTS);
        register(EXALTED_AURUM_ARMOR_NAME, AotAMaterials.EXALTED_AURUM, HELMET);
        register(EXALTED_AURUM_ARMOR_NAME, AotAMaterials.EXALTED_AURUM, CHESTPLATE);
        register(EXALTED_AURUM_ARMOR_NAME, AotAMaterials.EXALTED_AURUM, LEGGINGS);
        register(EXALTED_AURUM_ARMOR_NAME, AotAMaterials.EXALTED_AURUM, BOOTS);
        register(HOLY_ARMOR_NAME, AotAMaterials.HOLY, HELMET);
        register(HOLY_ARMOR_NAME, AotAMaterials.HOLY, CHESTPLATE);
        register(HOLY_ARMOR_NAME, AotAMaterials.HOLY, LEGGINGS);
        register(HOLY_ARMOR_NAME, AotAMaterials.HOLY, BOOTS);
        register(IRON_PLATE_ARMOR_NAME, AotAMaterials.IRON_PLATE, HELMET);
        register(IRON_PLATE_ARMOR_NAME, AotAMaterials.IRON_PLATE, CHESTPLATE);
        register(IRON_PLATE_ARMOR_NAME, AotAMaterials.IRON_PLATE, LEGGINGS);
        register(IRON_PLATE_ARMOR_NAME, AotAMaterials.IRON_PLATE, BOOTS);
        register(JAPANESE_LIGHT_ARMOR_NAME, AotAMaterials.JAPANESE_LIGHT, HELMET);
        register(JAPANESE_LIGHT_ARMOR_NAME, AotAMaterials.JAPANESE_LIGHT, CHESTPLATE);
        register(JAPANESE_LIGHT_ARMOR_NAME, AotAMaterials.JAPANESE_LIGHT, LEGGINGS);
        register(JAPANESE_LIGHT_ARMOR_NAME, AotAMaterials.JAPANESE_LIGHT, BOOTS);
        register(O_YOROI_ARMOR_NAME, AotAMaterials.O_YOROI, HELMET);
        register(O_YOROI_ARMOR_NAME, AotAMaterials.O_YOROI, CHESTPLATE);
        register(O_YOROI_ARMOR_NAME, AotAMaterials.O_YOROI, LEGGINGS);
        register(O_YOROI_ARMOR_NAME, AotAMaterials.O_YOROI, BOOTS);
        register(PHARAOH_ARMOR_NAME, AotAMaterials.PHARAOH, HELMET);
        register(PHARAOH_ARMOR_NAME, AotAMaterials.PHARAOH, CHESTPLATE);
        register(PHARAOH_ARMOR_NAME, AotAMaterials.PHARAOH, LEGGINGS);
        register(PHARAOH_ARMOR_NAME, AotAMaterials.PHARAOH, BOOTS);
        register(QUETZALCOATL_ARMOR_NAME, AotAMaterials.QUETZALCOATL, HELMET);
        register(QUETZALCOATL_ARMOR_NAME, AotAMaterials.QUETZALCOATL, CHESTPLATE);
        register(QUETZALCOATL_ARMOR_NAME, AotAMaterials.QUETZALCOATL, LEGGINGS);
        register(QUETZALCOATL_ARMOR_NAME, AotAMaterials.QUETZALCOATL, BOOTS);
        register(RAIJIN_ARMOR_NAME, AotAMaterials.RAIJIN, HELMET);
        register(RAIJIN_ARMOR_NAME, AotAMaterials.RAIJIN, CHESTPLATE);
        register(RAIJIN_ARMOR_NAME, AotAMaterials.RAIJIN, LEGGINGS);
        register(RAIJIN_ARMOR_NAME, AotAMaterials.RAIJIN, BOOTS);
    }

    /** The factory receives properties with the item id already set, as required since 1.21.2. */
    public abstract Supplier<Item> register(final String name, final Function<Item.Properties, Item> itemFactory);

    public abstract void register(String armorSetName, ArmorMaterial material, ArmorType slot);
}
