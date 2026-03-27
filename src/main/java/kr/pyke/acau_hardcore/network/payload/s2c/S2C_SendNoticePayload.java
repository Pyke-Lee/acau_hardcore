package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.client.gui.hud.NoticeHud;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record S2C_SendNoticePayload(String message, int expirationTime) implements CustomPacketPayload {
    public static final Type<S2C_SendNoticePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_notice_update"));

    public static final StreamCodec<FriendlyByteBuf, S2C_SendNoticePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, S2C_SendNoticePayload::message,
        ByteBufCodecs.VAR_INT, S2C_SendNoticePayload::expirationTime,
        S2C_SendNoticePayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }
}