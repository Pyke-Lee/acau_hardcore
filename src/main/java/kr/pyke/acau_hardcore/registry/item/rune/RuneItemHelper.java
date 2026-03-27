package kr.pyke.acau_hardcore.registry.item.rune;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.rune.RuneInstance;
import kr.pyke.acau_hardcore.registry.attribute.ModAttributes;
import kr.pyke.acau_hardcore.type.RUNE_EFFECT;
import kr.pyke.acau_hardcore.type.RUNE_TYPE;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;

public class RuneItemHelper {
    private static final String RUNE_TAG = "AppliedRune";
    private static final String RUNE_MOD_PREFIX = "rune_";

    private RuneItemHelper() { }

    public static @Nullable RuneInstance getRune(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) { return null; }

        CompoundTag root = customData.copyTag();
        if (!root.contains(RUNE_TAG)) { return null; }

        return root.getCompound(RUNE_TAG)
            .map(RuneInstance::fromTag)
            .orElse(null);
    }

    public static boolean hasRune(ItemStack stack) {
        return getRune(stack) != null;
    }

    public static void setRune(ItemStack stack, RuneInstance rune) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag root = customData != null ? customData.copyTag() : new CompoundTag();
        root.put(RUNE_TAG, rune.toTag());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));

        rebuildAttributeModifiers(stack, rune);
    }

    public static void removeRune(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) { return; }

        CompoundTag root = customData.copyTag();
        root.remove(RUNE_TAG);

        if (root.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
        else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        }

        rebuildAttributeModifiers(stack, null);
    }

    public static boolean canApply(RUNE_TYPE runeType, ItemStack target) {
        for (RUNE_EFFECT effect : RUNE_EFFECT.values()) {
            if (effect.getRuneType() != runeType) { continue; }
            if (effect.getTarget().canApplyTo(target)) { return true; }
        }
        return false;
    }

    private static void rebuildAttributeModifiers(ItemStack stack, @Nullable RuneInstance rune) {
        ItemAttributeModifiers existing = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        for (ItemAttributeModifiers.Entry entry : existing.modifiers()) {
            if (!entry.modifier().id().getPath().startsWith(RUNE_MOD_PREFIX)) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }

        if (rune != null) {
            EquipmentSlotGroup slot = getSlotGroup(stack);

            switch (rune.effect()) {
                case LIFE_STEAL -> addMod(builder, ModAttributes.LIFE_STEAL, slot.name().toLowerCase() + "_life_steal", rune.value1(), slot);
                case CRIT_CHANCE -> addMod(builder, ModAttributes.CRIT_CHANCE, slot.name().toLowerCase() + "_crit_chance", rune.value1(), slot);
                case CRIT_DAMAGE -> addMod(builder, ModAttributes.CRIT_DAMAGE, slot.name().toLowerCase() + "_crit_damage", rune.value1(), slot);
                case DODGE_CHANCE -> addMod(builder, ModAttributes.DODGE_CHANCE, slot.name().toLowerCase() + "_dodge_chance", rune.value1(), slot);
                case ATTACK_DAMAGE -> addMod(builder, Attributes.ATTACK_DAMAGE, slot.name().toLowerCase() + "_attack_damage", rune.value1(), slot);
                case HEALTH_BOOST -> addMod(builder, Attributes.MAX_HEALTH, slot.name().toLowerCase() + "_health_boost", rune.value1() * 2, slot);
                default -> { }
            }
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void addMod(ItemAttributeModifiers.Builder builder, Holder<Attribute> attr, String name, float value, EquipmentSlotGroup slot) {
        builder.add(attr, new AttributeModifier(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, RUNE_MOD_PREFIX + name), value, AttributeModifier.Operation.ADD_VALUE), slot);
    }

    private static EquipmentSlotGroup getSlotGroup(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot().isArmor()) {
            return EquipmentSlotGroup.valueOf(equippable.slot().name());
        }
        return EquipmentSlotGroup.MAINHAND;
    }
}