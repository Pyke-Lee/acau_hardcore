package kr.pyke.acau_hardcore.party;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Party {
    public static final int MAX_MEMBERS = 5;

    private final UUID partyID;
    private UUID leaderID;
    private final List<UUID> members;
    private final String teamName;

    public static final Codec<Party> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("party_id").forGetter(Party::getPartyID),
        UUIDUtil.CODEC.fieldOf("leader_id").forGetter(Party::getLeaderID),
        UUIDUtil.CODEC.listOf().fieldOf("members").forGetter(Party::getMembers),
        Codec.STRING.fieldOf("team_name").forGetter(Party::getTeamName)
    ).apply(instance, Party::new));

    public Party(UUID leaderID, String teamName) {
        this.partyID = UUID.randomUUID();
        this.leaderID = leaderID;
        this.members = new ArrayList<>();
        this.members.add(leaderID);
        this.teamName = teamName;
    }

    private Party(UUID partyID, UUID leaderID, List<UUID> members, String teamName) {
        this.partyID = partyID;
        this.leaderID = leaderID;
        this.members = new ArrayList<>(members);
        this.teamName = teamName;
    }

    public UUID getPartyID() { return this.partyID; }
    public UUID getLeaderID() { return this.leaderID; }
    public List<UUID> getMembers() { return Collections.unmodifiableList(this.members); }
    public String getTeamName() { return this.teamName; }

    public void setLeaderID(UUID leaderID) { this.leaderID = leaderID; }

    public boolean isLeader(UUID playerID) { return this.leaderID.equals(playerID); }
    public boolean isMember(UUID playerID) { return this.members.contains(playerID); }
    public boolean isFull() { return this.members.size() >= MAX_MEMBERS; }

    public int getMemberCount() { return members.size(); }
    public boolean addMember(UUID playerID) {
        if (isFull() || isMember(playerID)) { return false; }

        members.add(playerID);
        return true;
    }
    public boolean removeMember(UUID playerID) { return members.remove(playerID); }
}
