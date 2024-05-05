package net.grupa_tkd.kriforfab.platform;

import com.google.common.base.Suppliers;
import net.grupa_tkd.kriforfab.platform.services.IPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.List;
import java.util.function.Supplier;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
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
        return Suppliers.memoize(
                () -> FeatureSorter.buildFeaturesPerStep(
                        List.copyOf(dimensionOptions.generator().getBiomeSource().possibleBiomes()),
                        (biomeEntry) -> (biomeEntry.value().generationSettings).features(),
                        true
                )
        );
    }
}