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
// CHANGES:
// Ported to forge


package net.fabricmc.fabric.api.client.rendering.v1;

import net.fabricmc.fabric.rendering_init.CommonRendering;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A helpers for registering entity model layers and providers for the layer's textured model data.
 */
public final class EntityModelLayerRegistry {
    /**
     * Registers an entity model layer and registers a provider for a {@linkplain LayerDefinition}.
     *
     * @param modelLayer the entity model layer
     * @param provider the provider for the textured model data
     */
    public static void registerModelLayer(ModelLayerLocation modelLayer, Supplier<LayerDefinition> provider) {
        Objects.requireNonNull(modelLayer, "EntityModelLayer cannot be null");
        Objects.requireNonNull(provider, "TexturedModelDataProvider cannot be null");

        CommonRendering.registerModelLayer(modelLayer, provider);
    }

    private EntityModelLayerRegistry() {
    }

    @FunctionalInterface
    public interface TexturedModelDataProvider {
        /**
         * Creates the textured model data for use in a {@link ModelLayerLocation}.
         *
         * @return the textured model data for the entity model layer.
         */
        LayerDefinition createModelData();
    }
}
