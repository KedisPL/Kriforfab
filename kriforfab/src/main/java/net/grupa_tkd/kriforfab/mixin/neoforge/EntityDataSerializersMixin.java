package net.grupa_tkd.kriforfab.mixin.neoforge;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityDataSerializers.class, priority = 1)
public class EntityDataSerializersMixin {
    @Shadow
    @Final
    private static CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS;

    @Inject(method = "registerSerializer", at = @At(value = "HEAD"), cancellable = true)
    private static void registerSerializerMixin(EntityDataSerializer<?> entityDataSerializer, CallbackInfo callbackInfo) {
        SERIALIZERS.add(entityDataSerializer);
        callbackInfo.cancel();
    }
}
