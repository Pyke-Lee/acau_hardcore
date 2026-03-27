package kr.pyke.acau_hardcore.network.payload.c2s;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.shop.ShopData;
import kr.pyke.acau_hardcore.data.shop.ShopManager;
import kr.pyke.acau_hardcore.data.shop.ShopProduct;
import kr.pyke.acau_hardcore.data.shop.ShopServerTransaction;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

public record C2S_ShopTransactionPayload(String shopID, int productIndex, int multiplier, boolean isBuy) implements CustomPacketPayload {
    public static final Type<C2S_ShopTransactionPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "c2s_shop_transaction"));

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static final StreamCodec<RegistryFriendlyByteBuf, C2S_ShopTransactionPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, C2S_ShopTransactionPayload::shopID,
        ByteBufCodecs.INT, C2S_ShopTransactionPayload::productIndex,
        ByteBufCodecs.INT, C2S_ShopTransactionPayload::multiplier,
        ByteBufCodecs.BOOL, C2S_ShopTransactionPayload::isBuy,
        C2S_ShopTransactionPayload::new
    );

    public static void handle(C2S_ShopTransactionPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();

        context.server().execute(() -> {
            ShopData shop = ShopManager.getShop(payload.shopID());
            if (shop != null) {
                if (payload.productIndex() >= 0 && payload.productIndex() < shop.products.size()) {
                    ShopProduct product = shop.products.get(payload.productIndex());
                    if (payload.isBuy()) {
                        ShopServerTransaction.executeBuy(player, shop, product, payload.multiplier());
                    }
                    else {
                        ShopServerTransaction.executeSell(player, shop, product, payload.multiplier());
                    }
                }
                else {
                    PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "해당 상품을 찾을 수 없습니다.");
                }
            }
            else {
                PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "상점 데이터를 찾을 수 없습니다.");
            }
        });
    }
}
