package kr.pyke.acau_hardcore.handler;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class UseItemCallbackEventHandler {
    private UseItemCallbackEventHandler() { }

    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
           ItemStack heldItem = player.getMainHandItem();

           if (heldItem.is(Items.ENDER_EYE)) {
               return InteractionResult.FAIL;
           }

           if (player.level().dimension().equals(Level.OVERWORLD)) {
               if (!player.isCreative()) {
                   if (heldItem.is(Items.ENDER_PEARL)) {
                       return InteractionResult.FAIL;
                   }
                   if (heldItem.is(Items.END_CRYSTAL)) {
                       return InteractionResult.FAIL;
                   }
                   if (heldItem.is(ConventionalItemTags.BUCKETS)) {
                       return InteractionResult.FAIL;
                   }
               }
           }

           return InteractionResult.PASS;
        });
    }
}
