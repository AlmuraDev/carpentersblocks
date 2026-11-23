package com.carpentersblocks;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The 1.12.2 configuration (carpentersblocks.cfg), as a startup config: it is read before blocks register, since it
 * turns blocks on and off, and each side reads its own file like 1.12.2.
 */
public final class CbConfig {

    public static final ModConfigSpec SPEC;

    // Blocks
    public static final ModConfigSpec.BooleanValue ENABLE_BARRIER;
    public static final ModConfigSpec.BooleanValue ENABLE_BED;
    public static final ModConfigSpec.BooleanValue ENABLE_BUTTON;
    public static final ModConfigSpec.BooleanValue ENABLE_COLLAPSIBLE_BLOCK;
    public static final ModConfigSpec.BooleanValue ENABLE_DAYLIGHT_SENSOR;
    public static final ModConfigSpec.BooleanValue ENABLE_DOOR;
    public static final ModConfigSpec.BooleanValue ENABLE_FLOWER_POT;
    public static final ModConfigSpec.BooleanValue ENABLE_GARAGE_DOOR;
    public static final ModConfigSpec.BooleanValue ENABLE_GATE;
    public static final ModConfigSpec.BooleanValue ENABLE_HATCH;
    public static final ModConfigSpec.BooleanValue ENABLE_LADDER;
    public static final ModConfigSpec.BooleanValue ENABLE_LEVER;
    public static final ModConfigSpec.BooleanValue ENABLE_PRESSURE_PLATE;
    public static final ModConfigSpec.BooleanValue ENABLE_SAFE;
    public static final ModConfigSpec.BooleanValue ENABLE_SLOPE;
    public static final ModConfigSpec.BooleanValue ENABLE_STAIRS;
    public static final ModConfigSpec.BooleanValue ENABLE_TORCH;

    // Items
    public static final ModConfigSpec.BooleanValue ENABLE_HAMMER;
    public static final ModConfigSpec.BooleanValue ENABLE_CHISEL;
    public static final ModConfigSpec.BooleanValue ENABLE_TILE;
    public static final ModConfigSpec.IntValue TOOL_USES;
    public static final ModConfigSpec.BooleanValue TOOLS_DAMAGEABLE;

    // Features
    public static final ModConfigSpec.BooleanValue ENABLE_COVERS;
    public static final ModConfigSpec.BooleanValue ENABLE_OVERLAYS;
    public static final ModConfigSpec.BooleanValue ENABLE_SIDE_COVERS;
    public static final ModConfigSpec.BooleanValue ENABLE_DYE_COLORS;
    public static final ModConfigSpec.BooleanValue ENABLE_CHISEL_DESIGNS;
    public static final ModConfigSpec.BooleanValue ENABLE_OWNERSHIP;
    public static final ModConfigSpec.BooleanValue ENABLE_ILLUMINATION;
    public static final ModConfigSpec.BooleanValue ENABLE_TORCH_WEATHER_EFFECTS;
    public static final ModConfigSpec.BooleanValue ENABLE_ALPHA_PANES;
    public static final ModConfigSpec.BooleanValue ENABLE_FREESTANDING_LADDERS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> OVERLAY_ITEMS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> COVER_EXCEPTIONS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Blocks: turning one off removes it from the game (and from worlds that have it)").push("blocks");
        ENABLE_BARRIER = builder.define("enableBarrier", true);
        ENABLE_BED = builder.define("enableBed", true);
        ENABLE_BUTTON = builder.define("enableButton", true);
        ENABLE_COLLAPSIBLE_BLOCK = builder.define("enableCollapsibleBlock", true);
        ENABLE_DAYLIGHT_SENSOR = builder.define("enableDaylightSensor", true);
        ENABLE_DOOR = builder.define("enableDoor", true);
        ENABLE_FLOWER_POT = builder.define("enableFlowerPot", true);
        ENABLE_GARAGE_DOOR = builder.define("enableGarageDoor", true);
        ENABLE_GATE = builder.define("enableGate", true);
        ENABLE_HATCH = builder.define("enableHatch", true);
        ENABLE_LADDER = builder.define("enableLadder", true);
        ENABLE_LEVER = builder.define("enableLever", true);
        ENABLE_PRESSURE_PLATE = builder.define("enablePressurePlate", true);
        ENABLE_SAFE = builder.define("enableSafe", true);
        ENABLE_SLOPE = builder.define("enableSlope", true);
        ENABLE_STAIRS = builder.define("enableStairs", true);
        ENABLE_TORCH = builder.define("enableTorch", true);
        builder.pop();

        builder.push("items");
        ENABLE_HAMMER = builder.define("enableHammer", true);
        ENABLE_CHISEL = builder.define("enableChisel", true);
        ENABLE_TILE = builder.define("enableTile", true);
        TOOL_USES = builder.comment("Uses of the Carpenter's Hammer and Chisel").defineInRange("toolUses", 400, 1, 100000);
        TOOLS_DAMAGEABLE = builder.comment("Whether the Carpenter's Hammer and Chisel wear out").define("toolsDamageable", true);
        builder.pop();

        builder.push("features");
        ENABLE_COVERS = builder.define("enableCovers", true);
        ENABLE_OVERLAYS = builder.define("enableOverlays", true);
        ENABLE_SIDE_COVERS = builder.define("enableSideCovers", true);
        ENABLE_DYE_COLORS = builder.define("enableDyeColors", true);
        ENABLE_CHISEL_DESIGNS = builder.define("enableChiselDesigns", true);
        ENABLE_OWNERSHIP = builder.comment(
                "Prevents players besides you and server operators from editing your objects.",
                "This does not protect objects against destruction, and may allow activation if appropriate. The Carpenter's Safe is not affected.")
            .define("enableOwnership", true);
        ENABLE_ILLUMINATION = builder.comment("Lets players cover blocks with glowstone dust to make them give light")
            .define("enableIllumination", true);
        ENABLE_TORCH_WEATHER_EFFECTS = builder.comment("Whether torches go out in rain and snow")
            .define("enableTorchWeatherEffects", true);
        ENABLE_ALPHA_PANES = builder.comment("Whether panes (in doors, hatches, lanterns...) are drawn see-through, for translucent glass")
            .define("enableAlphaPanes", true);
        ENABLE_FREESTANDING_LADDERS = builder.comment("Whether wall ladders stay up when the block behind them is removed")
            .define("enableFreestandingLadders", false);
        OVERLAY_ITEMS = builder.comment(
                "Maps items to overlays, as item=overlay.",
                "Overlays are grass, snow, web, vine, hay and mycelium.")
            .defineListAllowEmpty("overlayItems", List.of(
                    "minecraft:wheat_seeds=grass",
                    "minecraft:snowball=snow",
                    "minecraft:string=web",
                    "minecraft:vine=vine",
                    "minecraft:wheat=hay",
                    "minecraft:brown_mushroom=mycelium",
                    "minecraft:red_mushroom=mycelium"),
                () -> "minecraft:item=grass", value -> value instanceof String string && string.contains("="));
        COVER_EXCEPTIONS = builder.comment("Blocks (by item id) allowed as covers although they are not full blocks")
            .defineListAllowEmpty("coverExceptions", List.of(), () -> "minecraft:item", value -> value instanceof String);
        builder.pop();

        SPEC = builder.build();
    }

    private CbConfig() {}

}
