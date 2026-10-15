package com.carpentersblocks.util.registry;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.BlockCarpentersBarrier;
import com.carpentersblocks.block.BlockCarpentersBed;
import com.carpentersblocks.block.BlockCarpentersFlowerPot;
import com.carpentersblocks.block.BlockCarpentersSafe;
import com.carpentersblocks.block.BlockCarpentersButton;
import com.carpentersblocks.block.BlockCarpentersDaylightSensor;
import com.carpentersblocks.block.BlockCarpentersDoor;
import com.carpentersblocks.block.BlockCarpentersGarageDoor;
import com.carpentersblocks.block.BlockCarpentersGate;
import com.carpentersblocks.block.BlockCarpentersHatch;
import com.carpentersblocks.block.BlockCarpentersLever;
import com.carpentersblocks.block.BlockCarpentersTorch;
import com.carpentersblocks.block.BlockCarpentersBlock;
import com.carpentersblocks.block.BlockCarpentersCollapsibleBlock;
import com.carpentersblocks.block.BlockCarpentersLadder;
import com.carpentersblocks.block.BlockCarpentersPressurePlate;
import com.carpentersblocks.block.BlockCarpentersSlope;
import com.carpentersblocks.block.BlockCarpentersStairs;
import com.carpentersblocks.item.ItemBlockCarpentersSlope;
import com.carpentersblocks.util.states.StateMap;
import com.carpentersblocks.util.states.loader.StateLoader;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRegistry {
	
	private static final Material material = Material.WOOD;
	
	public static final String REGISTRY_NAME_BARRIER = "carpenters_barrier";
	public static final String REGISTRY_NAME_BLOCK = "carpenters_block";
	public static final String REGISTRY_NAME_COLLAPSIBLE_BLOCK = "carpenters_collapsible_block";
	public static final String REGISTRY_NAME_LADDER = "carpenters_ladder";
	public static final String REGISTRY_NAME_PRESSURE_PLATE = "carpenters_pressure_plate";
	public static final String REGISTRY_NAME_SLOPE = "carpenters_slope";
	public static final String REGISTRY_NAME_STAIRS = "carpenters_stairs";
	public static final String REGISTRY_NAME_BUTTON = "carpenters_button";
	public static final String REGISTRY_NAME_DAYLIGHT_SENSOR = "carpenters_daylight_sensor";
	public static final String REGISTRY_NAME_DOOR = "carpenters_door";
	public static final String REGISTRY_NAME_GARAGE_DOOR = "carpenters_garage_door";
	public static final String REGISTRY_NAME_GATE = "carpenters_gate";
	public static final String REGISTRY_NAME_HATCH = "carpenters_hatch";
	public static final String REGISTRY_NAME_LEVER = "carpenters_lever";
	public static final String REGISTRY_NAME_TORCH = "carpenters_torch";
	public static final String REGISTRY_NAME_BED = "carpenters_bed";
	public static final String REGISTRY_NAME_FLOWER_POT = "carpenters_flower_pot";
	public static final String REGISTRY_NAME_SAFE = "carpenters_safe";
	public static final String VARIANT_INVENTORY = "inventory";
	
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
    
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
    	{ // Carpenters Block always enabled
    		blockCarpentersBlock = new BlockCarpentersBlock(material)
            		.setTranslationKey("blockCarpentersBlock")
                	.setRegistryName(REGISTRY_NAME_BLOCK)
                	.setHardness(0.2F)
                    .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
    	    Blocks.FIRE.setFireInfo(blockCarpentersBlock, 5, 20);
    		event.getRegistry().register(this.blockCarpentersBlock);
    	}
		if (ConfigRegistry.enableBarrier) {
    		blockCarpentersBarrier = new BlockCarpentersBarrier(material)
                .setTranslationKey("blockCarpentersBarrier")
                .setRegistryName(REGISTRY_NAME_BARRIER)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersBarrier, 5, 20);
    		event.getRegistry().register(this.blockCarpentersBarrier);
    	}
    	if (ConfigRegistry.enableBed) {
    		blockCarpentersBed = new BlockCarpentersBed(material)
                .setTranslationKey("blockCarpentersBed")
                .setRegistryName(REGISTRY_NAME_BED)
                .setHardness(0.4F);
            Blocks.FIRE.setFireInfo(blockCarpentersBed, 5, 20);
    		event.getRegistry().register(this.blockCarpentersBed);
    	}
    	if (ConfigRegistry.enableButton) {
    		blockCarpentersButton = new BlockCarpentersButton(Material.CIRCUITS)
                .setTranslationKey("blockCarpentersButton")
                .setRegistryName(REGISTRY_NAME_BUTTON)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersButton, 5, 20);
    		event.getRegistry().register(this.blockCarpentersButton);
    	}
    	if (ConfigRegistry.enableCollapsibleBlock) {
    		blockCarpentersCollapsibleBlock = new BlockCarpentersCollapsibleBlock(material)
                .setTranslationKey("blockCarpentersCollapsibleBlock")
                .setRegistryName(REGISTRY_NAME_COLLAPSIBLE_BLOCK)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersCollapsibleBlock, 5, 20);
    		event.getRegistry().register(this.blockCarpentersCollapsibleBlock);
    	}
    	if (ConfigRegistry.enableDaylightSensor) {
    		blockCarpentersDaylightSensor = new BlockCarpentersDaylightSensor(material)
                .setTranslationKey("blockCarpentersDaylightSensor")
                .setRegistryName(REGISTRY_NAME_DAYLIGHT_SENSOR)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersDaylightSensor, 5, 20);
    		event.getRegistry().register(this.blockCarpentersDaylightSensor);
    	}
    	if (ConfigRegistry.enableDoor) {
    		blockCarpentersDoor = new BlockCarpentersDoor(material)
                .setTranslationKey("blockCarpentersDoor")
                .setRegistryName(REGISTRY_NAME_DOOR)
                .setHardness(0.2F);
            Blocks.FIRE.setFireInfo(blockCarpentersDoor, 5, 20);
    		event.getRegistry().register(this.blockCarpentersDoor);
    	}
    	if (ConfigRegistry.enableFlowerPot) {
    		blockCarpentersFlowerPot = new BlockCarpentersFlowerPot(Material.CIRCUITS)
                .setTranslationKey("blockCarpentersFlowerPot")
                .setRegistryName(REGISTRY_NAME_FLOWER_POT)
                .setHardness(0.5F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersFlowerPot, 5, 20);
    		event.getRegistry().register(this.blockCarpentersFlowerPot);
    	}
    	if (ConfigRegistry.enableGarageDoor) {
    		blockCarpentersGarageDoor = new BlockCarpentersGarageDoor(material)
                .setTranslationKey("blockCarpentersGarageDoor")
                .setRegistryName(REGISTRY_NAME_GARAGE_DOOR)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersGarageDoor, 5, 20);
    		event.getRegistry().register(this.blockCarpentersGarageDoor);
    	}
    	if (ConfigRegistry.enableGate) {
    		blockCarpentersGate = new BlockCarpentersGate(material)
                .setTranslationKey("blockCarpentersGate")
                .setRegistryName(REGISTRY_NAME_GATE)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersGate, 5, 20);
    		event.getRegistry().register(this.blockCarpentersGate);
    	}
    	if (ConfigRegistry.enableHatch) {
    		blockCarpentersHatch = new BlockCarpentersHatch(material)
                .setTranslationKey("blockCarpentersHatch")
                .setRegistryName(REGISTRY_NAME_HATCH)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersHatch, 5, 20);
    		event.getRegistry().register(this.blockCarpentersHatch);
    	}
    	if (ConfigRegistry.enableLadder) {
    		blockCarpentersLadder = new BlockCarpentersLadder(material)
                .setTranslationKey("blockCarpentersLadder")
                .setRegistryName(REGISTRY_NAME_LADDER)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersLadder, 5, 20);
    		event.getRegistry().register(this.blockCarpentersLadder);
    	}
    	if (ConfigRegistry.enableLever) {
    		blockCarpentersLever = new BlockCarpentersLever(Material.CIRCUITS)
                .setTranslationKey("blockCarpentersLever")
                .setRegistryName(REGISTRY_NAME_LEVER)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersLever, 5, 20);
    		event.getRegistry().register(this.blockCarpentersLever);
    	}
    	if (ConfigRegistry.enablePressurePlate) {
        	StateLoader stateLoader = new StateLoader(REGISTRY_NAME_PRESSURE_PLATE);
        	StateMap stateMap = stateLoader.getStateMap();
        	blockCarpentersPressurePlate = new BlockCarpentersPressurePlate(Material.CIRCUITS, stateMap)
                .setTranslationKey("blockCarpentersPressurePlate")
                .setRegistryName(REGISTRY_NAME_PRESSURE_PLATE)
	            .setHardness(0.2F)
	            .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
	        Blocks.FIRE.setFireInfo(blockCarpentersPressurePlate, 5, 20);
    		event.getRegistry().register(this.blockCarpentersPressurePlate);
    	}
    	if (ConfigRegistry.enableSafe) {
    		blockCarpentersSafe = new BlockCarpentersSafe(material)
                .setTranslationKey("blockCarpentersSafe")
                .setRegistryName(REGISTRY_NAME_SAFE)
                .setHardness(2.5F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersSafe, 5, 20);
    		event.getRegistry().register(this.blockCarpentersSafe);
    	}
    	if (ConfigRegistry.enableSlope) {
    		blockCarpentersSlope = new BlockCarpentersSlope(Material.WOOD)
                    .setTranslationKey("blockCarpentersSlope")
                    .setRegistryName(REGISTRY_NAME_SLOPE)
                    .setHardness(0.2F)
                    .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersSlope, 5, 20);
    		event.getRegistry().register(this.blockCarpentersSlope);
    	}
    	if (ConfigRegistry.enableStairs) {
    	  	blockCarpentersStairs = new BlockCarpentersStairs(material)
                .setTranslationKey("blockCarpentersStairs")
                .setRegistryName(REGISTRY_NAME_STAIRS)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersStairs, 5, 20);
    		event.getRegistry().register(this.blockCarpentersStairs);
    	}
    	if (ConfigRegistry.enableTorch) {
    		blockCarpentersTorch = new BlockCarpentersTorch(Material.CIRCUITS)
                .setTranslationKey("blockCarpentersTorch")
                .setRegistryName(REGISTRY_NAME_TORCH)
                .setHardness(0.2F)
                .setCreativeTab(CarpentersBlocks.CREATIVE_TAB);
            Blocks.FIRE.setFireInfo(blockCarpentersTorch, 5, 20);
    		event.getRegistry().register(this.blockCarpentersTorch);
    	}
    }
    
    @SubscribeEvent
    public void registerItemBlocks(RegistryEvent.Register<Item> event) {
    	{ // Carpenters Block always enabled
	        event.getRegistry().register(new ItemBlock(this.blockCarpentersBlock).setRegistryName(this.REGISTRY_NAME_BLOCK));
    	}
    	if (ConfigRegistry.enableBarrier) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersBarrier).setRegistryName(this.REGISTRY_NAME_BARRIER));
    	}
    	if (ConfigRegistry.enableButton) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersButton).setRegistryName(this.REGISTRY_NAME_BUTTON));
    	}
    	if (ConfigRegistry.enableCollapsibleBlock) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersCollapsibleBlock).setRegistryName(this.REGISTRY_NAME_COLLAPSIBLE_BLOCK));
    	}
    	if (ConfigRegistry.enableDaylightSensor) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersDaylightSensor).setRegistryName(this.REGISTRY_NAME_DAYLIGHT_SENSOR));
    	}
    	if (ConfigRegistry.enableFlowerPot) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersFlowerPot).setRegistryName(this.REGISTRY_NAME_FLOWER_POT));
    	}
    	if (ConfigRegistry.enableGarageDoor) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersGarageDoor).setRegistryName(this.REGISTRY_NAME_GARAGE_DOOR));
    	}
    	if (ConfigRegistry.enableGate) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersGate).setRegistryName(this.REGISTRY_NAME_GATE));
    	}
    	if (ConfigRegistry.enableHatch) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersHatch).setRegistryName(this.REGISTRY_NAME_HATCH));
    	}
    	if (ConfigRegistry.enableLadder) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersLadder).setRegistryName(this.REGISTRY_NAME_LADDER));
    	}
    	if (ConfigRegistry.enableLever) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersLever).setRegistryName(this.REGISTRY_NAME_LEVER));
    	}
    	if (ConfigRegistry.enablePressurePlate) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersPressurePlate).setRegistryName(this.REGISTRY_NAME_PRESSURE_PLATE));
    	}
    	if (ConfigRegistry.enableSafe) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersSafe).setRegistryName(this.REGISTRY_NAME_SAFE));
    	}
    	if (ConfigRegistry.enableSlope) {
    		event.getRegistry().register(new ItemBlockCarpentersSlope(this.blockCarpentersSlope).setRegistryName(this.REGISTRY_NAME_SLOPE));
    	}
    	if (ConfigRegistry.enableStairs) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersStairs).setRegistryName(this.REGISTRY_NAME_STAIRS));
    	}
    	if (ConfigRegistry.enableTorch) {
    		event.getRegistry().register(new ItemBlock(this.blockCarpentersTorch).setRegistryName(this.REGISTRY_NAME_TORCH));
    	}
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
    	registerItemBlockRender(blockCarpentersBlock, VARIANT_INVENTORY, 0);
        if (ConfigRegistry.enableButton) {
        	registerItemBlockRender(blockCarpentersButton, VARIANT_INVENTORY, 0);
        }
        if (ConfigRegistry.enableDaylightSensor) {
        	registerItemBlockRender(blockCarpentersDaylightSensor, VARIANT_INVENTORY, 0);
        }
        if (ConfigRegistry.enableGarageDoor) {
        	registerItemBlockRender(blockCarpentersGarageDoor, VARIANT_INVENTORY, 0);
        }
        if (ConfigRegistry.enableGate) {
        	registerItemBlockRender(blockCarpentersGate, VARIANT_INVENTORY, 0);
        }
        if (ConfigRegistry.enableHatch) {
        	registerItemBlockRender(blockCarpentersHatch, VARIANT_INVENTORY, 0);
        }
        if (ConfigRegistry.enableLever) {
        	registerItemBlockRender(blockCarpentersLever, VARIANT_INVENTORY, 0);
        }
        if (ConfigRegistry.enableTorch) {
        	registerItemBlockRender(blockCarpentersTorch, VARIANT_INVENTORY, 0);
        }
        if (ConfigRegistry.enableFlowerPot) {
        	registerItemBlockRender(blockCarpentersFlowerPot, VARIANT_INVENTORY, 0);
        }
        if (ConfigRegistry.enableSafe) {
        	registerItemBlockRender(blockCarpentersSafe, VARIANT_INVENTORY, 0);
        }
        if (ConfigRegistry.enableBarrier) {
        	registerItemBlockRender(blockCarpentersBarrier, VARIANT_INVENTORY, 0);
        }
        if (ConfigRegistry.enableLadder) {
        	registerItemBlockRender(blockCarpentersLadder, VARIANT_INVENTORY, 0);
        }
        if (ConfigRegistry.enableStairs) {
        	registerItemBlockRender(blockCarpentersStairs, VARIANT_INVENTORY, 0);
        }

        /*
        if (enableBarrier) {
            carpentersBarrierRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersBarrierRenderID, new BlockHandlerCarpentersBarrier());
        }
        if (enableButton) {
            carpentersButtonRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersButtonRenderID, new BlockHandlerCarpentersButton());
        }
        if (enableDaylightSensor) {
            carpentersDaylightSensorRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersDaylightSensorRenderID, new BlockHandlerCarpentersDaylightSensor());
        }
        if (enableGarageDoor) {
            carpentersGarageDoorRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersGarageDoorRenderID, new BlockHandlerCarpentersGarageDoor());
        }
        if (enableGate) {
            carpentersGateRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersGateRenderID, new BlockHandlerCarpentersGate());
        }
        if (enableLever) {
            carpentersLeverRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersLeverRenderID, new BlockHandlerCarpentersLever());
        }
        */
        if (ConfigRegistry.enablePressurePlate) {
        	registerItemBlockRender(blockCarpentersPressurePlate, VARIANT_INVENTORY, 0);
        	registerItemBlockRender(blockCarpentersPressurePlate, "depressed", 1);
        }
        if (ConfigRegistry.enableSlope) {
        	registerItemBlockRender(blockCarpentersSlope, "wedge", 0);
        	registerItemBlockRender(blockCarpentersSlope, "oblique_interior", 1);
        	registerItemBlockRender(blockCarpentersSlope, "oblique_exterior", 2);
        	registerItemBlockRender(blockCarpentersSlope, "prism", 3);
        	registerItemBlockRender(blockCarpentersSlope, "prism_wedge", 4);
        }
        /*
        if (enableStairs) {
            carpentersStairsRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersStairsRenderID, new BlockHandlerCarpentersStairs());
        }
        if (enableHatch) {
            carpentersHatchRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersHatchRenderID, new BlockHandlerCarpentersHatch());
        }
        if (enableDoor) {
            carpentersDoorRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersDoorRenderID, new BlockHandlerCarpentersDoor());
        }
        if (enableBed) {
            carpentersBedRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersBedRenderID, new BlockHandlerCarpentersBed());
        }
        if (enableLadder) {
            carpentersLadderRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersLadderRenderID, new BlockHandlerCarpentersLadder());
        }
        */
        if (ConfigRegistry.enableCollapsibleBlock) {
        	registerItemBlockRender(blockCarpentersCollapsibleBlock, VARIANT_INVENTORY, 0);
        }
        /*
        if (enableTorch) {
            carpentersTorchRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersTorchRenderID, new BlockHandlerCarpentersTorch());
        }
        if (enableSafe) {
            carpentersSafeRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersSafeRenderID, new BlockHandlerCarpentersSafe());
        }
        if (enableFlowerPot) {
            carpentersFlowerPotRenderID = RenderingRegistry.getNextAvailableRenderId();
            RenderingRegistry.registerBlockHandler(carpentersFlowerPotRenderID, new BlockHandlerCarpentersFlowerPot());
        }
        */
    }
    
    @SideOnly(Side.CLIENT)
    private static void registerItemBlockRender(Block block, String variant, int metadata) {
		ModelResourceLocation modelResourceLocation = new ModelResourceLocation(block.getRegistryName(), variant);
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), metadata, modelResourceLocation);
    }

}
