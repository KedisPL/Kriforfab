package net.grupa_tkd.kriforfab;

import net.fabricmc.fabric.impl.object.builder.FabricDefaultAttributeRegistryImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Constants.MOD_ID)
public class Kriforfab {
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, "kriforfab");
    public static final RegistryObject<FluidType> NONE = FLUID_TYPES.register("none", () ->
            new FluidType(FluidType.Properties.create()) {});
    public Kriforfab() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
    
        // Use Forge to bootstrap the Common mod.
        eventBus.register(FabricDefaultAttributeRegistryImpl.class);
        CommonClass.init();
        FLUID_TYPES.register(eventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientInit() {
        CommonClientClass.init();
    }
}