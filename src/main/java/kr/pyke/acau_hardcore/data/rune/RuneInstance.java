package kr.pyke.acau_hardcore.data.rune;

import kr.pyke.acau_hardcore.type.RUNE_EFFECT;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public record RuneInstance(RUNE_EFFECT effect, float value1, float value2) {
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("effect", effect.getKey());
        tag.putFloat("v1", value1);
        tag.putFloat("v2", value2);
        return tag;
    }

    public static @Nullable RuneInstance fromTag(CompoundTag tag) {
        RUNE_EFFECT effect = RUNE_EFFECT.byKey(tag.getString("effect").orElse(""));
        if (effect == null) { return null; }

        float v1 = tag.getFloat("v1").orElse(0f);
        float v2 = tag.getFloat("v2").orElse(0f);
        return new RuneInstance(effect, v1, v2);
    }

    public String formatDescription() {
        return switch (effect) {
            case SMELT_ORE, SAND_TO_GLASS -> effect.getDescriptionFormat();
            case VEIN_MINE -> String.format(effect.getDescriptionFormat(), (int) value1 == 1 ? "상·하" : "상·하·좌·우");
            case LIFE_STEAL, CRIT_CHANCE, CRIT_DAMAGE, DODGE_CHANCE, ATTACK_DAMAGE, HEALTH_BOOST, SATURATION_ON_HIT, TREE_FELL, ORE_BONUS -> String.format(effect.getDescriptionFormat(), value1);
            default -> String.format(effect.getDescriptionFormat(), value1, value2);
        };
    }
}