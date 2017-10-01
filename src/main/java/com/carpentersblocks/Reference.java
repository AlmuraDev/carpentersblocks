package com.carpentersblocks;

import java.util.ArrayList;

public class Reference 
{
	//Mod Info
	public static final String MOD_NAME = "Carpenter's Blocks";
	public static final String MOD_ID = "carpentersblocks";
	public static final String VERSION = "@VERSION@";
	public static final String CLIENT_PATH = "com.carpentersblocks.proxy.ClientProxy";
	public static final String SERVER_PATH = "com.carpentersblocks.proxy.CommonProxy";
	
	//Block Registry Names
	public static final String REGISTRY_NAME_BLOCK = "carpenters_block";
	public static final String REGISTRY_NAME_COLLAPSIBLE = "carpenters_collapsible_block";
	public static final String REGISTRY_NAME_SLOPE = "carpenters_slope";
	public static final String REGISTRY_NAME_BED = "carpenters_bed";
	public static final String REGISTRY_NAME_BARRIER = "carpenters_barrier";
	public static final String REGISTRY_NAME_BUTTON = "carpenters_button";
	public static final String REGISTRY_NAME_DAYLIGHT_SENSOR = "carpenters_daylight_sensor";
	public static final String REGISTRY_NAME_DOOR = "carpenters_door";
	public static final String REGISTRY_NAME_FLOWER_POT = "carpenters_flower_pot";
	public static final String REGISTRY_NAME_GARAGE_DOOR = "carpenters_garage_door";
	public static final String REGISTRY_NAME_GATE = "carpenters_gate";
	public static final String REGISTRY_NAME_LADDER = "carpenters_ladder";
	public static final String REGISTRY_NAME_HATCH = "carpenters_hatch";
	public static final String REGISTRY_NAME_LEVER = "carpenters_lever";
	public static final String REGISTRY_NAME_SAFE = "carpenters_safe";
	public static final String REGISTRY_NAME_TORCH = "carpenters_torch";
	public static final String REGISTRY_NAME_STAIRS = "carpenters_stairs";
	public static final String REGISTRY_NAME_PRESSURE_PLATE = "carpenters_pressure_plate";
	
	//enable blocks
	public static boolean enableBarrier          = true;
    public static boolean enableBed              = true;
    public static boolean enableButton           = true;
    public static boolean enableCollapsibleBlock = true;
    public static boolean enableDaylightSensor   = true;
    public static boolean enableDoor             = true;
    public static boolean enableFlowerPot        = true;
    public static boolean enableGarageDoor       = true;
    public static boolean enableGate             = true;
    public static boolean enableHatch            = true;
    public static boolean enableLadder           = true;
    public static boolean enableLever            = true;
    public static boolean enablePressurePlate    = true;
    public static boolean enableSafe             = true;
    public static boolean enableSlope            = true;
    public static boolean enableStairs           = true;
    public static boolean enableTorch            = true;
    
    //enable items
    public static boolean enableHammer           = true;
    public static boolean enableChisel           = true;
    public static boolean enableTile             = true;
    
    //tool variables
    public static boolean itemCarpentersToolsDamageable        = true;
    public static double itemHammerDamageChanceFromSlopes      = 0.75D;
    public static double itemHammerDamageChanceFromStairs      = 1.0D;
    public static double itemHammerDamageChanceFromCollapsible = 0.2D;
    public static int slopeSmoothness           = 2;
    public static int multiBlockSizeLimit       = 500;
    public static int itemCarpentersToolsUses                  = 400;

    //features
    public static boolean enableCovers              = true;
    public static boolean enableOverlays            = true;
    public static boolean enableSideCovers          = true;
    public static boolean enableDyeColors           = true;
    public static boolean enableChiselDesigns       = true;
    public static boolean enableTorchWeatherEffects = true;
    public static boolean enableOwnership           = true;
    public static boolean enableIllumination        = true;
    public static boolean enableRoutableFluids      = true;
    public static boolean enableAlphaPanes          = true;
    public static boolean enableRailSlopes          = true;
    public static boolean enableGarageDoorVoidFill  = true;
    public static boolean enableFreeStandingLadders = false;
    
    //recipe return quantity
    public static int recipeQuantityBarrier          = 4;
    public static int recipeQuantityBed              = 1;
    public static int recipeQuantityBlock            = 5;
    public static int recipeQuantityButton           = 1;
    public static int recipeQuantityCollapsibleBlock = 9;
    public static int recipeQuantityDaylightSensor   = 1;
    public static int recipeQuantityDoor             = 1;
    public static int recipeQuantityFlowerPot        = 1;
    public static int recipeQuantityGarageDoor       = 8;
    public static int recipeQuantityGate             = 1;
    public static int recipeQuantityHatch            = 1;
    public static int recipeQuantityLadder           = 4;
    public static int recipeQuantityLever            = 1;
    public static int recipeQuantityPressurePlate    = 1;
    public static int recipeQuantitySafe             = 1;
    public static int recipeQuantitySlope            = 6;
    public static int recipeQuantityStairs           = 4;
    public static int recipeQuantityTorch            = 8;
    public static int recipeQuantityTile             = 12;
    
    //render ID
    public static int carpentersBarrierRenderID;
    public static int carpentersBedRenderID;
    public static int carpentersBlockRenderID;
    public static int carpentersButtonRenderID;
    public static int carpentersCollapsibleBlockRenderID;
    public static int carpentersDaylightSensorRenderID;
    public static int carpentersDoorRenderID;
    public static int carpentersFlowerPotRenderID;
    public static int carpentersGarageDoorRenderID;
    public static int carpentersGateRenderID;
    public static int carpentersHatchRenderID;
    public static int carpentersLadderRenderID;
    public static int carpentersLeverRenderID;
    public static int carpentersPressurePlateRenderID;
    public static int carpentersSafeRenderID;
    public static int carpentersSlopeRenderID;
    public static int carpentersStairsRenderID;
    public static int carpentersTorchRenderID; 

    //overlays
    public static ArrayList<String> overlayItems    = new ArrayList<String>();
    public static ArrayList<String> coverExceptions = new ArrayList<String>();
} 