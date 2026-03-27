package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.helprequest.HelpRequestData;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_HelpRequestRemovePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public record C2S_HelpRequestPurgePayload() implements CustomPacketPayload {
    public static final Type<C2S_HelpRequestPurgePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_help_purge"));

    public static final StreamCodec<FriendlyByteBuf, C2S_HelpRequestPurgePayload> STREAM_CODEC = StreamCodec.unit(new C2S_HelpRequestPurgePayload());

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_HelpRequestPurgePayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            HelpRequestData data = HelpRequestData.get(context.server());
            List<UUID> removed = data.removeCompleted();

            for (UUID id : removed) {
                S2C_HelpRequestRemovePayload response = new S2C_HelpRequestRemovePayload(id);
                for (ServerPlayer p : context.server().getPlayerList().getPlayers()) {
                    ServerPlayNetworking.send(p, response);
                }
            }
        });
    }
}