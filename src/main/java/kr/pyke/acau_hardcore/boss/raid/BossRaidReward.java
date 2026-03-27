package kr.pyke.acau_hardcore.boss.raid;

import kr.pyke.acau_hardcore.type.BOSS_RAID_TYPE;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class BossRaidReward {
    private final List<ItemStack> items;
    private final long currency;

    public BossRaidReward(List<ItemStack> items, long currency) {
        this.items = items;
        this.currency = currency;
    }

    public List<ItemStack> getItems() { return items; }
    public long getCurrency() { return currency; }

    public static final BossRaidReward REWARD = new BossRaidReward(
        List.of(
            new ItemStack(Items.ELYTRA, 1)
        ),
        50000
    );

    public static final BossRaidReward EXPERT_REWARD = new BossRaidReward(
        List.of(
            new ItemStack(Items.ELYTRA, 1)
        ),
        100000
    );

    public static BossRaidReward getReward(BOSS_RAID_TYPE type) {
        return switch (type) {
            case VANILLA -> REWARD;
            case EXPERT -> EXPERT_REWARD;
        };
    }
}
