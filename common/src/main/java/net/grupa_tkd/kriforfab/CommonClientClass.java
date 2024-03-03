package net.grupa_tkd.kriforfab;

import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;

public class CommonClientClass {
    public static void init() {
        ClientNetworkingImpl.clientInit();
    }
}