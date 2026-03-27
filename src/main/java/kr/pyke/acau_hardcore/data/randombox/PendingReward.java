package kr.pyke.acau_hardcore.data.randombox;

import kr.pyke.acau_hardcore.type.BOX_MESSAGE_TYPE;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record PendingReward(ItemStack stack, @Nullable String sound, BOX_MESSAGE_TYPE messageType, String message) { }