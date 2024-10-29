package net.grupa_tkd.kriforfab.more;

import net.minecraft.world.level.biome.Biome;

public interface BiomeMore {
    Biome.ClimateSettings getClimateSettings();
    void setClimateSettings(Biome.ClimateSettings climateSettings);
}
