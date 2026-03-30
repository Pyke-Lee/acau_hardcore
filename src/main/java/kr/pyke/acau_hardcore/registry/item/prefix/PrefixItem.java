package kr.pyke.acau_hardcore.registry.item.prefix;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.prefix.PrefixData;
import kr.pyke.acau_hardcore.prefix.PrefixRegistry;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.prefix.IPrefixes;
import kr.pyke.acau_hardcore.util.ColorParser;
import kr.pyke.util.constants.COLOR;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class PrefixItem extends Item {
    public PrefixItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(Level level, @NonNull Player player, @NonNull InteractionHand interactionHand) {
        if (level.isClientSide()) { return InteractionResult.PASS; }

        ItemStack itemStack = player.getMainHandItem();
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) { return InteractionResult.PASS; }

        CompoundTag compoundTag = customData.copyTag();
        String prefixID = compoundTag.getStringOr("prefix_id", "none");
        if (prefixID.equals("none")) {
            PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.RED.getColor(), "사용이 불가능한 아이템입니다.");
            return InteractionResult.FAIL;
        }

        IPrefixes prefixes = ModComponents.PREFIXES.get(player);
        if (prefixes.getPrefixes().contains(prefixID)) {
            PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.RED.getColor(), "이미 보유중인 칭호입니다.");
            return InteractionResult.FAIL;
        }

        prefixes.addPrefix(prefixID);
        PrefixData prefixData = PrefixRegistry.get(prefixID);
        String display = prefixData != null ? ColorParser.parse(prefixData.prefix()).getString() : prefixID;

        PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.LIME.getColor(), String.format("새로운 칭호 &7%s&r를 획득했습니다!", display));
        player.playSound(SoundEvents.UI_TOAST_IN, 1.f, 1.f);

        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack itemStack) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) { return super.getName(itemStack); }

        CompoundTag compoundTag = customData.copyTag();
        String prefixID = compoundTag.getStringOr("prefix_id", "none");
        if (prefixID.equals("none")) { return super.getName(itemStack); }

        PrefixData prefixData = PrefixRegistry.get(prefixID);
        if (prefixData == null) { return super.getName(itemStack); }

        return Component.empty().append(ColorParser.parse(prefixData.prefix()));
    }
}
