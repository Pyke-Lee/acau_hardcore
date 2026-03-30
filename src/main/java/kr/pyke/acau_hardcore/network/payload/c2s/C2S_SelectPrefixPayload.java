package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_SelectPrefixResponsePayload;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.prefix.IPrefixes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

public record C2S_SelectPrefixPayload(String id) implements CustomPacketPayload {
    public static final Type<C2S_SelectPrefixPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_select_prefix"));

    public static final StreamCodec<FriendlyByteBuf, C2S_SelectPrefixPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        C2S_SelectPrefixPayload::id,
        C2S_SelectPrefixPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_SelectPrefixPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer player = context.player();
            IPrefixes prefixes = ModComponents.PREFIXES.get(player);

            if (payload.id().equals("none") || prefixes.getPrefixes().contains(payload.id())) {
                prefixes.selectPrefix(payload.id());
                ServerPlayNetworking.send(player, new S2C_SelectPrefixResponsePayload(true));
            }
            else {
                ServerPlayNetworking.send(player, new S2C_SelectPrefixResponsePayload(false));
            }
        });
    }
}