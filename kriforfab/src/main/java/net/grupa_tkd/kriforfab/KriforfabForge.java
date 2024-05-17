package net.grupa_tkd.kriforfab;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class KriforfabForge {
    public KriforfabForge() {
        IEventBus eventBus = null;

        Constants.MOD_LOADER = "Forge";
        CommonClass.init();
    }
}
