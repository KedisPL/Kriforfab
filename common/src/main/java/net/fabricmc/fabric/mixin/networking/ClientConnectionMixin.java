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

package net.fabricmc.fabric.mixin.networking;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.DisconnectPacketSource;
import net.fabricmc.fabric.impl.networking.GenericFutureListenerHolder;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.PacketCallbackListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;

@Mixin(Connection.class)
abstract class ClientConnectionMixin implements ChannelInfoHolder {
    @Shadow
    private PacketListener packetListener;

    @Shadow
    public abstract void disconnect(Component disconnectReason);

    @Shadow
    public abstract void send(Packet<?> packet, @Nullable PacketSendListener arg);

    @Unique
    private Map<ConnectionProtocol, Collection<ResourceLocation>> playChannels;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initAddedFields(PacketFlow side, CallbackInfo ci) {
        this.playChannels = new ConcurrentHashMap<>();
    }

    // Must be fully qualified due to mixin not working in production without it
    @Redirect(method = "exceptionCaught", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"))
    private void resendOnExceptionCaught(Connection self, Packet<?> packet, PacketSendListener listener, ChannelHandlerContext context, Throwable ex) {
        PacketListener handler = this.packetListener;
        Component disconnectMessage = Component.translatable("disconnect.genericReason", "Internal Exception: " + ex);

        if (handler instanceof DisconnectPacketSource) {
            this.send(((DisconnectPacketSource) handler).createDisconnectPacket(disconnectMessage), listener);
        } else {
            this.disconnect(disconnectMessage); // Don't send packet if we cannot send proper packets
        }
    }

    @Inject(method = "sendPacket", at = @At(value = "FIELD", target = "Lnet/minecraft/network/Connection;sentPackets:I"))
    private void checkPacket(Packet<?> packet, PacketSendListener callback, boolean flush, CallbackInfo ci) {
        if (this.packetListener instanceof PacketCallbackListener) {
            ((PacketCallbackListener) this.packetListener).sent(packet);
        }
    }

    @Inject(method = "setListener", at = @At("HEAD"))
    private void unwatchAddon(PacketListener packetListener, CallbackInfo ci) {
        if (this.packetListener instanceof NetworkHandlerExtensions oldListener) {
            oldListener.getAddon().endSession();
        }
    }

    @Inject(method = "channelInactive", at = @At("HEAD"))
    private void disconnectAddon(ChannelHandlerContext channelHandlerContext, CallbackInfo ci) {
        if (packetListener instanceof NetworkHandlerExtensions extension) {
            extension.getAddon().handleDisconnect();
        }
    }

    @Inject(method = "handleDisconnection", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketListener;onDisconnect(Lnet/minecraft/network/chat/Component;)V"))
    private void disconnectAddon(CallbackInfo ci) {
        if (packetListener instanceof NetworkHandlerExtensions extension) {
            extension.getAddon().handleDisconnect();
        }
    }

    @Inject(method = "doSendPacket", at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelFuture;addListener(Lio/netty/util/concurrent/GenericFutureListener;)Lio/netty/channel/ChannelFuture;", remap = false), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void sendInternal(Packet<?> packet, @Nullable PacketSendListener callbacks, boolean flush, CallbackInfo ci, ChannelFuture channelFuture) {
        if (callbacks instanceof GenericFutureListenerHolder holder) {
            channelFuture.addListener(holder.getDelegate());
            channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            ci.cancel();
        }
    }

    @Override
    public Collection<ResourceLocation> getPendingChannelsNames(ConnectionProtocol state) {
        return this.playChannels.computeIfAbsent(state, (key) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }
}
