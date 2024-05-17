package net.fabricmc.fabric.object_builder_api_init;

import net.fabricmc.fabric.impl.object.builder.FabricDefaultAttributeRegistryImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod("fabric_object_builder_api_v1")
public class ObjectBuilderNeoForge {
    public ObjectBuilderNeoForge(IEventBus eventBus) {
        eventBus.addListener(this::onAttributesCreate);
    }

    public void onAttributesCreate(EntityAttributeCreationEvent event) {
        FabricDefaultAttributeRegistryImpl.SUPPLIERS.forEach(event::put);
    }
}
