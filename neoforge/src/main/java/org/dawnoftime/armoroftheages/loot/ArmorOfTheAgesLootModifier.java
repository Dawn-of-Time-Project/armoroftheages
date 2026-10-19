package org.dawnoftime.armoroftheages.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.dawnoftime.armoroftheages.ArmorOfTheAgesForge;
import org.dawnoftime.armoroftheages.config.AOTAConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

public class ArmorOfTheAgesLootModifier extends LootModifier {
    public static final Supplier<MapCodec<ArmorOfTheAgesLootModifier>> CODEC = Suppliers.memoize(
            () -> RecordCodecBuilder.mapCodec(
                    instance -> codecStart(instance)
                            .and(Codec.STRING.fieldOf("armor_name").forGetter(m -> m.armorSetName))
                            .and(Codec.FLOAT.fieldOf("armor_state").forGetter(m -> m.state))
                            .apply(instance, ArmorOfTheAgesLootModifier::new)
            )
    );


    public static Optional<Holder<LootItemCondition>> allOf(LootItemCondition.Builder... terms) {
        return Optional.of(Holder.direct(AllOfCondition.allOf(terms).build()));
    }

    private final String armorSetName;
    private final float state;
    private final Random RANDOM = new Random();

    public ArmorOfTheAgesLootModifier(Optional<Holder<LootItemCondition>> conditionsIn, String armorSetName, float state) {
        this(conditionsIn, IGlobalLootModifier.DEFAULT_PRIORITY, armorSetName, state);
    }

    public ArmorOfTheAgesLootModifier(Optional<Holder<LootItemCondition>> conditionsIn, int priority, String armorSetName, float state) {
        super(conditionsIn, priority);
        this.armorSetName = armorSetName;
        this.state = state;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext lootContext) {
        boolean shouldGenerate = LootTablesToModify.ARMOR_GENERATION_MAP.getOrDefault(armorSetName, false);

        if (AOTAConfig.get().generateArmorLoot && shouldGenerate) {
            List<Identifier> armorPieceLocations = ArmorOfTheAgesForge.ItemRegistryImpl
                    .ARMORS_LOCATION_FROM_NAME
                    .get(armorSetName);

            Item armorPieceItem = BuiltInRegistries.ITEM.getValue(armorPieceLocations.get(RANDOM.nextInt(armorPieceLocations.size())));
            ItemStack armorPieceItemStack = new ItemStack(armorPieceItem);

            generatedLoot.add(addDamage(armorPieceItemStack, lootContext));
        }

        return generatedLoot;
    }

    public ItemStack addDamage(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.isDamageableItem()) {
            int i = itemStack.getMaxDamage();
            float f = 0.0F;
            float g = 1.0F - Mth.clamp(state + f, 0.0F, 1.0F);
            itemStack.setDamageValue(Mth.floor(g * (float)i));
        }

        return itemStack;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
