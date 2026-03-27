package kr.pyke.acau_hardcore.data.shop;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ShopClientValidation {
    public static boolean canBuy(LocalPlayer player, ShopProduct product, int multiplier, long currentCurrency) {
        if (!product.buyable) { return false; }

        int totalItemAmount = product.amount * multiplier;
        ItemStack itemStack = product.createItemStack(1);
        if (!hasSpace(player, itemStack, totalItemAmount)) { return false; }

        if (product.payment_type.equals("currency")) {
            int totalPrice = product.buy_price * multiplier;
            return currentCurrency >= totalPrice;
        }
        else if (product.payment_type.equals("item")) {
            int totalPrice = product.barter_buy_amount * multiplier;
            return countItem(player, product.getBarterBuyItem(), DataComponentPatch.EMPTY, false) >= totalPrice;
        }

        return false;
    }

    public static boolean canSell(LocalPlayer player, ShopProduct product, int multiplier) {
        if (!product.sellable) { return false; }

        int totalItemAmount = product.amount * multiplier;
        ItemStack itemStack = product.createItemStack(1);
        if (countItem(player, itemStack.getItem(), product.getComponentPatch(), product.strict_match) < totalItemAmount) {
            return false;
        }

        if (product.payment_type.equals("item")) {
            int totalRewardAmount = product.barter_sell_amount * multiplier;
            ItemStack reward = new ItemStack(product.getBarterSellItem());

            return hasSpace(player, reward, totalRewardAmount);
        }

        return true;
    }

    private static boolean hasSpace(LocalPlayer player, ItemStack itemStack, int totalItemAmount) {
        int availableSpace = 0;
        for (int i = 0; i < player.getInventory().getNonEquipmentItems().size(); ++i) {
            ItemStack item = player.getInventory().getNonEquipmentItems().get(i);
            if (item.isEmpty()) { availableSpace += itemStack.getMaxStackSize(); }
            else if (ItemStack.isSameItemSameComponents(item, itemStack)) { availableSpace += (itemStack.getMaxStackSize() - itemStack.getCount()); }
        }

        return availableSpace >= totalItemAmount;
    }

    public static int countItem(LocalPlayer player, Item itemStack, DataComponentPatch patch, boolean strict) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getNonEquipmentItems().size(); ++i) {
            ItemStack item = player.getInventory().getNonEquipmentItems().get(i);
            if (!item.isEmpty() && item.getItem().equals(itemStack)) {
                if (strict) {
                    if (item.getComponentsPatch().equals(patch)) { count += item.getCount(); }
                }
                else { count += item.getCount(); }
            }
        }

        return count;
    }
}
