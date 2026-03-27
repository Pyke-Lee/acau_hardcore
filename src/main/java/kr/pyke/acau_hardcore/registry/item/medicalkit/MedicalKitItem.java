package kr.pyke.acau_hardcore.registry.item.medicalkit;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class MedicalKitItem extends Item {
    public MedicalKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand interactionHand) {
        if (level.isClientSide()) { return InteractionResult.PASS; }

        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (player.getCooldowns().isOnCooldown(itemstack)) { return InteractionResult.PASS; }
        if (player.getHealth() >= player.getMaxHealth()) { return InteractionResult.PASS; }

        player.heal(4.f);
        player.getCooldowns().addCooldown(itemstack, 10);
        if (!player.getAbilities().instabuild) { itemstack.shrink(1); }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5f, 1.f);

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext ctx, @NonNull TooltipDisplay display, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag) {
        consumer.accept(Component.literal("체력을 4 회복합니다.").withStyle(ChatFormatting.GRAY));
    }
}
