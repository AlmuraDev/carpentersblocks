package com.carpentersblocks.util.registry;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.item.ItemCarpentersChisel;
import com.carpentersblocks.item.ItemCarpentersBed;
import com.carpentersblocks.item.ItemCarpentersDoor;
import com.carpentersblocks.item.ItemCarpentersTile;
import com.carpentersblocks.item.ItemCarpentersHammer;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRegistry {

    public static Item itemCarpentersHammer;
    public static Item itemCarpentersChisel;
    public static Item itemCarpentersDoor;
    public static Item itemCarpentersBed;
    public static Item itemCarpentersTile;
    
    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
    	if (ConfigRegistry.enableHammer) {
			itemCarpentersHammer = new ItemCarpentersHammer()
				.setRegistryName("carpenters_hammer")
				.setTranslationKey("itemCarpentersHammer")
				.setCreativeTab(CarpentersBlocks.CREATIVE_TAB)
				.setMaxStackSize(1);
			if (ConfigRegistry.itemCarpentersToolsDamageable) {
				itemCarpentersHammer.setMaxDamage(ConfigRegistry.itemCarpentersToolsUses);
			}
    		event.getRegistry().register(this.itemCarpentersHammer);
    	}
    	if (ConfigRegistry.enableChisel) {
			itemCarpentersChisel = new ItemCarpentersChisel()
				.setRegistryName("carpenters_chisel")
				.setTranslationKey("itemCarpentersChisel")
				.setCreativeTab(CarpentersBlocks.CREATIVE_TAB)
				.setMaxStackSize(1);
			if (ConfigRegistry.itemCarpentersToolsDamageable) {
				itemCarpentersChisel.setMaxDamage(ConfigRegistry.itemCarpentersToolsUses);
			}
    		event.getRegistry().register(this.itemCarpentersChisel);
    	}
    	if (ConfigRegistry.enableDoor) {
    		itemCarpentersDoor = new ItemCarpentersDoor()
    			.setRegistryName("carpenters_door_item")
    			.setTranslationKey("itemCarpentersDoor")
    			.setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
    		event.getRegistry().register(itemCarpentersDoor);
    	}
    	if (ConfigRegistry.enableBed) {
    		itemCarpentersBed = new ItemCarpentersBed()
    			.setRegistryName("carpenters_bed_item")
    			.setTranslationKey("itemCarpentersBed")
    			.setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
    		event.getRegistry().register(itemCarpentersBed);
    	}
    	if (ConfigRegistry.enableTile) {
    		itemCarpentersTile = new ItemCarpentersTile()
    			.setRegistryName("carpenters_tile")
    			.setTranslationKey("itemCarpentersTile")
    			.setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
    		event.getRegistry().register(itemCarpentersTile);
    	}
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        if (ConfigRegistry.enableHammer) {
        	ModelLoader.setCustomModelResourceLocation(itemCarpentersHammer, 0, new ModelResourceLocation(CarpentersBlocks.MOD_ID + ":" + "carpenters_hammer"));
        }
        if (ConfigRegistry.enableChisel) {
            ModelLoader.setCustomModelResourceLocation(itemCarpentersChisel, 0, new ModelResourceLocation(CarpentersBlocks.MOD_ID + ":" + "carpenters_chisel"));
        }
        if (ConfigRegistry.enableDoor) {
            ModelLoader.setCustomModelResourceLocation(itemCarpentersDoor, 0, new ModelResourceLocation(CarpentersBlocks.MOD_ID + ":" + "carpenters_door_item", "inventory"));
        }
        if (ConfigRegistry.enableBed) {
            ModelLoader.setCustomModelResourceLocation(itemCarpentersBed, 0, new ModelResourceLocation(CarpentersBlocks.MOD_ID + ":" + "carpenters_bed_item", "inventory"));
        }
        if (ConfigRegistry.enableTile) {
            ModelLoader.setCustomModelResourceLocation(itemCarpentersTile, 0, new ModelResourceLocation(CarpentersBlocks.MOD_ID + ":" + "carpenters_tile", "inventory"));
        }
        /*
        if (enableTile) {
            itemCarpentersTile = new ItemCarpentersTile().setTranslationKey("itemCarpentersTile");
            GameRegistry.registerItem(itemCarpentersTile, "itemCarpentersTile");
        }
        if (BlockRegistry.enableDoor) {
            itemCarpentersDoor = new ItemCarpentersDoor().setTranslationKey("itemCarpentersDoor");
            GameRegistry.registerItem(itemCarpentersDoor, "itemCarpentersDoor");
        }
        if (BlockRegistry.enableBed) {
            itemCarpentersBed = new ItemCarpentersBed().setTranslationKey("itemCarpentersBed");
            GameRegistry.registerItem(itemCarpentersBed, "itemCarpentersBed");
        }*/
    }
    
}
