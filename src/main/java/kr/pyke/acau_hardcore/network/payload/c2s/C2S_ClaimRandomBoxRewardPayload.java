package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.randombox.PendingRewardManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record C2S_ClaimRandomBoxRewardPayload() implements CustomPacketPayload {
    public static final Type<C2S_ClaimRandomBoxRewardPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_claim_random_box"));

    public static final StreamCodec<FriendlyByteBuf, C2S_ClaimRandomBoxRewardPayload> STREAM_CODEC = StreamCodec.unit(new C2S_ClaimRandomBoxRewardPayload());

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_ClaimRandomBoxRewardPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> PendingRewardManager.claim(context.player()));
    }
}