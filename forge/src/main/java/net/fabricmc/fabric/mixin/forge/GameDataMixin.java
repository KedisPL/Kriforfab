package net.fabricmc.fabric.mixin.neoforge;

import net.minecraftforge.registries.GameData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static net.minecraftforge.registries.ForgeRegistry.REGISTRIES;

@Mixin(GameData.class)
public class GameDataMixin {
    @Shadow(remap = false)
    @Final
    private static final Logger LOGGER = LogManager.getLogger();
    @Overwrite(remap = false)
    public static void vanillaSnapshot()
    {
        unfreezeData();
        LOGGER.debug(REGISTRIES, "Removed vanilla freeze snapshot by Kriforfab");
    }

    @Shadow(remap = false)
    public static void unfreezeData() {
    }
}
