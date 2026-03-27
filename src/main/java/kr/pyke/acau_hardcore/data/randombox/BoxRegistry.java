package kr.pyke.acau_hardcore.data.randombox;

import com.google.gson.*;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_SyncBoxRegistryPayload;
import kr.pyke.acau_hardcore.type.BOX_MESSAGE_TYPE;
import kr.pyke.acau_hardcore.type.BOX_RARITY;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class BoxRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, BoxDefinition> BOXES = new LinkedHashMap<>();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getGameDir().resolve("random_box");

    private BoxRegistry() { }

    public static boolean load(MinecraftServer server, boolean reload) {
        BOXES.clear();

        try {
            if (Files.notExists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
                return true;
            }

            try (Stream<Path> paths = Files.list(CONFIG_DIR)) {
                paths.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                    String boxID = path.getFileName().toString().replace(".json", "");

                    try (FileReader reader = new FileReader(path.toFile(), StandardCharsets.UTF_8)) {
                        JsonObject json = GSON.fromJson(reader, JsonObject.class);
                        BoxDefinition definition = parseDefinition(boxID, json);

                        if (definition != null) {
                            BOXES.put(boxID, definition);
                        }
                    }
                    catch (Exception e) {
                        AcauHardCore.LOGGER.error("Failed to load box: {}", boxID, e);
                    }
                });
            }
        }
        catch (Exception e) {
            AcauHardCore.LOGGER.error("Box registry error", e);
            return false;
        }

        if (reload) {
            S2C_SyncBoxRegistryPayload payload = S2C_SyncBoxRegistryPayload.fromRegistry();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, payload);
            }
        }

        return true;
    }

    private static BoxDefinition parseDefinition(String boxID, JsonObject json) {
        String displayName = json.has("display_name") ? json.get("display_name").getAsString() : boxID;
        int modelData = json.has("model_data") ? json.get("model_data").getAsInt() : 0;
        String openSound = json.has("open_sound") ? json.get("open_sound").getAsString() : null;

        if (!json.has("rewards")) { return null; }

        JsonArray rewardsArray = json.getAsJsonArray("rewards");
        List<BoxReward> rewards = new ArrayList<>();
        for (JsonElement element : rewardsArray) {
            rewards.add(parseReward(element.getAsJsonObject()));
        }

        return new BoxDefinition(boxID, displayName, modelData, openSound, List.copyOf(rewards));
    }

    private static BoxReward parseReward(JsonObject json) {
        return new BoxReward(
            json.get("item").getAsString(),
            json.has("count") ? json.get("count").getAsInt() : 1,
            json.has("nbt") ? json.get("nbt").getAsString() : null,
            json.has("custom_name") ? json.get("custom_name").getAsString() : null,
            json.has("weight") ? json.get("weight").getAsInt() : 1,
            json.has("rarity") ? BOX_RARITY.byKey(json.get("rarity").getAsString()) : BOX_RARITY.COMMON,
            json.has("sound") ? json.get("sound").getAsString() : null,
            json.has("message_type") ? BOX_MESSAGE_TYPE.byKey(json.get("message_type").getAsString()) : BOX_MESSAGE_TYPE.PRIVATE,
            json.has("message") ? json.get("message").getAsString() : ""
        );
    }

    public static BoxDefinition get(String boxId) { return BOXES.get(boxId); }
    public static Collection<BoxDefinition> getAll() { return BOXES.values(); }
    public static Set<String> getAllIDs() { return BOXES.keySet(); }
    public static boolean exists(String boxId) { return BOXES.containsKey(boxId); }
    public static int size() { return BOXES.size(); }
}