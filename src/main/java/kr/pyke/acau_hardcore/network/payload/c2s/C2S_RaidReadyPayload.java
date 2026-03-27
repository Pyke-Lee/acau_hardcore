package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.boss.raid.BossRaidManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record C2S_RaidReadyPayload(boolean ready) implements CustomPacketPayload {
    public static final Type<C2S_RaidReadyPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_raid_ready"));

    public static final StreamCodec<FriendlyByteBuf, C2S_RaidReadyPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, C2S_RaidReadyPayload::ready,
        C2S_RaidReadyPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_RaidReadyPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> BossRaidManager.handleReadyResponse(context.server(), context.player(), payload.ready()));
    }
}