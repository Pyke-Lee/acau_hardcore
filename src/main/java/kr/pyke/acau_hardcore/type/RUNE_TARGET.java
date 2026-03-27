package kr.pyke.acau_hardcore.type;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public enum RUNE_TARGET {
    GENERAL_LIFE("general_life"),
    PICKAXE("pickaxe"),
    HOE("hoe"),
    SHOVEL("shovel"),
    AXE("axe"),
    FISHING("fishing"),
    GENERAL_COMBAT("general_combat"),
    WEAPON("weapon"),
    ARMOR("armor");

    private final String key;

    RUNE_TARGET(String key) { this.key = key; }

    public boolean canApplyTo(ItemStack stack) {
        return switch (this) {
            case GENERAL_LIFE, GENERAL_COMBAT -> stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || isArmor(stack) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES) || stack.is(ItemTags.FISHING_ENCHANTABLE);
            case PICKAXE -> stack.is(ItemTags.PICKAXES);
            case HOE -> stack.is(ItemTags.HOES);
            case SHOVEL -> stack.is(ItemTags.SHOVELS);
            case AXE -> stack.is(ItemTags.AXES);
            case FISHING -> stack.is(ItemTags.FISHING_ENCHANTABLE);
            case WEAPON -> stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES);
            case ARMOR -> isArmor(stack);
        };
    }

    private static boolean isArmor(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().isArmor();
    }

    public static RUNE_TARGET byKey(String key) {
        for (RUNE_TARGET target : values()) {
            if (target.key.equals(key)) {
                return target;
            }
        }
        return GENERAL_LIFE;
    }
}