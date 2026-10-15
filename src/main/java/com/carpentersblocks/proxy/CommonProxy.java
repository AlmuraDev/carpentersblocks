package com.carpentersblocks.proxy;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.entity.EntityCarpentersTile;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.tileentity.TECarpentersSafe;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.EventHandler;
import com.carpentersblocks.util.handler.OverlayHandler;
import com.carpentersblocks.util.handler.PacketHandler;
import com.carpentersblocks.util.registry.BlockRegistry;
import com.carpentersblocks.util.registry.ConfigRegistry;
import com.carpentersblocks.util.registry.ItemRegistry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
    	ConfigRegistry.loadConfiguration(event);
        MinecraftForge.EVENT_BUS.register(new BlockRegistry());
        MinecraftForge.EVENT_BUS.register(new ItemRegistry());
        DesignHandler.preInit(event);
        if (ConfigRegistry.enableTile) {
            EntityRegistry.registerModEntity(new ResourceLocation(CarpentersBlocks.MOD_ID, "carpenters_tile"), EntityCarpentersTile.class, "CarpentersTile", 0, CarpentersBlocks.MOD_ID, 64, Integer.MAX_VALUE, false);
        }
    }
    
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        CarpentersBlocks.channel.register(new PacketHandler());
        
        // Initialize overlays
        if (ConfigRegistry.enableOverlays) {
            OverlayHandler.init();
        }

        // Register tile entities
        //GameRegistry.registerTileEntity(              CbTileEntity.class,            "TileEntityCarpentersSlope"); // Compatibility mapping
        //GameRegistry.registerTileEntity(              CbTileEntity.class,              "TileEntityCarpentersBed"); // Compatibility mapping
        GameRegistry.registerTileEntity(              CbTileEntity.class,            "TileEntityCarpentersBlock");
        GameRegistry.registerTileEntity(        TECarpentersSafe.class,             "TileEntityCarpentersSafe");
        // TODO: Implement these
        //GameRegistry.registerTileEntity(CbDaylightSensorTileEntity.class,              "TileEntityCarpentersExt"); // Compatibility mapping
        //GameRegistry.registerTileEntity(CbDaylightSensorTileEntity.class,   "TileEntityCarpentersDaylightSensor");
        //GameRegistry.registerTileEntity(     CbFlowerPotTileEntity.class,        "TileEntityCarpentersFlowerPot");
        //GameRegistry.registerTileEntity(          CbSafeTileEntity.class,             "TileEntityCarpentersSafe");
        //GameRegistry.registerTileEntity(         CbTorchTileEntity.class,            "TileEntityCarpentersTorch");
        //GameRegistry.registerTileEntity(    CbGarageDoorTileEntity.class,       "TileEntityCarpentersGarageDoor");

        // Register entities
        if (ConfigRegistry.enableTile) {
            //EntityRegistry.registerModEntity(EntityCarpentersTile.class, "CarpentersTile", ENTITY_ID_TILE, CarpentersBlocks.instance, 64, 999, false);
        }
    }

	public void postInit(FMLPostInitializationEvent event) {}
	
}
