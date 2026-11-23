package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carpentersblocks.CarpentersBlocks;

import net.minecraft.util.ResourceLocation;

/**
 * Fixed textures used by {@link ShapePart}s. Safe to use on the server; the client stitches them.
 */
public class ShapeTextures {

	private static final List<ResourceLocation> _all = new ArrayList<ResourceLocation>();

	public static final ResourceLocation IRON_BLOCK = new ResourceLocation("minecraft", "blocks/iron_block");
	public static final ResourceLocation GOLD_BLOCK = new ResourceLocation("minecraft", "blocks/gold_block");
	public static final ResourceLocation DIAMOND_BLOCK = new ResourceLocation("minecraft", "blocks/diamond_block");
	public static final ResourceLocation EMERALD_BLOCK = new ResourceLocation("minecraft", "blocks/emerald_block");
	public static final ResourceLocation LAPIS_BLOCK = new ResourceLocation("minecraft", "blocks/lapis_block");
	public static final ResourceLocation REDSTONE_BLOCK = new ResourceLocation("minecraft", "blocks/redstone_block");
	public static final ResourceLocation WOOL_WHITE = new ResourceLocation("minecraft", "blocks/wool_colored_white");
	public static final ResourceLocation LEVER = mod("lever/lever");
	public static final ResourceLocation TORCH = mod("torch/torch");
	public static final ResourceLocation TORCH_HEAD_LIT = mod("torch/torch_head_lit");
	public static final ResourceLocation TORCH_HEAD_SMOLDERING = mod("torch/torch_head_smoldering");
	public static final ResourceLocation TORCH_HEAD_UNLIT = mod("torch/torch_head_unlit");
	public static final ResourceLocation LANTERN_GLASS = mod("torch/lantern_glass");
	public static final ResourceLocation DOOR_SCREEN_TALL = mod("door/door_screen_tall");
	public static final ResourceLocation DOOR_GLASS_TALL_TOP = mod("door/door_glass_tall_top");
	public static final ResourceLocation DOOR_GLASS_TALL_BOTTOM = mod("door/door_glass_tall_bottom");
	public static final ResourceLocation DOOR_GLASS_TOP = mod("door/door_glass_top");
	public static final ResourceLocation DOOR_FRENCH_GLASS_TOP = mod("door/door_french_glass_top");
	public static final ResourceLocation DOOR_FRENCH_GLASS_BOTTOM = mod("door/door_french_glass_bottom");
	public static final ResourceLocation HATCH_FRENCH_GLASS = mod("hatch/hatch_french_glass");
	public static final ResourceLocation HATCH_GLASS = mod("hatch/hatch_glass");
	public static final ResourceLocation HATCH_SCREEN = mod("hatch/hatch_screen");
	public static final ResourceLocation DAYLIGHT_SENSOR_GLASS_TOP = mod("daylightsensor/daylight_sensor_glass_top");
	public static final ResourceLocation SAFE_LIGHT = mod("safe/safe_light");
	public static final ResourceLocation FLOWER_POT = mod("flowerpot/flower_pot");
	public static final ResourceLocation FLOWER_POT_GLASS = mod("flowerpot/flower_pot_glass");
	public static final ResourceLocation BED_PILLOW = mod("bed/bed_pillow");
	public static final ResourceLocation GARAGE_GLASS = mod("garagedoor/glass");
	public static final ResourceLocation GARAGE_GLASS_TOP = mod("garagedoor/glass_top");

	private static ResourceLocation mod(String path) {
		ResourceLocation resourceLocation = new ResourceLocation(CarpentersBlocks.MOD_ID, "blocks/" + path);
		_all.add(resourceLocation);
		return resourceLocation;
	}

	/**
	 * Gets the mod textures to stitch into the block atlas.
	 */
	public static List<ResourceLocation> getModTextures() {
		return Collections.unmodifiableList(_all);
	}

}
