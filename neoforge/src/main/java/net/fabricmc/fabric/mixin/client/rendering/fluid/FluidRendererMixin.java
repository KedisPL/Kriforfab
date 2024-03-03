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

package net.fabricmc.fabric.mixin.client.rendering.fluid;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRendererHookContainer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlockRenderer.class)
public class FluidRendererMixin {
    @Final
    @Shadow
    private TextureAtlasSprite[] lavaIcons;
    @Final
    @Shadow
    private TextureAtlasSprite[] waterIcons;
    @Shadow
    private TextureAtlasSprite waterOverlay;

    private final ThreadLocal<FluidRendererHookContainer> fabric_renderHandler = ThreadLocal.withInitial(FluidRendererHookContainer::new);
    private final ThreadLocal<Boolean> fabric_customRendering = ThreadLocal.withInitial(() -> false);
    private final ThreadLocal<Block> fabric_neighborBlock = new ThreadLocal<>();

    @Inject(at = @At("RETURN"), method = "setupSprites")
    public void onResourceReloadReturn(CallbackInfo info) {
        LiquidBlockRenderer self = (LiquidBlockRenderer) (Object) this;
        ((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).onFluidRendererReload(self, waterIcons, lavaIcons, waterOverlay);
    }

    @Inject(at = @At("HEAD"), method = "tesselate", cancellable = true)
    public void tesselate(BlockAndTintGetter view, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo info) {
        if (!fabric_customRendering.get()) {
            // Prevent recursively looking up custom fluid renderers when default behavior is being invoked
            try {
                fabric_customRendering.set(true);
                tessellateViaHandler(view, pos, vertexConsumer, blockState, fluidState, info);
            } finally {
                fabric_customRendering.set(false);
            }
        }

        if (info.isCancelled()) {
            return;
        }

        FluidRendererHookContainer ctr = fabric_renderHandler.get();
        ctr.getSprites(view, pos, fluidState);
    }

    @Unique
    private void tessellateViaHandler(BlockAndTintGetter view, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo info) {
        FluidRendererHookContainer ctr = fabric_renderHandler.get();
        FluidRenderHandler handler = ((FluidRenderHandlerRegistryImpl) FluidRenderHandlerRegistry.INSTANCE).getOverride(fluidState.getType());

        ctr.view = view;
        ctr.pos = pos;
        ctr.blockState = blockState;
        ctr.fluidState = fluidState;
        ctr.handler = handler;

        if (handler != null) {
            handler.renderFluid(pos, view, vertexConsumer, blockState, fluidState);
            info.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "tesselate")
    public void tesselateReturn(BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        fabric_renderHandler.get().clear();
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;isNeighborSameFluid(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/material/FluidState;)Z"), method = "tesselate", ordinal = 0)
    public boolean modLavaCheck(boolean chk) {
        // First boolean local is set by vanilla according to 'matches lava'
        // but uses the negation consistent with 'matches water'
        // for determining if special water sprite should be used behind glass.

        // Has other uses but those are overridden by this mixin and have
        // already happened by the time this hook is called

        // If this fluid has an overlay texture, set this boolean too false
        final FluidRendererHookContainer ctr = fabric_renderHandler.get();
        return !ctr.hasOverlay;
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;isNeighborSameFluid(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/material/FluidState;)Z"), method = "tesselate", ordinal = 0)
    public TextureAtlasSprite[] modSpriteArray(TextureAtlasSprite[] chk) {
        FluidRendererHookContainer ctr = fabric_renderHandler.get();
        return ctr.handler != null ? ctr.sprites : chk;
    }

    @Redirect(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/extensions/common/IClientFluidTypeExtensions;getTintColor(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"))
    public int modTintColor(IClientFluidTypeExtensions extensions, FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        FluidRendererHookContainer ctr = fabric_renderHandler.get();
        if (ctr.handler != null) {
            // Include alpha in tint color
            int color = ctr.handler.getFluidColor(ctr.view, ctr.pos, ctr.fluidState);
            return 0xFF000000 | color;
        }
        return extensions.getTintColor(state, getter, pos);
    }

    @Redirect(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/textures/FluidSpriteCache;getFluidSprites(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;)[Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private TextureAtlasSprite[] redirectSprites(BlockAndTintGetter level, BlockPos pos, FluidState fluidStateIn) {
        FluidRendererHookContainer ctr = fabric_renderHandler.get();
        if (ctr.handler != null) {
            return new TextureAtlasSprite[] {
                    ctr.sprites[0],
                    ctr.sprites[1],
                    ctr.hasOverlay ? ctr.overlay : null
            };
        }
        return FluidSpriteCache.getFluidSprites(level, pos, fluidStateIn);
    }
}
