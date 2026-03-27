package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
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

public record S2C_RaidReadyUpdatePayload(List<ReadyData> members, int secondsLeft) implements CustomPacketPayload {
    public static final Type<S2C_RaidReadyUpdatePayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_raid_ready_update"));

    public record ReadyData(UUID uuid, String name, boolean ready) { }

    public static final StreamCodec<FriendlyByteBuf, ReadyData> READY_DATA_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull ReadyData decode(@NonNull FriendlyByteBuf buf) {
            UUID uuid = UUIDUtil.STREAM_CODEC.decode(buf);
            String name = ByteBufCodecs.STRING_UTF8.decode(buf);
            boolean ready = buf.readBoolean();
            return new ReadyData(uuid, name, ready);
        }

        @Override
        public void encode(@NonNull FriendlyByteBuf buf, @NonNull ReadyData data) {
            UUIDUtil.STREAM_CODEC.encode(buf, data.uuid());
            ByteBufCodecs.STRING_UTF8.encode(buf, data.name());
            buf.writeBoolean(data.ready());
        }
    };

    public static final StreamCodec<FriendlyByteBuf, S2C_RaidReadyUpdatePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull S2C_RaidReadyUpdatePayload decode(@NonNull FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<ReadyData> members = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                members.add(READY_DATA_CODEC.decode(buf));
            }
            int secondsLeft = buf.readVarInt();
            return new S2C_RaidReadyUpdatePayload(members, secondsLeft);
        }

        @Override
        public void encode(@NonNull FriendlyByteBuf buf, @NonNull S2C_RaidReadyUpdatePayload payload) {
            buf.writeVarInt(payload.members().size());
            for (ReadyData data : payload.members()) {
                READY_DATA_CODEC.encode(buf, data);
            }
            buf.writeVarInt(payload.secondsLeft());
        }
    };

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }
}