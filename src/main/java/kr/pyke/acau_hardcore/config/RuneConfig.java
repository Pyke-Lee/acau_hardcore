package kr.pyke.acau_hardcore.config;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.type.RUNE_EFFECT;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RuneConfig {
    private static final String FILE_NAME = "rune_config.conf";
    public static final Map<String, float[]> RANGES = new LinkedHashMap<>();

    static {
        put("chicken_loot", 10f, 50f, 1f, 2f);
        put("rabbit_loot", 10f, 50f, 1f, 2f);
        put("pig_loot", 10f, 50f, 1f, 2f);
        put("sheep_loot", 10f, 50f, 1f, 2f);
        put("cow_loot", 10f, 50f, 1f, 2f);
        put("monster_loot", 10f, 50f, 1f, 2f);
        put("harvest_bonemeal", 10f, 50f, 1f, 2f);
        put("xp_boost", 20f, 50f, 3f, 5f);
        put("vein_mine", 1f, 2f, 0f, 0f);
        put("ore_bonus", 0.5f, 5f, 0f, 0f);
        put("crop_bonus", 20f, 50f, 1f, 3f);
        put("seed_bonus", 20f, 50f, 1f, 3f);
        put("tree_fell", 3f, 5f, 0f, 0f);
        put("life_steal", 0.5f, 1.5f, 0f, 0f);
        put("crit_chance", 1f, 10f, 0f, 0f);
        put("crit_damage", 1f, 10f, 0f, 0f);
        put("dodge_chance", 0.5f, 5f, 0f, 0f);
        put("low_hp_attack", 10f, 25f, 3f, 7f);
        put("attack_damage", 0.5f, 5f, 0f, 0f);
        put("execute", 0.1f, 3f, 10f, 30f);
        put("health_boost", 0.5f, 3f, 0f, 0f);
        put("saturation_on_hit", 1f, 2f, 0f, 0f);
        put("miner_fortune", 1f, 3f, 0f, 0f);
        put("farmer_fortune", 1f, 3f, 0f, 0f);
        put("fisher_fortune", 1f, 3f, 0f, 0f);
    }

    private static void put(String key, float v1Min, float v1Max, float v2Min, float v2Max) {
        RANGES.put(key, new float[]{v1Min, v1Max, v2Min, v2Max});
    }

    public static float[] getRange(String effectKey) {
        return RANGES.getOrDefault(effectKey, new float[]{0f, 0f, 0f, 0f});
    }

    public static void load(MinecraftServer server) {
        Path path = server.getServerDirectory().resolve("config").resolve(FILE_NAME);

        if (!path.toFile().exists()) {
            save(server);
            return;
        }

        Map<String, String> data = ConfigParser.load(path);

        for (Map.Entry<String, float[]> entry : RANGES.entrySet()) {
            String key = entry.getKey();
            float[] range = entry.getValue();

            range[0] = parseFloat(data, key + "_v1_min", range[0]);
            range[1] = parseFloat(data, key + "_v1_max", range[1]);
            range[2] = parseFloat(data, key + "_v2_min", range[2]);
            range[3] = parseFloat(data, key + "_v2_max", range[3]);
        }

        AcauHardCore.LOGGER.info("룬 Config 로드 완료: {}", path);
    }

    public static void save(MinecraftServer server) {
        Path path = server.getServerDirectory().resolve("config").resolve(FILE_NAME);
        List<ConfigParser.ConfigEntry> entries = new ArrayList<>();

        for (Map.Entry<String, float[]> entry : RANGES.entrySet()) {
            String key = entry.getKey();
            float[] range = entry.getValue();

            RUNE_EFFECT effect = RUNE_EFFECT.byKey(key);
            String label = effect != null ? effect.getDescriptionFormat() : key;

            entries.add(new ConfigParser.ConfigEntry.Comment(key));
            entries.add(new ConfigParser.ConfigEntry.Value(key + "_v1_min", range[0]));
            entries.add(new ConfigParser.ConfigEntry.Value(key + "_v1_max", range[1]));
            if (range[2] != 0f || range[3] != 0f) {
                entries.add(new ConfigParser.ConfigEntry.Value(key + "_v2_min", range[2]));
                entries.add(new ConfigParser.ConfigEntry.Value(key + "_v2_max", range[3]));
            }
            entries.add(new ConfigParser.ConfigEntry.BlankLine());
        }

        ConfigParser.save(path, entries);
    }

    private static float parseFloat(Map<String, String> data, String key, float defaultValue) {
        String value = data.get(key);
        if (value == null) { return defaultValue; }

        try { return Float.parseFloat(value); }
        catch (Exception e) { return defaultValue; }
    }
}