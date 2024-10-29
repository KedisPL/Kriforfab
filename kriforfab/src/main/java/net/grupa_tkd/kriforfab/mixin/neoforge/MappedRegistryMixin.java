package net.grupa_tkd.kriforfab.mixin.neoforge;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MappedRegistry.class)
public class MappedRegistryMixin {
    @Inject(method = "freeze", at = @At(value = "INVOKE", target = "Ljava/lang/IllegalStateException;<init>(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 2), cancellable = true)
    public void freezeMixin(CallbackInfoReturnable<Registry<?>> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue((MappedRegistry)(Object)this);
        return;
    }
}
