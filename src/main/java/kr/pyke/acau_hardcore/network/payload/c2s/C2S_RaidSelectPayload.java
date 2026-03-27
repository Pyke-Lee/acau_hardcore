package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.boss.raid.BossRaidManager;
import kr.pyke.acau_hardcore.type.BOSS_RAID_TYPE;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record C2S_RaidSelectPayload(String raidTypeKey) implements CustomPacketPayload {
    public static final Type<C2S_RaidSelectPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_raid_select"));

    public static final StreamCodec<FriendlyByteBuf, C2S_RaidSelectPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, C2S_RaidSelectPayload::raidTypeKey,
        C2S_RaidSelectPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(C2S_RaidSelectPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            BOSS_RAID_TYPE type = BOSS_RAID_TYPE.byKey(payload.raidTypeKey());
            if (type != null) {
                BossRaidManager.requestStart(context.server(), context.player(), type);
            }
        });
    }
}