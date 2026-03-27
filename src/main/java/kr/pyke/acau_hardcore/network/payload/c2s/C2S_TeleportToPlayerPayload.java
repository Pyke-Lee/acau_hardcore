package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.acau_hardcore.AcauHardCore;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record C2S_TeleportToPlayerPayload(UUID targetUuid) implements CustomPacketPayload {
    public static final Type<C2S_TeleportToPlayerPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_teleport_to_player"));

    public static final StreamCodec<FriendlyByteBuf, C2S_TeleportToPlayerPayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        C2S_TeleportToPlayerPayload::targetUuid,
        C2S_TeleportToPlayerPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_TeleportToPlayerPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer operator = context.player();
            ServerPlayer target = context.server().getPlayerList().getPlayer(payload.targetUuid());
            if (target == null) { return; }

            ServerLevel targetLevel = target.level();
            Vec3 targetPos = target.position();

            TeleportTransition transition = new TeleportTransition(targetLevel, targetPos, Vec3.ZERO, target.getYRot(), target.getXRot(), TeleportTransition.DO_NOTHING);
            operator.teleport(transition);
        });
    }
}