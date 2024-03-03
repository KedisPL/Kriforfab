// FROM Forgified Fabric API https://github.com/Sinytra/ForgifiedFabricAPI/
package net.fabricmc.fabric.mixin.forge;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeHooks.class)
public abstract class ForgeHooksMixin {

    @Inject(method = "getVanillaFluidType", at = @At(value = "NEW", target = "(Ljava/lang/String;)Ljava/lang/RuntimeException;"), remap = false, cancellable = true)
    private static void getFabricVanillaFluidType(Fluid fluid, CallbackInfoReturnable<FluidType> cir) {
        cir.setReturnValue(ForgeMod.WATER_TYPE.get());
    }
}