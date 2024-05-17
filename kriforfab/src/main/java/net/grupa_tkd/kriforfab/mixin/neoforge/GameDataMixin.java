package net.grupa_tkd.kriforfab.mixin.neoforge;

import net.neoforged.neoforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GameData.class, priority = 1)
public class GameDataMixin {
    @Overwrite(remap = false)
    public static void vanillaSnapshot()
    {
        unfreezeData();
    }

    @Shadow(remap = false)
    public static void unfreezeData() {
    }
}
