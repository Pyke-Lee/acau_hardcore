package kr.pyke.acau_hardcore.mixin.server.end;

import kr.pyke.acau_hardcore.level.end.CustomSpikeDefinitions;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(SpikeFeature.class)
public class SpikeFeatureMixin {
    @Inject(method = "getSpikesForLevel", at = @At("HEAD"), cancellable = true)
    private static void getCustomSpikes(WorldGenLevel level, CallbackInfoReturnable<List<SpikeFeature.EndSpike>> cir) {
        if (level instanceof ServerLevelAccessor accessor) {
            ServerLevel serverLevel = accessor.getLevel();
            if (serverLevel.dimension() == Level.END || serverLevel.dimension() == ModDimensions.EXPERT_END) {
                cir.setReturnValue(CustomSpikeDefinitions.getSpikes(serverLevel.getStructureManager()));
            }
        }
    }

    @Inject(method = "placeSpike", at = @At("HEAD"), cancellable = true)
    private void onPlaceSpike(ServerLevelAccessor level, RandomSource random, SpikeConfiguration config, SpikeFeature.EndSpike spike, CallbackInfo ci) {
        ServerLevel serverLevel = level.getLevel();
        if (serverLevel.dimension() != Level.END && serverLevel.dimension() != ModDimensions.EXPERT_END) { return; }

        int spikeIndex = CustomSpikeDefinitions.getIndex(spike);
        if (spikeIndex < 0) { return; }

        StructureTemplateManager manager = serverLevel.getStructureManager();
        Optional<StructureTemplate> opt = CustomSpikeDefinitions.loadSpikeTemplate(manager, spikeIndex);
        if (opt.isEmpty()) { return; }

        StructureTemplate template = opt.get();
        Vec3i size = template.getSize();

        int placeX = spike.getCenterX() - size.getX() / 2;
        int placeZ = spike.getCenterZ() - size.getZ() / 2;
        BlockPos placePos = new BlockPos(placeX, 0, placeZ);

        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true);
        template.placeInWorld(level, placePos, placePos, settings, random, 2);

        int crystalY = size.getY() + 1;
        EndCrystal crystal = EntityType.END_CRYSTAL.create(serverLevel, EntitySpawnReason.STRUCTURE);
        if (crystal != null) {
            crystal.setBeamTarget(config.getCrystalBeamTarget());
            crystal.setInvulnerable(config.isCrystalInvulnerable());
            crystal.snapTo(spike.getCenterX() + 0.5, crystalY, spike.getCenterZ() + 0.5, random.nextFloat() * 360.f, 0.f);
            level.addFreshEntity(crystal);

            BlockPos crystalBlock = crystal.blockPosition();
            level.setBlock(crystalBlock.below(), Blocks.BEDROCK.defaultBlockState(), 3);
            level.setBlock(crystalBlock, FireBlock.getState(level, crystalBlock), 3);
        }

        ci.cancel();
    }
}