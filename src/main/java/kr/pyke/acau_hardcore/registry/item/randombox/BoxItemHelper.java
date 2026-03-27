package kr.pyke.acau_hardcore.registry.item.randombox;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import kr.pyke.acau_hardcore.data.randombox.BoxDefinition;
import kr.pyke.acau_hardcore.data.randombox.BoxReward;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BoxItemHelper {
    private BoxItemHelper() { }

    public static ItemStack createBoxStack(Item baseItem, BoxDefinition definition) {
        ItemStack itemStack = new ItemStack(baseItem);

        CompoundTag tag = new CompoundTag();
        tag.putString("box_id", definition.id());
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (definition.modelData() > 0) {
            itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of((float) definition.modelData()), List.of(), List.of(), List.of()));
        }

        itemStack.set(DataComponents.ITEM_NAME, Component.literal(definition.displayName()));

        return itemStack;
    }

    public static @Nullable String getBoxID(ItemStack itemStack) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) { return null; }

        CompoundTag tag = customData.copyTag();
        return tag.getString("box_id").orElse(null);
    }

    public static ItemStack createRewardStack(BoxReward reward, @Nullable HolderLookup.Provider registries) {
        Item item = BuiltInRegistries.ITEM.getValue(reward.getItemID());
        ItemStack itemStack = new ItemStack(item, reward.count());

        if (reward.nbt() != null && !reward.nbt().isBlank() && registries != null) {
            try {
                String itemString = reward.item() + reward.nbt();
                ItemParser parser = new ItemParser(registries);
                ItemParser.ItemResult result = parser.parse(new StringReader(itemString));

                itemStack = new ItemStack(result.item(), reward.count());

                final ItemStack finalStack = itemStack;
                DataComponentPatch patch = result.components();
                patch.entrySet().forEach(entry ->
                    entry.getValue().ifPresent(value -> {
                        @SuppressWarnings("unchecked")
                        DataComponentType<Object> type = (DataComponentType<Object>) entry.getKey();
                        finalStack.set(type, value);
                    })
                );
            } catch (CommandSyntaxException ignored) { }
        }

        if (reward.customName() != null && !reward.customName().isBlank()) {
            itemStack.set(DataComponents.ITEM_NAME, Component.literal(reward.customName()));
        }

        return itemStack;
    }
}