package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import kr.pyke.acau_hardcore.util.Tracker;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class UseBlockCallbackEventHandler {
    private UseBlockCallbackEventHandler() { }

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide()) { return InteractionResult.PASS; }

            if (level.dimension().equals(Level.END) || level.dimension().equals(ModDimensions.EXPERT_END)) {
                ItemStack heldItem = player.getItemInHand(hand);
                BlockPos hitPos = hitResult.getBlockPos();
                BlockState hitState = level.getBlockState(hitPos);

                if (heldItem.is(ItemTags.BEDS)) {
                    if (player.isShiftKeyDown() || !hitState.hasBlockEntity()) {
                        return InteractionResult.FAIL;
                    }
                }

                if (heldItem.getItem() instanceof BlockItem) {
                    BlockPos targetPos = hitPos.relative(hitResult.getDirection());
                    if (hitState.canBeReplaced()) {
                        targetPos = hitPos;
                    }
                    Tracker.PLACED_BLOCK.get(level.dimension()).add(targetPos);
                }

                if (heldItem.is(Items.BUCKET)) {
                    if (hitState.getFluidState().is(Fluids.WATER)) {
                        BlockPos relativePos = hitPos.relative(hitResult.getDirection());
                        Tracker.PLACED_BLOCK.get(level.dimension()).remove(hitPos);
                        Tracker.PLACED_BLOCK.get(level.dimension()).remove(relativePos);
                    }
                }
            }

            return InteractionResult.PASS;
        });
    }
}
