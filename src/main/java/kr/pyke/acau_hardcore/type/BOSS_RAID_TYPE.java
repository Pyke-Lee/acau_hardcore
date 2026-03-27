package kr.pyke.acau_hardcore.type;

import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public enum BOSS_RAID_TYPE {
    VANILLA("vanilla", "바닐라", Level.END, HARDCORE_TYPE.BEGINNER),
    EXPERT("expert", "숙련자", ModDimensions.EXPERT_END, HARDCORE_TYPE.EXPERT);

    private final String key;
    private final String displayName;
    private final ResourceKey<Level> dimension;
    private final HARDCORE_TYPE requiredType;

    BOSS_RAID_TYPE(String key, String displayName, ResourceKey<Level> dimension, HARDCORE_TYPE requiredType) {
        this.key = key;
        this.displayName = displayName;
        this.dimension = dimension;
        this.requiredType = requiredType;
    }

    public String getKey() { return this.key; }
    public String getDisplayName() { return this.displayName; }
    public ResourceKey<Level> getDimension() { return this.dimension; }
    public HARDCORE_TYPE getRequiredType() { return this.requiredType; }

    public static BOSS_RAID_TYPE byKey(String key) {
        for (BOSS_RAID_TYPE type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }

        return null;
    }

    public static BOSS_RAID_TYPE fromHardCoreType(HARDCORE_TYPE hardcoreType) {
        for (BOSS_RAID_TYPE type : values()) {
            if (type.requiredType.equals(hardcoreType)) {
                return type;
            }
        }

        return null;
    }
}
