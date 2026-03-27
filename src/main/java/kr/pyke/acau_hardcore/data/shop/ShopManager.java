package kr.pyke.acau_hardcore.data.shop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_ShopDataSyncAllPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ShopManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, ShopData> SHOPS = new HashMap<>();

    public static void load(MinecraftServer server, boolean reload) {
        SHOPS.clear();

        File configDir = new File(FabricLoader.getInstance().getGameDir().toFile(), "custom_shop");
        if (!configDir.exists()) {
            configDir.mkdirs();
            return;
        }

        File[] files = configDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) { return; }

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                ShopData data = GSON.fromJson(reader, ShopData.class);
                if (data != null && data.id != null) { SHOPS.put(data.id, data); }
            }
            catch (Exception e) {
                AcauHardCore.LOGGER.error("상점 JSON 파일을 파싱하는 중 오류가 발생했습니다. 파일명: {}", file.getName());
                AcauHardCore.LOGGER.error("오류 상세 내용: {}", e.getMessage());
            }
        }

        if (reload) {
            syncDatas(server);
        }
    }

    public static ShopData getShop(String id) { return SHOPS.get(id); }
    public static Map<String, ShopData> getShops() { return SHOPS; }

    public static void syncDatas(MinecraftServer server) {
        if (SHOPS.isEmpty()) { return; }

        S2C_ShopDataSyncAllPayload payload = new S2C_ShopDataSyncAllPayload(SHOPS);
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, payload));
        }
    }
}
