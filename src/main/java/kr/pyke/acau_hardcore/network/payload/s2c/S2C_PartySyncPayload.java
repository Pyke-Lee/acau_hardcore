package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record S2C_PartySyncPayload(boolean inParty, UUID leaderId, List<MemberData> members) implements CustomPacketPayload {
    public static final Type<S2C_PartySyncPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_party_sync"));

    public record MemberData(UUID uuid, String name, float health, float maxHealth, float absorption, boolean online) { }

    public static final StreamCodec<FriendlyByteBuf, MemberData> MEMBER_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull MemberData decode(@NonNull FriendlyByteBuf buf) {
            UUID uuid = UUIDUtil.STREAM_CODEC.decode(buf);
            String name = ByteBufCodecs.STRING_UTF8.decode(buf);
            float health = buf.readFloat();
            float maxHealth = buf.readFloat();
            float absorption = buf.readFloat();
            boolean online = buf.readBoolean();
            return new MemberData(uuid, name, health, maxHealth, absorption, online);
        }

        @Override
        public void encode(@NonNull FriendlyByteBuf buf, @NonNull MemberData data) {
            UUIDUtil.STREAM_CODEC.encode(buf, data.uuid());
            ByteBufCodecs.STRING_UTF8.encode(buf, data.name());
            buf.writeFloat(data.health());
            buf.writeFloat(data.maxHealth());
            buf.writeFloat(data.absorption());
            buf.writeBoolean(data.online());
        }
    };

    public static final StreamCodec<FriendlyByteBuf, S2C_PartySyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull S2C_PartySyncPayload decode(@NonNull FriendlyByteBuf buf) {
            boolean inParty = buf.readBoolean();
            UUID leaderId = UUIDUtil.STREAM_CODEC.decode(buf);
            int size = buf.readVarInt();
            List<MemberData> members = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                members.add(MEMBER_CODEC.decode(buf));
            }
            return new S2C_PartySyncPayload(inParty, leaderId, members);
        }

        @Override
        public void encode(@NonNull FriendlyByteBuf buf, @NonNull S2C_PartySyncPayload payload) {
            buf.writeBoolean(payload.inParty());
            UUIDUtil.STREAM_CODEC.encode(buf, payload.leaderId());
            buf.writeVarInt(payload.members().size());
            for (MemberData member : payload.members()) {
                MEMBER_CODEC.encode(buf, member);
            }
        }
    };

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_PartySyncPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> AcauHardCoreCache.updateParty(payload));
    }
}