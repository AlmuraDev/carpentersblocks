package com.carpentersblocks.client;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.ICoverableBlock;
import com.carpentersblocks.client.model.CoverTints;
import com.carpentersblocks.client.model.CoverableItemModel;
import com.carpentersblocks.client.model.CoverableModel;
import com.carpentersblocks.registry.CbBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import com.carpentersblocks.item.ItemBlockCarpentersSlope;
import com.carpentersblocks.network.SlopeSelectPayload;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
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
        NeoForge.EVENT_BUS.addListener(CarpentersBlocksClient::onMouseScroll);
    }

    /**
     * Sneak and scroll with a slope in hand switches the slope it places (1.12.2's EventHandler.onMouseEvent).
     */
    private static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !player.isShiftKeyDown() || !(player.getMainHandItem().getItem() instanceof ItemBlockCarpentersSlope)) {
            return;
        }
        if (event.getScrollDeltaY() != 0) {
            ClientPacketDistributor.sendToServer(new SlopeSelectPayload(player.getInventory().getSelectedSlot(), event.getScrollDeltaY() > 0));
        }
        event.setCanceled(true);
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
            .filter(block -> block instanceof ICoverableBlock)
            .toArray(Block[]::new);
        event.register(CoverTints.createTintSources(), blocks);
    }

}
