package kr.pyke.acau_hardcore.network.payload.s2c;

import kr.pyke.acau_hardcore.AcauHardCore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record S2C_OpenRaidSelectPayload(boolean vanillaInProgress, boolean expertInProgress, String playerRaidTypeKey, long cooldownTicks, List<ItemStack> vanillaRewardItems, long vanillaCurrency, List<ItemStack> expertRewardItems, long expertCurrency) implements CustomPacketPayload {
    public static final Type<S2C_OpenRaidSelectPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_open_raid_select"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2C_OpenRaidSelectPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull S2C_OpenRaidSelectPayload decode(@NonNull RegistryFriendlyByteBuf buf) {
            boolean vanillaInProgress = buf.readBoolean();
            boolean expertInProgress = buf.readBoolean();
            String playerRaidTypeKey = ByteBufCodecs.STRING_UTF8.decode(buf);
            long cooldownTicks = buf.readLong();

            List<ItemStack> vanillaItems = decodeItemList(buf);
            long vanillaCurrency = buf.readLong();
            List<ItemStack> expertItems = decodeItemList(buf);
            long expertCurrency = buf.readLong();

            return new S2C_OpenRaidSelectPayload(vanillaInProgress, expertInProgress, playerRaidTypeKey, cooldownTicks, vanillaItems, vanillaCurrency, expertItems, expertCurrency);
        }

        @Override
        public void encode(@NonNull RegistryFriendlyByteBuf buf, @NonNull S2C_OpenRaidSelectPayload payload) {
            buf.writeBoolean(payload.vanillaInProgress());
            buf.writeBoolean(payload.expertInProgress());
            ByteBufCodecs.STRING_UTF8.encode(buf, payload.playerRaidTypeKey());
            buf.writeLong(payload.cooldownTicks());

            encodeItemList(buf, payload.vanillaRewardItems());
            buf.writeLong(payload.vanillaCurrency());
            encodeItemList(buf, payload.expertRewardItems());
            buf.writeLong(payload.expertCurrency());
        }

        private List<ItemStack> decodeItemList(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<ItemStack> items = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }
            return items;
        }

        private void encodeItemList(RegistryFriendlyByteBuf buf, List<ItemStack> items) {
            buf.writeVarInt(items.size());
            for (ItemStack item : items) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, item);
            }
        }
    };

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }
}