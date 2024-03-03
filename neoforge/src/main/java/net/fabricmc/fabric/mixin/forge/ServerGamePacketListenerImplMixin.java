package net.fabricmc.fabric.mixin.forge;

import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
    public ServerGamePacketListenerImplMixin(MinecraftServer p_295057_, Connection p_294822_, CommonListenerCookie p_301980_) {
        super(p_295057_, p_294822_, p_301980_);
    }

    // I hope it will not break the NeoForge networking system.
    @Overwrite
    public void handleCustomPayload(net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket p_294276_) {
        super.handleCustomPayload(p_294276_);
    }
}
