package kr.pyke.acau_hardcore.data.shop;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import kr.pyke.acau_hardcore.AcauHardCore;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ShopProduct {
    public String item;
    public int amount;
    public JsonObject components;
    public boolean strict_match;
    public String payment_type;
    public boolean buyable;
    public int buy_price;
    public boolean sellable;
    public int sell_price;

    public String barter_buy_item;
    public int barter_buy_amount;
    public JsonObject barter_buy_components;

    public String barter_sell_item;
    public int barter_sell_amount;
    public JsonObject barter_sell_components;

    public Item getItem() { return BuiltInRegistries.ITEM.getValue(Identifier.parse(this.item)); }
    public Item getBarterBuyItem() { return BuiltInRegistries.ITEM.getValue(Identifier.parse(this.barter_buy_item)); }
    public Item getBarterSellItem() { return BuiltInRegistries.ITEM.getValue(Identifier.parse(this.barter_sell_item)); }

    public ItemStack createItemStack(int multiplier) {
        return createStack(this.getItem(), this.amount * multiplier, this.components);
    }

    public ItemStack createBarterBuyStack(int multiplier) {
        return createStack(this.getBarterBuyItem(), this.barter_buy_amount * multiplier, this.barter_buy_components);
    }

    public ItemStack createBarterSellStack(int multiplier) {
        return createStack(this.getBarterSellItem(), this.barter_sell_amount * multiplier, this.barter_sell_components);
    }

    private ItemStack createStack(Item item, int count, JsonObject jsonComponents) {
        ItemStack stack = new ItemStack(item, count);
        if (jsonComponents != null && !jsonComponents.keySet().isEmpty()) {
            DataComponentPatch patch = DataComponentPatch.CODEC.parse(JsonOps.INSTANCE, jsonComponents)
                .resultOrPartial(err -> AcauHardCore.LOGGER.error("컴포넌트 파싱 오류: {}", err))
                .orElse(DataComponentPatch.EMPTY);
            stack.applyComponents(patch);
        }
        return stack;
    }

    public DataComponentPatch getComponentPatch() { return parsePatch(this.components); }
    public DataComponentPatch getBarterBuyPatch() { return parsePatch(this.barter_buy_components); }
    public DataComponentPatch getBarterSellPatch() { return parsePatch(this.barter_sell_components); }

    private DataComponentPatch parsePatch(JsonObject json) {
        if (json != null && !json.keySet().isEmpty()) {
            return DataComponentPatch.CODEC.parse(JsonOps.INSTANCE, json).result().orElse(DataComponentPatch.EMPTY);
        }
        return DataComponentPatch.EMPTY;
    }
}