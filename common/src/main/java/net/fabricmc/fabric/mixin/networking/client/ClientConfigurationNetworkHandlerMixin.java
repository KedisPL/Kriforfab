/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.networking.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.client.ClientConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@Mixin(value = ClientConfigurationPacketListenerImpl.class, priority = 999)
public abstract class ClientConfigurationNetworkHandlerMixin extends ClientCommonPacketListenerImpl implements NetworkHandlerExtensions {
    @Unique
    private ClientConfigurationNetworkAddon addon;

    protected ClientConfigurationNetworkHandlerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initAddon(CallbackInfo ci) {
        this.addon = new ClientConfigurationNetworkAddon((ClientConfigurationPacketListenerImpl) (Object) this, this.minecraft);
        // A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
        ClientNetworkingImpl.setClientConfigurationAddon(this.addon);
        this.addon.lateInit();
    }

    @Inject(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setListener(Lnet/minecraft/network/PacketListener;)V", shift = At.Shift.BEFORE))
    public void onReady(ClientboundFinishConfigurationPacket packet, CallbackInfo ci) {
        this.addon.handleReady();
    }

    @Override
    public ClientConfigurationNetworkAddon getAddon() {
        return addon;
    }
}
