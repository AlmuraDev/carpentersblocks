package com.carpentersblocks;

import org.slf4j.Logger;

import com.carpentersblocks.network.SlopeSelectPayload;
import com.carpentersblocks.registry.CbBlockEntities;
import com.carpentersblocks.registry.CbBlocks;
import com.carpentersblocks.registry.CbCreativeTabs;
import com.carpentersblocks.registry.CbDataComponents;
import com.carpentersblocks.registry.CbItems;
import com.carpentersblocks.util.block.SlopeShape;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.OverlayHandler;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

@Mod(CarpentersBlocks.MOD_ID)
public final class CarpentersBlocks {

    public static final String MOD_ID = "carpentersblocks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CarpentersBlocks(IEventBus modBus, ModContainer container) {
        // Startup configs load here, before the registries below read them
        container.registerConfig(ModConfig.Type.STARTUP, CbConfig.SPEC);
        DesignHandler.init();
        SlopeShape.setResolution(CbConfig.SLOPE_SMOOTHNESS.get());

        CbBlocks.REGISTER.register(modBus);
        CbItems.REGISTER.register(modBus);
        CbBlockEntities.REGISTER.register(modBus);
        CbCreativeTabs.REGISTER.register(modBus);
        CbDataComponents.REGISTER.register(modBus);

        modBus.addListener(CarpentersBlocks::onCommonSetup);
        modBus.addListener(CarpentersBlocks::registerCapabilities);
        modBus.addListener(CarpentersBlocks::registerPayloads);
        NeoForge.EVENT_BUS.register(CommonEvents.class);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // Items are registered by now
        event.enqueueWork(OverlayHandler::init);
    }

    /**
     * Pipes and hoppers reach the safe through its sides (1.12.2's ISidedInventory).
     */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, CbBlockEntities.SAFE.get(), WorldlyContainerWrapper::new);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(SlopeSelectPayload.TYPE, SlopeSelectPayload.STREAM_CODEC, SlopeSelectPayload::handle);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

}
