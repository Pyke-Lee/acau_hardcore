package kr.pyke.acau_hardcore.boss.raid;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_OpenRaidReadyPayload;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_OpenRaidSelectPayload;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_RaidSelectUpdatePayload;
import kr.pyke.acau_hardcore.party.Party;
import kr.pyke.acau_hardcore.party.PartyManager;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.hardcore.IHardCoreInfo;
import kr.pyke.acau_hardcore.type.BOSS_RAID_TYPE;
import kr.pyke.acau_hardcore.type.HARDCORE_TYPE;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.*;

public class BossRaidManager {
    private static final long COOLDOWN_TICKS = 20L * 60 * 60;

    private static final Map<BOSS_RAID_TYPE, BossRaidSession> activeSessions = new HashMap<>();
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static long serverTick = 0;

    private BossRaidManager() { }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            serverTick++;
            if (activeSessions.isEmpty()) { return; }

            tickSessions(server);
        });

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, amount) -> {
            if (!(entity instanceof ServerPlayer player)) {
                return true;
            }

            BossRaidSession session = getSessionByPlayer(player.getUUID());
            if (session == null || session.getState() != BossRaidSession.STATE.IN_PROGRESS) { return true; }
            if (session.isPlayerDead(player.getUUID())) { return true; }

            session.onPlayerDeathCancelled(player);
            if (session.checkWipe()) { handleWipe(player.level().getServer(), session); }

            return false;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof EnderDragon dragon)) { return; }
            if (dragon.level().isClientSide()) { return; }

            ResourceKey<Level> dimension = dragon.level().dimension();
            for (BossRaidSession session : activeSessions.values()) {
                if (session.getRaidType().getDimension().equals(dimension) && session.getState() == BossRaidSession.STATE.IN_PROGRESS) {
                    session.onDragonKilled(dragon.level().getServer());
                    break;
                }
            }
        });
    }

    private static void tickSessions(MinecraftServer server) {
        Iterator<Map.Entry<BOSS_RAID_TYPE, BossRaidSession>> iterator = activeSessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BOSS_RAID_TYPE, BossRaidSession> entry = iterator.next();
            BossRaidSession session = entry.getValue();

            boolean finished = session.tick(server);
            if (finished) {
                if (session.getState() == BossRaidSession.STATE.COMPLETING) {
                    for (UUID id : session.getParticipants()) {
                        cooldowns.put(id, serverTick);
                    }
                }
                iterator.remove();
                broadcastRaidStatus(server);
            }
        }
    }

    private static void handleWipe(MinecraftServer server, BossRaidSession session) {
        BOSS_RAID_TYPE raidType = session.getRaidType();
        List<UUID> participants = session.getParticipants();

        session.handleFail(server);
        activeSessions.remove(raidType);
        broadcastRaidStatus(server);

        for (UUID id : participants) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                player.kill(player.level());
            }
        }
    }

    public static void openRaidSelect(ServerPlayer player) {
        IHardCoreInfo info = ModComponents.HARDCORE_INFO.get(player);
        if (!info.isStarted()) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "하드코어를 시작해야 레이드에 참가할 수 있습니다.");
            return;
        }

        HARDCORE_TYPE playerType = info.getHardcoreType();
        BOSS_RAID_TYPE matchingRaid = BOSS_RAID_TYPE.fromHardCoreType(playerType);
        String playerRaidTypeKey = matchingRaid != null ? matchingRaid.getKey() : "";

        BossRaidReward vanillaReward = BossRaidReward.getReward(BOSS_RAID_TYPE.VANILLA);
        BossRaidReward expertReward = BossRaidReward.getReward(BOSS_RAID_TYPE.EXPERT);

        S2C_OpenRaidSelectPayload payload = new S2C_OpenRaidSelectPayload(activeSessions.containsKey(BOSS_RAID_TYPE.VANILLA), activeSessions.containsKey(BOSS_RAID_TYPE.EXPERT), playerRaidTypeKey, getCooldownRemaining(player.getUUID()), vanillaReward.getItems(), vanillaReward.getCurrency(), expertReward.getItems(), expertReward.getCurrency());

        ServerPlayNetworking.send(player, payload);
    }

    public static void requestStart(MinecraftServer server, ServerPlayer player, BOSS_RAID_TYPE raidType) {
        IHardCoreInfo info = ModComponents.HARDCORE_INFO.get(player);
        if (!info.isStarted()) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "하드코어를 시작해야 레이드에 참가할 수 있습니다.");
            return;
        }

        if (info.getHardcoreType() != raidType.getRequiredType()) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), String.format("%s 레이드는 %s 난이도만 도전할 수 있습니다.", raidType.getDisplayName(), raidType.getRequiredType().getDisplayName()));
            return;
        }

        if (getCooldownRemaining(player.getUUID()) > 0) {
            long remaining = getCooldownRemaining(player.getUUID());
            long minutes = (remaining / 20) / 60;
            long seconds = (remaining / 20) % 60;
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), String.format("레이드 쿨타임이 남아있습니다. (%d분 %d초)", minutes, seconds));
            return;
        }

        if (activeSessions.containsKey(raidType)) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), String.format("%s 레이드가 이미 진행 중입니다.", raidType.getDisplayName()));
            return;
        }

        if (getSessionByPlayer(player.getUUID()) != null) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "이미 레이드에 참여 중입니다.");
            return;
        }

        PartyManager partyManager = PartyManager.getServerState(server);
        Party party = partyManager.getPartyByPlayer(player.getUUID());
        List<UUID> participants;

        if (party != null) {
            if (!party.isLeader(player.getUUID())) {
                PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티장만 레이드를 시작할 수 있습니다.");
                return;
            }

            String validationError = validatePartyMembers(server, party, raidType);
            if (validationError != null) {
                PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), validationError);
                return;
            }

            participants = new ArrayList<>(party.getMembers());
        }
        else {
            participants = List.of(player.getUUID());
        }

        BossRaidSession session = new BossRaidSession(raidType, player.getUUID(), participants);
        activeSessions.put(raidType, session);
        broadcastRaidStatus(server);

        if (session.getState() == BossRaidSession.STATE.READY_CHECK) {
            for (UUID memberID : participants) {
                ServerPlayer member = server.getPlayerList().getPlayer(memberID);
                if (member != null) {
                    S2C_OpenRaidReadyPayload payload = new S2C_OpenRaidReadyPayload(
                        raidType.getKey(),
                        raidType.getDisplayName(),
                        memberID.equals(player.getUUID())
                    );
                    ServerPlayNetworking.send(member, payload);
                }
            }
            session.setReady(server, player.getUUID(), true);
        }
        else {
            session.enterRaid(server);
        }
    }

    private static String validatePartyMembers(MinecraftServer server, Party party, BOSS_RAID_TYPE raidType) {
        for (UUID memberID : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberID);
            if (member == null) { return "파티원 중 오프라인인 플레이어가 있습니다."; }

            IHardCoreInfo memberInfo = ModComponents.HARDCORE_INFO.get(member);
            String name = member.getDisplayName().getString();
            if (!memberInfo.isStarted()) { return String.format("&7%s&r님이 하드코어를 시작하지 않았습니다.", name); }
            if (memberInfo.getHardcoreType() != raidType.getRequiredType()) { return String.format("&7%s&r님의 난이도(%s)가 레이드 조건(%s)과 맞지 않습니다.", name, memberInfo.getHardcoreType().getDisplayName(), raidType.getRequiredType().getDisplayName()); }
            if (getCooldownRemaining(memberID) > 0) { return String.format("&7%s&r님의 레이드 쿨타임이 남아있습니다.", name); }
            if (getSessionByPlayer(memberID) != null) { return String.format("&7%s&r님이 이미 다른 레이드에 참여 중입니다.", name); }
        }

        return null;
    }

    public static void handleReadyResponse(MinecraftServer server, ServerPlayer player, boolean ready) {
        BossRaidSession session = getSessionByPlayer(player.getUUID());
        if (session == null || session.getState() != BossRaidSession.STATE.READY_CHECK) { return; }

        if (ready) {
            session.setReady(server, player.getUUID(), true);
        }
        else {
            session.cancelByPlayer(server, player.getUUID());
            activeSessions.remove(session.getRaidType());
            broadcastRaidStatus(server);
        }
    }

    public static BossRaidSession getSessionByPlayer(UUID playerID) {
        for (BossRaidSession session : activeSessions.values()) {
            if (session.isParticipant(playerID)) {
                return session;
            }
        }

        return null;
    }

    public static boolean isInRaid(UUID playerID) { return getSessionByPlayer(playerID) != null; }

    public static long getCooldownRemaining(UUID playerID) {
        Long lastClear = cooldowns.get(playerID);
        if (lastClear == null) { return 0; }

        return Math.max(0, COOLDOWN_TICKS - (serverTick - lastClear));
    }

    public static void clearCooldown(UUID playerID) {
        cooldowns.remove(playerID);
    }

    public static void clearAllSessions() {
        activeSessions.clear();
        cooldowns.clear();
    }

    private static void broadcastRaidStatus(MinecraftServer server) {
        S2C_RaidSelectUpdatePayload payload = new S2C_RaidSelectUpdatePayload(activeSessions.containsKey(BOSS_RAID_TYPE.VANILLA), activeSessions.containsKey(BOSS_RAID_TYPE.EXPERT));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void forfeitRaid(MinecraftServer server, ServerPlayer player) {
        BossRaidSession session = getSessionByPlayer(player.getUUID());
        if (session == null) { return; }

        BOSS_RAID_TYPE raidType = session.getRaidType();
        List<UUID> participants = session.getParticipants();
        session.handleFail(server);
        activeSessions.remove(raidType);
        broadcastRaidStatus(server);

        for (UUID id : participants) {
            ServerPlayer member = server.getPlayerList().getPlayer(id);
            if (member != null) {
                if (member.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                    member.setGameMode(GameType.SURVIVAL);
                }
                member.kill(member.level());
            }
        }

        for (UUID id : participants) {
            ServerPlayer member = server.getPlayerList().getPlayer(id);
            if (member != null) {
                PykeLib.sendSystemMessage(member, COLOR.RED.getColor(), String.format("&7%s&r님이 레이드를 포기하여 실패 처리되었습니다.", player.getDisplayName().getString()));
            }
        }
    }

    public static void forceEndRaid(MinecraftServer server, BOSS_RAID_TYPE raidType, ServerPlayer admin) {
        BossRaidSession session = activeSessions.get(raidType);
        if (session == null) {
            PykeLib.sendSystemMessage(admin, COLOR.RED.getColor(), String.format("%s 레이드가 진행 중이 아닙니다.", raidType.getDisplayName()));
            return;
        }

        List<UUID> participants = session.getParticipants();

        for (UUID id : participants) {
            ServerPlayer member = server.getPlayerList().getPlayer(id);
            if (member != null) {
                if (member.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                    member.setGameMode(GameType.SURVIVAL);
                    member.setHealth(member.getMaxHealth());
                }
                ModComponents.HARDCORE_INFO.get(member).teleportPrevPosition();
                PykeLib.sendSystemMessage(member, COLOR.YELLOW.getColor(), "관리자에 의해 레이드가 강제 종료되었습니다.");
            }
        }

        activeSessions.remove(raidType);
        broadcastRaidStatus(server);
        PykeLib.sendSystemMessage(admin, COLOR.LIME.getColor(), String.format("%s 레이드를 강제 종료했습니다. (참가자 %d명 복귀)", raidType.getDisplayName(), participants.size()));
    }
}