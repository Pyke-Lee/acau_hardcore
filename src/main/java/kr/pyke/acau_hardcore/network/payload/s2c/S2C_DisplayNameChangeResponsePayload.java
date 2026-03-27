package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.client.gui.screen.ChangeDisplayNameScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record S2C_DisplayNameChangeResponsePayload(boolean success, String message) implements CustomPacketPayload {
    public static final Type<S2C_DisplayNameChangeResponsePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_display_name_change_response"));

    public static final StreamCodec<FriendlyByteBuf, S2C_DisplayNameChangeResponsePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, S2C_DisplayNameChangeResponsePayload::success,
        ByteBufCodecs.STRING_UTF8, S2C_DisplayNameChangeResponsePayload::message,
        S2C_DisplayNameChangeResponsePayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }
}