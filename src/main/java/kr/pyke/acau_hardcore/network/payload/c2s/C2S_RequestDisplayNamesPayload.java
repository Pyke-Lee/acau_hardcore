package kr.pyke.acau_hardcore.network.payload.c2s;

import com.mojang.authlib.GameProfile;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.displayname.DisplayNameData;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_SendSingleDisplayNamePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public record C2S_RequestDisplayNamesPayload(List<UUID> uuids) implements CustomPacketPayload {
    public static final Type<C2S_RequestDisplayNamesPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_request_display_names"));

    public static final StreamCodec<FriendlyByteBuf, C2S_RequestDisplayNamesPayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()),
        C2S_RequestDisplayNamesPayload::uuids,
        C2S_RequestDisplayNamesPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_RequestDisplayNamesPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer requester = context.player();
            DisplayNameData displayNameData = DisplayNameData.getServerState(context.server());

            for (UUID uuid : payload.uuids()) {
                String name = displayNameData.getDisplayName(uuid);
                if (name == null) {
                    ServerPlayer online = context.server().getPlayerList().getPlayer(uuid);
                    if (online != null) {
                        name = online.getName().getString();
                    }
                    else {
                        name = context.server().services().profileResolver().fetchById(uuid).map(GameProfile::name).orElse("Unknown");
                    }
                }

                ServerPlayNetworking.send(requester, new S2C_SendSingleDisplayNamePayload(uuid, name));
            }
        });
    }
}