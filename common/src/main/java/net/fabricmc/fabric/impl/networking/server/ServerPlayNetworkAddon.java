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

package net.fabricmc.fabric.impl.networking.server;

import java.util.Collections;
import java.util.List;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.payload.ResolvedPayload;

public final class ServerPlayNetworkAddon extends AbstractChanneledNetworkAddon<ServerPlayNetworkAddon.Handler> {
    private final ServerGamePacketListenerImpl handler;
    private final MinecraftServer server;
    private boolean sentInitialRegisterPacket;

    public ServerPlayNetworkAddon(ServerGamePacketListenerImpl handler, Connection connection, MinecraftServer server) {
        super(ServerNetworkingImpl.PLAY, connection, "ServerPlayNetworkAddon for " + handler.player.getDisplayName());
        this.handler = handler;
        this.server = server;

        // Must register pending channels via lateinit
        this.registerPendingChannels((ChannelInfoHolder) this.connection, ConnectionProtocol.PLAY);
    }

    @Override
    protected void invokeInitEvent() {
        ServerPlayConnectionEvents.INIT.invoker().onPlayInit(this.handler, this.server);
    }

    public void onClientReady() {
        ServerPlayConnectionEvents.JOIN.invoker().onPlayReady(this.handler, this, this.server);

        this.sendInitialChannelRegistrationPacket();
        this.sentInitialRegisterPacket = true;
    }

    @Override
    protected void receive(Handler handler, ResolvedPayload payload) {
        handler.receive(this.server, this.handler.player, this.handler, payload, this);
    }

    // impl details

    @Override
    protected void schedule(Runnable task) {
        this.handler.player.server.execute(task);
    }

    @Override
    public Packet<?> createPacket(ResourceLocation channelName, FriendlyByteBuf buf) {
        return ServerPlayNetworking.createS2CPacket(channelName, buf);
    }

    @Override
    public Packet<?> createPacket(FabricPacket packet) {
        return ServerPlayNetworking.createS2CPacket(packet);
    }

    @Override
    protected void invokeRegisterEvent(List<ResourceLocation> ids) {
        S2CPlayChannelEvents.REGISTER.invoker().onChannelRegister(this.handler, this, this.server, ids);
    }

    @Override
    protected void invokeUnregisterEvent(List<ResourceLocation> ids) {
        S2CPlayChannelEvents.UNREGISTER.invoker().onChannelUnregister(this.handler, this, this.server, ids);
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
        ServerPlayConnectionEvents.DISCONNECT.invoker().onPlayDisconnect(this.handler, this.server);
    }

    @Override
    protected boolean isReservedChannel(ResourceLocation channelName) {
        return NetworkingImpl.isReservedCommonChannel(channelName);
    }

    public interface Handler {
        void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, ResolvedPayload payload, PacketSender responseSender);
    }
}
