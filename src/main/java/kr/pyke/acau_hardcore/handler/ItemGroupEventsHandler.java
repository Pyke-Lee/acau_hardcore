package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.data.randombox.BoxDefinition;
import kr.pyke.acau_hardcore.data.randombox.ClientBoxRegistry;
import kr.pyke.acau_hardcore.prefix.PrefixRegistry;
import kr.pyke.acau_hardcore.registry.item.randombox.BoxItemHelper;
import kr.pyke.acau_hardcore.data.randombox.BoxRegistry;
import kr.pyke.acau_hardcore.registry.creativemodetabs.ModCreativeModeTabs;
import kr.pyke.acau_hardcore.registry.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class ItemGroupEventsHandler {
    private ItemGroupEventsHandler() { }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ModCreativeModeTabs.CREATIVE_TAB_KEY).register(entries -> {
            for (BoxDefinition definition : BoxRegistry.getAll()) {
                entries.accept(BoxItemHelper.createBoxStack(ModItems.RANDOM_BOX, definition));
            }

            for (BoxDefinition definition : ClientBoxRegistry.getAll()) {
                entries.accept(BoxItemHelper.createBoxStack(ModItems.RANDOM_BOX, definition));
            }
        });

        ItemGroupEvents.modifyEntriesEvent(ModCreativeModeTabs.PREFIX_TAB_KEY).register(entries -> {
            for (String id : PrefixRegistry.getKeys()) {
                ItemStack itemStack = new ItemStack(ModItems.PREFIX_BOOK);
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("prefix_id", id);
                itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
                entries.accept(itemStack);
            }
        });
    }
}
