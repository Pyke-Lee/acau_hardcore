package kr.pyke.acau_hardcore.registry.item.drink;

import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.hardcore.IHardCoreInfo;
import kr.pyke.acau_hardcore.registry.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class WaterDrinkItem extends DrinkItem {
    private final int hydrationSeconds;

    public WaterDrinkItem(Properties properties, int hydrationSeconds) {
        super(properties);
        this.hydrationSeconds = hydrationSeconds;
    }

    @Override
    protected void onDrink(ServerPlayer player, ItemStack drinkStack) {
        IHardCoreInfo info = ModComponents.HARDCORE_INFO.get(player);
        info.drinkWater(hydrationSeconds);
    }

    @Override @SuppressWarnings({})
    public void appendHoverText(@NonNull ItemStack itemStack, @NonNull TooltipContext tooltipContext, @NonNull TooltipDisplay tooltipDisplay, Consumer<Component> consumer, @NonNull TooltipFlag tooltipFlag) {
        String duration = formatDuration(hydrationSeconds);
        consumer.accept(Component.literal("섭취 시 " + duration + " 동안 갈증이 해소됩니다.").withStyle(ChatFormatting.GRAY));
        if (this.equals(ModItems.DIRTY_WATER)) {
            consumer.accept(Component.literal(String.format("%.0f%% 확률로 디버프(허기)가 %.0f초 발생합니다.", ModConfig.INSTANCE.dirtyWaterPenaltyChance * 100, ModConfig.INSTANCE.dirtyWaterPenaltyDuration)).withStyle(ChatFormatting.GRAY));
        }
    }

    private static String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        if (minutes > 0 && seconds > 0) { return minutes + "분 " + seconds + "초"; }
        else if (minutes > 0) { return minutes + "분"; }
        else { return seconds + "초"; }
    }
}
