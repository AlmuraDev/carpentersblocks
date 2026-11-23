package com.carpentersblocks.registry;

import com.carpentersblocks.CarpentersBlocks;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Data kept on items (1.12.2 kept it in the item damage).
 */
public final class CbDataComponents {

    public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CarpentersBlocks.MOD_ID);

    /**
     * Which slope a slope item places, an index into ItemBlockCarpentersSlope's slopes (1.12.2: the item damage).
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SLOPE_TYPE = REGISTER.registerComponentType("slope_type",
        builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    private CbDataComponents() {}

}
