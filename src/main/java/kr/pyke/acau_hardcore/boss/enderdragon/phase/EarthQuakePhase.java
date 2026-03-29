package kr.pyke.acau_hardcore.boss.enderdragon.phase;

import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EarthQuakePhase extends AbstractDragonSittingPhase {
    private int ticks;
    private float waveRadius;
    private final Set<UUID> hitPlayers = new HashSet<>();

    public EarthQuakePhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void doServerTick(@NonNull ServerLevel level) {
        this.ticks++;
        this.dragon.setDeltaMovement(Vec3.ZERO);

        int chargeLimit = ModDimensions.isExpertDimension(level.dimension()) ? ModConfig.INSTANCE.expertDragonEarthQuakeTicks : ModConfig.INSTANCE.dragonEarthQuakeTicks;

        if (this.ticks <= chargeLimit) {
            if (this.ticks % 10 == 0) {
                float pitch = 0.5f + ((float)this.ticks / (float)chargeLimit);
                level.playSound(null, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 1.f, pitch);
            }

            level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.dragon.getX(), this.dragon.getY() + 1, this.dragon.getZ(), 15, 1.2, 1, 1.2, 0.05);
        }
        else if (this.ticks <= chargeLimit + 80) {
            if (this.ticks == chargeLimit + 1) {
                level.playSound(null, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 3.f, 1.f);
            }

            this.waveRadius += 0.8f;
            this.spawnWaveParticles(level);
            this.checkWaveCollision(level);
        }
        else {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
        }
    }

    private void spawnWaveParticles(ServerLevel level) {
        for (int i = 0; i < 72; i++) {
            double angle = Math.toRadians(i * 5);
            double px = this.dragon.getX() + Math.cos(angle) * this.waveRadius;
            double pz = this.dragon.getZ() + Math.sin(angle) * this.waveRadius;
            level.sendParticles(ParticleTypes.EXPLOSION, px, this.dragon.getY() - 3d, pz, 1, 0, 0, 0, 0);
        }
    }

    private void checkWaveCollision(ServerLevel level) {
        float damage = ModDimensions.isExpertDimension(level.dimension()) ? ModConfig.INSTANCE.expertDragonEarthQuakeDamage : ModConfig.INSTANCE.dragonEarthQuakeDamage;
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && !player.isCreative() && !this.hitPlayers.contains(player.getUUID())) {
                double dist = Math.sqrt(player.distanceToSqr(this.dragon.getX(), player.getY(), this.dragon.getZ()));
                if (Math.abs(dist - this.waveRadius) < 1.5f && player.onGround()) {
                    player.hurtServer(level, this.dragon.damageSources().sonicBoom(this.dragon), damage);
                    player.setDeltaMovement(player.getDeltaMovement().add(0, 1.2, 0));
                    player.hurtMarked = true;
                    this.hitPlayers.add(player.getUUID());
                }
            }
        }
    }

    @Override
    public void begin() {
        this.ticks = 0;
        this.waveRadius = 0.f;
        this.hitPlayers.clear();
    }

    @Override
    public @NonNull EnderDragonPhase<EarthQuakePhase> getPhase() {
        return ModEnderDragonPhases.EARTHQUAKE_PHASE;
    }
}