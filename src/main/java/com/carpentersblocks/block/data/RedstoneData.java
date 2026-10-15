package com.carpentersblocks.block.data;

import net.minecraft.util.EnumFacing;

/**
 * Data shared by buttons and levers (1.7.10 data/Button, data/Lever).
 * <p>
 * Bits 0-2: attach side. Bit 3: on. Bit 4: inverted polarity. Bit 6 (levers): handle along Z.
 */
public class RedstoneData {

	private static final int SIDE_MASK = 0x7;
	private static final int ON_BIT = 0x8;
	private static final int POLARITY_BIT = 0x10;
	private static final int AXIS_Z_BIT = 0x40;

	public static EnumFacing getSide(int data) {
		int side = data & SIDE_MASK;
		return side <= 5 ? EnumFacing.byIndex(side) : EnumFacing.UP;
	}

	public static int setSide(int data, EnumFacing side) {
		return (data & ~SIDE_MASK) | side.getIndex();
	}

	public static boolean isOn(int data) {
		return (data & ON_BIT) != 0;
	}

	public static int setOn(int data, boolean on) {
		return on ? data | ON_BIT : data & ~ON_BIT;
	}

	public static boolean isInverted(int data) {
		return (data & POLARITY_BIT) != 0;
	}

	public static int togglePolarity(int data) {
		return data ^ POLARITY_BIT;
	}

	public static boolean isAxisZ(int data) {
		return (data & AXIS_Z_BIT) != 0;
	}

	public static int setAxisZ(int data, boolean axisZ) {
		return axisZ ? data | AXIS_Z_BIT : data & ~AXIS_Z_BIT;
	}

	/**
	 * Gets the output: 15 when on, or when off with inverted polarity.
	 */
	public static int getPowerOutput(int data) {
		return isOn(data) != isInverted(data) ? 15 : 0;
	}

}
