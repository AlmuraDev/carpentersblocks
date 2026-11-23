package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carpentersblocks.CarpentersBlocks;

import net.minecraft.resources.Identifier;

/**
 * Fixed textures used by {@link ShapePart}s. Safe to use on the server. Block atlas names (26.3 puts every textures/block file in the atlas).
 */
public class ShapeTextures {

    private static final List<Identifier> _all = new ArrayList<Identifier>();

    public static final Identifier IRON_BLOCK = Identifier.fromNamespaceAndPath("minecraft", "block/iron_block");
    public static final Identifier GOLD_BLOCK = Identifier.fromNamespaceAndPath("minecraft", "block/gold_block");
    public static final Identifier DIAMOND_BLOCK = Identifier.fromNamespaceAndPath("minecraft", "block/diamond_block");
    public static final Identifier EMERALD_BLOCK = Identifier.fromNamespaceAndPath("minecraft", "block/emerald_block");
    public static final Identifier LAPIS_BLOCK = Identifier.fromNamespaceAndPath("minecraft", "block/lapis_block");
    public static final Identifier REDSTONE_BLOCK = Identifier.fromNamespaceAndPath("minecraft", "block/redstone_block");
    public static final Identifier WOOL_WHITE = Identifier.fromNamespaceAndPath("minecraft", "block/white_wool");
    public static final Identifier LEVER = mod("lever/lever");
    public static final Identifier TORCH = mod("torch/torch");
    public static final Identifier TORCH_HEAD_LIT = mod("torch/torch_head_lit");
    public static final Identifier TORCH_HEAD_SMOLDERING = mod("torch/torch_head_smoldering");
    public static final Identifier TORCH_HEAD_UNLIT = mod("torch/torch_head_unlit");
    public static final Identifier LANTERN_GLASS = mod("torch/lantern_glass");
    public static final Identifier DOOR_SCREEN_TALL = mod("door/door_screen_tall");
    public static final Identifier DOOR_GLASS_TALL_TOP = mod("door/door_glass_tall_top");
    public static final Identifier DOOR_GLASS_TALL_BOTTOM = mod("door/door_glass_tall_bottom");
    public static final Identifier DOOR_GLASS_TOP = mod("door/door_glass_top");
    public static final Identifier DOOR_FRENCH_GLASS_TOP = mod("door/door_french_glass_top");
    public static final Identifier DOOR_FRENCH_GLASS_BOTTOM = mod("door/door_french_glass_bottom");
    public static final Identifier HATCH_FRENCH_GLASS = mod("hatch/hatch_french_glass");
    public static final Identifier HATCH_GLASS = mod("hatch/hatch_glass");
    public static final Identifier HATCH_SCREEN = mod("hatch/hatch_screen");
    public static final Identifier DAYLIGHT_SENSOR_GLASS_TOP = mod("daylightsensor/daylight_sensor_glass_top");
    public static final Identifier SAFE_LIGHT = mod("safe/safe_light");
    public static final Identifier FLOWER_POT = mod("flowerpot/flower_pot");
    public static final Identifier FLOWER_POT_GLASS = mod("flowerpot/flower_pot_glass");
    public static final Identifier BED_PILLOW = mod("bed/bed_pillow");
    public static final Identifier GARAGE_GLASS = mod("garagedoor/glass");
    public static final Identifier GARAGE_GLASS_TOP = mod("garagedoor/glass_top");

    private static Identifier mod(String path) {
        Identifier resourceLocation = Identifier.fromNamespaceAndPath(CarpentersBlocks.MOD_ID, "block/" + path);
        _all.add(resourceLocation);
        return resourceLocation;
    }

    /**
     * Gets the mod textures to stitch into the block atlas.
     */
    public static List<Identifier> getModTextures() {
        return Collections.unmodifiableList(_all);
    }

}
