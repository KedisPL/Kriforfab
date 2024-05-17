package net.fabricmc.fabric.biome_api_init;

import com.google.common.base.Suppliers;
import net.grupa_tkd.kriforfab.Constants;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class BiomeApiCommon {
    public static Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep(LevelStem dimensionOptions) {
        if (Objects.equals(Constants.MOD_LOADER, "NeoForge")) {
            return net.neoforged.neoforge.common.util.Lazy.of(
                    () -> FeatureSorter.buildFeaturesPerStep(
                            List.copyOf(dimensionOptions.generator().getBiomeSource().possibleBiomes()),
                            biomeEntry -> dimensionOptions.generator().getBiomeGenerationSettings(biomeEntry).features(),
                            true
                    )
            );
        } else {
            return Suppliers.memoize(
                    () -> FeatureSorter.buildFeaturesPerStep(
                            List.copyOf(dimensionOptions.generator().getBiomeSource().possibleBiomes()),
                            biomeEntry -> dimensionOptions.generator().getBiomeGenerationSettings(biomeEntry).features(),
                            true
                    )
            );
        }
    }
}
