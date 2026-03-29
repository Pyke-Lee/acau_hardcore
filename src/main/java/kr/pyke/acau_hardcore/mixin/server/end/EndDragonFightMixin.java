package kr.pyke.acau_hardcore.mixin.server.end;

import com.google.common.collect.Lists;
import kr.pyke.acau_hardcore.util.Tracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.dimension.end.DragonRespawnAnimation;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Mixin(EndDragonFight.class)
public abstract class EndDragonFightMixin {
    @Shadow @Final private ServerBossEvent dragonEvent;
    @Shadow private @Nullable UUID dragonUUID;
    @Shadow @Final private ServerLevel level;
    @Shadow private boolean dragonKilled;
    @Shadow private @Nullable DragonRespawnAnimation respawnStage;
    @Shadow private @Nullable BlockPos portalLocation;
    @Shadow @Final private BlockPos origin;

    @Shadow protected abstract BlockPattern.@Nullable BlockPatternMatch findExitPortal();
    @Shadow protected abstract void spawnExitPortal(boolean active);
    @Shadow protected abstract void respawnDragon(List<EndCrystal> crystals);

    @Redirect(
        method = "updatePlayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getPlayers(Ljava/util/function/Predicate;)Ljava/util/List;"
        )
    )
    private List<ServerPlayer> redirectGetPlayers(ServerLevel instance, Predicate<? super ServerPlayer> predicate) {
        List<ServerPlayer> originPlayers = instance.getPlayers(predicate);
        List<ServerPlayer> filteredPlayers = new ArrayList<>();

        for (ServerPlayer player : originPlayers) {
            if (!player.isSpectator()) {
                filteredPlayers.add(player);
            }
        }

        return filteredPlayers;
    }

    @Inject(method = "tick", at =@At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.dragonEvent.getPlayers().isEmpty()) {
            if (this.dragonUUID != null) {
                Entity dragon = this.level.getEntity(this.dragonUUID);
                if (dragon instanceof EnderDragon) {
                    dragon.discard();
                }
                cleanEndWorld();

                this.dragonUUID = null;
                this.dragonKilled = true;
                this.respawnStage = null;
            }
            else {
                if (this.dragonKilled && this.respawnStage == null) {
                    this.triggerAutoRespawn();
                }
            }
        }
    }

    @Unique
    private void triggerAutoRespawn() {
        BlockPos blockPos = this.portalLocation;

        if (blockPos == null) {
            BlockPattern.BlockPatternMatch match = this.findExitPortal();
            if (match == null) {
                this.spawnExitPortal(true);
            }
        }

        List<EndCrystal> list = Lists.newArrayList();
        this.respawnDragon(list);
    }

    @Unique
    private void cleanEndWorld() {
        for (SpikeFeature.EndSpike spike : SpikeFeature.getSpikesForLevel(this.level)) {
            for (EndCrystal crystal : this.level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox())) {
                crystal.discard();
            }
        }

        for (Boat boat : this.level.getEntitiesOfClass(Boat.class, new AABB(this.origin).inflate(192d))) {
            boat.ejectPassengers();
            boat.discard();
        }

        for (BlockPos pos : Tracker.PLACED_BLOCK.get(this.level.dimension())) {
            this.level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        Tracker.PLACED_BLOCK.get(this.level.dimension()).clear();
    }
}
