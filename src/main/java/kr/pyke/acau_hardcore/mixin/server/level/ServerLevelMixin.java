package kr.pyke.acau_hardcore.mixin.server.level;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
    @Shadow private EndDragonFight dragonFight;

    protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Override
    public @NonNull Difficulty getDifficulty() {
        if (this.dimension().equals(Level.OVERWORLD)) {
            return Difficulty.PEACEFUL;
        }

        return this.getLevelData().getDifficulty();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initDragonFightForExpertEnd(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey<Level> resourceKey, LevelStem levelStem, boolean bl, long l, List<CustomSpawner> list, boolean bl2, RandomSequences randomSequences, CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;

        if (self.dimension().equals(ModDimensions.EXPERT_END)) {
            long seed = self.getSeed();
            this.dragonFight = new EndDragonFight(self, seed, EndDragonFight.Data.DEFAULT);
        }
    }

    @ModifyVariable(method = "explode", at = @At("HEAD"), argsOnly = true)
    private Level.ExplosionInteraction preventOverworldExplosionDamage(Level.ExplosionInteraction original) {
        if (this.dimension() == Level.OVERWORLD) {
            return Level.ExplosionInteraction.NONE;
        }

        return original;
    }

    @Inject(method = "tickCustomSpawners", at = @At("HEAD"), cancellable = true)
    private void onTickCustomSpawners(boolean spawnEnemies, CallbackInfo ci) {
        if (this.dimension() == Level.OVERWORLD) {
            ci.cancel();
        }
    }
}