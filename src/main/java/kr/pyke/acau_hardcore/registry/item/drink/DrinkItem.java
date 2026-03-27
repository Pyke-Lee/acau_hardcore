package kr.pyke.acau_hardcore.registry.item.drink;

import kr.pyke.acau_hardcore.event.DrinkEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class DrinkItem extends Item {
    public DrinkItem(Properties properties) {
        super(properties);
    }

    public static Consumable drinkConsumable(float consumeSeconds) {
        return Consumable.builder().consumeSeconds(consumeSeconds).animation(ItemUseAnimation.DRINK).sound(SoundEvents.GENERIC_DRINK).build();
    }

    public static Consumable drinkConsumable() {
        return drinkConsumable(1.6f);
    }

    @Override
    public @NonNull ItemStack finishUsingItem(@NonNull ItemStack stack, @NonNull Level level, @NonNull LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            ItemStack originalCopy = stack.copy();

            if (!DrinkEvents.BEFORE_DRINK.invoker().beforeDrink(player, originalCopy)) {
                return stack;
            }

            onDrink(player, originalCopy);
            ItemStack result = super.finishUsingItem(stack, level, entity);
            DrinkEvents.AFTER_DRINK.invoker().afterDrink(player, originalCopy);

            return result;
        }

        return super.finishUsingItem(stack, level, entity);
    }

    protected void onDrink(ServerPlayer player, ItemStack drinkStack) { }
}
