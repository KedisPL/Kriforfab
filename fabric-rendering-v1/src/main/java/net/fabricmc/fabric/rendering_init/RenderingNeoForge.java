package net.fabricmc.fabric.rendering_init;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ClientHooks;

import java.util.function.Supplier;

@Mod("fabric_rendering_v1")
public class RenderingNeoForge {
    public RenderingNeoForge(IEventBus eventBus) {
    }

    public static void registerModelLayer(ModelLayerLocation modelLayer, Supplier<LayerDefinition> provider) {
        ClientHooks.registerLayerDefinition(modelLayer, provider);
    }
}
