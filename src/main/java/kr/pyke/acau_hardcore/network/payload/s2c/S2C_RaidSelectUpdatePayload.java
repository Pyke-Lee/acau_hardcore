package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record S2C_RaidSelectUpdatePayload(boolean vanillaInProgress, boolean expertInProgress) implements CustomPacketPayload {
    public static final Type<S2C_RaidSelectUpdatePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_raid_select_update"));

    public static final StreamCodec<FriendlyByteBuf, S2C_RaidSelectUpdatePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, S2C_RaidSelectUpdatePayload::vanillaInProgress,
        ByteBufCodecs.BOOL, S2C_RaidSelectUpdatePayload::expertInProgress,
        S2C_RaidSelectUpdatePayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }
}