package net.grupa_tkd.kriforfab.mixin;

import net.grupa_tkd.kriforfab.more.BiomeMore;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Biome.class)
public class BiomeMixin implements BiomeMore {
    @Mutable
    @Shadow @Final
    private Biome.ClimateSettings climateSettings;

    @Override
    public Biome.ClimateSettings getClimateSettings() {
        return this.climateSettings;
    }

    @Override
    public void setClimateSettings(Biome.ClimateSettings climateSettings) {
        this.climateSettings = climateSettings;
    }
}
