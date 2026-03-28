package kr.pyke.acau_hardcore.mixin.server.end;

import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(EnderDragonPhaseManager.class)
public class EnderDragonPhaseManagerMixin {
    @Shadow @Final @Mutable private DragonPhaseInstance[] phases;

    @Inject(method = "getPhase", at = @At("HEAD"))
    private <T extends DragonPhaseInstance> void ensurePhaseArrayCapacity(EnderDragonPhase<T> phase, CallbackInfoReturnable<T> cir) {
        int id = phase.getId();
        if (id >= this.phases.length) {
            this.phases = Arrays.copyOf(this.phases, id + 1);
        }
    }
}