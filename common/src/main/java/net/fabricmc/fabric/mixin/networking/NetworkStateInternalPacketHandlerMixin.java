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

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.payload.RetainedPayload;
import net.fabricmc.fabric.impl.networking.payload.UntypedPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

@Mixin(targets = "net.minecraft.network.ConnectionProtocol$PacketSet")
public class NetworkStateInternalPacketHandlerMixin {
    /**
     * Only retain custom packet buffer to {@link RetainedPayload} on the receiving side,
     * otherwise resolve to {@link UntypedPayload}.
     */
    @ModifyVariable(method = "addPacket", at = @At("HEAD"), argsOnly = true)
    private Function<FriendlyByteBuf, Packet<?>> replaceCustomPayloadFactory(Function<FriendlyByteBuf, Packet<?>> original, Class<?> type) {
        if (type == ServerboundCustomPayloadPacket.class) {
            return buf -> {
                try {
                    NetworkingImpl.FACTORY_RETAIN.set(true);
                    return new ServerboundCustomPayloadPacket(buf);
                } finally {
                    NetworkingImpl.FACTORY_RETAIN.set(false);
                }
            };
        } else if (type == ClientboundCustomPayloadPacket.class) {
            return buf -> {
                try {
                    NetworkingImpl.FACTORY_RETAIN.set(true);
                    return new ClientboundCustomPayloadPacket(buf);
                } finally {
                    NetworkingImpl.FACTORY_RETAIN.set(false);
                }
            };
        }

        return original;
    }
}
