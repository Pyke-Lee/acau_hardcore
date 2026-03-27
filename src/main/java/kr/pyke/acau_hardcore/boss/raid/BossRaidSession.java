package kr.pyke.acau_hardcore.boss.raid;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.data.mailbox.MailBoxData;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_RaidReadyUpdatePayload;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.type.BOSS_RAID_TYPE;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class BossRaidSession {
    public enum STATE {
        READY_CHECK,
        IN_PROGRESS,
        COMPLETING,
        FAILED
    }

    private final UUID sessionID;
    private final BOSS_RAID_TYPE raidType;
    private final UUID initiatorID;
    private final List<UUID> participants;
    private final Map<UUID, Boolean> readyStatus;
    private final Set<UUID> deadPlayers;

    private STATE state;
    private int readyCheckTicks;
    private int completionTicks;

    private static final int READY_CHECK_TIMEOUT = 600;
    private static final int COMPLETION_DELAY = 200;

    public BossRaidSession(BOSS_RAID_TYPE raidType, UUID initiatorID, List<UUID> participants) {
        this.sessionID = UUID.randomUUID();
        this.raidType = raidType;
        this.initiatorID = initiatorID;
        this.participants = new ArrayList<>(participants);
        this.readyStatus = new LinkedHashMap<>();
        this.deadPlayers = new HashSet<>();

        if (participants.size() == 1) {
            this.state = STATE.IN_PROGRESS;
        }
        else {
            this.state = STATE.READY_CHECK;
            for (UUID id : participants) {
                readyStatus.put(id, id.equals(initiatorID));
            }
            this.readyCheckTicks = READY_CHECK_TIMEOUT;
        }
    }

    public UUID getSessionID() { return sessionID; }
    public BOSS_RAID_TYPE getRaidType() { return raidType; }
    public UUID getInitiatorID() { return initiatorID; }
    public List<UUID> getParticipants() { return Collections.unmodifiableList(participants); }
    public STATE getState() { return state; }

    public boolean isParticipant(UUID playerID) { return participants.contains(playerID); }
    public boolean isPlayerDead(UUID playerID) { return deadPlayers.contains(playerID); }

    public void setReady(MinecraftServer server, UUID playerID, boolean ready) {
        if (state != STATE.READY_CHECK || !readyStatus.containsKey(playerID)) { return; }

        readyStatus.put(playerID, ready);
        syncReadyStatus(server);
    }

    public boolean isAllReady() { return readyStatus.values().stream().allMatch(Boolean::booleanValue); }

    public void onPlayerDeathCancelled(ServerPlayer player) {
        deadPlayers.add(player.getUUID());
        player.setHealth(player.getMaxHealth());
        player.setGameMode(GameType.SPECTATOR);
        PykeLib.sendSystemMessage(player, COLOR.GRAY.getColor(), "사망하여 관전 모드로 전환되었습니다.");
    }

    public boolean isAllDead() { return deadPlayers.containsAll(participants); }

    public void onDragonKilled(MinecraftServer server) {
        if (state != STATE.IN_PROGRESS) { return; }

        state = STATE.COMPLETING;
        completionTicks = COMPLETION_DELAY;

        broadcastTitle(server, "§a레이드 성공!", "§710초 후 복귀합니다...");
        broadcastMessage(server, COLOR.LIME.getColor(), "엔더 드래곤을 처치했습니다! 10초 후 보상과 함께 복귀합니다.");
    }

    public boolean tick(MinecraftServer server) {
        return switch (state) {
            case READY_CHECK -> tickReadyCheck(server);
            case COMPLETING -> tickCompleting(server);
            case IN_PROGRESS, FAILED -> false;
        };
    }

    private boolean tickReadyCheck(MinecraftServer server) {
        readyCheckTicks--;

        if (readyCheckTicks > 0 && readyCheckTicks % 100 == 0) {
            int secondsLeft = readyCheckTicks / 20;
            broadcastMessage(server, COLOR.YELLOW.getColor(), String.format("레이드 준비까지 &e%d초&r 남았습니다.", secondsLeft));
            syncReadyStatus(server);
        }

        if (isAllReady()) {
            state = STATE.IN_PROGRESS;
            enterRaid(server);
            return false;
        }

        if (readyCheckTicks <= 0) {
            broadcastMessage(server, COLOR.RED.getColor(), "준비 시간이 초과되어 레이드가 취소되었습니다.");
            return true;
        }

        return false;
    }

    private boolean tickCompleting(MinecraftServer server) {
        completionTicks--;

        if (completionTicks <= 0) {
            handleSuccess(server);
            return true;
        }

        return false;
    }

    public void checkWipe() {
        if (state != STATE.IN_PROGRESS) { return; }
        if (isAllDead()) {
            state = STATE.FAILED;
        }
    }

    public void enterRaid(MinecraftServer server) {
        ResourceKey<Level> dim = raidType.getDimension();
        ServerLevel targetLevel = server.getLevel(dim);
        if (targetLevel == null) {
            broadcastMessage(server, COLOR.RED.getColor(), "레이드 차원을 불러올 수 없습니다.");
            return;
        }

        var respawnData = targetLevel.getRespawnData();
        Vec3 spawnPos = new Vec3(respawnData.pos().getX() + 0.5, respawnData.pos().getY(), respawnData.pos().getZ() + 0.5);

        TeleportTransition transition = new TeleportTransition(targetLevel, spawnPos, Vec3.ZERO, respawnData.yaw(), respawnData.pitch(), TeleportTransition.DO_NOTHING);

        for (UUID id : participants) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                ModComponents.HARDCORE_INFO.get(player).setPrevPosition();
                player.teleport(transition);
            }
        }

        broadcastTitle(server, "§c엔더 드래곤 레이드", "§7" + raidType.getDisplayName() + " 모드");
        broadcastMessage(server, COLOR.AQUA.getColor(), String.format("§e%s§r 레이드가 시작되었습니다!", raidType.getDisplayName()));
    }

    private void handleSuccess(MinecraftServer server) {
        BossRaidReward reward = BossRaidReward.getReward(raidType);

        for (UUID id : participants) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) { continue; }

            if (deadPlayers.contains(id)) {
                player.setGameMode(GameType.SURVIVAL);
                player.setHealth(player.getMaxHealth());
            }

            ModComponents.HARDCORE_INFO.get(player).teleportPrevPosition();

            MailBoxData mailBoxData = MailBoxData.create("엔더 드래곤 레이드 보상", "시스템", "엔더 드래곤 레이드 보상", List.copyOf(reward.getItems()));
            ModComponents.MAIL_BOX.get(player).addMail(mailBoxData);

            if (reward.getCurrency() > 0) {
                ModComponents.HARDCORE_INFO.get(player).addCurrency(reward.getCurrency());
            }

            PykeLib.sendSystemMessage(player, COLOR.LIME.getColor(), "§e레이드 보상§f을 받았습니다!");
        }
    }

    public void handleFail(MinecraftServer server) {
        for (UUID id : participants) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) { continue; }

            ModComponents.HARDCORE_INFO.get(player).teleportPrevPosition();
            player.setGameMode(GameType.SURVIVAL);
        }
    }

    public void cancelByPlayer(MinecraftServer server, UUID playerID) {
        if (state != STATE.READY_CHECK) { return; }

        ServerPlayer player = server.getPlayerList().getPlayer(playerID);
        String name = player != null ? player.getDisplayName().getString() : "???";
        broadcastMessage(server, COLOR.RED.getColor(), String.format("&7%s&r님이 레이드 참가를 거절하여 취소되었습니다.", name));
    }

    private void syncReadyStatus(MinecraftServer server) {
        List<S2C_RaidReadyUpdatePayload.ReadyData> dataList = new ArrayList<>();
        for (Map.Entry<UUID, Boolean> entry : readyStatus.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            String name = player != null ? player.getDisplayName().getString() : "오프라인";
            dataList.add(new S2C_RaidReadyUpdatePayload.ReadyData(entry.getKey(), name, entry.getValue()));
        }

        int secondsLeft = readyCheckTicks / 20;
        S2C_RaidReadyUpdatePayload payload = new S2C_RaidReadyUpdatePayload(dataList, secondsLeft);

        for (UUID id : participants) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    private void broadcastMessage(MinecraftServer server, int color, String message) {
        for (UUID id : participants) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                PykeLib.sendSystemMessage(player, color, message);
            }
        }
    }

    private void broadcastTitle(MinecraftServer server, String title, String subtitle) {
        for (UUID id : participants) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
            }
        }
    }
}