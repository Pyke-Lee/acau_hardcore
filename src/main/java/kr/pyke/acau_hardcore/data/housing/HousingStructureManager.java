package kr.pyke.acau_hardcore.data.housing;

import kr.pyke.acau_hardcore.mixin.server.accessor.StructureTemplateAccessor;
import kr.pyke.acau_hardcore.util.HousingBuildQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.*;

public class HousingStructureManager {
    private static final Map<UUID, BlockPos[]> tempPosition = new HashMap<>();

    private HousingStructureManager() { }

    public static void setPos1(UUID uuid, BlockPos pos) {
        BlockPos[] positions = tempPosition.computeIfAbsent(uuid, k -> new BlockPos[2]);
        positions[0] = pos;
    }

    public static void setPos2(UUID uuid, BlockPos pos) {
        BlockPos[] positions = tempPosition.computeIfAbsent(uuid, k -> new BlockPos[2]);
        positions[1] = pos;
    }

    public static BlockPos[] getPositions(UUID uuid) {
        return tempPosition.get(uuid);
    }

    public static void clearPositions(UUID uuid) {
        tempPosition.remove(uuid);
    }

    public static void changeTier(ServerLevel level, HousingZone zone, int targetTier, Identifier structureID, boolean isDowngrade) {
        if (isDowngrade) {
            BlockPos min = zone.getMinPos();
            BlockPos max = zone.getMaxPos();
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        if (!level.getBlockState(blockPos).isAir()) {
                            HousingBuildQueue.addTask(level, blockPos, Blocks.AIR.defaultBlockState(), null, true);
                        }
                    }
                }
            }
        }

        StructureTemplateManager manager = level.getStructureManager();
        Optional<StructureTemplate> templateOptional = manager.get(structureID);
        if (templateOptional.isPresent()) {
            StructureTemplate template = templateOptional.get();
            List<StructureTemplate.Palette> palettes = ((StructureTemplateAccessor) template).getPalettes();
            if (!palettes.isEmpty()) {
                StructureTemplate.Palette palette = palettes.getFirst();
                for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
                    BlockPos targetBlockPos = blockInfo.pos().offset(zone.getMinPos());
                    HousingBuildQueue.addTask(level, targetBlockPos, blockInfo.state(), blockInfo.nbt(), false);
                }
            }
        }
    }
}
