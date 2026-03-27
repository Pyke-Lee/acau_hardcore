package kr.pyke.acau_hardcore.data.displayname;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.util.Utils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DisplayNameData extends SavedData {
    private static final String FILE_NAME = "displayname";
    private final Map<UUID, String> displayNames = new HashMap<>();
    private final Map<String, UUID> displayNamesReverse = new HashMap<>();

    public static final Codec<DisplayNameData> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING).xmap(
        map -> {
            DisplayNameData data = new DisplayNameData();
            for (Map.Entry<UUID, String> entry : map.entrySet()) {
                data.displayNames.put(entry.getKey(), entry.getValue());
                data.displayNamesReverse.put(entry.getValue(), entry.getKey());
            }

            return data;
        },
        DisplayNameData::getDisplayNames
    );

    public static final SavedDataType<DisplayNameData> TYPE = new SavedDataType<>(
        FILE_NAME,
        DisplayNameData::new,
        CODEC,
        DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    public static DisplayNameData getServerState(MinecraftServer server) {
        ServerLevel serverLevel = server.overworld();

        return serverLevel.getDataStorage().computeIfAbsent(TYPE);
    }

    public String getDisplayName(UUID uuid) { return displayNames.get(uuid); }

    public Map<UUID, String> getDisplayNames() { return this.displayNames; }

    public String getRealName(String displayName) {
        UUID uuid = displayNamesReverse.get(displayName);
        if (null == uuid) { return displayName; }

        MinecraftServer server = AcauHardCore.SERVER_INSTANCE;
        if (null == server) { return displayName; }

        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (null != player) { return player.getGameProfile().name(); }

        Optional<GameProfile> profile = server.services().profileResolver().fetchById(uuid);
        return profile.map(GameProfile::name).orElse(displayName);
    }

    public void setDisplayName(UUID uuid, String displayName) {
        String stripDisplayName = Utils.stripColor(displayName);

        if (displayNames.containsKey(uuid)) {
            String oldDisplayName = displayNames.get(uuid);
            displayNamesReverse.remove(oldDisplayName);
        }

        displayNames.put(uuid, stripDisplayName);
        displayNamesReverse.put(stripDisplayName, uuid);

        this.setDirty();
    }
}