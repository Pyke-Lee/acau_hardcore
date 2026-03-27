package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import kr.pyke.acau_hardcore.util.WaterTracker;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class UseBlockCallbackEventHandler {
    private UseBlockCallbackEventHandler() { }

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
           if (level.dimension().equals(Level.END) || level.dimension().equals(ModDimensions.EXPERT_END)) {
               ItemStack heldItem = player.getItemInHand(hand);
               if (heldItem.is(ItemTags.BEDS)) {
                   return InteractionResult.FAIL;
               }

               if (heldItem.is(Items.WATER_BUCKET)) {
                   BlockPos targetPos = hitResult.getBlockPos().relative(hitResult.getDirection());
                   if (level.getBlockState(hitResult.getBlockPos()).canBeReplaced()) {
                       targetPos = hitResult.getBlockPos();
                   }
                   WaterTracker.PLACED_WATER.add(targetPos);
               }

               if (heldItem.is(Items.BUCKET)) {
                   BlockPos hitPos = hitResult.getBlockPos();
                   BlockPos relativePos = hitResult.getBlockPos().relative(hitResult.getDirection());
                   WaterTracker.PLACED_WATER.remove(hitPos);
                   WaterTracker.PLACED_WATER.remove(relativePos);
               }
           }

           return InteractionResult.PASS;
        });
    }
}
