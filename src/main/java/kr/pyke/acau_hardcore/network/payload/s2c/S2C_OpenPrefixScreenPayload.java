package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record S2C_OpenPrefixScreenPayload() implements CustomPacketPayload {
    public static final Type<S2C_OpenPrefixScreenPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_open_prefix_screen"));

    public static final StreamCodec<FriendlyByteBuf, S2C_OpenPrefixScreenPayload> STREAM_CODEC = StreamCodec.unit(new S2C_OpenPrefixScreenPayload());

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }
}
