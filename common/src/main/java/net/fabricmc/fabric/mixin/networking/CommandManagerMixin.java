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
// Minor change with Development Environment

package net.fabricmc.fabric.mixin.networking;

import com.mojang.brigadier.CommandDispatcher;
import net.grupa_tkd.kriforfab.platform.KriforfabServices;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.DebugConfigCommand;

@Mixin(Commands.class)
public class CommandManagerMixin {
    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/BanIpCommands;register(Lcom/mojang/brigadier/CommandDispatcher;)V"))
    private void init(Commands.CommandSelection environment, CommandBuildContext commandRegistryAccess, CallbackInfo ci) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            // Command is registered when isDevelopment is set.
            return;
        }

        if (!KriforfabServices.PLATFORM.isDevelopmentEnvironment()) {
            // Only register this command in a dev env
            return;
        }

        DebugConfigCommand.register(this.dispatcher);
    }
}
