package kr.pyke.acau_hardcore.data.helprequest;

import com.mojang.serialization.Codec;
import kr.pyke.acau_hardcore.type.HELP_REQUEST_STATE;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

/**
 * 도움 요청 데이터를 서버에 영구 저장합니다.
 */
public class HelpRequestData extends SavedData {
    private static final String FILE_NAME = "help_requests";

    private final Map<UUID, HelpRequest> requests = new LinkedHashMap<>();

    public static final Codec<HelpRequestData> CODEC = Codec.list(HelpRequest.CODEC).xmap(
        list -> {
            HelpRequestData data = new HelpRequestData();
            for (HelpRequest request : list) {
                data.requests.put(request.requestId(), request);
            }
            return data;
        },
        data -> List.copyOf(data.requests.values())
    );

    public static final SavedDataType<HelpRequestData> TYPE = new SavedDataType<>(FILE_NAME, HelpRequestData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    public static HelpRequestData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();

        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    /** 요청 추가 */
    public HelpRequest addRequest(UUID requesterUuid, String message) {
        HelpRequest request = HelpRequest.create(requesterUuid, message);
        requests.put(request.requestId(), request);
        setDirty();
        return request;
    }

    /** 요청 조회 */
    public Optional<HelpRequest> getRequest(UUID requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }

    /** 전체 요청 목록 (최신순) */
    public List<HelpRequest> getAllRequests() {
        List<HelpRequest> list = new ArrayList<>(requests.values());
        list.sort(Comparator.comparingLong(HelpRequest::requestTime).reversed());
        return list;
    }

    /** 상태별 필터링 */
    public List<HelpRequest> getRequestsByStatus(HELP_REQUEST_STATE status) {
        return getAllRequests().stream().filter(r -> r.status() == status).toList();
    }

    /** 상태 변경 */
    public Optional<HelpRequest> updateStatus(UUID requestId, HELP_REQUEST_STATE newStatus, UUID operatorUuid) {
        HelpRequest existing = requests.get(requestId);
        if (existing == null) { return Optional.empty(); }

        HelpRequest updated = existing.withStatus(newStatus, operatorUuid);
        requests.put(requestId, updated);
        setDirty();
        return Optional.of(updated);
    }

    /** 단일 요청 삭제 */
    public boolean removeRequest(UUID requestId) {
        boolean removed = requests.remove(requestId) != null;
        if (removed) { setDirty(); }
        return removed;
    }

    /** 완료된 요청 전체 삭제 */
    public List<UUID> removeCompleted() {
        List<UUID> removed = new ArrayList<>();
        requests.entrySet().removeIf(entry -> {
            if (entry.getValue().status() == HELP_REQUEST_STATE.COMPLETED) {
                removed.add(entry.getKey());
                return true;
            }
            return false;
        });
        if (!removed.isEmpty()) { setDirty(); }
        return removed;
    }

    /** 전체 요청 수 */
    public int size() {
        return requests.size();
    }
}