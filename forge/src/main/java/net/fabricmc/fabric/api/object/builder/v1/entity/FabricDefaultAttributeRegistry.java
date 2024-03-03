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
// 1. Removed some stuff from docs.
// 2. Ported to forge events

package net.fabricmc.fabric.api.object.builder.v1.entity;

import net.fabricmc.fabric.impl.object.builder.FabricDefaultAttributeRegistryImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows registering custom default attributes for living entities.
 *
 * <p>A registered default attribute for an entity type can be retrieved through
 * {@link net.minecraft.world.entity.ai.attributes.DefaultAttributes#getSupplier(EntityType)}.</p>
 *
 * @see net.minecraft.world.entity.ai.attributes.DefaultAttributes
 */
public final class FabricDefaultAttributeRegistry {
    /**
     * Private logger, not exposed.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricDefaultAttributeRegistry.class);

    private FabricDefaultAttributeRegistry() {
    }

    /**
     * Registers a default attribute for a type of living entity.
     *
     * @param type    the entity type
     * @param builder the builder that creates the default attribute
     * @see	FabricDefaultAttributeRegistry#register(EntityType, AttributeSupplier)
     */
    public static void register(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder) {
        register(type, builder.build());
    }

    /**
     * Registers a default attribute for a type of living entity.
     *
     * <p>It can be used in a fashion similar to this:
     * <blockquote><pre>
     * EntityAttributeRegistry.INSTANCE.register(type, LivingEntity.createLivingAttributes());
     * </pre></blockquote>
     *
     * <p>If a registration overrides another, a debug log message will be emitted. Existing registrations
     * can be checked at {@link net.minecraft.world.entity.ai.attributes.DefaultAttributes#hasSupplier(EntityType)}.
     *
     * @param type      the entity type
     * @param container the container for the default attribute
     */
    public static void register(EntityType<? extends LivingEntity> type, AttributeSupplier container) {
        if (FabricDefaultAttributeRegistryImpl.register(type, container) != null) {
            LOGGER.debug("Overriding existing registration for entity type {}", BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }
    }
}
