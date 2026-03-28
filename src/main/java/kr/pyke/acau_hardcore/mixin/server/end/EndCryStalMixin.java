package kr.pyke.acau_hardcore.mixin.server.end;

import kr.pyke.PykeLib;
import kr.pyke.util.constants.COLOR;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EndCrystal.class)
public class EndCryStalMixin {
    @Inject(
        method = "hurtServer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/boss/enderdragon/EndCrystal;remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"
        )
    )
    private void onCrystalDestroyed(ServerLevel level, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        EndCrystal self = (EndCrystal) (Object) this;

        int remaining = 0;
        for (SpikeFeature.EndSpike spike : SpikeFeature.getSpikesForLevel(level)) {
            List<EndCrystal> crystals = level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox());
            for (EndCrystal crystal : crystals) {
                if (!crystal.getUUID().equals(self.getUUID())) {
                    remaining++;
                }
            }
        }

        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator()) {
                PykeLib.sendSystemMessage(player, COLOR.YELLOW.getColor(), String.format("엔드 크리스탈이 파괴되었습니다! (남은 크리스탈: &e%d&r개)", remaining));
            }
        }
    }
}
