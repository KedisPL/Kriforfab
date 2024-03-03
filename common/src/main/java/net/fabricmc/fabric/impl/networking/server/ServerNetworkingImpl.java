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

import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.payload.ResolvablePayload;
import net.fabricmc.fabric.impl.networking.payload.ResolvedPayload;
import net.fabricmc.fabric.impl.networking.payload.TypedPayload;
import net.fabricmc.fabric.impl.networking.payload.UntypedPayload;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

public final class ServerNetworkingImpl {
    public static final GlobalReceiverRegistry<ServerLoginNetworking.LoginQueryResponseHandler> LOGIN = new GlobalReceiverRegistry<>(ConnectionProtocol.LOGIN);
    public static final GlobalReceiverRegistry<ResolvablePayload.Handler<ServerConfigurationNetworkAddon.Handler>> CONFIGURATION = new GlobalReceiverRegistry<>(ConnectionProtocol.CONFIGURATION);
    public static final GlobalReceiverRegistry<ResolvablePayload.Handler<ServerPlayNetworkAddon.Handler>> PLAY = new GlobalReceiverRegistry<>(ConnectionProtocol.PLAY);

    public static ServerPlayNetworkAddon getAddon(ServerGamePacketListenerImpl handler) {
        return (ServerPlayNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
    }

    public static ServerLoginNetworkAddon getAddon(ServerLoginPacketListenerImpl handler) {
        return (ServerLoginNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
    }

    public static ServerConfigurationNetworkAddon getAddon(ServerConfigurationPacketListenerImpl handler) {
        return (ServerConfigurationNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
    }

    public static Packet<ClientCommonPacketListener> createS2CPacket(ResourceLocation channel, FriendlyByteBuf buf) {
        return new ClientboundCustomPayloadPacket(new UntypedPayload(channel, buf));
    }

    public static Packet<ClientCommonPacketListener> createS2CPacket(FabricPacket packet) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        Objects.requireNonNull(packet.getType(), "Packet#getType cannot return null");

        ResolvedPayload payload = new TypedPayload(packet);
        if (NetworkingImpl.FORCE_PACKET_SERIALIZATION) payload = payload.resolve(null);

        return new ClientboundCustomPayloadPacket(payload);
    }
}
