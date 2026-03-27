package kr.pyke.acau_hardcore.registry.item.food;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class CombatRationItem extends Item {
    public CombatRationItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext ctx, @NonNull TooltipDisplay display, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag) {
        consumer.accept(Component.literal("배고픔을 모두 회복합니다.").withStyle(ChatFormatting.GRAY));
    }
}
