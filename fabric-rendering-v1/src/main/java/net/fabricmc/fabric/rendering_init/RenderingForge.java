package net.fabricmc.fabric.rendering_init;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod("fabric_rendering_v1")
public class RenderingForge {
    public RenderingForge() {
        IEventBus eventBus = null;
    }

    public static void registerModelLayer(ModelLayerLocation modelLayer, Supplier<LayerDefinition> provider) {
        ForgeHooksClient.registerLayerDefinition(modelLayer, provider);
    }
}
