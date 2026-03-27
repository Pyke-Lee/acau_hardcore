package kr.pyke.acau_hardcore.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FoodEvents {
    private FoodEvents() { }

    /**
     * 음식을 먹기 전에 호출됩니다.
     * false를 반환하면 먹기가 취소됩니다.
     */
    public static final Event<BeforeEat> BEFORE_EAT = EventFactory.createArrayBacked(
        BeforeEat.class,
        listeners -> (player, foodStack) -> {
            for (BeforeEat listener : listeners) {
                if (!listener.beforeEat(player, foodStack)) {
                    return false;
                }
            }
            return true;
        }
    );

    /**
     * 음식을 먹은 후에 호출됩니다.
     */
    public static final Event<AfterEat> AFTER_EAT = EventFactory.createArrayBacked(
        AfterEat.class,
        listeners -> (player, foodStack) -> {
            for (AfterEat listener : listeners) {
                listener.afterEat(player, foodStack);
            }
        }
    );

    @FunctionalInterface
    public interface BeforeEat {
        /**
         * @param player 먹는 플레이어
         * @param foodStack 먹는 음식 아이템 (소비 전)
         * @return false면 먹기 취소
         */
        boolean beforeEat(ServerPlayer player, ItemStack foodStack);
    }

    @FunctionalInterface
    public interface AfterEat {
        /**
         * @param player 먹은 플레이어
         * @param foodStack 먹은 음식 아이템 (소비 전 복사본)
         */
        void afterEat(ServerPlayer player, ItemStack foodStack);
    }
}
