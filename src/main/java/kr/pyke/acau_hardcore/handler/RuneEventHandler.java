package kr.pyke.acau_hardcore.handler;

import kr.pyke.acau_hardcore.data.rune.RuneInstance;
import kr.pyke.acau_hardcore.registry.item.rune.RuneItemHelper;
import kr.pyke.acau_hardcore.type.RUNE_EFFECT;
import kr.pyke.acau_hardcore.util.BlockBreakQueue;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class RuneEventHandler {
    private RuneEventHandler() { }

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((level, player, blockPos, blockState, blockEntity) -> {
            if (level instanceof ServerLevel serverLevel) {
                if (BlockBreakQueue.isProcessing) { return; }

                handleBlockBreakRunes(player, serverLevel, blockPos, blockState.getBlock());
            }
        });
    }

    public static List<RuneInstance> getEquippedRunes(Player player) {
        List<RuneInstance> runes = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.OFFHAND) { continue; }

            ItemStack itemStack = player.getItemBySlot(slot);
            RuneInstance rune = RuneItemHelper.getRune(itemStack);
            if (rune != null) { runes.add(rune); }
        }

        return runes;
    }

    private static void handleBlockBreakRunes(Player player, ServerLevel serverLevel, BlockPos blockPos, Block brokenBlock) {
        List<RuneInstance> runes = getEquippedRunes(player);

        for (RuneInstance rune : runes) {
            if (rune.effect() == RUNE_EFFECT.VEIN_MINE) {
                boolean includeHorizontal = rune.value1() != 1;

                for (Direction direction : Direction.values()) {
                    if (!includeHorizontal && direction.getAxis().isHorizontal()) { continue; }

                    BlockPos nextPos = blockPos.relative(direction);
                    if (serverLevel.getBlockState(nextPos).is(brokenBlock)) {
                        BlockBreakQueue.addTask(serverLevel, nextPos, player);
                    }
                }
            }
            else if (rune.effect() == RUNE_EFFECT.TREE_FELL) {
                if (brokenBlock.defaultBlockState().is(BlockTags.LOGS)) {
                    findAndQueueLogs(serverLevel, blockPos, brokenBlock, player, (int) rune.value1());
                }
            }
        }
    }

    private static void findAndQueueLogs(ServerLevel level, BlockPos startPos, Block targetBlock, Player player, int maxHeight) {
        for (int i = 1; i <= maxHeight; i++) {
            BlockPos nextPos = startPos.above(i);
            if (level.getBlockState(nextPos).is(targetBlock)) {
                BlockBreakQueue.addTask(level, nextPos, player);
            }
            else {
                break;
            }
        }
    }
}
