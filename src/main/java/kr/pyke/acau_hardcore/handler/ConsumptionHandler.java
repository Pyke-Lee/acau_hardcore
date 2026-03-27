package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.event.DrinkEvents;
import kr.pyke.acau_hardcore.registry.item.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class ConsumptionHandler {
    private ConsumptionHandler() { }

    public static void register() {
        DrinkEvents.AFTER_DRINK.register((player, drinkStack) -> {
            if (drinkStack.is(ModItems.DIRTY_WATER)) {
                if (player.getRandom().nextFloat() < ModConfig.INSTANCE.dirtyWaterPenaltyChance) {
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, (int) (ModConfig.INSTANCE.dirtyWaterPenaltyDuration * 20), 0));
                }
            }
        });
    }
}
