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

import java.util.Queue;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.fabricmc.fabric.api.networking.v1.FabricServerConfigurationNetworkHandler;
import net.fabricmc.fabric.impl.networking.DisconnectPacketSource;
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@Mixin(value = ServerConfigurationPacketListenerImpl.class, priority = 900)
public abstract class ServerConfigurationNetworkHandlerMixin extends ServerCommonPacketListenerImpl implements NetworkHandlerExtensions, DisconnectPacketSource, FabricServerConfigurationNetworkHandler {
    @Shadow
    @Nullable
    private ConfigurationTask currentTask;

    @Shadow
    protected abstract void finishCurrentTask(ConfigurationTask.Type key);

    @Shadow
    @Final
    private Queue<ConfigurationTask> configurationTasks;

    @Shadow
    public abstract boolean isAcceptingMessages();

    @Shadow
    public abstract void startConfiguration();

    @Unique
    private ServerConfigurationNetworkAddon addon;

    @Unique
    private boolean sentConfiguration;

    @Unique
    private boolean earlyTaskExecution;

    public ServerConfigurationNetworkHandlerMixin(MinecraftServer server, Connection connection, CommonListenerCookie arg) {
        super(server, connection, arg);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initAddon(CallbackInfo ci) {
        this.addon = new ServerConfigurationNetworkAddon((ServerConfigurationPacketListenerImpl) (Object) this, this.server);
        // A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
        this.addon.lateInit();
    }

    @Inject(method = "startConfiguration", at = @At("HEAD"), cancellable = true)
    private void onClientReady(CallbackInfo ci) {
        // Send the initial channel registration packet
        if (this.addon.startConfiguration()) {
            assert currentTask == null;
            ci.cancel();
            return;
        }

        // Ready to start sending packets
        if (!sentConfiguration) {
            this.addon.preConfiguration();
            sentConfiguration = true;
            earlyTaskExecution = true;
        }

        // Run the early tasks
        if (earlyTaskExecution) {
            if (pollEarlyTasks()) {
                ci.cancel();
                return;
            } else {
                earlyTaskExecution = false;
            }
        }

        // All early tasks should have been completed
        assert currentTask == null;
        assert configurationTasks.isEmpty();

        // Run the vanilla tasks.
        this.addon.configuration();
    }

    @Unique
    private boolean pollEarlyTasks() {
        if (!earlyTaskExecution) {
            throw new IllegalStateException("Early task execution has finished");
        }

        if (this.currentTask != null) {
            throw new IllegalStateException("Task " + this.currentTask.type().id() + " has not finished yet");
        }

        if (!this.isAcceptingMessages()) {
            return false;
        }

        final ConfigurationTask task = this.configurationTasks.poll();

        if (task != null) {
            this.currentTask = task;
            task.start(this::send);
            return true;
        }

        return false;
    }

    @Override
    public ServerConfigurationNetworkAddon getAddon() {
        return addon;
    }

    @Override
    public Packet<?> createDisconnectPacket(Component message) {
        return new ClientboundDisconnectPacket(message);
    }

    @Override
    public void addTask(ConfigurationTask task) {
        configurationTasks.add(task);
    }

    @Override
    public void completeTask(ConfigurationTask.Type key) {
        if (!earlyTaskExecution) {
            finishCurrentTask(key);
            return;
        }

        final ConfigurationTask.Type currentKey = this.currentTask != null ? this.currentTask.type() : null;

        if (!key.equals(currentKey)) {
            throw new IllegalStateException("Unexpected request for task finish, current task: " + currentKey + ", requested: " + key);
        }

        this.currentTask = null;
        startConfiguration();
    }
}
