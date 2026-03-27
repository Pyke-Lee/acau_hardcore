package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record S2C_OpenShopPayload(String shopID) implements CustomPacketPayload {
    public static final Type<S2C_OpenShopPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_open_shop"));

    public static final StreamCodec<FriendlyByteBuf, S2C_OpenShopPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        S2C_OpenShopPayload::shopID,
        S2C_OpenShopPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }
}