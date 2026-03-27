package kr.pyke.acau_hardcore.data.shop;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.logger.ShopLogger;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.util.constants.COLOR;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ShopServerTransaction {
    public static void executeBuy(ServerPlayer player, ShopData shop, ShopProduct product, int multiplier) {
        int totalItemAmount = product.amount * multiplier;
        ItemStack itemStack = product.createItemStack(1);

        if (product.payment_type.equals("currency")) {
            int totalPrice = product.buy_price * multiplier;
            ModComponents.HARDCORE_INFO.get(player).subCurrency(totalPrice);
            giveItems(player, itemStack, totalItemAmount);
            ShopLogger.logTransaction(shop.id, "BUY", player.getName().getString(), product.item, totalItemAmount, product.buy_price, totalPrice, "currency");
            PykeLib.sendSystemMessage(player, COLOR.LIME.getColor(), String.format("아이템을 구매했습니다. (%s x%s)", itemStack.getHoverName().getString(), totalItemAmount));
        }
        else if (product.payment_type.equals("item")) {
            int totalCostAmount = product.barter_buy_amount * multiplier;
            Item barterItem = product.getBarterBuyItem();

            consumeItem(player, barterItem, product.getBarterBuyPatch(), product.strict_match, totalCostAmount);
            giveItems(player, itemStack, totalItemAmount);
            ShopLogger.logTransaction(shop.id, "BUY", player.getName().getString(), product.item, multiplier, product.barter_buy_amount, totalCostAmount, product.barter_buy_item);
            PykeLib.sendSystemMessage(player, COLOR.LIME.getColor(), String.format("아이템을 구매했습니다. (%s x%s)", itemStack.getHoverName().getString(), totalItemAmount));
        }
    }

    public static void executeSell(ServerPlayer player, ShopData shop, ShopProduct product, int multiplier) {
        int totalItemAmount = product.amount * multiplier;
        ItemStack itemStack = product.createItemStack(1);

        if (product.payment_type.equals("currency")) {
            consumeItem(player, itemStack.getItem(), product.getComponentPatch(), product.strict_match, totalItemAmount);
            int totalPrice = product.sell_price * multiplier;
            ModComponents.HARDCORE_INFO.get(player).addCurrency(totalPrice);
            ShopLogger.logTransaction(shop.id, "SELL", player.getName().getString(), product.item, multiplier, product.sell_price, totalPrice, "currency");
            PykeLib.sendSystemMessage(player, COLOR.LIME.getColor(), String.format("아이템을 판매했습니다. (%s x%s)", itemStack.getHoverName().getString(), totalItemAmount));
        }
        else {
            int totalRewardAmount = product.barter_sell_amount * multiplier;
            consumeItem(player, itemStack.getItem(), product.getComponentPatch(), product.strict_match, totalItemAmount);

            ItemStack rewardStack = product.createBarterSellStack(1);
            giveItems(player, rewardStack, totalRewardAmount);
            ShopLogger.logTransaction(shop.id, "SELL", player.getName().getString(), product.item, multiplier, product.barter_sell_amount, totalRewardAmount, product.barter_sell_item);
            PykeLib.sendSystemMessage(player, COLOR.LIME.getColor(), String.format("아이템을 판매했습니다. (%s x%s)", itemStack.getHoverName().getString(), totalItemAmount));
        }
    }

    private static void consumeItem(ServerPlayer player, Item itemStack, DataComponentPatch patch, boolean strict, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getNonEquipmentItems().size(); ++i) {
            ItemStack item = player.getInventory().getNonEquipmentItems().get(i);
            if (!item.isEmpty() && item.getItem().equals(itemStack)) {
                boolean match = !strict || item.getComponentsPatch().equals(patch);
                if (match) {
                    int take = Math.min(item.getCount(), remaining);
                    item.shrink(take);
                    remaining -= take;
                    if (remaining <= 0) { return; }
                }
            }
        }
    }

    private static void giveItems(ServerPlayer player, ItemStack itemStack, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            int insertAmount = Math.min(remaining, itemStack.getMaxStackSize());
            ItemStack insertItem = itemStack.copyWithCount(insertAmount);
            player.getInventory().add(insertItem);
            remaining -= insertAmount;
        }
    }
}