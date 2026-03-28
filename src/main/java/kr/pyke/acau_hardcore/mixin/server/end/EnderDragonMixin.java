package kr.pyke.acau_hardcore.mixin.server.end;

import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragon.class)
public class EnderDragonMixin {
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        if (!dragon.level().isClientSide()) {
            AttributeInstance healthAttribute = dragon.getAttribute(Attributes.MAX_HEALTH);
            double configHealth = ModDimensions.isExpertDimension(dragon.level().dimension()) ? ModConfig.INSTANCE.expertDragonMaxHealth : ModConfig.INSTANCE.dragonMaxHealth;

            if (healthAttribute != null && healthAttribute.getBaseValue() != configHealth) {
                float healthRatio = dragon.getHealth() / dragon.getMaxHealth();
                healthAttribute.setBaseValue(configHealth);
                dragon.setHealth(dragon.getMaxHealth() * healthRatio);
            }
        }
    }

    @ModifyArg(
        method = "knockBack(Lnet/minecraft/server/level/ServerLevel;Ljava/util/List;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        ),
        index = 2
    )
    private float modifyWingDamage(float originalAmount) {
        EnderDragon dragon = (EnderDragon) (Object) this;

        return ModDimensions.isExpertDimension(dragon.level().dimension()) ? ModConfig.INSTANCE.expertDragonWingDamage : ModConfig.INSTANCE.dragonWingDamage;
    }

    @ModifyArg(
        method = "hurt(Lnet/minecraft/server/level/ServerLevel;Ljava/util/List;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        ),
        index = 2
    )
    private float modifyHeadDamage(float originalAmount) {
        EnderDragon dragon = (EnderDragon) (Object) this;

        return ModDimensions.isExpertDimension(dragon.level().dimension()) ? ModConfig.INSTANCE.expertDragonHeadDamage : ModConfig.INSTANCE.dragonHeadDamage;
    }
}
