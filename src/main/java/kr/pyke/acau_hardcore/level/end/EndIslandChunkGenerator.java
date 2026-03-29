package kr.pyke.acau_hardcore.level.end;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EndIslandChunkGenerator extends ChunkGenerator {
    public static final MapCodec<EndIslandChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource)
        ).apply(instance, EndIslandChunkGenerator::new)
    );

    private static final Identifier STRUCTURE_ID = Identifier.fromNamespaceAndPath("acau_hardcore", "end_island");
    private static final int PLACEMENT_Y = 48;

    public EndIslandChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override protected @NonNull MapCodec<? extends ChunkGenerator> codec() { return CODEC; }

    @Override
    public @NonNull CompletableFuture<ChunkAccess> fillFromNoise(@NonNull Blender blender, @NonNull RandomState randomState, @NonNull StructureManager structureManager, @NonNull ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void buildSurface(WorldGenRegion level, @NonNull StructureManager structureManager, @NonNull RandomState random, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();

        ServerLevel serverLevel = level.getLevel();
        StructureTemplateManager templateManager = serverLevel.getStructureManager();
        Optional<StructureTemplate> opt = templateManager.get(STRUCTURE_ID);

        if (opt.isEmpty()) {
            return;
        }

        StructureTemplate template = opt.get();
        Vec3i size = template.getSize();

        int placeX = -size.getX() / 2;
        int placeZ = -size.getZ() / 2;

        int minChunkX = placeX >> 4;
        int maxChunkX = (placeX + size.getX() - 1) >> 4;
        int minChunkZ = placeZ >> 4;
        int maxChunkZ = (placeZ + size.getZ() - 1) >> 4;

        if (chunkPos.x >= minChunkX && chunkPos.x <= maxChunkX
            && chunkPos.z >= minChunkZ && chunkPos.z <= maxChunkZ) {
            BlockPos placePos = new BlockPos(placeX, PLACEMENT_Y, placeZ);
            StructurePlaceSettings settings = new StructurePlaceSettings()
                .setIgnoreEntities(false);
            template.placeInWorld(level, placePos, placePos, settings, level.getRandom(), 2);
        }
    }

    @Override
    public void spawnOriginalMobs(@NonNull WorldGenRegion level) {

    }

    @Override
    public int getGenDepth() {
        return 0;
    }

    @Override
    public void applyCarvers(@NonNull WorldGenRegion level, long seed, @NonNull RandomState random, @NonNull BiomeManager biomeManager, @NonNull StructureManager structureManager, @NonNull ChunkAccess chunk) {
    }

    @Override
    public int getSeaLevel() {
        return -63;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.@NonNull Types type, LevelHeightAccessor level, @NonNull RandomState random) {
        return level.getMinY();
    }

    @Override
    public @NonNull NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, @NonNull RandomState random) {
        return new NoiseColumn(height.getMinY(), new BlockState[]{ Blocks.AIR.defaultBlockState()});
    }

    @Override
    public void addDebugScreenInfo(@NonNull List<String> info, @NonNull RandomState random, @NonNull BlockPos pos) {

    }
}