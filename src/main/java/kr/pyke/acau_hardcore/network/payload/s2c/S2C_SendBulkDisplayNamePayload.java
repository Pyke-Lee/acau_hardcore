package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record S2C_SendBulkDisplayNamePayload(Map<UUID, String> displayNames) implements CustomPacketPayload {
    public static final Type<S2C_SendBulkDisplayNamePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_displayname_bulk"));

    public static final StreamCodec<FriendlyByteBuf, S2C_SendBulkDisplayNamePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.STRING_UTF8), S2C_SendBulkDisplayNamePayload::displayNames,
        S2C_SendBulkDisplayNamePayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_SendBulkDisplayNamePayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> AcauHardCoreCache.displayNames.putAll(payload.displayNames()));
    }
}
