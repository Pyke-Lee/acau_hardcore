package kr.pyke.acau_hardcore.mixin.server.end;

import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.boss.enderdragon.phase.ModEnderDragonPhases;
import kr.pyke.util.constants.COLOR;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EnderDragon.class)
public class EnderDragonPhase2Mixin {
    @Unique private int dragonPhase = 1;
    @Unique private boolean patternTriggered = false;

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void onAiStep(CallbackInfo ci) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        if (dragon.level().isClientSide() || !dragon.isAlive()) {
            return;
        }

        ServerLevel level = (ServerLevel) dragon.level();

        if (this.dragonPhase == 1 && dragon.getHealth() <= dragon.getMaxHealth() * 0.5f) {
            this.dragonPhase = 2;
            this.patternTriggered = false;
            this.regenerateCrystals(level);
            dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING);
        }

        if (this.dragonPhase == 2 && !this.patternTriggered && dragon.getPhaseManager().getCurrentPhase().isSitting()) {
            this.patternTriggered = true;
            dragon.getPhaseManager().setPhase(ModEnderDragonPhases.EARTHQUAKE_PHASE);
        }
    }

    @Unique
    private void regenerateCrystals(ServerLevel level) {
        for (SpikeFeature.EndSpike spike : SpikeFeature.getSpikesForLevel(level)) {
            List<EndCrystal> crystals = level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox());
            if (crystals.isEmpty()) {
                EndCrystal crystal = EntityType.END_CRYSTAL.create(level, EntitySpawnReason.EVENT);
                if (crystal != null) {
                    crystal.setPos(spike.getCenterX() + 0.5, spike.getHeight() + 1, spike.getCenterZ() + 0.5);
                    crystal.setShowBottom(false);
                    level.addFreshEntity(crystal);
                }
            }
        }

        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator()) {
                PykeLib.sendSystemMessage(player, COLOR.RED.getColor(), "엔드 크리스탈이 재생성되었습니다!");
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onAddAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        output.putInt("DragonPhase", this.dragonPhase);
        output.putBoolean("PatternTriggered", this.patternTriggered);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onReadAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        this.dragonPhase = input.getIntOr("DragonPhase", 1);
        this.patternTriggered = input.getBooleanOr("PatternTriggered", false);
    }
}