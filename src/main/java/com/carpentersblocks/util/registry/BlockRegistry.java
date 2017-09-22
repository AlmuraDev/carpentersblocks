package com.carpentersblocks.util.registry;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.Reference;
import com.carpentersblocks.block.BlockCarpentersBarrier;
import com.carpentersblocks.block.BlockCarpentersBed;
import com.carpentersblocks.block.BlockCarpentersBlock;
import com.carpentersblocks.block.BlockCarpentersButton;
import com.carpentersblocks.block.BlockCarpentersCollapsibleBlock;
import com.carpentersblocks.block.BlockCarpentersDaylightSensor;
import com.carpentersblocks.block.BlockCarpentersDoor;
import com.carpentersblocks.block.BlockCarpentersFlowerPot;
import com.carpentersblocks.block.BlockCarpentersGarageDoor;
import com.carpentersblocks.block.BlockCarpentersGate;
import com.carpentersblocks.block.BlockCarpentersHatch;
import com.carpentersblocks.block.BlockCarpentersLadder;
import com.carpentersblocks.block.BlockCarpentersLever;
import com.carpentersblocks.block.BlockCarpentersPressurePlate;
import com.carpentersblocks.block.BlockCarpentersSafe;
import com.carpentersblocks.block.BlockCarpentersSlope;
import com.carpentersblocks.block.BlockCarpentersStairs;
import com.carpentersblocks.block.BlockCarpentersTorch;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPressurePlate.Sensitivity;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import static com.carpentersblocks.Reference.*;

public class BlockRegistry 
{	
	private static final Material material = Material.WOOD; 
	
    public static Block blockCarpentersBarrier;
    public static Block blockCarpentersBed;
    public static Block blockCarpentersBlock;
    public static Block blockCarpentersButton;
    public static Block blockCarpentersCollapsibleBlock;
    public static Block blockCarpentersDaylightSensor;
    public static Block blockCarpentersDoor;
    public static Block blockCarpentersFlowerPot;
    public static Block blockCarpentersGarageDoor;
    public static Block blockCarpentersGate;
    public static Block blockCarpentersHatch;
    public static Block blockCarpentersLadder;
    public static Block blockCarpentersLever;
    public static Block blockCarpentersPressurePlate;
    public static Block blockCarpentersSafe;
    public static Block blockCarpentersSlope;
    public static Block blockCarpentersStairs;
    public static Block blockCarpentersTorch;

