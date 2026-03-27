package kr.pyke.acau_hardcore.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.Queue;

public class HousingBuildQueue {
    private static final int BLOCKS_PER_TICK = 64;
    private static final Queue<BuildTask> QUEUE = new LinkedList<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
           processQueue();
        });
    }

    public static void addTask(ServerLevel level, BlockPos blockPos, BlockState blockState, CompoundTag compoundTag, boolean isDowngrade) {
        QUEUE.add(new BuildTask(level, blockPos, blockState, compoundTag, isDowngrade));
    }

    private static void processQueue() {
        if (QUEUE.isEmpty()) { return; }

        int processed = 0;
        while (!QUEUE.isEmpty() && processed < BLOCKS_PER_TICK) {
            BuildTask task = QUEUE.poll();
            if (task != null) {
                executeTask(task);
                processed++;
            }
        }
    }

    private static void executeTask(BuildTask task) {
        if (task.isDowngrade) {
            task.level.sendParticles(ParticleTypes.EXPLOSION, task.blockPos.getX() + 0.5, task.blockPos.getY(), task.blockPos.getZ() + 0.5, 1, 0d, 0d, 0d, 0d);
            task.getLevel().playSound(null, task.blockPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.25f, 1.f);
        }

        task.level.setBlock(task.blockPos, task.blockState, Block.UPDATE_ALL);

        if (task.nbt != null) {
            BlockEntity blockEntity = BlockEntity.loadStatic(task.blockPos, task.blockState, task.nbt, task.level.registryAccess());
            if (blockEntity != null) {
                task.getLevel().setBlockEntity(blockEntity);
            }
        }
    }

    public static class BuildTask {
        private final ServerLevel level;
        private final BlockPos blockPos;
        private final BlockState blockState;
        private final CompoundTag nbt;
        private final boolean isDowngrade;

        public BuildTask(ServerLevel level, BlockPos blockPos, BlockState blockState, CompoundTag nbt, boolean isDowngrade) {
            this.level = level;
            this.blockPos = blockPos;
            this.blockState = blockState;
            this.nbt = nbt;
            this.isDowngrade = isDowngrade;
        }

        public ServerLevel getLevel() { return this.level; }
        public BlockPos getBlockPos() { return this.blockPos; }
        public BlockState getBlockState() { return this.blockState; }
        public CompoundTag getNbt() { return this.nbt; }
        public boolean isDowngrade() { return this.isDowngrade; }
    }
}
