package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.helprequest.HelpRequest;
import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record S2C_HelpRequestSyncAllPayload(List<HelpRequest> requests) implements CustomPacketPayload {
    public static final Type<S2C_HelpRequestSyncAllPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_help_sync_all"));

    public static final StreamCodec<FriendlyByteBuf, S2C_HelpRequestSyncAllPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.fromCodec(HelpRequest.CODEC.listOf()),
        S2C_HelpRequestSyncAllPayload::requests,
        S2C_HelpRequestSyncAllPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_HelpRequestSyncAllPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> AcauHardCoreCache.setAll(payload.requests()));
    }
}