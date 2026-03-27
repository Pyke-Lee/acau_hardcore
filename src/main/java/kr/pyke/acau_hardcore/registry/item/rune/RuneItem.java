package kr.pyke.acau_hardcore.registry.item.rune;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.data.rune.RuneInstance;
import kr.pyke.acau_hardcore.data.rune.RuneRoller;
import kr.pyke.acau_hardcore.type.RUNE_TYPE;
import kr.pyke.util.constants.COLOR;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class RuneItem extends Item {
    private final RUNE_TYPE runeType;

    public RuneItem(Properties properties, RUNE_TYPE runeType) {
        super(properties);
        this.runeType = runeType;
    }

    public RUNE_TYPE getRuneType() {
        return runeType;
    }

    @Override
    public boolean overrideStackedOnOther(@NonNull ItemStack stack, @NonNull Slot slot, @NonNull ClickAction action, @NonNull Player player) {
        if (action != ClickAction.SECONDARY) { return false; }

        ItemStack targetStack = slot.getItem();
        if (targetStack.isEmpty()) { return false; }
        if (!RuneItemHelper.canApply(runeType, targetStack)) { return false; }

        if (player instanceof ServerPlayer serverPlayer) {
            applyTo(serverPlayer, stack, targetStack);
        }

        return true;
    }

    public void applyTo(ServerPlayer player, ItemStack runeStack, ItemStack targetStack) {
        if (targetStack.isEmpty()) { return; }

        if (!RuneItemHelper.canApply(runeType, targetStack)) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "이 아이템에는 적용할 수 없습니다.");
            return;
        }

        RuneInstance rune = RuneRoller.roll(runeType, targetStack, player.getRandom());
        if (rune == null) {
            PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "적용 가능한 효과가 없습니다.");
            return;
        }

        RuneItemHelper.setRune(targetStack, rune);
        runeStack.shrink(1);

        CustomData customData = targetStack.get(DataComponents.CUSTOM_DATA);
        boolean hasTag = customData != null && customData.copyTag().contains("KeepOnDeath");
        if (!hasTag) {
            CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();
            tag.putBoolean("KeepOnDeath", true);
            targetStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }

        SoundEvent sound = rune.effect().isRare() ? SoundEvents.UI_TOAST_CHALLENGE_COMPLETE : SoundEvents.ENCHANTMENT_TABLE_USE;
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 0.5f, 1.0f);

        PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "(RUNE) " + rune.formatDescription());
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext ctx, @NonNull TooltipDisplay display, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag) {
        consumer.accept(Component.literal("장비에 사용하여 랜덤 효과를 부여합니다.").withStyle(ChatFormatting.GRAY));
        consumer.accept(Component.literal("기존 효과는 덮어쓰기됩니다.").withStyle(ChatFormatting.GRAY));
    }
}