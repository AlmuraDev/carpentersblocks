package com.carpentersblocks.util.registry;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.item.ItemCarpentersChisel;
import com.carpentersblocks.item.ItemCarpentersHammer;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import static com.carpentersblocks.Reference.*;
public class ItemRegistry 
{ 
    public static Item itemCarpentersHammer;
    public static Item itemCarpentersChisel;
    public static Item itemCarpentersDoor;
    public static Item itemCarpentersBed;
    public static Item itemCarpentersTile;   
    
    /**
     * Registers item IDs.
     */
    public static void preInit(FMLPreInitializationEvent event)
    {  
        // Register items
        
        if (enableHammer)
        {
            itemCarpentersHammer = new ItemCarpentersHammer()
            		.setRegistryName("carpenters_hammer")
            		.setUnlocalizedName("itemCarpentersHammer")
            		.setCreativeTab(CarpentersBlocks.CREATIVE_TAB)
            		.setMaxStackSize(1);
            GameRegistry.register(itemCarpentersHammer);
            if (itemCarpentersToolsDamageable)
            {
            	itemCarpentersHammer.setMaxDamage(itemCarpentersToolsUses);
            }
            if (Side.CLIENT.equals(event.getSide()))
            {
            	ModelLoader.setCustomModelResourceLocation(itemCarpentersHammer, 0, new ModelResourceLocation(MOD_ID + ":" + "carpenters_hammer"));
            }
        }
        if (enableChisel) 
        {
            itemCarpentersChisel = new ItemCarpentersChisel()
            		.setRegistryName("carpenters_chisel")
            		.setUnlocalizedName("itemCarpentersChisel")
            		.setCreativeTab(CarpentersBlocks.CREATIVE_TAB)
            		.setMaxStackSize(1);
            GameRegistry.register(itemCarpentersChisel);
            if (itemCarpentersToolsDamageable)
            {
            	itemCarpentersChisel.setMaxDamage(itemCarpentersToolsUses);
            }
            if (Side.CLIENT.equals(event.getSide()))
            {
            	ModelLoader.setCustomModelResourceLocation(itemCarpentersChisel, 0, new ModelResourceLocation(MOD_ID + ":" + "carpenters_chisel"));
            }
        }
        /*
        if (enableTile) {
            itemCarpentersTile = new ItemCarpentersTile().setUnlocalizedName("itemCarpentersTile");
            GameRegistry.registerItem(itemCarpentersTile, "itemCarpentersTile");
        }
        if (BlockRegistry.enableDoor) {
            itemCarpentersDoor = new ItemCarpentersDoor().setUnlocalizedName("itemCarpentersDoor");
            GameRegistry.registerItem(itemCarpentersDoor, "itemCarpentersDoor");
        }
        if (BlockRegistry.enableBed) {
            itemCarpentersBed = new ItemCarpentersBed().setUnlocalizedName("itemCarpentersBed");
            GameRegistry.registerItem(itemCarpentersBed, "itemCarpentersBed");
        }*/
    }

    public static void init(FMLInitializationEvent event)
    {
        if (enableHammer)
        {
            GameRegistry.addShapedRecipe(new ResourceLocation("carpentersblocks", "itemCarpentersHammer"),new ResourceLocation("carpentersblocks",
                    "Carpenters Tools"),(new
                    ItemStack(itemCarpentersHammer, 1)), new Object[] { "XX ", " YX", " Y ", 'X', Items.IRON_INGOT, 'Y',
                    BlockRegistry.blockCarpentersBlock });
        }
        if (enableChisel) 
        {
            GameRegistry.addShapedRecipe(new ResourceLocation("carpentersblocks", "itemCarpentersChisel"),new ResourceLocation("carpentersblocks",
                    "Carpenters Tools"), new ItemStack(itemCarpentersChisel, 1), new Object[] { "X", "Y", 'X', Items.IRON_INGOT, 'Y',
                    BlockRegistry.blockCarpentersBlock });
        }
        /*        if (enableTile) {
            GameRegistry.addRecipe(new ItemStack(itemCarpentersTile, recipeQuantityTile), new Object[] { "XXX", "YYY", 'X', Blocks.hardened_clay, 'Y', BlockRegistry.blockCarpentersBlock });
        }
        if (BlockRegistry.enableDoor) {
            GameRegistry.addRecipe(new ItemStack(itemCarpentersDoor, BlockRegistry.recipeQuantityDoor), new Object[] { "XX", "XX", "XX", 'X', BlockRegistry.blockCarpentersBlock });
        }
        if (BlockRegistry.enableBed) {
            GameRegistry.addRecipe(new ItemStack(itemCarpentersBed, BlockRegistry.recipeQuantityBed), new Object[] { "XXX", "YYY", 'X', Blocks.wool, 'Y', BlockRegistry.blockCarpentersBlock });
        }*/
    }  
}
