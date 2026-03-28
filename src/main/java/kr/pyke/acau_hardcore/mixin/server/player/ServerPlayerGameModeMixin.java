package kr.pyke.acau_hardcore.mixin.server.player;

import kr.pyke.acau_hardcore.data.housing.HousingZone;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.housing.IHousingData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onUseItemOn(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos blockPos = hitResult.getBlockPos();
        IHousingData housingData = ModComponents.HOUSING_DATA.get(level);
        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.isInsideZone(blockPos)) {
                if (zone.getOwnerID() != null && !zone.getOwnerID().equals(this.player.getUUID())) {
                    if (!player.isCreative()) {
                        cir.setReturnValue(InteractionResult.PASS);
                    }
                }

                return;
            }
        }

        if (level.dimension() == Level.OVERWORLD && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    public void onDestroyBlock(BlockPos blockPos, CallbackInfoReturnable<InteractionResult> cir) {
        IHousingData housingData = ModComponents.HOUSING_DATA.get(this.level);
        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.isInsideZone(blockPos)) {
                if (zone.getOwnerID() != null && !zone.getOwnerID().equals(this.player.getUUID())) {
                    if (!player.isCreative()) {
                        cir.setReturnValue(InteractionResult.PASS);
                    }
                }

                return;
            }
        }

        if (level.dimension() == Level.OVERWORLD && !player.isCreative()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
