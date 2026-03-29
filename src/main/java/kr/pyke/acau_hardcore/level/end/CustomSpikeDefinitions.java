package kr.pyke.acau_hardcore.level.end;

import net.minecraft.world.level.levelgen.feature.SpikeFeature;

import java.util.List;

public class CustomSpikeDefinitions {
    private static final List<SpikeFeature.EndSpike> SPIKES = List.of(
        new SpikeFeature.EndSpike(42, 0, 2, 76, false),
        new SpikeFeature.EndSpike(0, 42, 3, 79, false),
        new SpikeFeature.EndSpike(-42, 0, 2, 82, false),
        new SpikeFeature.EndSpike(0, -42, 3, 85, false),
        new SpikeFeature.EndSpike(30, 30, 2, 88, false),
        new SpikeFeature.EndSpike(-30, 30, 3, 91, false),
        new SpikeFeature.EndSpike(-30, -30, 2, 94, false),
        new SpikeFeature.EndSpike(30, -30, 3, 97, true),
        new SpikeFeature.EndSpike(13, 40, 2, 100, true),
        new SpikeFeature.EndSpike(-13, -40, 3, 103, false)
    );

    public static List<SpikeFeature.EndSpike> getSpikes() { return SPIKES; }
}
