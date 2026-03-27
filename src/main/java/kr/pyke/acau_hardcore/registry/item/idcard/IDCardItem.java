package kr.pyke.acau_hardcore.registry.item.idcard;

import kr.pyke.acau_hardcore.client.AcauHardCoreClient;
import kr.pyke.acau_hardcore.client.gui.screen.ChangeDisplayNameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class IDCardItem extends Item {
    public IDCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(Level level, @NonNull Player player, @NonNull InteractionHand interactionHand) {
        if (level.isClientSide()) { AcauHardCoreClient.openChangeDisplayName(); }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) { AcauHardCoreClient.openChangeDisplayName(); }

        return InteractionResult.SUCCESS;
    }
}
