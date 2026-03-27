package kr.pyke.acau_hardcore.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class BlockBreakQueue {
    private static final Queue<BreakTask> QUEUE = new LinkedList<>();
    private static final Set<BlockPos> QUEUED_BLOCKS = new HashSet<>();
    public static boolean isProcessing = false;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (QUEUE.isEmpty()) { return; }

            isProcessing = true;
            int blocksPerTick = Math.max(5, QUEUE.size() / 10);

            for (int i = 0; i < blocksPerTick; i++) {
                if (QUEUE.isEmpty()) { break; }

                BreakTask task = QUEUE.poll();
                if (task != null) {
                    QUEUED_BLOCKS.remove(task.pos());
                    task.level().destroyBlock(task.pos(), true, task.player());
                }
            }

            isProcessing = false;
        });
    }

    public static void addTask(ServerLevel level, BlockPos pos, Player player) {
        if (QUEUED_BLOCKS.add(pos)) {
            QUEUE.add(new BreakTask(level, pos, player));
        }
    }

    private record BreakTask(ServerLevel level, BlockPos pos, Player player) { }
}