package kr.pyke.acau_hardcore.data.randombox;

import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record BoxDefinition(String id, String displayName, int modelData, @Nullable String openSound, List<BoxReward> rewards) {
    public BoxReward roll(RandomSource random) {
        int totalWeight = getTotalWeight();
        int roll = random.nextInt(totalWeight);

        int cumulative = 0;
        for (BoxReward reward : rewards) {
            cumulative += reward.weight();
            if (roll < cumulative) { return reward; }
        }

        return rewards.getLast();
    }

    public int getTotalWeight() {
        return rewards.stream().mapToInt(BoxReward::weight).sum();
    }

    public double getChance(BoxReward reward) {
        return (reward.weight() * 100d) / getTotalWeight();
    }
}
