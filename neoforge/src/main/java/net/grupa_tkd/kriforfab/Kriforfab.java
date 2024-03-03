package net.grupa_tkd.kriforfab;

import net.fabricmc.fabric.impl.object.builder.FabricDefaultAttributeRegistryImpl;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(Constants.MOD_ID)
public class Kriforfab {
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, "kriforfab");
    public static final Holder<FluidType> NONE = FLUID_TYPES.register("none", () ->
            new FluidType(FluidType.Properties.create()) {});
    
    public Kriforfab(IEventBus eventBus) {
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
    
        // Use Forge to bootstrap the Common mod.
        //FluidHandlerCompat.init(eventBus);
        eventBus.register(FabricDefaultAttributeRegistryImpl.class);
        CommonClass.init();
        FLUID_TYPES.register(eventBus);
        eventBus.addListener(this::doClientStuff);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(CommonClientClass::init);
    }
}