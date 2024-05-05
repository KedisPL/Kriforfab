package net.grupa_tkd.kriforfab.platform;

import com.google.common.base.Suppliers;
import net.grupa_tkd.kriforfab.platform.services.IPlatformHelper;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

import java.util.List;
import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep(LevelStem dimensionOptions) {
        return net.neoforged.neoforge.common.util.Lazy.of(
                () -> FeatureSorter.buildFeaturesPerStep(
                        List.copyOf(dimensionOptions.generator().getBiomeSource().possibleBiomes()),
                        (biomeEntry) -> (biomeEntry.value().generationSettings).features(),
                        true
                )
        );
    }
}