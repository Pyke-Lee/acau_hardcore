package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.data.rune.RuneInstance;
import kr.pyke.acau_hardcore.registry.item.rune.RuneItemHelper;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.CustomData;

public class ItemTooltipHandler {
    private ItemTooltipHandler() { }

    public static void register() {
        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, lines) -> {
            RuneInstance rune = RuneItemHelper.getRune(itemStack);
            if (rune != null) {
                lines.add(Component.literal("(RUNE) ").withStyle(rune.effect().getColor()).append(Component.literal(rune.formatDescription()).withStyle(ChatFormatting.WHITE)));
            }

            CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
            if (customData != null && customData.copyTag().contains("KeepOnDeath")) {
                lines.add(Component.literal(" 귀속 아이템").withStyle(ChatFormatting.GRAY));
            }
        });
    }
}
