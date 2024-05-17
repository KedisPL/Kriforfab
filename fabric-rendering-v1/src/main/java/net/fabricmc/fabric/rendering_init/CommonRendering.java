package net.fabricmc.fabric.rendering_init;

import net.grupa_tkd.kriforfab.Constants;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.Objects;
import java.util.function.Supplier;

public class CommonRendering {
    public static void registerModelLayer(ModelLayerLocation modelLayer, Supplier<LayerDefinition> provider) {
        if (Objects.equals(Constants.MOD_LOADER, "NeoForge")) {
            RenderingNeoForge.registerModelLayer(modelLayer, provider);
        } else {
            RenderingForge.registerModelLayer(modelLayer, provider);
        }
    }
}
