package kr.pyke.acau_hardcore.mixin.server.entity;

import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.data.rune.RuneInstance;
import kr.pyke.acau_hardcore.event.FoodEvents;
import kr.pyke.acau_hardcore.handler.RuneEventHandler;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import kr.pyke.acau_hardcore.type.RUNE_EFFECT;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique private ItemStack foodBeingEaten = ItemStack.EMPTY;

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void captureFood(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player)) { return; }

        ItemStack useItem = self.getUseItem();
        if (useItem.isEmpty()) { return; }

        if (useItem.has(DataComponents.FOOD) && useItem.getUseAnimation() == ItemUseAnimation.EAT) {
            if (FoodEvents.BEFORE_EAT.invoker().beforeEat(player, useItem)) {
                this.foodBeingEaten = useItem.copy();
            }
        }
    }

    @Inject(method = "completeUsingItem", at = @At("TAIL"))
    private void AfterEat(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player)) { return; }

        if (!this.foodBeingEaten.isEmpty()) {
            FoodEvents.AFTER_EAT.invoker().afterEat(player, this.foodBeingEaten);
            this.foodBeingEaten = ItemStack.EMPTY;
        }
    }

    @Inject(
        method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onHurtServerHead(ServerLevel serverLevel, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity target = (LivingEntity) (Object) this;

        if (target instanceof Player player) {
            List<RuneInstance> runes = RuneEventHandler.getEquippedRunes(player);
            for (RuneInstance rune : runes) {
                if (rune.effect() == RUNE_EFFECT.DODGE_CHANCE) {
                    if (player.getRandom().nextFloat() * 100f <= rune.value1()) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                else if (rune.effect() == RUNE_EFFECT.SATURATION_ON_HIT) {
                    if (player.getRandom().nextFloat() * 100f <= rune.value1()) {
                        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20, 0, false, false, true));
                    }
                }
            }
        }
    }

    @ModifyVariable(
        method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At("HEAD"),
        argsOnly = true, ordinal = 0
    )
    private float modifyDamage(float amount, ServerLevel level, DamageSource source) {
        if (source.getEntity() instanceof Player attacker) {
            List<RuneInstance> runes = RuneEventHandler.getEquippedRunes(attacker);
            float modifiedAmount = amount;

            for (RuneInstance rune : runes) {
                if (rune.effect() == RUNE_EFFECT.LOW_HP_ATTACK) {
                    float attackerHpPct = (attacker.getHealth() / attacker.getMaxHealth()) * 100f;
                    if (attackerHpPct <= rune.value1()) {
                        modifiedAmount += rune.value2();
                    }
                }
                else if (rune.effect() == RUNE_EFFECT.CRIT_DAMAGE) {
                    modifiedAmount += modifiedAmount * (rune.value1() / 100f);
                }
            }
            return modifiedAmount;
        }
        return amount;
    }

    @Inject(
        method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At("RETURN")
    )
    private void onHurtServerSuccess(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) { return; }

        if (source.getEntity() instanceof Player attacker) {
            LivingEntity target = (LivingEntity) (Object) this;
            List<RuneInstance> runes = RuneEventHandler.getEquippedRunes(attacker);

            for (RuneInstance rune : runes) {
                if (rune.effect() == RUNE_EFFECT.LIFE_STEAL) {
                    attacker.heal(rune.value1());
                }
                else if (rune.effect() == RUNE_EFFECT.EXECUTE) {
                    if (target instanceof EnderDragon || target instanceof WitherBoss || target instanceof Player) { return; }

                    float targetHpPct = (target.getHealth() / target.getMaxHealth()) * 100f;
                    if (attacker.getRandom().nextFloat() * 100f <= rune.value1() && targetHpPct <= rune.value2()) {
                        target.setHealth(0.f);
                    }
                }
            }
        }
    }

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float modifyDragonBreathDamage(float amount, ServerLevel level, DamageSource damageSource) {
        if(damageSource.getDirectEntity() instanceof AreaEffectCloud cloud) {
            if(cloud.getOwner() instanceof EnderDragon) {
                return ModDimensions.isExpertDimension(level.dimension()) ? ModConfig.INSTANCE.expertDragonBreathDamage : ModConfig.INSTANCE.dragonBreathDamage;
            }
        }

        return amount;
    }
}