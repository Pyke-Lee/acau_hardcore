package kr.pyke.acau_hardcore.util;

import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Tracker {
    public static final Map<ResourceKey<Level>, Set<BlockPos>> PLACED_BLOCK = new HashMap<>();

    public static void register() {
        PLACED_BLOCK.put(Level.END, new HashSet<>());
        PLACED_BLOCK.put(ModDimensions.EXPERT_END, new HashSet<>());
    }
}
