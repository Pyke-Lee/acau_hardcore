package kr.pyke.acau_hardcore.level.end;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomSpikeDefinitions {

    private static final int SPIKE_COUNT = 10;
    private static final int SPIKE_DISTANCE = 42;

    private static @Nullable List<SpikeFeature.EndSpike> cachedSpikes = null;

    public static List<SpikeFeature.EndSpike> getSpikes(StructureTemplateManager manager) {
        if (cachedSpikes != null) { return cachedSpikes; }

        List<SpikeFeature.EndSpike> spikes = new ArrayList<>();
        for (int i = 0; i < SPIKE_COUNT; i++) {
            int centerX = Mth.floor(42.0 * Math.cos(2.0 * (-Math.PI + (Math.PI / 10.0) * i)));
            int centerZ = Mth.floor(42.0 * Math.sin(2.0 * (-Math.PI + (Math.PI / 10.0) * i)));

            Optional<StructureTemplate> opt = loadSpikeTemplate(manager, i);

            int radius, height;
            if (opt.isPresent()) {
                Vec3i size = opt.get().getSize();
                radius = Math.max(size.getX(), size.getZ()) / 2;
                height = size.getY();
            }
            else {
                radius = 3;
                height = 76 + i * 3;
            }

            spikes.add(new SpikeFeature.EndSpike(centerX, centerZ, radius, height, false));
        }

        cachedSpikes = List.copyOf(spikes);
        return cachedSpikes;
    }

    /**
     * custom_spike_{index}.nbt 로드. 없으면 custom_spike_0.nbt 폴백.
     */
    public static Optional<StructureTemplate> loadSpikeTemplate(StructureTemplateManager manager, int index) {
        Identifier spikeId = Identifier.fromNamespaceAndPath("acau_hardcore", "custom_spike_" + index);
        Optional<StructureTemplate> opt = manager.get(spikeId);

        if (opt.isEmpty() && index != 0) {
            Identifier fallbackId = Identifier.fromNamespaceAndPath("acau_hardcore", "custom_spike_0");
            opt = manager.get(fallbackId);
        }

        return opt;
    }

    public static int getIndex(SpikeFeature.EndSpike spike) {
        if (cachedSpikes == null) { return -1; }

        for (int i = 0; i < cachedSpikes.size(); i++) {
            if (cachedSpikes.get(i) == spike) {
                return i;
            }
        }

        return -1;
    }

    public static void invalidateCache() { cachedSpikes = null; }
}