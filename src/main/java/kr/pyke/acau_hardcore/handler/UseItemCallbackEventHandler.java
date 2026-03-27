package kr.pyke.acau_hardcore.handler;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class UseItemCallbackEventHandler {
    private UseItemCallbackEventHandler() { }

    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
           ItemStack heldItem = player.getMainHandItem();

           if (heldItem.is(Items.ENDER_EYE)) {
               return InteractionResult.FAIL;
           }

           return InteractionResult.PASS;
        });
    }
}
