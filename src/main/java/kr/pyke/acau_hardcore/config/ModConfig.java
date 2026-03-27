package kr.pyke.acau_hardcore.config;

import kr.pyke.acau_hardcore.AcauHardCore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ModConfig {
    private static final String FILE_NAME = "hardcore_config.conf";
    public static ModConfig INSTANCE = new ModConfig();

    // 갈증 시스템
    public int thirstDecreaseChance = 300;
    public float thirstExhaustionRate = 0.025f;

    // 더러운 물 디버프 확률
    public float dirtyWaterPenaltyChance = 0.25f;
    public float dirtyWaterPenaltyDuration = 30.f;

    // 몬스터 강화
    public double expertHealthMultiplier = 0.5d;
    public double expertDamageMultiplier = 0.3d;
    public double expertSpeedMultiplier = 0.1d;
    public double expertArmorBonus = 4d;
    public double expertKnockbackResistance = 0.2d;

    // 도움 요청 쿨타임
    public float helpRequestCooldown = 30.f;

    // 희귀 광물 기본 드랍 확률
    public float rareOreDropChance = 0.005f;
    public float expertRareOreDropChance = 0.01f;

    // 엔더 드래곤 설정
    public double dragonMaxHealth = 200d;
    public float dragonHeadDamage = 10.f;
    public float dragonWingDamage = 5.f;
    public float dragonBreathDamage = 3.f;
    public float dragonFireballDamage = 6.f;
    public int dragonEarthQuakeTicks = 100;
    public float dragonEarthQuakeDamage = 10.f;

    public double expertDragonMaxHealth = 300d;
    public float expertDragonHeadDamage = 13.f;
    public float expertDragonWingDamage = 7.f;
    public float expertDragonBreathDamage = 4.f;
    public float expertDragonFireballDamage = 8.f;
    public int expertDragonEarthQuakeTicks = 100;
    public float expertDragonEarthQuakeDamage = 13.f;

    public static void load(MinecraftServer server) {
        Path path = getConfigPath(server);

        if (!path.toFile().exists()) {
            AcauHardCore.LOGGER.error("Config 파일이 없습니다. 기본값으로 파일을 생성합니다: {}", path);
            INSTANCE = new ModConfig();
            save(server);
            return;
        }

        Map<String, String> data = ConfigParser.load(path);
        ModConfig config = new ModConfig();

        config.thirstDecreaseChance = getInt(data, "thirst_decrease_chance", config.thirstDecreaseChance, 1, Integer.MAX_VALUE);
        config.thirstExhaustionRate = getFloat(data, "thirst_exhaustion_rate", config.thirstExhaustionRate, 0f, 10f);

        config.dirtyWaterPenaltyChance = getFloat(data, "dirty_water_penalty_chance", config.dirtyWaterPenaltyChance, 0f, 1f);
        config.dirtyWaterPenaltyDuration = getFloat(data, "dirty_water_penalty_duration", config.dirtyWaterPenaltyDuration, 0f, Float.MAX_VALUE);

        config.expertHealthMultiplier = getDouble(data, "expert_health_multiplier", config.expertHealthMultiplier, 0d, 100d);
        config.expertDamageMultiplier = getDouble(data, "expert_damage_multiplier", config.expertDamageMultiplier, 0d, 100d);
        config.expertSpeedMultiplier = getDouble(data, "expert_speed_multiplier", config.expertSpeedMultiplier, 0d, 10d);
        config.expertArmorBonus = getDouble(data, "expert_armor_bonus", config.expertArmorBonus, 0d, 30d);
        config.expertKnockbackResistance = getDouble(data, "expert_knockback_resistance", config.expertKnockbackResistance, 0d, 1d);

        config.helpRequestCooldown = getFloat(data, "help_request_cooldown", config.helpRequestCooldown, 0.f, Float.MAX_VALUE);

        config.rareOreDropChance = getFloat(data, "rare_ore_drop_chance", config.rareOreDropChance, 0.f, 1.f);
        config.expertRareOreDropChance = getFloat(data, "expert_rare_ore_drop_chance", config.rareOreDropChance, 0.f, 1.f);

        config.dragonMaxHealth = getDouble(data, "dragon_max_health", config.dragonMaxHealth, 1d, 10000d);
        config.dragonHeadDamage = getFloat(data, "dragon_head_damage", config.dragonHeadDamage, 0.f, 1024.f);
        config.dragonWingDamage = getFloat(data, "dragon_wing_damage", config.dragonWingDamage, 0.f, 1024.f);
        config.dragonBreathDamage = getFloat(data, "dragon_breath_damage", config.dragonBreathDamage, 0.f, 1024.f);
        config.dragonFireballDamage = getFloat(data, "dragon_fireball_damage", config.dragonFireballDamage, 0.f, 1024.f);
        config.dragonEarthQuakeTicks = getInt(data, "dragon_earthquake_ticks", config.dragonEarthQuakeTicks, 1, 1024);
        config.dragonEarthQuakeDamage = getFloat(data, "dragon_earthquake_damage", config.dragonEarthQuakeDamage, 0.f, 1024.f);

        config.expertDragonMaxHealth = getDouble(data, "expert_dragon_max_health", config.expertDragonMaxHealth, 1d, 10000d);
        config.expertDragonHeadDamage = getFloat(data, "expert_dragon_head_damage", config.expertDragonHeadDamage, 0.f, 1024.f);
        config.expertDragonWingDamage = getFloat(data, "expert_dragon_wing_damage", config.expertDragonWingDamage, 0.f, 1024.f);
        config.expertDragonBreathDamage = getFloat(data, "expert_dragon_breath_damage", config.expertDragonBreathDamage, 0.f, 1024.f);
        config.expertDragonFireballDamage = getFloat(data, "expert_dragon_fireball_damage", config.expertDragonFireballDamage, 0.f, 1024.f);
        config.expertDragonEarthQuakeTicks = getInt(data, "expert_dragon_earthquake_ticks", config.expertDragonEarthQuakeTicks, 1, 1024);
        config.expertDragonEarthQuakeDamage = getFloat(data, "expert_dragon_earthquake_damage", config.expertDragonEarthQuakeDamage, 0.f, 1024.f);

        INSTANCE = config;
        AcauHardCore.LOGGER.info("Config 로드 완료: {}", path);
    }

    public static void save(MinecraftServer server) {
        Path path = getConfigPath(server);
        ModConfig config = INSTANCE;

        List<ConfigParser.ConfigEntry> entries = List.of(
            new ConfigParser.ConfigEntry.Comment("갈증 시스템"),
            new ConfigParser.ConfigEntry.Comment("갈증 감소 확률 (randomTickSpeed 기반, 낮을수록 빨리 감소) [1 ~ ]"),
            new ConfigParser.ConfigEntry.Value("thirst_decrease_chance", config.thirstDecreaseChance),
            new ConfigParser.ConfigEntry.Comment("갈증 0일 때 배고픔 소모량 [0.0 ~ 10.0]"),
            new ConfigParser.ConfigEntry.Value("thirst_exhaustion_rate", config.thirstExhaustionRate),
            new ConfigParser.ConfigEntry.BlankLine(),

            new ConfigParser.ConfigEntry.Comment("물 섭취 디버프"),
            new ConfigParser.ConfigEntry.Comment("더러운 물 디버프 확률 [0.0 ~ 1.0]"),
            new ConfigParser.ConfigEntry.Value("dirty_water_penalty_chance", config.dirtyWaterPenaltyChance),
            new ConfigParser.ConfigEntry.Comment("더러운 물 디버프 지속시간 (초) [0.0 ~ ]"),
            new ConfigParser.ConfigEntry.Value("dirty_water_penalty_duration", config.dirtyWaterPenaltyDuration),
            new ConfigParser.ConfigEntry.BlankLine(),

            new ConfigParser.ConfigEntry.Comment("숙련자 월드 몬스터 강화"),
            new ConfigParser.ConfigEntry.Comment("배율: 0.5 = +50%, 1.0 = +100% [0.0 ~ 100.0]"),
            new ConfigParser.ConfigEntry.Value("expert_health_multiplier", config.expertHealthMultiplier),
            new ConfigParser.ConfigEntry.Value("expert_damage_multiplier", config.expertDamageMultiplier),
            new ConfigParser.ConfigEntry.Comment("이동속도 배율 [0.0 ~ 10.0]"),
            new ConfigParser.ConfigEntry.Value("expert_speed_multiplier", config.expertSpeedMultiplier),
            new ConfigParser.ConfigEntry.Comment("고정 방어력 추가 [0.0 ~ 30.0]"),
            new ConfigParser.ConfigEntry.Value("expert_armor_bonus", config.expertArmorBonus),
            new ConfigParser.ConfigEntry.Comment("넉백 저항 [0.0 ~ 1.0]"),
            new ConfigParser.ConfigEntry.Value("expert_knockback_resistance", config.expertKnockbackResistance),
            new ConfigParser.ConfigEntry.BlankLine(),

            new ConfigParser.ConfigEntry.Comment("도움 요청 쿨타임: 기본 30초"),
            new ConfigParser.ConfigEntry.Value("help_request_cooldown", config.helpRequestCooldown),
            new ConfigParser.ConfigEntry.BlankLine(),

            new ConfigParser.ConfigEntry.Comment("희귀 광물 기본 드랍 확률(0 ~ 1)"),
            new ConfigParser.ConfigEntry.Value("rare_ore_drop_chance", config.rareOreDropChance),
            new ConfigParser.ConfigEntry.Value("expert_rare_ore_drop_chance", config.expertRareOreDropChance),

            new ConfigParser.ConfigEntry.Comment("바닐라 엔더 드래곤 설정"),
            new ConfigParser.ConfigEntry.Comment("드래곤 최대 체력 [1 ~ 10000]"),
            new ConfigParser.ConfigEntry.Value("dragon_max_health", config.dragonMaxHealth),
            new ConfigParser.ConfigEntry.Comment("머리 타격 데미지 [0 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_head_damage", config.dragonHeadDamage),
            new ConfigParser.ConfigEntry.Comment("날개 치기(넉백) 데미지 [0 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_wing_damage", config.dragonWingDamage),
            new ConfigParser.ConfigEntry.Comment("브레스(바닥 잔류 구름) 틱당 데미지 [0 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_breath_damage", config.dragonBreathDamage),
            new ConfigParser.ConfigEntry.Comment("드래곤 파이어볼 폭발/직격 데미지 [0 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_fireball_damage", config.dragonFireballDamage),
            new ConfigParser.ConfigEntry.Comment("드래곤 지진파 시전 간격 [1 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_earthquake_ticks", config.dragonEarthQuakeTicks),
            new ConfigParser.ConfigEntry.Comment("드래곤 지진파 데미지 [0 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_earthquake_damage", config.dragonEarthQuakeDamage),

            new ConfigParser.ConfigEntry.Comment("숙련자 엔더 드래곤 설정"),
            new ConfigParser.ConfigEntry.Comment("드래곤 최대 체력 [1 ~ 10000]"),
            new ConfigParser.ConfigEntry.Value("dragon_max_health", config.expertDragonMaxHealth),
            new ConfigParser.ConfigEntry.Comment("머리 타격 데미지 [0 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_head_damage", config.expertDragonHeadDamage),
            new ConfigParser.ConfigEntry.Comment("날개 치기(넉백) 데미지 [0 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_wing_damage", config.expertDragonWingDamage),
            new ConfigParser.ConfigEntry.Comment("브레스(바닥 잔류 구름) 틱당 데미지 [0 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_breath_damage", config.expertDragonBreathDamage),
            new ConfigParser.ConfigEntry.Comment("드래곤 파이어볼 폭발/직격 데미지 [0 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_fireball_damage", config.expertDragonFireballDamage),
            new ConfigParser.ConfigEntry.Comment("드래곤 지진파 시전 간격 [1 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_earthquake_ticks", config.expertDragonEarthQuakeTicks),
            new ConfigParser.ConfigEntry.Comment("드래곤 지진파 데미지 [0 ~ 1024]"),
            new ConfigParser.ConfigEntry.Value("dragon_earthquake_damage", config.expertDragonEarthQuakeDamage)
        );

        ConfigParser.save(path, entries);
    }

    public static boolean reload(MinecraftServer server) {
        try {
            load(server);
            return true;
        }
        catch (Exception e) {
            AcauHardCore.LOGGER.error("Failed to load config file!", e);
            return false;
        }
    }

    private static Path getConfigPath(MinecraftServer server) {
        return server.getServerDirectory().resolve("config").resolve(FILE_NAME);
    }

    private static int getInt(Map<String, String> data, String key, int defaultValue, int min, int max) {
        String value = data.get(key);
        if (value == null) { return defaultValue; }

        try {
            int parsed = Integer.parseInt(value);
            int clamped = Mth.clamp(parsed, min, max);
            if (parsed != clamped) {
                AcauHardCore.LOGGER.warn("Config 범위 초과: {} = {} → {} (범위: {} ~ {})", key, parsed, clamped, min, max);
            }
            return clamped;
        }
        catch (NumberFormatException e) {
            AcauHardCore.LOGGER.warn("Config 파싱 오류: {}의 값 '{}'을 정수로 변환할 수 없습니다. 기본값 {} 사용", key, value, defaultValue);
            return defaultValue;
        }
    }

    private static float getFloat(Map<String, String> data, String key, float defaultValue, float min, float max) {
        String value = data.get(key);
        if (value == null) { return defaultValue; }

        try {
            float parsed = Float.parseFloat(value);
            float clamped = Mth.clamp(parsed, min, max);
            if (parsed != clamped) {
                AcauHardCore.LOGGER.warn("Config 범위 초과: {} = {} → {} (범위: {} ~ {})", key, parsed, clamped, min, max);
            }
            return clamped;
        }
        catch (NumberFormatException e) {
            AcauHardCore.LOGGER.warn("Config 파싱 오류: {}의 값 '{}'을 실수로 변환할 수 없습니다. 기본값 {} 사용", key, value, defaultValue);
            return defaultValue;
        }
    }

    private static double getDouble(Map<String, String> data, String key, double defaultValue, double min, double max) {
        String value = data.get(key);
        if (value == null) { return defaultValue; }

        try {
            double parsed = Double.parseDouble(value);
            double clamped = Mth.clamp(parsed, min, max);
            if (parsed != clamped) {
                AcauHardCore.LOGGER.warn("Config 범위 초과: {} = {} → {} (범위: {} ~ {})", key, parsed, clamped, min, max);
            }
            return clamped;
        }
        catch (NumberFormatException e) {
            AcauHardCore.LOGGER.warn("Config 파싱 오류: {}의 값 '{}'을 실수로 변환할 수 없습니다. 기본값 {} 사용", key, value, defaultValue);
            return defaultValue;
        }
    }
}