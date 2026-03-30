package kr.pyke.acau_hardcore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_PrefixSyncAllPayload;
import kr.pyke.acau_hardcore.prefix.PrefixData;
import kr.pyke.acau_hardcore.prefix.PrefixRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PrefixConfig {
    private static final String FILE_NAME = "prefixes.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load(MinecraftServer server) {
        Path path = getConfigPath(server);
        if (!path.toFile().exists()) {
            AcauHardCore.LOGGER.error("칭호 Config 파일이 없습니다. 기본값으로 파일을 생성합니다: {}", path);
            createDefaultConfig(path);
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject jsonObject = GSON.fromJson(reader, JsonObject.class);
            PrefixRegistry.clear();

            if (jsonObject != null && jsonObject.has("prefixes")) {
                JsonArray prefixes = jsonObject.getAsJsonArray("prefixes");
                for (int i = 0; i < prefixes.size(); i++) {
                    JsonObject entry = prefixes.get(i).getAsJsonObject();
                    String id = entry.get("id").getAsString();
                    String prefix = entry.get("prefix").getAsString();
                    PrefixRegistry.register(id, new PrefixData(id, prefix));
                }
            }
            AcauHardCore.LOGGER.debug("칭호 Config 파싱 완료 및 레지스트리 등록 완료: {}개", PrefixRegistry.getKeys().size());
            AcauHardCore.LOGGER.info("칭호 Config 로드 완료: {}", path);
        }
        catch (Exception e) {
            AcauHardCore.LOGGER.error("칭호 Config 파일 읽기 실패: {}", path, e);
        }
    }

    private static void createDefaultConfig(Path path) {
        try {
            JsonObject root = new JsonObject();
            JsonArray prefixes = new JsonArray();

            JsonObject defaultEntry = new JsonObject();
            defaultEntry.addProperty("id", "test");
            defaultEntry.addProperty("prefix", "테스트");
            prefixes.add(defaultEntry);

            root.add("prefixes", prefixes);

            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(root, writer);
            }

            PrefixRegistry.clear();
            PrefixRegistry.register("test", new PrefixData("test", "테스트"));
            AcauHardCore.LOGGER.debug("기본 칭호 Config 생성 및 레지스트리 등록 완료");
        }
        catch (Exception e) {
            AcauHardCore.LOGGER.error("칭호 Config 파일 생성 실패: {}", path, e);
        }
    }

    public static boolean reload(MinecraftServer server) {
        try {
            load(server);
            syncToAll(server);
            return true;
        }
        catch (Exception e) {
            AcauHardCore.LOGGER.error("Failed to load prefixe config file!", e);
            return false;
        }
    }

    public static void syncToAll(MinecraftServer server) {
        List<PrefixData> prefixes = new ArrayList<>(PrefixRegistry.getAll());
        S2C_PrefixSyncAllPayload payload = new S2C_PrefixSyncAllPayload(prefixes);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private static Path getConfigPath(MinecraftServer server) {
        return server.getServerDirectory().resolve("config").resolve(FILE_NAME);
    }
}
