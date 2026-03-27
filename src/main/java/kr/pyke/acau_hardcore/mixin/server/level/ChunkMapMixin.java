package kr.pyke.acau_hardcore.mixin.server.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Unique
    private static final Map<ResourceKey<Level>, Integer> SEED_INDEX_MAP = Map.of(
        ModDimensions.BEGINNER_OVERWORLD, 1,
        ModDimensions.EXPERT_OVERWORLD, 2,
        ModDimensions.EXPERT_NETHER, 3,
        ModDimensions.EXPERT_END, 4
    );

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/RandomState;create(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)Lnet/minecraft/world/level/levelgen/RandomState;"
        )
    )
    private RandomState modifyDimensionSeed(NoiseGeneratorSettings noiseGeneratorSettings, HolderGetter<NormalNoise.NoiseParameters> holderGetter, long seed, Operation<RandomState> original, @Local(argsOnly = true, ordinal = 0) ServerLevel level) {
        Integer index = SEED_INDEX_MAP.get(level.dimension());
        if (index != null) { seed = hashSeed(seed, index); }

        return original.call(noiseGeneratorSettings, holderGetter, seed);
    }

    @Unique
    private static long hashSeed(long originalSeed, int worldIndex) {
        long hash = originalSeed ^ (0x9E3779B97F4A7C15L * worldIndex);
        hash = (hash ^ (hash >>> 30)) * 0xBF58476D1CE4E5B9L;
        hash = (hash ^ (hash >>> 27)) * 0x94D049BB133111EBL;

        return hash ^ (hash >>> 31);
    }
}
