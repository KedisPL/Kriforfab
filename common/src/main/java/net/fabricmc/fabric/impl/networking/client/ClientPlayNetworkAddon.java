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

package net.fabricmc.fabric.impl.networking.client;

import java.util.Collections;
import java.util.List;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.payload.ResolvedPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public final class ClientPlayNetworkAddon extends AbstractChanneledNetworkAddon<ClientPlayNetworkAddon.Handler> {
    private final ClientPacketListener handler;
    private final Minecraft client;
    private boolean sentInitialRegisterPacket;

    private static final Logger LOGGER = LogUtils.getLogger();

    public ClientPlayNetworkAddon(ClientPacketListener handler, Minecraft client) {
        super(ClientNetworkingImpl.PLAY, handler.getConnection(), "ClientPlayNetworkAddon for " + handler.getLocalGameProfile().getName());
        this.handler = handler;
        this.client = client;

        // Must register pending channels via lateinit
        this.registerPendingChannels((ChannelInfoHolder) this.connection, ConnectionProtocol.PLAY);
    }

    @Override
    protected void invokeInitEvent() {
        ClientPlayConnectionEvents.INIT.invoker().onPlayInit(this.handler, this.client);
    }

    public void onServerReady() {
        try {
            ClientPlayConnectionEvents.JOIN.invoker().onPlayReady(this.handler, this, this.client);
        } catch (RuntimeException e) {
            LOGGER.error("Exception thrown while invoking ClientPlayConnectionEvents.JOIN", e);
        }

        // The client cannot send any packets, including `minecraft:register` until after GameJoinS2CPacket is received.
        this.sendInitialChannelRegistrationPacket();
        this.sentInitialRegisterPacket = true;
    }

    @Override
    protected void receive(Handler handler, ResolvedPayload payload) {
        handler.receive(this.client, this.handler, payload, this);
    }

    // impl details

    @Override
    protected void schedule(Runnable task) {
        Minecraft.getInstance().execute(task);
    }

    @Override
    public Packet<?> createPacket(ResourceLocation channelName, FriendlyByteBuf buf) {
        return ClientPlayNetworking.createC2SPacket(channelName, buf);
    }

    @Override
    public Packet<?> createPacket(FabricPacket packet) {
        return ClientPlayNetworking.createC2SPacket(packet);
    }

    @Override
    protected void invokeRegisterEvent(List<ResourceLocation> ids) {
        C2SPlayChannelEvents.REGISTER.invoker().onChannelRegister(this.handler, this, this.client, ids);
    }

    @Override
    protected void invokeUnregisterEvent(List<ResourceLocation> ids) {
        C2SPlayChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.handler, this, this.client, ids);
    }

    @Override
    protected void handleRegistration(ResourceLocation channelName) {
        // If we can already send packets, immediately send the register packet for this channel
        if (this.sentInitialRegisterPacket) {
            final FriendlyByteBuf buf = this.createRegistrationPacket(Collections.singleton(channelName));

            if (buf != null) {
                this.sendPacket(NetworkingImpl.REGISTER_CHANNEL, buf);
            }
        }
    }

    @Override
    protected void handleUnregistration(ResourceLocation channelName) {
        // If we can already send packets, immediately send the unregister packet for this channel
        if (this.sentInitialRegisterPacket) {
            final FriendlyByteBuf buf = this.createRegistrationPacket(Collections.singleton(channelName));

            if (buf != null) {
                this.sendPacket(NetworkingImpl.UNREGISTER_CHANNEL, buf);
            }
        }
    }

    @Override
    protected void invokeDisconnectEvent() {
        ClientPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(this.handler, this.client);
    }

    @Override
    protected boolean isReservedChannel(ResourceLocation channelName) {
        return NetworkingImpl.isReservedCommonChannel(channelName);
    }

    public interface Handler {
        void receive(Minecraft client, ClientPacketListener handler, ResolvedPayload payload, PacketSender responseSender);
    }
}
