package net.fabricmc.fabric.mixin.forge;

import net.neoforged.neoforge.registries.GameData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GameData.class)
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
