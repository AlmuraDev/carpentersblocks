package com.carpentersblocks.proxy;

import com.carpentersblocks.CBConfig;
import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.Reference;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.EventHandler;
import com.carpentersblocks.util.handler.OverlayHandler;
import com.carpentersblocks.util.handler.PacketHandler;
import com.carpentersblocks.util.registry.BlockRegistry;
import com.carpentersblocks.util.registry.ItemRegistry;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy
{ 
    public void preInit(FMLPreInitializationEvent event, Configuration config) 
    {
    	CBConfig.config(config); 
        BlockRegistry.preInit(event); // Do before item registration
        if (event.getSide() == Side.CLIENT) {
            ItemRegistry.preInit(event);
        }
        DesignHandler.preInit(event);        
    }
    
    public void init(FMLInitializationEvent event) 
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        CarpentersBlocks.channel.register(new PacketHandler());

        // Initialize blocks and items
        BlockRegistry.init(event);
        ItemRegistry.init(event);

        // Initialize overlays
        if (Reference.enableOverlays) 
        {
            OverlayHandler.init();
        }

        // Register tile entities
        // Broken:
        //GameRegistry.registerTileEntity(CbTileEntity.class,            "TileEntityCarpentersSlope"); // Compatibility mapping
        //GameRegistry.registerTileEntity(              CbTileEntity.class,              "TileEntityCarpentersBed"); // Compatibility mapping
        GameRegistry.registerTileEntity(CbTileEntity.class,"TileEntityCarpentersBlock");
        // TODO: Implement these
        //GameRegistry.registerTileEntity(CbDaylightSensorTileEntity.class,              "TileEntityCarpentersExt"); // Compatibility mapping
        //GameRegistry.registerTileEntity(CbDaylightSensorTileEntity.class,   "TileEntityCarpentersDaylightSensor");
        //GameRegistry.registerTileEntity(     CbFlowerPotTileEntity.class,        "TileEntityCarpentersFlowerPot");
        //GameRegistry.registerTileEntity(          CbTileEntity.class,             "TileEntityCarpentersSafe");
        //GameRegistry.registerTileEntity(         CbTorchTileEntity.class,            "TileEntityCarpentersTorch");
        //GameRegistry.registerTileEntity(    CbGarageDoorTileEntity.class,       "TileEntityCarpentersGarageDoor");

        // Register entities
        if (Reference.enableTile) 
        {
            //EntityRegistry.registerModEntity(EntityCarpentersTile.class, "CarpentersTile", ENTITY_ID_TILE, CarpentersBlocks.instance, 64, 999, false);
        }
    } 
}
