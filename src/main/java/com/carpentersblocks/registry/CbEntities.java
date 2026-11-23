package com.carpentersblocks.registry;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.CbConfig;
import com.carpentersblocks.entity.EntityCarpentersTile;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The entities (1.12.2: the Carpenter's Tile only).
 */
public final class CbEntities {

    public static final DeferredRegister.Entities REGISTER = DeferredRegister.createEntities(CarpentersBlocks.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCarpentersTile>> CARPENTERS_TILE = CbConfig.ENABLE_TILE.get()
        ? REGISTER.registerEntityType("carpenters_tile", EntityCarpentersTile::new, MobCategory.MISC,
            builder -> builder.sized(1.0F, 1.0F).noLootTable().clientTrackingRange(10).updateInterval(Integer.MAX_VALUE))
        : null;

    private CbEntities() {}

}
