package kr.pyke.acau_hardcore.registry.item.ticket;

import kr.pyke.acau_hardcore.client.AcauHardCoreClient;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class TicketItem extends Item {
    public TicketItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(Level level, @NonNull Player player, @NonNull InteractionHand interactionHand) {
        if (!level.isClientSide()) { return InteractionResult.PASS; }
        if (ModComponents.HARDCORE_INFO.get(player).isStarted()) { return InteractionResult.PASS; }

        AcauHardCoreClient.openSelectHardCore();

        return InteractionResult.SUCCESS;
    }
}
