package net.fabricmc.fabric.object_builder_api_init;

import net.fabricmc.fabric.impl.object.builder.FabricDefaultAttributeRegistryImpl;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod("fabric_object_builder_api_v1")
public class ObjectBuilderForge {
    public ObjectBuilderForge() {
        IEventBus eventBus = null;

        eventBus.addListener(this::onAttributesCreate);
    }

    public void onAttributesCreate(EntityAttributeCreationEvent event) {
        FabricDefaultAttributeRegistryImpl.SUPPLIERS.forEach(event::put);
    }
}
