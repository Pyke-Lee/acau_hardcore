package kr.pyke.acau_hardcore.util;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.data.displayname.DisplayNameData;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_SendSingleDisplayNamePayload;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class Utils {
    private Utils() { }

    public static void refreshTabList(ServerPlayer player) {
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED
        );
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(actions, List.of(player));
        player.level().getServer().getPlayerList().broadcastAll(packet);
    }

    public static String stripColor(String displayName) {
        return displayName.replaceAll("(?i)[&§][0-9A-FK-OR]", "");
    }

    public static void updateDisplayName(ServerPlayer target, String displayName, ServerPlayer sender) {
        updateDisplayName(target, displayName);

        String targetName = target.getGameProfile().name();
        PykeLib.sendSystemMessage(sender, COLOR.LIME.getColor(), String.format("&7%s&f님의 이름을 &7%s&f(으)로 변경하였습니다.", targetName, displayName));
    }

    public static void updateDisplayName(ServerPlayer target, String displayName) {
        MinecraftServer server = target.level().getServer();

        DisplayNameData data = DisplayNameData.getServerState(server);
        data.setDisplayName(target.getUUID(), displayName);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            S2C_SendSingleDisplayNamePayload payload = new S2C_SendSingleDisplayNamePayload(target.getUUID(), displayName);
            ServerPlayNetworking.send(player, payload);
        }

        refreshTabList(target);

        PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), String.format("&f이름이 &7%s&f(으)로 변경되었습니다.", displayName));
    }

    public static <E extends Enum<E>> E parseEnum(String s, Class<E> cls) {
        try { return Enum.valueOf(cls, s.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { return null; }
    }
}
