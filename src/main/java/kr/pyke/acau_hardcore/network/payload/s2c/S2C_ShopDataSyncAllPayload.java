package kr.pyke.acau_hardcore.network.payload.s2c;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.cache.AcauHardCoreCache;
import kr.pyke.acau_hardcore.data.helprequest.HelpRequest;
import kr.pyke.acau_hardcore.data.shop.ShopData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

public record S2C_ShopDataSyncAllPayload(Map<String, ShopData> shops) implements CustomPacketPayload {
    public static final Type<S2C_ShopDataSyncAllPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_shop_sync_all"));
    private static final Gson GSON = new Gson();

    public static final StreamCodec<FriendlyByteBuf, S2C_ShopDataSyncAllPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8.map(str -> GSON.fromJson(str, new TypeToken<Map<String, ShopData>>() {}.getType()), GSON::toJson),
        S2C_ShopDataSyncAllPayload::shops,
        S2C_ShopDataSyncAllPayload::new
    );

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(S2C_ShopDataSyncAllPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            AcauHardCoreCache.SHOPS.clear();
            AcauHardCoreCache.SHOPS.putAll(payload.shops);
        });
    }
}