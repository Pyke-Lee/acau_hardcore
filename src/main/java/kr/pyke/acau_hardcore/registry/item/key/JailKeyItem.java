package kr.pyke.acau_hardcore.registry.item.key;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.util.constants.COLOR;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class JailKeyItem extends Item {
    public JailKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        if (level.isClientSide()) { return InteractionResult.PASS; }

        ItemStack itemstack = player.getItemInHand(hand);
        var info = ModComponents.HARDCORE_INFO.get(player);
        if (info.isJail()) {
            if (!player.getAbilities().instabuild) { itemstack.shrink(1); }
            info.exitJail();
            player.getCooldowns().addCooldown(itemstack, 20);
            PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.LIME.getColor(), "감옥에서 탈출하여 이전 위치로 돌아갑니다.");

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}
