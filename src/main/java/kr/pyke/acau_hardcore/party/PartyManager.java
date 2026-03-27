package kr.pyke.acau_hardcore.party;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_PartySyncPayload;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

import java.util.*;

public class PartyManager extends SavedData {
    private static final String DATA_NAME = "party_data";

    private final Map<UUID, Party> parties = new HashMap<>();
    private final Map<UUID, UUID> playerPartyMap = new HashMap<>();
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();

    public static final Codec<PartyManager> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Party.CODEC.listOf().optionalFieldOf("parties", List.of())
            .forGetter(manager -> new ArrayList<>(manager.parties.values()))
    ).apply(instance, PartyManager::new));

    public static final SavedDataType<PartyManager> TYPE = new SavedDataType<>(DATA_NAME, PartyManager::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    public PartyManager() { }

    private PartyManager(List<Party> partyList) {
        for (Party party : partyList) {
            parties.put(party.getPartyID(), party);
            for (UUID memberId : party.getMembers()) {
                playerPartyMap.put(memberId, party.getPartyID());
            }
        }
    }

    public static PartyManager getServerState(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Party getPartyByPlayer(UUID playerID) {
        UUID partyID = playerPartyMap.get(playerID);
        if (partyID == null) { return null; }

        return parties.get(partyID);
    }

    public Party getPartyByID(UUID partyID) {
        return parties.get(partyID);
    }

    public boolean isInParty(UUID playerID) {
        return playerPartyMap.containsKey(playerID);
    }

    public boolean hasPendingInvite(UUID playerID) {
        return pendingInvites.containsKey(playerID);
    }

    public UUID getPendingInvitePartyID(UUID playerID) {
        return pendingInvites.get(playerID);
    }

    public Party createParty(MinecraftServer server, ServerPlayer leader, String partyName) {
        if (isInParty(leader.getUUID())) { return null; }

        Party party = new Party(leader.getUUID(), partyName);
        parties.put(party.getPartyID(), party);
        playerPartyMap.put(leader.getUUID(), party.getPartyID());

        createTeam(server, party);
        addPlayerToTeam(server, party, leader);

        setDirty();
        syncPartyToMembers(server, party);
        return party;
    }

    public boolean invitePlayer(MinecraftServer server, ServerPlayer inviter, ServerPlayer target) {
        Party party = getPartyByPlayer(inviter.getUUID());
        if (party == null || !party.isLeader(inviter.getUUID())) { return false; }
        if (party.isFull()) {
            PykeLib.sendSystemMessage(inviter, COLOR.RED.getColor(), String.format("파티가 가득 찼습니다. (최대 %s명)", Party.MAX_MEMBERS));
            return false;
        }

        if (isInParty(target.getUUID())) {
            PykeLib.sendSystemMessage(inviter, COLOR.RED.getColor(), "해당 플레이어는 이미 파티에 소속되어 있습니다.");
            return false;
        }

        if (pendingInvites.containsKey(target.getUUID())) {
            PykeLib.sendSystemMessage(inviter, COLOR.RED.getColor(), "해당 플레이어에게 이미 파티 초대가 발송되어 있습니다.");
            return false;
        }

        pendingInvites.put(target.getUUID(), party.getPartyID());
        PykeLib.sendSystemMessage(inviter, COLOR.LIME.getColor(), String.format("&7%s&r님에게 파티 초대를 보냈습니다.", target.getDisplayName().getString()));
        PykeLib.sendSystemMessage(target, COLOR.AQUA.getColor(), String.format("&7%s&r님이 파티에 초대했습니다. &a/파티 수락&f 또는 &c/파티 거절", inviter.getDisplayName().getString()));

        UUID targetUUID = target.getUUID();
        UUID partyID = party.getPartyID();
        server.execute(() -> server.execute(new TickTask(server.getTickCount() + 600, () -> {
            if (pendingInvites.containsKey(targetUUID) && partyID.equals(pendingInvites.get(targetUUID))) {
                pendingInvites.remove(targetUUID);
                ServerPlayer onlineTarget = server.getPlayerList().getPlayer(targetUUID);
                if (onlineTarget != null) {
                    PykeLib.sendSystemMessage(onlineTarget, COLOR.GRAY.getColor(), "파티 초대가 만료되었습니다.");
                }
            }
        })));

        setDirty();
        return true;
    }

    public boolean acceptInvite(MinecraftServer server, ServerPlayer player) {
        UUID partyID = pendingInvites.remove(player.getUUID());
        if (partyID == null) { return false; }

        Party party = parties.get(partyID);
        if (party == null) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "해당 파티가 더 이상 존재하지 않습니다.");
            return false;
        }
        if (party.isFull()) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티가 가득 찼습니다.");
            return false;
        }
        if (isInParty(player.getUUID())) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "이미 파티에 소속되어 있습니다.");
            return false;
        }

        party.addMember(player.getUUID());
        playerPartyMap.put(player.getUUID(), partyID);
        addPlayerToTeam(server, party, player);

        notifyPartyMembers(server, party, COLOR.LIME.getColor(), String.format("&7%s&r님이 파티에 참가했습니다.", player.getDisplayName().getString()));

        setDirty();
        syncPartyToMembers(server, party);
        return true;
    }

    public boolean rejectInvite(MinecraftServer server, ServerPlayer player) {
        UUID partyID = pendingInvites.remove(player.getUUID());
        if (partyID == null) { return false; }

        Party party = parties.get(partyID);
        if (party != null) {
            ServerPlayer leader = server.getPlayerList().getPlayer(party.getLeaderID());
            if (leader != null) {
                PykeLib.sendSystemMessage(leader, COLOR.RED.getColor(), String.format("&e%s&f님이 파티 초대를 거절했습니다.", player.getDisplayName().getString()));
            }
        }

        PykeLib.sendSystemMessage(player, COLOR.GRAY.getColor(), "파티 초대를 거절했습니다.");
        return true;
    }

    public boolean leaveParty(MinecraftServer server, ServerPlayer player) {
        Party party = getPartyByPlayer(player.getUUID());
        if (party == null) { return false; }

        if (party.isLeader(player.getUUID())) {
            if (party.getMemberCount() > 1) {
                PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "파티장은 탈퇴할 수 없습니다. &e/파티 위임&f 또는 &e/파티 해산&f을 사용하세요.");
                return false;
            }
            else {
                return disbandParty(server, player);
            }
        }

        party.removeMember(player.getUUID());
        playerPartyMap.remove(player.getUUID());
        removePlayerFromTeam(server, party, player);

        PykeLib.sendSystemMessage(player, COLOR.GRAY.getColor(), "파티에서 탈퇴했습니다.");
        notifyPartyMembers(server, party, COLOR.YELLOW.getColor(), String.format("&7%s&r님이 파티에서 탈퇴했습니다.", player.getDisplayName().getString()));

        setDirty();
        syncPartyToMembers(server, party);
        syncEmptyParty(player);
        return true;
    }

    public boolean kickMember(MinecraftServer server, ServerPlayer leader, ServerPlayer target) {
        Party party = getPartyByPlayer(leader.getUUID());
        if (party == null || !party.isLeader(leader.getUUID())) { return false; }
        if (!party.isMember(target.getUUID())) {
            PykeLib.sendSystemMessage(leader, COLOR.RED.getColor(), "해당 플레이어는 파티원이 아닙니다.");
            return false;
        }
        if (party.isLeader(target.getUUID())) {
            PykeLib.sendSystemMessage(leader, COLOR.RED.getColor(), "자기 자신을 추방할 수 없습니다.");
            return false;
        }

        party.removeMember(target.getUUID());
        playerPartyMap.remove(target.getUUID());
        removePlayerFromTeam(server, party, target);

        PykeLib.sendSystemMessage(target, COLOR.RED.getColor(), "파티에서 추방당했습니다.");
        notifyPartyMembers(server, party, COLOR.YELLOW.getColor(), String.format("&7%s&r님이 파티에서 추방되었습니다.", target.getDisplayName().getString()));

        setDirty();
        syncPartyToMembers(server, party);
        syncEmptyParty(target);
        return true;
    }

    public boolean disbandParty(MinecraftServer server, ServerPlayer leader) {
        Party party = getPartyByPlayer(leader.getUUID());
        if (party == null || !party.isLeader(leader.getUUID())) { return false; }

        List<UUID> membersCopy = new ArrayList<>(party.getMembers());
        for (UUID memberId : membersCopy) {
            playerPartyMap.remove(memberId);
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                syncEmptyParty(member);
            }
        }

        removeTeam(server, party);
        parties.remove(party.getPartyID());

        for (UUID memberId : membersCopy) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                PykeLib.sendSystemMessage(member, COLOR.RED.getColor(), "파티가 해산되었습니다.");
            }
        }

        setDirty();
        return true;
    }

    public boolean transferLeader(MinecraftServer server, ServerPlayer leader, ServerPlayer target) {
        Party party = getPartyByPlayer(leader.getUUID());
        if (party == null || !party.isLeader(leader.getUUID())) {
            return false;
        }
        if (!party.isMember(target.getUUID())) {
            PykeLib.sendSystemMessage(leader, COLOR.RED.getColor(), "해당 플레이어는 파티원이 아닙니다.");
            return false;
        }
        if (party.isLeader(target.getUUID())) {
            PykeLib.sendSystemMessage(leader, COLOR.RED.getColor(), "이미 파티장입니다.");
            return false;
        }

        party.setLeaderID(target.getUUID());
        notifyPartyMembers(server, party, COLOR.AQUA.getColor(), String.format("&7%s&r님이 새로운 파티장이 되었습니다.", target.getDisplayName().getString()));

        setDirty();
        syncPartyToMembers(server, party);
        return true;
    }

    public void teleportParty(MinecraftServer server, UUID partyID, ServerLevel targetLevel, Vec3 pos, float yaw, float pitch) {
        Party party = parties.get(partyID);
        if (party == null) { return; }

        TeleportTransition transition = new TeleportTransition(targetLevel, pos, Vec3.ZERO, yaw, pitch, TeleportTransition.DO_NOTHING);
        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.teleport(transition);
            }
        }
    }

    public void teleportParty(MinecraftServer server, UUID partyId, TeleportTransition transition) {
        Party party = parties.get(partyId);
        if (party == null) { return; }

        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.teleport(transition);
            }
        }
    }

    public void teleportPartyByPlayer(MinecraftServer server, UUID playerID, TeleportTransition transition) {
        Party party = getPartyByPlayer(playerID);
        if (party == null) { return; }

        teleportParty(server, party.getPartyID(), transition);
    }

    public void tickHealthSync(MinecraftServer server) {
        for (Party party : parties.values()) {
            syncPartyToMembers(server, party);
        }
    }

    private void createTeam(MinecraftServer server, Party party) {
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(party.getTeamName());

        if (team == null) {
            team = scoreboard.addPlayerTeam(party.getTeamName());
        }

        team.setAllowFriendlyFire(false);
        team.setColor(ChatFormatting.RESET);
        team.setNameTagVisibility(Team.Visibility.ALWAYS);
        team.setCollisionRule(Team.CollisionRule.ALWAYS);
    }

    private void addPlayerToTeam(MinecraftServer server, Party party, ServerPlayer player) {
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(party.getTeamName());
        if (team != null) {
            scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
        }
    }

    private void removePlayerFromTeam(MinecraftServer server, Party party, ServerPlayer player) {
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(party.getTeamName());
        if (team != null) {
            scoreboard.removePlayerFromTeam(player.getScoreboardName(), team);
        }
    }

    private void removeTeam(MinecraftServer server, Party party) {
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(party.getTeamName());
        if (team != null) {
            scoreboard.removePlayerTeam(team);
        }
    }

    public void restoreTeams(MinecraftServer server) {
        for (Party party : parties.values()) {
            createTeam(server, party);
            for (UUID memberId : party.getMembers()) {
                ServerPlayer player = server.getPlayerList().getPlayer(memberId);
                if (player != null) {
                    addPlayerToTeam(server, party, player);
                }
            }
        }
    }

    public void onPlayerJoin(MinecraftServer server, ServerPlayer player) {
        Party party = getPartyByPlayer(player.getUUID());
        if (party != null) {
            addPlayerToTeam(server, party, player);
            syncPartyToMembers(server, party);
        }
    }

    public void onPlayerLeave(MinecraftServer server, ServerPlayer player) {
        Party party = getPartyByPlayer(player.getUUID());
        if (party != null) {
            syncPartyToMembers(server, party);
        }
        pendingInvites.remove(player.getUUID());
    }

    private void syncPartyToMembers(MinecraftServer server, Party party) {
        List<S2C_PartySyncPayload.MemberData> memberDataList = new ArrayList<>();

        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            String name;
            float health;
            float maxHealth;
            boolean online;

            if (member != null) {
                name = member.getDisplayName().getString();
                health = member.getHealth();
                maxHealth = member.getMaxHealth();
                online = true;
            }
            else {
                name = "오프라인";
                health = 0f;
                maxHealth = 20f;
                online = false;
            }

            memberDataList.add(new S2C_PartySyncPayload.MemberData(memberId, name, health, maxHealth, online));
        }

        S2C_PartySyncPayload payload = new S2C_PartySyncPayload(true, party.getLeaderID(), memberDataList);

        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                ServerPlayNetworking.send(member, payload);
            }
        }
    }

    private void syncEmptyParty(ServerPlayer player) {
        S2C_PartySyncPayload payload = new S2C_PartySyncPayload(false, new UUID(0, 0), List.of());
        ServerPlayNetworking.send(player, payload);
    }

    private void notifyPartyMembers(MinecraftServer server, Party party, int color, String message) {
        for (UUID memberId : party.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                PykeLib.sendSystemMessage(member, color, message);
            }
        }
    }
}
