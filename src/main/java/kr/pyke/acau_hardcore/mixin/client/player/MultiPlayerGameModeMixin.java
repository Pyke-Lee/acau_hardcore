package kr.pyke.acau_hardcore.mixin.client.player;

import kr.pyke.acau_hardcore.data.housing.HousingZone;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.housing.IHousingData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Final @Shadow private Minecraft minecraft;

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos blockPos = hitResult.getBlockPos();
        IHousingData housingData = ModComponents.HOUSING_DATA.get(player.level());
        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.isInsideZone(blockPos)) {
                if (zone.getOwnerID() != null && !zone.getOwnerID().equals(player.getUUID())) {
                    if (!player.isCreative()) {
                        cir.setReturnValue(InteractionResult.PASS);
                    }
                    return;
                }
                else if (zone.getOwnerID().equals(player.getUUID())) {
                    return;
                }
            }
        }

        if (player.level().dimension() == Level.OVERWORLD && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    public void onDestroyBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft.level == null || minecraft.player == null) { return; }

        IHousingData housingData = ModComponents.HOUSING_DATA.get(minecraft.level);
        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.isInsideZone(blockPos)) {
                if (zone.getOwnerID() != null && !zone.getOwnerID().equals(minecraft.player.getUUID())) {
                    if (!minecraft.player.isCreative()) {
                        cir.setReturnValue(false);
                    }
                    return;
                }
            }
        }

        if (minecraft.level.dimension() == Level.OVERWORLD && !minecraft.player.isCreative()) {
            cir.setReturnValue(false);
        }
    }
}
