package net.grupa_tkd.kriforfab;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public class KriforfabClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CommonClientClass.init();
    }
}
