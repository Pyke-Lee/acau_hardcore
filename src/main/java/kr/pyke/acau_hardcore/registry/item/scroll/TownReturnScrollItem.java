package kr.pyke.acau_hardcore.registry.item.scroll;

import kr.pyke.acau_hardcore.data.ServerSavedData;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public class TownReturnScrollItem extends Item {
    public TownReturnScrollItem(Properties properties) {
        super(properties.component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(0.f, 1.f, List.of(), BlocksAttacks.ItemDamageFunction.DEFAULT, Optional.empty(), Optional.empty(), Optional.empty())));
    }

    @Override
    public @NonNull ItemUseAnimation getUseAnimation(@NonNull ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(@NonNull ItemStack itemStack, @NonNull LivingEntity user) {
        return 100;
    }

    @Override
    public @NonNull ItemStack finishUsingItem(@NonNull ItemStack itemStack, Level level, @NonNull LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer serverPlayer) {
            if (ModComponents.HARDCORE_INFO.get(serverPlayer).isJail()) { return itemStack; }

            ServerSavedData data = ServerSavedData.getServerState(serverPlayer.level().getServer());
            TeleportTransition transition = data.createLobbyTransition(serverPlayer.level().getServer(), TeleportTransition.DO_NOTHING);

            if (transition != null) {
                serverPlayer.teleport(transition);
                serverPlayer.getCooldowns().addCooldown(itemStack, 200);

                if (!serverPlayer.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
        }

        return itemStack;
    }
}
