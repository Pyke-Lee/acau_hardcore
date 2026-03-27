package kr.pyke.acau_hardcore.data.randombox;

import kr.pyke.PykeLib;
import kr.pyke.util.constants.COLOR;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PendingRewardManager {
    private static final Map<UUID, PendingReward> PENDING = new ConcurrentHashMap<>();

    private PendingRewardManager() { }

    public static void store(UUID playerUuid, PendingReward reward) {
        PENDING.put(playerUuid, reward);
    }

    public static void claim(ServerPlayer player) {
        PendingReward reward = PENDING.remove(player.getUUID());
        if (reward == null || reward.stack().isEmpty()) return;

        if (!player.getInventory().add(reward.stack().copy())) {
            player.drop(reward.stack().copy(), false);
        }

        if (reward.sound() != null) {
            SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(reward.sound()));
            if (soundEvent != null) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        String msg = reward.message().replace("%player%", player.getDisplayName().getString());
        if (!msg.isBlank()) {
            switch (reward.messageType()) {
                case PRIVATE -> PykeLib.sendSystemMessage(player, COLOR.LIME.getColor(), msg);
                case BROADCAST -> PykeLib.sendSystemMessage(player.level().getServer().getPlayerList().getPlayers(), COLOR.LIME.getColor(), msg);
                case NOTICE -> PykeLib.sendBroadcastMessage(player.level().getServer().getPlayerList().getPlayers(), COLOR.LIME.getColor(), msg);
            }
        }
    }

    public static boolean hasPending(UUID playerUuid) {
        return PENDING.containsKey(playerUuid);
    }
}