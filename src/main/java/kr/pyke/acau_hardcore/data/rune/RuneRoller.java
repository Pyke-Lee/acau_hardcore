package kr.pyke.acau_hardcore.data.rune;

import kr.pyke.acau_hardcore.config.RuneConfig;
import kr.pyke.acau_hardcore.type.RUNE_EFFECT;
import kr.pyke.acau_hardcore.type.RUNE_TYPE;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class RuneRoller {
    private RuneRoller() { }

    public static @Nullable RuneInstance roll(RUNE_TYPE runeType, ItemStack targetItem, RandomSource random) {
        List<RUNE_EFFECT> candidates = new ArrayList<>();

        for (RUNE_EFFECT effect : RUNE_EFFECT.values()) {
            if (effect.getRuneType() != runeType) { continue; }
            if (!effect.getTarget().canApplyTo(targetItem)) { continue; }
            candidates.add(effect);
        }

        if (candidates.isEmpty()) { return null; }

        int totalWeight = 0;
        for (RUNE_EFFECT effect : candidates) {
            if (effect.isRare()) {
                totalWeight += 1;
            }
            else {
                totalWeight += 3;
            }
        }

        int randomWeight = random.nextInt(totalWeight);
        RUNE_EFFECT chosen = candidates.getFirst();

        for (RUNE_EFFECT effect : candidates) {
            int weight = 3;
            if (effect.isRare()) {
                weight = 1;
            }

            if (randomWeight < weight) {
                chosen = effect;
                break;
            }
            randomWeight -= weight;
        }

        return rollSpecific(chosen, random);
    }

    public static @NonNull RuneInstance rollSpecific(RUNE_EFFECT chosen, RandomSource random) {
        float[] range = RuneConfig.getRange(chosen.getKey());

        float v1 = rollValue(random, range[0], range[1], getStep(chosen, 1));
        float v2 = rollValue(random, range[2], range[3], getStep(chosen, 2));

        return new RuneInstance(chosen, v1, v2);
    }

    private static float rollValue(RandomSource random, float min, float max, float step) {
        if (min == 0f && max == 0f) { return 0f; }
        if (step <= 0f) { return min + random.nextFloat() * (max - min); }

        int steps = Math.round((max - min) / step);
        if (steps <= 0) { return min; }

        return min + random.nextInt(steps + 1) * step;
    }

    private static float getStep(RUNE_EFFECT effect, int vi) {
        return switch (effect) {
            case LIFE_STEAL, DODGE_CHANCE, ATTACK_DAMAGE, SATURATION_ON_HIT -> vi == 1 ? 0.1f : 0f;
            case CRIT_CHANCE, CRIT_DAMAGE -> vi == 1 ? 0.5f : 0f;
            case LOW_HP_ATTACK, ORE_BONUS -> 0.1f;
            case EXECUTE -> vi == 1 ? 0.1f : 1f;
            case HEALTH_BOOST -> vi == 1 ? 0.5f : 0f;
            case VEIN_MINE, TREE_FELL -> vi == 1 ? 1f : 0f;
            default -> 1f;
        };
    }
}