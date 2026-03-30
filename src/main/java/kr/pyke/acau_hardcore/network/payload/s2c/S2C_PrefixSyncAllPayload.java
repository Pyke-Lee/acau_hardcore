package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.prefix.PrefixData;
import kr.pyke.acau_hardcore.prefix.PrefixRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record S2C_PrefixSyncAllPayload(List<PrefixData> prefixes) implements CustomPacketPayload {
    public static final Type<S2C_PrefixSyncAllPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_title_sync_all"));

    public static final StreamCodec<FriendlyByteBuf, S2C_PrefixSyncAllPayload> STREAM_CODEC = StreamCodec.composite(
        PrefixData.STREAM_CODEC.apply(ByteBufCodecs.list()),
        S2C_PrefixSyncAllPayload::prefixes,
        S2C_PrefixSyncAllPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_PrefixSyncAllPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            PrefixRegistry.clear();
            for (PrefixData data : payload.prefixes()) {
                PrefixRegistry.register(data.id(), data);
            }
        });
    }
}
