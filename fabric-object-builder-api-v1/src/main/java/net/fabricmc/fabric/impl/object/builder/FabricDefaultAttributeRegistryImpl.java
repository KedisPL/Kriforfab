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

package net.fabricmc.fabric.impl.object.builder;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.HashMap;
import java.util.Map;

public final class FabricDefaultAttributeRegistryImpl {
    public static final Map<EntityType<? extends LivingEntity>, AttributeSupplier> SUPPLIERS = new HashMap<>();

    public static AttributeSupplier register(EntityType<? extends LivingEntity> entityType, AttributeSupplier supplier) {
        return SUPPLIERS.put(entityType, supplier);
    }

    private FabricDefaultAttributeRegistryImpl() {}
}