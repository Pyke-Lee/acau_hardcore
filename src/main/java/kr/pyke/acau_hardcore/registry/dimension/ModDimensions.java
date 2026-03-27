package kr.pyke.acau_hardcore.registry.dimension;

import kr.pyke.acau_hardcore.AcauHardCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;

public class ModDimensions {
    private ModDimensions() { }

    public static final ResourceKey<Level> BEGINNER_OVERWORLD = key("beginner_overworld");

    public static final ResourceKey<Level> EXPERT_OVERWORLD = key("expert_overworld");
    public static final ResourceKey<Level> EXPERT_NETHER = key("expert_nether");
    public static final ResourceKey<Level> EXPERT_END = key("expert_end");

    public static final Map<ResourceKey<Level>, ResourceKey<Level>> NETHER_PORTAL_MAP = Map.of(
        BEGINNER_OVERWORLD, Level.NETHER,
        Level.NETHER, BEGINNER_OVERWORLD,
        EXPERT_OVERWORLD, EXPERT_NETHER,
        EXPERT_NETHER, EXPERT_OVERWORLD
    );

    public static boolean isGameDimension(ResourceKey<Level> dimension) {
        return dimension.equals(BEGINNER_OVERWORLD) || dimension.equals(Level.NETHER) || dimension.equals(Level.END)
            || dimension.equals(EXPERT_OVERWORLD) || dimension.equals(EXPERT_NETHER) || dimension.equals(EXPERT_END);
    }

    public static boolean isExpertDimension(ResourceKey<Level> dimension) {
        return dimension.equals(EXPERT_OVERWORLD) || dimension.equals(EXPERT_NETHER) || dimension.equals(EXPERT_END);
    }

    private static ResourceKey<Level> key(String name) {
        return ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, name));
    }
}
