package net.grupa_tkd.kriforfab;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class KriforfabNeoForge {
    public KriforfabNeoForge(IEventBus eventBus) {
        Constants.MOD_LOADER = "NeoForge";
        CommonClass.init();
    }
}
