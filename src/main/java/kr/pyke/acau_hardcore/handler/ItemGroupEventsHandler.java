package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.data.randombox.BoxDefinition;
import kr.pyke.acau_hardcore.data.randombox.ClientBoxRegistry;
import kr.pyke.acau_hardcore.registry.item.randombox.BoxItemHelper;
import kr.pyke.acau_hardcore.data.randombox.BoxRegistry;
import kr.pyke.acau_hardcore.registry.creativemodetabs.ModCreativeModeTabs;
import kr.pyke.acau_hardcore.registry.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

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
    }
}
