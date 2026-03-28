package kr.pyke.acau_hardcore.data.cache;

import kr.pyke.acau_hardcore.data.helprequest.HelpRequest;
import kr.pyke.acau_hardcore.data.shop.ShopData;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_PartySyncPayload;
import kr.pyke.acau_hardcore.type.HELP_REQUEST_STATE;
import net.minecraft.client.Minecraft;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AcauHardCoreCache {
    public static Map<UUID, String> displayNames = new HashMap<>();

    public static Map<String, ShopData> SHOPS = new HashMap<>();

    private static final Map<UUID, HelpRequest> helpRequests = new LinkedHashMap<>();

    private static final List<Runnable> helpListeners = new CopyOnWriteArrayList<>();
    private static final List<Runnable> nameListeners = new CopyOnWriteArrayList<>();

    private static boolean partyActive = false;
    private static UUID partyLeaderId = null;
    private static final List<S2C_PartySyncPayload.MemberData> partyMembers = new ArrayList<>();
    private static final Set<UUID> partyMemberUUIDs = new HashSet<>();

    public static void addHelpListener(Runnable listener) { helpListeners.add(listener); }
    public static void removeHelpListener(Runnable listener) { helpListeners.remove(listener); }
    private static void notifyHelpListeners() { helpListeners.forEach(Runnable::run); }

    public static void addNameListener(Runnable listener) { nameListeners.add(listener); }
    public static void removeNameListener(Runnable listener) { nameListeners.remove(listener); }
    private static void notifyNameListeners() { nameListeners.forEach(Runnable::run); }

    public static void setAll(List<HelpRequest> requests) {
        helpRequests.clear();
        for (HelpRequest request : requests) {
            helpRequests.put(request.requestId(), request);
        }
        notifyHelpListeners();
    }

    public static void update(HelpRequest request) {
        helpRequests.put(request.requestId(), request);
        notifyHelpListeners();
    }

    public static void remove(UUID requestId) {
        helpRequests.remove(requestId);
        notifyHelpListeners();
    }

    public static List<HelpRequest> getAll() {
        List<HelpRequest> list = new ArrayList<>(helpRequests.values());
        list.sort(Comparator.comparingLong(HelpRequest::requestTime));
        return list;
    }

    public static List<HelpRequest> getByStatus(HELP_REQUEST_STATE status) {
        return getAll().stream().filter(r -> r.status() == status).toList();
    }

    public static Optional<HelpRequest> get(UUID requestId) {
        return Optional.ofNullable(helpRequests.get(requestId));
    }

    public static int getWaitingCount() {
        return (int) helpRequests.values().stream().filter(r -> r.status() == HELP_REQUEST_STATE.WAITING).count();
    }

    public static void clear() {
        helpRequests.clear();
    }

    public static void putDisplayName(UUID uuid, String name) {
        displayNames.put(uuid, name);
        notifyNameListeners();
    }

    public static void putAllDisplayNames(Map<UUID, String> names) {
        displayNames.putAll(names);
        notifyNameListeners();
    }

    /** 캐시에 없는 UUID 목록을 반환합니다. */
    public static List<UUID> getMissingNames(Collection<UUID> uuids) {
        List<UUID> missing = new ArrayList<>();
        for (UUID uuid : uuids) {
            if (!displayNames.containsKey(uuid)) {
                missing.add(uuid);
            }
        }
        return missing;
    }

    public static void updateParty(S2C_PartySyncPayload payload) {
        partyActive = payload.inParty();
        partyLeaderId = payload.leaderId();

        partyMembers.clear();
        partyMemberUUIDs.clear();

        if (partyActive) {
            partyMembers.addAll(payload.members());
            for (S2C_PartySyncPayload.MemberData member : payload.members()) {
                partyMemberUUIDs.add(member.uuid());
            }
        }
    }

    public static void clearParty() {
        partyActive = false;
        partyLeaderId = null;
        partyMembers.clear();
        partyMemberUUIDs.clear();
    }

    public static boolean isInParty() { return partyActive; }
    public static UUID getPartyLeaderId() { return partyLeaderId; }
    public static List<S2C_PartySyncPayload.MemberData> getPartyMembers() { return Collections.unmodifiableList(partyMembers); }

    public static List<S2C_PartySyncPayload.MemberData> getOtherPartyMembers() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) { return partyMembers; }

        UUID localUUID = mc.player.getUUID();
        List<S2C_PartySyncPayload.MemberData> others = new ArrayList<>();
        for (S2C_PartySyncPayload.MemberData member : partyMembers) {
            if (!member.uuid().equals(localUUID)) {
                others.add(member);
            }
        }

        return others;
    }

    public static boolean shouldGlow(UUID playerUUID) {
        if (!partyActive) { return false; }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(playerUUID)) { return false; }

        return partyMemberUUIDs.contains(playerUUID);
    }
}

