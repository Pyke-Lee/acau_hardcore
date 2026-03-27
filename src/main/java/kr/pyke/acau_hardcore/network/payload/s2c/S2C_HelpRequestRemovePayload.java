package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record S2C_HelpRequestRemovePayload(UUID requestId) implements CustomPacketPayload {
    public static final Type<S2C_HelpRequestRemovePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_help_remove"));

    public static final StreamCodec<FriendlyByteBuf, S2C_HelpRequestRemovePayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        S2C_HelpRequestRemovePayload::requestId,
        S2C_HelpRequestRemovePayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_HelpRequestRemovePayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> AcauHardCoreCache.remove(payload.requestId()));
    }
}