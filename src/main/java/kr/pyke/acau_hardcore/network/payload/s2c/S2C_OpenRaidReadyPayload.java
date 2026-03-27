package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record S2C_OpenRaidReadyPayload(String raidTypeKey, String raidTypeName, boolean isInitiator) implements CustomPacketPayload {
    public static final Type<S2C_OpenRaidReadyPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_open_raid_ready"));

    public static final StreamCodec<FriendlyByteBuf, S2C_OpenRaidReadyPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, S2C_OpenRaidReadyPayload::raidTypeKey,
        ByteBufCodecs.STRING_UTF8, S2C_OpenRaidReadyPayload::raidTypeName,
        ByteBufCodecs.BOOL, S2C_OpenRaidReadyPayload::isInitiator,
        S2C_OpenRaidReadyPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }
}