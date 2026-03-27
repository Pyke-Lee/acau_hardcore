package kr.pyke.acau_hardcore.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class DrinkEvents {
    private DrinkEvents() { }

    /**
     * 드링크를 마시기 전에 호출됩니다.
     * false를 반환하면 마시기가 취소됩니다.
     */
    public static final Event<BeforeDrink> BEFORE_DRINK = EventFactory.createArrayBacked(
        BeforeDrink.class,
        listeners -> (player, drinkStack) -> {
            for (BeforeDrink listener : listeners) {
                if (!listener.beforeDrink(player, drinkStack)) {
                    return false;
                }
            }

            return true;
        }
    );

    /**
     * 드링크를 마신 후에 호출됩니다.
     */
    public static final Event<AfterDrink> AFTER_DRINK = EventFactory.createArrayBacked(
        AfterDrink.class,
        listeners -> (player, drinkStack) -> {
            for (AfterDrink listener : listeners) {
                listener.afterDrink(player, drinkStack);
            }
        }
    );

    @FunctionalInterface
    public interface BeforeDrink {
        /**
         * @param player 마시는 플레이어
         * @param drinkStack 마시는 드링크 아이템 (소비 전)
         * @return false면 마시기 취소
         */
        boolean beforeDrink(ServerPlayer player, ItemStack drinkStack);
    }

    @FunctionalInterface
    public interface AfterDrink {
        /**
         * @param player 마신 플레이어
         * @param drinkStack 마신 드링크 아이템 (소비 전 복사본)
         */
        void afterDrink(ServerPlayer player, ItemStack drinkStack);
    }
}
