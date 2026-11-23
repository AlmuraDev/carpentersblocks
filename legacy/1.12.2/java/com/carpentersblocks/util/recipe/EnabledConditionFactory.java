package com.carpentersblocks.util.recipe;

import java.util.function.BooleanSupplier;

import com.carpentersblocks.util.registry.ConfigRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

/**
 * Recipe condition <code>carpentersblocks:enabled</code>, so recipes for blocks and items
 * turned off in the config are skipped instead of failing to load.
 * <p>
 * Usage: <code>"conditions": [ { "type": "carpentersblocks:enabled", "feature": "ladder" } ]</code>
 */
public class EnabledConditionFactory implements IConditionFactory {

	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json) {
		String feature = JsonUtils.getString(json, "feature");
		boolean enabled = isEnabled(feature);
		return () -> enabled;
	}

	private static boolean isEnabled(String feature) {
		switch (feature) {
			case "barrier":           return ConfigRegistry.enableBarrier;
			case "bed":               return ConfigRegistry.enableBed;
			case "button":            return ConfigRegistry.enableButton;
			case "chisel":            return ConfigRegistry.enableChisel;
			case "collapsible_block": return ConfigRegistry.enableCollapsibleBlock;
			case "daylight_sensor":   return ConfigRegistry.enableDaylightSensor;
			case "door":              return ConfigRegistry.enableDoor;
			case "flower_pot":        return ConfigRegistry.enableFlowerPot;
			case "garage_door":       return ConfigRegistry.enableGarageDoor;
			case "gate":              return ConfigRegistry.enableGate;
			case "hammer":            return ConfigRegistry.enableHammer;
			case "hatch":             return ConfigRegistry.enableHatch;
			case "ladder":            return ConfigRegistry.enableLadder;
			case "lever":             return ConfigRegistry.enableLever;
			case "pressure_plate":    return ConfigRegistry.enablePressurePlate;
			case "safe":              return ConfigRegistry.enableSafe;
			case "slope":             return ConfigRegistry.enableSlope;
			case "stairs":            return ConfigRegistry.enableStairs;
			case "tile":              return ConfigRegistry.enableTile;
			case "torch":             return ConfigRegistry.enableTorch;
			default:
				throw new JsonSyntaxException("Unknown Carpenter's Blocks feature '" + feature + "'");
		}
	}

}
