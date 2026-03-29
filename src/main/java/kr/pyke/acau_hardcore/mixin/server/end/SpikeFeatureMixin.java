package kr.pyke.acau_hardcore.mixin.server.end;

import kr.pyke.acau_hardcore.level.end.CustomSpikeDefinitions;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SpikeFeature.class)
public class SpikeFeatureMixin {
    @Inject(method = "getSpikesForLevel", at = @At("HEAD"), cancellable = true)
    private static void getCustomSpikes(WorldGenLevel level, CallbackInfoReturnable<List<SpikeFeature.EndSpike>> cir) {
        if (level instanceof ServerLevelAccessor accessor) {
            ServerLevel serverLevel = accessor.getLevel();
            if (serverLevel.dimension() == Level.END || serverLevel.dimension() == ModDimensions.EXPERT_END) {
                cir.setReturnValue(CustomSpikeDefinitions.getSpikes());
            }
        }
    }
}
