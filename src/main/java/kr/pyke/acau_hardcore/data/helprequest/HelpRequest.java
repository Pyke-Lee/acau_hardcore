package kr.pyke.acau_hardcore.data.helprequest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kr.pyke.acau_hardcore.type.HELP_REQUEST_STATE;
import net.minecraft.core.UUIDUtil;

import java.util.Optional;
import java.util.UUID;

/**
 * 도움 요청 데이터입니다.
 *
 * @param requestId    요청 고유 ID
 * @param requesterUuid 요청자 UUID
 * @param message       요청 메시지
 * @param requestTime   요청 시간 (epoch millis)
 * @param status        처리 상태
 * @param handlerUuid   담당자 UUID (없으면 empty)
 */
public record HelpRequest(UUID requestId, UUID requesterUuid, String message, long requestTime, HELP_REQUEST_STATE status, Optional<UUID> handlerUuid) {
    public static final Codec<HelpRequest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("request_id").forGetter(HelpRequest::requestId),
        UUIDUtil.CODEC.fieldOf("requester_uuid").forGetter(HelpRequest::requesterUuid),
        Codec.STRING.fieldOf("message").forGetter(HelpRequest::message),
        Codec.LONG.fieldOf("request_time").forGetter(HelpRequest::requestTime),
        HELP_REQUEST_STATE.CODEC.fieldOf("status").forGetter(HelpRequest::status),
        UUIDUtil.CODEC.optionalFieldOf("handler_uuid").forGetter(HelpRequest::handlerUuid)
    ).apply(instance, HelpRequest::new));

    /**
     * 새 요청을 생성합니다 (대기 상태, 담당자 없음).
     */
    public static HelpRequest create(UUID requesterUuid, String message) {
        return new HelpRequest(UUID.randomUUID(), requesterUuid, message, System.currentTimeMillis(), HELP_REQUEST_STATE.WAITING, Optional.empty());
    }

    /**
     * 상태를 변경합니다.
     * - 대기 → 처리/완료: 담당자 할당
     * - 처리 → 완료: 기존 담당자 유지
     * - → 대기: 담당자 제거
     */
    public HelpRequest withStatus(HELP_REQUEST_STATE newStatus, UUID operatorUuid) {
        Optional<UUID> newHandler;

        if (newStatus == HELP_REQUEST_STATE.WAITING) { newHandler = Optional.empty(); }
        else if (this.handlerUuid.isPresent()) { newHandler = this.handlerUuid; }
        else { newHandler = Optional.of(operatorUuid); }

        return new HelpRequest(this.requestId, this.requesterUuid, this.message, this.requestTime, newStatus, newHandler);
    }
}