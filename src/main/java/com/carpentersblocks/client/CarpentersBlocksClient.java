package com.carpentersblocks.client;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.BlockCoverable;
import com.carpentersblocks.client.model.CoverTints;
import com.carpentersblocks.client.model.CoverableItemModel;
import com.carpentersblocks.client.model.CoverableModel;
import com.carpentersblocks.registry.CbBlocks;

import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * The client side: block models and tints. Nothing outside this package refers to client classes, so the dedicated
 * server never loads them.
 */
@Mod(value = CarpentersBlocks.MOD_ID, dist = Dist.CLIENT)
public final class CarpentersBlocksClient {

    public CarpentersBlocksClient(IEventBus modBus) {
        modBus.addListener(CarpentersBlocksClient::registerBlockStateModels);
        modBus.addListener(CarpentersBlocksClient::registerTints);
        modBus.addListener(CarpentersBlocksClient::registerItemModels);
    }

    private static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(CoverableItemModel.TYPE, CoverableItemModel.Unbaked.CODEC);
    }

    private static void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(CoverableModel.TYPE, CoverableModel.Unbaked.CODEC);
    }

    private static void registerTints(RegisterColorHandlersEvent.BlockTintSources event) {
        Block[] blocks = CbBlocks.REGISTER.getEntries().stream()
            .map(DeferredHolder::get)
            .filter(block -> block instanceof BlockCoverable)
            .toArray(Block[]::new);
        event.register(CoverTints.createTintSources(), blocks);
    }

}
