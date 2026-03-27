package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.helprequest.HelpRequest;
import kr.pyke.acau_hardcore.data.helprequest.HelpRequestData;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_HelpRequestUpdatePayload;
import kr.pyke.acau_hardcore.type.HELP_REQUEST_STATE;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;

public record C2S_HelpRequestChangeStatusPayload(UUID requestId, String statusKey) implements CustomPacketPayload {
    public static final Type<C2S_HelpRequestChangeStatusPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_help_change_status"));

    public static final StreamCodec<FriendlyByteBuf, C2S_HelpRequestChangeStatusPayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, C2S_HelpRequestChangeStatusPayload::requestId,
        ByteBufCodecs.STRING_UTF8, C2S_HelpRequestChangeStatusPayload::statusKey,
        C2S_HelpRequestChangeStatusPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_HelpRequestChangeStatusPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer operator = context.player();

            HelpRequestData data = HelpRequestData.get(context.server());
            HELP_REQUEST_STATE newStatus = HELP_REQUEST_STATE.byKey(payload.statusKey());
            Optional<HelpRequest> updated = data.updateStatus(payload.requestId(), newStatus, operator.getUUID());

            updated.ifPresent(request -> {
                S2C_HelpRequestUpdatePayload response = new S2C_HelpRequestUpdatePayload(request);
                for (ServerPlayer p : context.server().getPlayerList().getPlayers()) {
                    ServerPlayNetworking.send(p, response);
                }
            });
        });
    }
}