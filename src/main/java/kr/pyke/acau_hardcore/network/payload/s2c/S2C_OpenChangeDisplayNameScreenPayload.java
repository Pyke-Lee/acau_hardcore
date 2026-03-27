package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.client.gui.screen.ChangeDisplayNameScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record S2C_OpenChangeDisplayNameScreenPayload() implements CustomPacketPayload {
    public static final Type<S2C_OpenChangeDisplayNameScreenPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_display_name_change_screen_open"));

    public static final StreamCodec<FriendlyByteBuf, S2C_OpenChangeDisplayNameScreenPayload> STREAM_CODEC = StreamCodec.unit(new S2C_OpenChangeDisplayNameScreenPayload());

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }
}