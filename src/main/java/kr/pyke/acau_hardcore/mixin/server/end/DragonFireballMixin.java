package kr.pyke.acau_hardcore.mixin.server.end;

import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFireball.class)
public class DragonFireballMixin {
    @Inject(method = "onHit", at = @At("HEAD"))
    private void onFireballHit(HitResult result, CallbackInfo ci) {
        DragonFireball fireball = (DragonFireball)(Object)this;
        if (result.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult)result).getEntity();
            if (!target.equals(fireball.getOwner()) && target instanceof LivingEntity livingTarget) {
                if (!fireball.level().isClientSide() && fireball.level() instanceof ServerLevel serverLevel) {
                    float damage = ModDimensions.isExpertDimension(serverLevel.dimension()) ? ModConfig.INSTANCE.expertDragonFireballDamage : ModConfig.INSTANCE.dragonFireballDamage;
                    LivingEntity owner = null;
                    if (fireball.getOwner() instanceof LivingEntity livingOwner) {
                        owner = livingOwner;
                    }

                    livingTarget.hurtServer(serverLevel, fireball.damageSources().mobProjectile(fireball, owner), damage);
                }
            }
        }
    }
}
