// FROM Forgified Fabric API https://github.com/Sinytra/ForgifiedFabricAPI/
package net.fabricmc.fabric.mixin.neoforge;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(ForgeRegistry.class)
public abstract class ForgeRegistryMixin<V> implements IForgeRegistry<V> {
    @Shadow
    @Final
    private BiMap<Object, V> owners;

    // Mixin AP complained about not finding the target method, so we use @Desc instead of a string
    @Inject(target = @Desc(value = "getDelegateOrThrow", args = Object.class, ret = Holder.Reference.class), at = @At("HEAD"), cancellable = true, remap = false)
    private void getDelegateOrThrow(V value, CallbackInfoReturnable<Holder.Reference<V>> cir) {
        cir.setReturnValue(getDelegate(value).orElseGet(() -> {
            MappedRegistry<V> registry = GameData.getWrapper(getRegistryKey(), Lifecycle.stable());
            try {
                // Create intrusive holder on the registry to bind the key later
                return registry.createIntrusiveHolder(value);
            } catch (IllegalStateException e) {
                // Fallback if the registry does not support intrusive holders
                return Holder.Reference.createIntrusive(registry.holderOwner(), value);
            }
        }));
    }

    @Inject(method = "makeSnapshot", at = @At("HEAD"), remap = false)
    private void resetOwners(CallbackInfoReturnable<ForgeRegistry.Snapshot> cir) {
        // When POI Types have their blockstate lists modified by mods, it breaks value -> key lookup
        // Resetting the owners map hash cache fixes the issue
        Map<Object, V> copy = new HashMap<>(this.owners);
        this.owners.clear();
        this.owners.putAll(copy);
    }

    @Inject(method = "sync", at = @At(value = "NEW", target = "(Ljava/lang/String;)Ljava/lang/RuntimeException;"), remap = false, cancellable = true)
    private void sync(ResourceLocation name, ForgeRegistry<V> from, CallbackInfo ci) {
        ci.cancel();
    }
}