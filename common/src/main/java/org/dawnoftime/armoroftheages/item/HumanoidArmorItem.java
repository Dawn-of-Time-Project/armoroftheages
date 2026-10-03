package org.dawnoftime.armoroftheages.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import org.dawnoftime.armoroftheages.CommonClass;
import org.dawnoftime.armoroftheages.client.ArmorModelProvider;
import org.dawnoftime.armoroftheages.registry.ModelProviderRegistry;
import org.dawnoftime.armoroftheages.setbonus.SetBonus;
import org.dawnoftime.armoroftheages.setbonus.SetBonusRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static org.dawnoftime.armoroftheages.Constants.MOD_ID;

public class HumanoidArmorItem extends Item {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private final String armorSetName;
    private final String armorPartName;
    private final ArmorMaterial material;
    private final ArmorType armorType;

    private static Rarity deriveRarity(ArmorMaterial material) {
        if (material.knockbackResistance() > 0f) return Rarity.EPIC;
        if (material.toughness() >= 2f) return Rarity.RARE;
        if (material.defense().getOrDefault(ArmorType.HELMET, 0) >= 2) return Rarity.UNCOMMON;
        return Rarity.COMMON;
    }

    /**
     * The equippable component needs an asset id, otherwise vanilla does not put the piece in the render state
     * (the mixin would see empty slots) and draws the item icon on the head instead.
     * No equipment json exists for this id, so vanilla renders no layer and the model is rendered by
     * {@link org.dawnoftime.armoroftheages.mixin.MixinHumanoidArmorLayer}.
     */
    public HumanoidArmorItem(Properties properties, @NotNull String armorSetName, ArmorMaterial material, ArmorType type) {
        super(properties
                .stacksTo(1)
                .durability(type.getDurability(material.durability()))
                .rarity(deriveRarity(material))
                .attributes(material.createAttributes(type))
                .enchantable(material.enchantmentValue())
                .repairable(material.repairIngredient())
                .component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot()).setEquipSound(material.equipSound()).setAsset(material.assetId()).build()));
        this.armorSetName = armorSetName;
        this.material = material;
        this.armorType = type;
        this.armorPartName = armorSetName + "_" + type.getSlot().getName();
    }

    public EquipmentSlot getArmorSlot() {
        return this.armorType.getSlot();
    }

    public @Nullable ArmorModelProvider getModelProvider() {
        return ModelProviderRegistry.REGISTRY.get(this.armorPartName);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipComponents, tooltipFlag);
        for (Holder<Item> repairItem : BuiltInRegistries.ITEM.getTagOrEmpty(this.material.repairIngredient())) {
            MutableComponent text = Component.translatable("tooltip." + MOD_ID + ".repair_with").withStyle(ChatFormatting.GRAY)
                    .append(repairItem.value().getDefaultInstance().getHoverName().plainCopy().withStyle(ChatFormatting.YELLOW));
            tooltipComponents.accept(text);
            break;
        }

        SetBonusRegistry.get(this.armorSetName).ifPresent(bonus -> {
            tooltipComponents.accept(Component.empty());

            Player player = CommonClass.LOCAL_PLAYER_SUPPLIER.get();

            for (EquipmentSlot slot : ARMOR_SLOTS) {
                boolean equipped = isSlotEquippedWithSet(player, slot);
                tooltipComponents.accept(Component.translatable("set_bonus." + MOD_ID + ".slot." + slot.getName())
                        .withStyle(equipped ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY));
            }

            boolean fullSetEquipped = player != null
                    && isSlotEquippedWithSet(player, EquipmentSlot.HEAD)
                    && isSlotEquippedWithSet(player, EquipmentSlot.CHEST)
                    && isSlotEquippedWithSet(player, EquipmentSlot.LEGS)
                    && isSlotEquippedWithSet(player, EquipmentSlot.FEET);
            ChatFormatting nameColor = fullSetEquipped ? bonus.nameColor() : ChatFormatting.DARK_GRAY;
            MutableComponent bonusName = Component.translatable(bonus.nameTranslationKey()).withStyle(nameColor);
            tooltipComponents.accept(Component.translatable("set_bonus." + MOD_ID + ".unlock", bonusName)
                    .withStyle(ChatFormatting.GRAY));

            if (bonus.descriptionTranslationKey() != null) {
                tooltipComponents.accept(Component.translatable(bonus.descriptionTranslationKey())
                        .withStyle(fullSetEquipped ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY));
            }
        });
    }

    private boolean isSlotEquippedWithSet(@Nullable Player player, EquipmentSlot slot) {
        if (player == null) return false;
        ItemStack equipped = player.getItemBySlot(slot);
        if (equipped.isEmpty()) return false;
        Identifier key = BuiltInRegistries.ITEM.getKey(equipped.getItem());
        return MOD_ID.equals(key.getNamespace())
                && (armorSetName + "_" + slot.getName()).equals(key.getPath());
    }
}
