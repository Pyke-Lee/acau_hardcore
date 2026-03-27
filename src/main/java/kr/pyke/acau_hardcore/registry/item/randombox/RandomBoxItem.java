package kr.pyke.acau_hardcore.registry.item.randombox;

import com.mojang.blaze3d.platform.InputConstants;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.data.randombox.*;
import kr.pyke.acau_hardcore.network.payload.s2c.S2C_OpenRandomBoxPayload;
import kr.pyke.util.constants.COLOR;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class RandomBoxItem extends Item {
    public RandomBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) { return InteractionResult.SUCCESS; }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        String boxId = BoxItemHelper.getBoxID(stack);
        if (boxId == null) { return InteractionResult.PASS; }

        BoxDefinition definition = BoxRegistry.get(boxId);
        if (definition == null) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "존재하지 않는 상자입니다: " + boxId);
            return InteractionResult.FAIL;
        }

        if (PendingRewardManager.hasPending(serverPlayer.getUUID())) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "이전 상자의 보상을 먼저 확인해주세요.");
            return InteractionResult.FAIL;
        }

        RandomSource random = serverPlayer.getRandom();
        BoxReward winningReward = definition.roll(random);
        int rewardIndex = definition.rewards().indexOf(winningReward);

        ItemStack rewardStack = BoxItemHelper.createRewardStack(winningReward, level.registryAccess());

        PendingRewardManager.store(serverPlayer.getUUID(), new PendingReward(
            rewardStack,
            winningReward.sound(),
            winningReward.messageType(),
            winningReward.message()
        ));

        stack.shrink(1);

        SoundEvent openSound = SoundEvents.CHEST_OPEN;
        if (definition.openSound() != null) {
            SoundEvent custom = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(definition.openSound()));
            if (custom != null) { openSound = custom; }
        }
        serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), openSound, SoundSource.PLAYERS, 1.0f, 1.0f);

        ServerPlayNetworking.send(serverPlayer, new S2C_OpenRandomBoxPayload(
            boxId, rewardStack, rewardIndex, winningReward.rarity().getKey()
        ));

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack, @NonNull TooltipContext ctx, @NonNull TooltipDisplay display, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag) {
        String boxId = BoxItemHelper.getBoxID(stack);
        if (boxId == null) { return; }

        BoxDefinition definition = BoxRegistry.exists(boxId) ? BoxRegistry.get(boxId) : ClientBoxRegistry.get(boxId);
        if (definition == null) {
            consumer.accept(Component.literal("알 수 없는 상자").withStyle(ChatFormatting.RED));
            return;
        }

        consumer.accept(Component.literal("우클릭으로 개봉").withStyle(ChatFormatting.GRAY));

        boolean isShiftDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT);
        if (isShiftDown) {
            for (BoxReward reward : definition.rewards()) {
                double chance = definition.getChance(reward);

                Identifier identifier = Identifier.parse(reward.item());
                Item item = BuiltInRegistries.ITEM.getValue(identifier);
                ItemStack itemStack = new ItemStack(item);

                String name = reward.customName() != null ? reward.customName() : itemStack.getDisplayName().getString().substring(1, itemStack.getDisplayName().getString().length() - 1);

                consumer.accept(Component.literal("  ").append(Component.literal(name + " x" + reward.count()).withStyle(ChatFormatting.WHITE)).append(Component.literal(String.format(" (%.1f%%)", chance)).withStyle(ChatFormatting.DARK_GRAY)));
            }
        }
        else {
            consumer.accept(Component.literal("[Shift] 키를 눌러서 확률 확인").withStyle(ChatFormatting.GRAY));
        }
    }
}