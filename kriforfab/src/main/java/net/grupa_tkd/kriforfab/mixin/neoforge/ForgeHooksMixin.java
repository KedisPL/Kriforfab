// FROM Forgified Fabric API https://github.com/Sinytra/ForgifiedFabricAPI/
package net.grupa_tkd.kriforfab.mixin.neoforge;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CommonHooks.class, priority = 1)
public abstract class ForgeHooksMixin {

    @Inject(method = "getVanillaFluidType", at = @At(value = "NEW", target = "(Ljava/lang/String;)Ljava/lang/RuntimeException;"), remap = false, cancellable = true)
    private static void getFabricVanillaFluidType(Fluid fluid, CallbackInfoReturnable<FluidType> cir) {
        cir.setReturnValue(NeoForgeMod.WATER_TYPE.value());
    }
}