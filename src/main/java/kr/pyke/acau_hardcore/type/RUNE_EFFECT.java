package kr.pyke.acau_hardcore.type;

import net.minecraft.ChatFormatting;

public enum RUNE_EFFECT {
    CHICKEN_LOOT(RUNE_TYPE.LIFE, RUNE_TARGET.GENERAL_LIFE, "chicken_loot","닭 처치 시 %.0f%% 확률로 전리품 %.0f개 추가 획득", false),
    RABBIT_LOOT(RUNE_TYPE.LIFE, RUNE_TARGET.GENERAL_LIFE, "rabbit_loot","토끼 처치 시 %.0f%% 확률로 전리품 %.0f개 추가 획득", false),
    PIG_LOOT(RUNE_TYPE.LIFE, RUNE_TARGET.GENERAL_LIFE, "pig_loot","돼지 처치 시 %.0f%% 확률로 전리품 %.0f개 추가 획득", false),
    SHEEP_LOOT(RUNE_TYPE.LIFE, RUNE_TARGET.GENERAL_LIFE, "sheep_loot","양 처치 시 %.0f%% 확률로 전리품 %.0f개 추가 획득", false),
    COW_LOOT(RUNE_TYPE.LIFE, RUNE_TARGET.GENERAL_LIFE, "cow_loot","소 처치 시 %.0f%% 확률로 전리품 %.0f개 추가 획득", false),
    MONSTER_LOOT(RUNE_TYPE.LIFE, RUNE_TARGET.GENERAL_LIFE, "monster_loot","몬스터 처치 시 %.0f%% 확률로 전리품 %.0f개 추가 획득", false),
    HARVEST_BONEMEAL(RUNE_TYPE.LIFE, RUNE_TARGET.GENERAL_LIFE, "harvest_bonemeal","농작물 수확 시 %.0f%% 확률로 뼛가루 %.0f개 드랍", false),
    XP_BOOST(RUNE_TYPE.LIFE, RUNE_TARGET.GENERAL_LIFE, "xp_boost","경험치 획득 시 %.0f%% 확률로 획득량 %.0f 증가", false),

    SMELT_ORE(RUNE_TYPE.LIFE, RUNE_TARGET.PICKAXE, "smelt_ore","광물 블록 채굴 시 주괴 상태로 드랍", true),
    VEIN_MINE(RUNE_TYPE.LIFE, RUNE_TARGET.PICKAXE, "vein_mine","블록 채굴 시 %s 1칸 동시 채굴", false),
    ORE_BONUS(RUNE_TYPE.LIFE, RUNE_TARGET.PICKAXE, "ore_bonus","광물 채굴 시 %.0f%% 확률로 특수 광물 드랍", true),
    MINER_FORTUNE(RUNE_TYPE.LIFE, RUNE_TARGET.PICKAXE, "miner_fortune","광부의 행운 %.0f 단계", true),

    CROP_BONUS(RUNE_TYPE.LIFE, RUNE_TARGET.HOE, "crop_bonus","농작물 수확 시 %.0f%% 확률로 작물 %.0f개 추가 획득", false),
    SEED_BONUS(RUNE_TYPE.LIFE, RUNE_TARGET.HOE, "seed_bonus","농작물 수확 시 %.0f%% 확률로 씨앗 %.0f개 추가 획득", false),
    FARMER_FORTUNE(RUNE_TYPE.LIFE, RUNE_TARGET.HOE, "farmer_fortune","농부의 행운 %.0f 단계", true),

    SAND_TO_GLASS(RUNE_TYPE.LIFE, RUNE_TARGET.SHOVEL, "sand_to_glass","모래 블록 채굴 시 유리 블록으로 드랍", true),

    TREE_FELL(RUNE_TYPE.LIFE, RUNE_TARGET.AXE, "tree_fell","나무 원목 채굴 시 위로 %.0f칸까지 동시 채굴", false),

    FISHER_FORTUNE(RUNE_TYPE.LIFE, RUNE_TARGET.FISHING, "fisher_fortune","어부의 행운 %.0f 단계", true),

    LIFE_STEAL(RUNE_TYPE.COMBAT, RUNE_TARGET.GENERAL_COMBAT, "life_steal","생명력 흡수 %.1f", false),
    CRIT_CHANCE(RUNE_TYPE.COMBAT, RUNE_TARGET.GENERAL_COMBAT, "crit_chance","크리티컬 확률 +%.1f%%", false),
    CRIT_DAMAGE(RUNE_TYPE.COMBAT, RUNE_TARGET.GENERAL_COMBAT, "crit_damage","크리티컬 대미지 +%.1f%%", false),
    DODGE_CHANCE(RUNE_TYPE.COMBAT, RUNE_TARGET.GENERAL_COMBAT, "dodge_chance","회피 확률 +%.1f%%", false),
    LOW_HP_ATTACK(RUNE_TYPE.COMBAT, RUNE_TARGET.GENERAL_COMBAT, "low_hp_attack","체력 %.0f%% 이하일 때 공격력 +%.1f", false),

    ATTACK_DAMAGE(RUNE_TYPE.COMBAT, RUNE_TARGET.WEAPON, "attack_damage","공격력 +%.1f", false),
    EXECUTE(RUNE_TYPE.COMBAT, RUNE_TARGET.WEAPON, "execute","타격 시 %.1f%% 확률로 대상 체력 %.0f%% 이하일 때 즉시 처치", true),

    HEALTH_BOOST(RUNE_TYPE.COMBAT, RUNE_TARGET.ARMOR, "health_boost","체력 +%.1f 칸", false),
    SATURATION_ON_HIT(RUNE_TYPE.COMBAT, RUNE_TARGET.ARMOR, "saturation_on_hit","피격 시 %.1f%% 확률로 포화 1 1초 부여", false);

    private final RUNE_TYPE runeType;
    private final RUNE_TARGET target;
    private final String key;
    private final String descriptionFormat;
    private final boolean rare;

    RUNE_EFFECT(RUNE_TYPE runeType, RUNE_TARGET target, String key, String descriptionFormat, boolean rare) {
        this.runeType = runeType;
        this.target = target;
        this.key = key;
        this.descriptionFormat = descriptionFormat;
        this.rare = rare;
    }

    public RUNE_TYPE getRuneType() { return runeType; }
    public RUNE_TARGET getTarget() { return target; }
    public String getKey() { return key; }
    public String getDescriptionFormat() { return descriptionFormat; }
    public boolean isRare() { return rare; }
    public ChatFormatting getColor() { return rare ? ChatFormatting.GOLD : ChatFormatting.GREEN; }

    public static RUNE_EFFECT byKey(String key) {
        for (RUNE_EFFECT effect : values()) {
            if (effect.key.equals(key)) {
                return effect;
            }
        }
        return null;
    }
}