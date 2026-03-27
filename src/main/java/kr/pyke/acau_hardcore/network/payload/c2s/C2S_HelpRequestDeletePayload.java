package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.helprequest.HelpRequestData;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_HelpRequestRemovePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record C2S_HelpRequestDeletePayload(UUID requestId) implements CustomPacketPayload {
    public static final Type<C2S_HelpRequestDeletePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_help_delete"));

    public static final StreamCodec<FriendlyByteBuf, C2S_HelpRequestDeletePayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        C2S_HelpRequestDeletePayload::requestId,
        C2S_HelpRequestDeletePayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_HelpRequestDeletePayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            HelpRequestData data = HelpRequestData.get(context.server());
            if (data.removeRequest(payload.requestId())) {
                S2C_HelpRequestRemovePayload response = new S2C_HelpRequestRemovePayload(payload.requestId());
                for (ServerPlayer p : context.server().getPlayerList().getPlayers()) {
                    ServerPlayNetworking.send(p, response);
                }
            }
        });
    }
}