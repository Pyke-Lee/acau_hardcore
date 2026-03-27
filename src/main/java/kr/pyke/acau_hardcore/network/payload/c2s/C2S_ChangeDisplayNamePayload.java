package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_DisplayNameChangeResponsePayload;
import kr.pyke.acau_hardcore.util.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public record C2S_ChangeDisplayNamePayload(String displayName) implements CustomPacketPayload {
    public static final Type<C2S_ChangeDisplayNamePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_change_display_name"));

    public static final StreamCodec<FriendlyByteBuf, C2S_ChangeDisplayNamePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, C2S_ChangeDisplayNamePayload::displayName,
        C2S_ChangeDisplayNamePayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void send(String displayName) {
        ClientPlayNetworking.send(new C2S_ChangeDisplayNamePayload(displayName));
    }

    public static void handle(C2S_ChangeDisplayNamePayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        String newName = payload.displayName();

        context.player().level().getServer().execute(() -> {
            if (newName.length() < 2 || newName.length() > 16) {
                ServerPlayNetworking.send(player, new S2C_DisplayNameChangeResponsePayload(false, "닉네임은 2~16자여야 합니다."));
                return;
            }

            String myCurrentNick = AcauHardCoreCache.displayNames.get(player.getUUID());
            String myEffectiveName = (myCurrentNick != null && !myCurrentNick.isEmpty()) ? myCurrentNick : player.getName().getString();

            if (newName.equalsIgnoreCase(myEffectiveName)) {
                ServerPlayNetworking.send(player, new S2C_DisplayNameChangeResponsePayload(false, "현재 사용 중인 이름입니다."));
                return;
            }

            boolean isDuplicate = false;
            for (ServerPlayer p : player.level().getServer().getPlayerList().getPlayers()) {
                if (p.getUUID().equals(player.getUUID())) { continue; }

                String originalName = p.getName().getString();
                String customName = AcauHardCoreCache.displayNames.get(p.getUUID());

                if (newName.equalsIgnoreCase(originalName) || (newName.equalsIgnoreCase(customName))) {
                    isDuplicate = true;
                    break;
                }
            }

            if (isDuplicate) {
                ServerPlayNetworking.send(player, new S2C_DisplayNameChangeResponsePayload(false, "이미 사용 중인 닉네임입니다."));
                return;
            }

            if (!player.isCreative()) {
                ItemStack heldItem = player.getMainHandItem();
                if (!heldItem.isEmpty()) {
                    heldItem.shrink(1);
                }
            }

            Utils.updateDisplayName(player, newName);
            ServerPlayNetworking.send(player, new S2C_DisplayNameChangeResponsePayload(true, ""));
        });
    }
}