    /**
     * Block preinitialization.
     */
    public static void preInit(FMLPreInitializationEvent event)
    { 
        // Register blocks
        
    	blockCarpentersBlock = new BlockCarpentersBlock(material)
    		.setUnlocalizedName("blockCarpentersBlock")
        	.setRegistryName(REGISTRY_NAME_BLOCK)
        	.setHardness(0.2F)
            .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
        ForgeRegistries.BLOCKS.register(blockCarpentersBlock);
        ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersBlock).setRegistryName(blockCarpentersBlock.getRegistryName()));
        Blocks.FIRE.setFireInfo(blockCarpentersBlock, 5, 20);

         if(enableBarrier)
         {
             blockCarpentersBarrier = new BlockCarpentersBarrier(material, MapColor.WOOD)
                 .setUnlocalizedName("blockCarpentersBarrier")
                 .setRegistryName(REGISTRY_NAME_BARRIER)
                 .setHardness(0.2F)
                 .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
             ForgeRegistries.BLOCKS.register(blockCarpentersBarrier);
             ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersBarrier).setRegistryName(blockCarpentersBarrier.getRegistryName()));
             Blocks.FIRE.setFireInfo(blockCarpentersBarrier, 5, 20);
         }
        
        if(enableBed) 
        {
            blockCarpentersBed = new BlockCarpentersBed(material)
                .setUnlocalizedName("blockCarpentersBed")
                .setRegistryName(REGISTRY_NAME_BED)
                .setHardness(0.4F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersBed);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersBed).setRegistryName(blockCarpentersBed.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersBed, 5, 20);
        }
        
        if(enableButton) 
        {
            blockCarpentersButton = new BlockCarpentersButton(true)
                .setUnlocalizedName("blockCarpentersButton")
                .setRegistryName(REGISTRY_NAME_BUTTON)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersButton);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersButton).setRegistryName(blockCarpentersButton.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersButton, 5, 20);
        }

        if(enableCollapsibleBlock) 
        {
            blockCarpentersCollapsibleBlock = new BlockCarpentersCollapsibleBlock(material)
                .setUnlocalizedName("blockCarpentersCollapsibleBlock")
                .setRegistryName(REGISTRY_NAME_COLLAPSIBLE)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersCollapsibleBlock);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersCollapsibleBlock).setRegistryName(blockCarpentersCollapsibleBlock
                    .getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersCollapsibleBlock, 5, 20);
        } 
        
        if(enableDaylightSensor) 
        {
            blockCarpentersDaylightSensor = new BlockCarpentersDaylightSensor(false)
                .setUnlocalizedName("blockCarpentersDaylightSensor")
                .setRegistryName(REGISTRY_NAME_DAYLIGHT_SENSOR)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersDaylightSensor);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersDaylightSensor).setRegistryName(blockCarpentersDaylightSensor
                    .getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersDaylightSensor, 5, 20);
        }

        if(enableDoor) 
        {
            blockCarpentersDoor = new BlockCarpentersDoor(material)
                .setUnlocalizedName("blockCarpentersDoor")
                .setRegistryName(REGISTRY_NAME_DOOR)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersDoor);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersDoor).setRegistryName(blockCarpentersDoor.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersDoor, 5, 20);
        }

        if(enableFlowerPot) 
        {
            blockCarpentersFlowerPot = new BlockCarpentersFlowerPot(Material.CIRCUITS)
                .setUnlocalizedName("blockCarpentersFlowerPot")
                .setRegistryName(REGISTRY_NAME_FLOWER_POT)
                .setHardness(0.5F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersFlowerPot);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersFlowerPot).setRegistryName(blockCarpentersFlowerPot.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersFlowerPot, 5, 20);
        }

        if(enableGarageDoor) 
        {
            blockCarpentersGarageDoor = new BlockCarpentersGarageDoor(material)
                .setUnlocalizedName("blockCarpentersGarageDoor")
                .setRegistryName(REGISTRY_NAME_GARAGE_DOOR)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersGarageDoor);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersGarageDoor).setRegistryName(blockCarpentersGarageDoor.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersGarageDoor, 5, 20);
        }

        if(enableGate) 
        {
            blockCarpentersGate = new BlockCarpentersGate(material)
                .setUnlocalizedName("blockCarpentersGate")
                .setRegistryName(REGISTRY_NAME_GATE)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersGate);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersGate).setRegistryName(blockCarpentersGate.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersGate, 5, 20);
        }

        if(enableHatch) 
        {
            blockCarpentersHatch = new BlockCarpentersHatch(material)
                .setUnlocalizedName("blockCarpentersHatch")
                .setRegistryName(REGISTRY_NAME_HATCH)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersHatch);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersHatch).setRegistryName(blockCarpentersHatch.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersHatch, 5, 20);
        }

        if(enableLadder) 
        {
            blockCarpentersLadder = new BlockCarpentersLadder(material)
                .setUnlocalizedName("blockCarpentersLadder")
                .setRegistryName(REGISTRY_NAME_LADDER)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersLadder);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersLadder).setRegistryName(blockCarpentersLadder.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersLadder, 5, 20);
        }

        if(enableLever) 
        {
            blockCarpentersLever = new BlockCarpentersLever(Material.CIRCUITS)
                .setUnlocalizedName("blockCarpentersLever")
                .setRegistryName(REGISTRY_NAME_LEVER)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersLever);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersLever).setRegistryName(blockCarpentersLever.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersLever, 5, 20);
        }

        if(enablePressurePlate) 
        {
            blockCarpentersPressurePlate = new BlockCarpentersPressurePlate(Material.CIRCUITS, Sensitivity.EVERYTHING)
                .setUnlocalizedName("blockCarpentersPressurePlate")
                .setRegistryName(REGISTRY_NAME_PRESSURE_PLATE)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersPressurePlate);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersPressurePlate).setRegistryName(blockCarpentersPressurePlate.getRegistryName
                    ()));
            Blocks.FIRE.setFireInfo(blockCarpentersPressurePlate, 5, 20);
        }

        if(enableSafe) 
        {
            blockCarpentersSafe = new BlockCarpentersSafe(material)
                .setUnlocalizedName("blockCarpentersSafe")
                .setRegistryName(REGISTRY_NAME_SAFE)
                .setHardness(2.5F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersSafe);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersSafe).setRegistryName(blockCarpentersSafe.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersSafe, 5, 20);
        }
        
        if (enableSlope) 
        {
        		blockCarpentersSlope = new BlockCarpentersSlope(material)
                        .setRegistryName(REGISTRY_NAME_SLOPE) 
                        .setHardness(0.2F)
                        .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersSlope);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersSlope).setRegistryName(blockCarpentersSlope.getRegistryName()));
        	
        }

        if(enableStairs) 
        {
            blockCarpentersStairs = new BlockCarpentersStairs(blockCarpentersBlock.getDefaultState())
                .setUnlocalizedName("blockCarpentersStairs")
                .setRegistryName(REGISTRY_NAME_STAIRS)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            ForgeRegistries.BLOCKS.register(blockCarpentersStairs);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersStairs).setRegistryName(blockCarpentersStairs.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersStairs, 5, 20);
        }

        if(enableTorch) 
        {
            blockCarpentersTorch = new BlockCarpentersTorch(blockCarpentersTorch.getDefaultState())
                .setUnlocalizedName("blockCarpentersTorch")
                .setRegistryName(REGISTRY_NAME_TORCH)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB)
                .setLightLevel(1.0F);

            if (Reference.enableTorchWeatherEffects) 
            {
            	blockCarpentersTorch.setTickRandomly(true);
            }

            ForgeRegistries.BLOCKS.register(blockCarpentersTorch);
            ForgeRegistries.ITEMS.register(new ItemBlock(blockCarpentersTorch).setRegistryName(blockCarpentersTorch.getRegistryName()));
            Blocks.FIRE.setFireInfo(blockCarpentersTorch, 5, 20);
        }
    }
    
    @SideOnly(Side.CLIENT)
    private static void registerItemBlockRender(Block block) 
    {
		ModelResourceLocation modelResourceLocation = new ModelResourceLocation(block.getRegistryName(), "inventory");
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(block), 0, modelResourceLocation); 
    }

    @SideOnly(Side.CLIENT)
    public static void registerRenderers() 
    {
    	registerItemBlockRender(blockCarpentersBlock);

        if(enableBarrier) 
        {
        	registerItemBlockRender(blockCarpentersBarrier);
        }
        
        if(enableButton) 
        {
        	registerItemBlockRender(blockCarpentersButton);
        }
        
        if(enableDaylightSensor) 
        {
        	registerItemBlockRender(blockCarpentersDaylightSensor);
        }
        
        if(enableGarageDoor) 
        {
        	registerItemBlockRender(blockCarpentersDoor);
        }
        
        if(enableGate) 
        {
        	registerItemBlockRender(blockCarpentersGate);
        }
        
        if(enableLever) 
        {
        	registerItemBlockRender(blockCarpentersLever);
        }
        
        if(enablePressurePlate) 
        {
            registerItemBlockRender(blockCarpentersPressurePlate);
        }
        
        if(enableSlope) 
        {
        	registerItemBlockRender(blockCarpentersSlope);
        }
        
        if(enableStairs) 
        {
        	registerItemBlockRender(blockCarpentersStairs);
        }
        
        if(enableHatch) 
        {
        	registerItemBlockRender(blockCarpentersHatch);
        }
        if(enableDoor) 
        {
        	registerItemBlockRender(blockCarpentersDoor);
        }
        
        if(enableBed) 
        {
        	registerItemBlockRender(blockCarpentersBed);
        }
        
        if(enableLadder) 
        {
        	registerItemBlockRender(blockCarpentersLadder);
        }
        if(enableCollapsibleBlock) 
        {
        	registerItemBlockRender(blockCarpentersCollapsibleBlock);
        }
        
        if(enableTorch) 
        {
        	registerItemBlockRender(blockCarpentersTorch);
        }
        
        if(enableSafe) 
        {
        	registerItemBlockRender(blockCarpentersSafe);
        }
        
        if(enableFlowerPot) 
        {
        	registerItemBlockRender(blockCarpentersFlowerPot);
        }
    }
    
     /*
     * Block initialiation.
     */
    public static void init(FMLInitializationEvent event) 
    {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCarpentersBlock, recipeQuantityBlock), "XXX", "XYX", "XXX", 'X', "stickWood", 'Y', "plankWood"));

        if(enableBarrier) 
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCarpentersBarrier, recipeQuantityBarrier), " Y ", "XYX", 'X', "stickWood", 'Y', blockCarpentersBlock));
        }
        
        if(enableButton) 
        {
            GameRegistry.addRecipe(new ItemStack(blockCarpentersButton, recipeQuantityButton), new Object[] { "X", 'X', blockCarpentersBlock });
        }
        
        if(enableCollapsibleBlock) 
        {
            GameRegistry.addRecipe(new ItemStack(blockCarpentersCollapsibleBlock, recipeQuantityCollapsibleBlock), new Object[] { "XXX", "XXX", "XXX", 'X', blockCarpentersBlock });
        }
        
        if(enableDaylightSensor) 
        {
            GameRegistry.addRecipe(new ItemStack(blockCarpentersDaylightSensor, recipeQuantityDaylightSensor), new Object[] { "WWW", "XYX", "ZZZ", 'W', Blocks.GLASS, 'X', Items.REDSTONE, 'Y', new ItemStack(Items.DYE, 1, 4), 'Z', blockCarpentersBlock });
        }
        
        if(enableFlowerPot) 
        {
            GameRegistry.addRecipe(new ItemStack(blockCarpentersFlowerPot, recipeQuantityFlowerPot), new Object[] { "X X", " X ", 'X', blockCarpentersBlock });
        }
        
        if(enableGarageDoor) 
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCarpentersGarageDoor, recipeQuantityGarageDoor), "XXX", "X X", "XXX", 'X', blockCarpentersBlock));
        }
        
        if(enableGate) 
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCarpentersGate, recipeQuantityGate), "XYX", "XYX", 'X', "stickWood", 'Y', blockCarpentersBlock));
        }
        
        if(enableHatch) 
        {
            GameRegistry.addRecipe(new ItemStack(blockCarpentersHatch, recipeQuantityHatch), new Object[] { "XXX", "XXX", 'X', blockCarpentersBlock });
        }
        
        if(enableLadder) 
        {
            GameRegistry.addRecipe(new ItemStack(blockCarpentersLadder, recipeQuantityLadder), new Object[] { "X X", "XXX", "X X", 'X', blockCarpentersBlock });
        }
        
        if(enableLever) 
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCarpentersLever, recipeQuantityLever), "X", "Y", 'X', "stickWood", 'Y', blockCarpentersBlock));
        }
        
        if(enablePressurePlate) 
        {
            GameRegistry.addRecipe(new ItemStack(blockCarpentersPressurePlate, recipeQuantityPressurePlate), new Object[] { "XX", 'X', blockCarpentersBlock });
        }
        
        if(enableSafe) 
        {
            GameRegistry.addRecipe(new ItemStack(blockCarpentersSafe, recipeQuantitySafe), new Object[] { "XXX", "XYX", "XZX", 'X', blockCarpentersBlock, 'Y', Blocks.IRON_BLOCK, 'Z', Items.REDSTONE });
        }
        
        if(enableSlope) 
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCarpentersSlope, recipeQuantitySlope), "  X", " XY", "XYY", 'X', "stickWood", 'Y', blockCarpentersBlock));
        }
        
        if(enableStairs) 
        {
            GameRegistry.addRecipe(new ItemStack(blockCarpentersStairs, recipeQuantityStairs), new Object[] { "  X", " XX", "XXX", 'X', blockCarpentersBlock });
        }
        
        if(enableTorch) 
        {
            GameRegistry.addRecipe(new ItemStack(blockCarpentersTorch, recipeQuantityTorch), new Object[] { "X", "Y", 'X', new ItemStack(Items.COAL, 1, 0), 'Y', blockCarpentersBlock });
            GameRegistry.addRecipe(new ItemStack(blockCarpentersTorch, recipeQuantityTorch), new Object[] { "X", "Y", 'X', new ItemStack(Items.COAL, 1, 1), 'Y', blockCarpentersBlock });
        }
    }
}