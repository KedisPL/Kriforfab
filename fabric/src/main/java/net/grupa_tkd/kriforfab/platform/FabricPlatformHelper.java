package net.grupa_tkd.kriforfab.platform;

import com.google.common.base.Suppliers;
import net.grupa_tkd.kriforfab.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.List;
import java.util.function.Supplier;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep(LevelStem dimensionOptions) {
        return Suppliers.memoize(
                () -> FeatureSorter.buildFeaturesPerStep(
                        List.copyOf(dimensionOptions.generator().getBiomeSource().possibleBiomes()),
                        (biomeEntry) -> (biomeEntry.value().generationSettings).features(),
                        true
                )
        );
    }
}
