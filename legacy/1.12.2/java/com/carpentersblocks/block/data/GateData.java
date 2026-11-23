package com.carpentersblocks.block.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Carpenter's Gate data and shapes, ported from 1.7.10 (data/Gate, BlockHandlerCarpentersGate).
 * <p>
 * Data bits 0-3: type (same as {@link BarrierData}). Bit 4: opens toward negative side.
 * Bit 5: gate runs along Z. Bit 6: open. Render data bits 16-17: gate below, gate above.
 */
public class GateData {

	private static final int TYPE_MASK = 0xf;
	private static final int DIR_NEG_BIT = 0x10;
	private static final int ON_Z_BIT = 0x20;
	private static final int OPEN_BIT = 0x40;

	public static final int GATE_BELOW = 1 << 16;
	public static final int GATE_ABOVE = 1 << 17;
	public static final int INVENTORY = 1 << 18;

	public static int getType(int data) {
		return BarrierData.getType(data & TYPE_MASK);
	}

	public static int setType(int data, int type) {
		return (data & ~TYPE_MASK) | type;
	}

	public static boolean isOpen(int data) {
		return (data & OPEN_BIT) != 0;
	}

	public static int setOpen(int data, boolean open) {
		return open ? data | OPEN_BIT : data & ~OPEN_BIT;
	}

	public static boolean isOnZ(int data) {
		return (data & ON_Z_BIT) != 0;
	}

	public static int setOnZ(int data, boolean onZ) {
		return onZ ? data | ON_Z_BIT : data & ~ON_Z_BIT;
	}

	public static boolean opensNegative(int data) {
		return (data & DIR_NEG_BIT) != 0;
	}

	public static int setOpensNegative(int data, boolean negative) {
		return negative ? data | DIR_NEG_BIT : data & ~DIR_NEG_BIT;
	}

	/**
	 * Gets the rotation the shapes (authored running along Z, opening toward +X) are turned by.
	 */
	private static EnumFacing getRotation(int data) {
		if (isOnZ(data)) {
			return opensNegative(data) ? EnumFacing.NORTH : EnumFacing.SOUTH;
		}
		return opensNegative(data) ? EnumFacing.EAST : EnumFacing.WEST;
	}

	public static List<AxisAlignedBB> getBoxes(int renderData) {
		List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
		if ((renderData & INVENTORY) != 0) {
			boxes.add(new AxisAlignedBB(0, 0.3125, 0.4375, 0.125, 1, 0.5625));
			boxes.add(new AxisAlignedBB(0.875, 0.3125, 0.4375, 1, 1, 0.5625));
			boxes.add(new AxisAlignedBB(0.125, 0.5, 0.4375, 0.875, 0.9375, 0.5625));
			return Collections.unmodifiableList(boxes);
		}
		boolean below = (renderData & GATE_BELOW) != 0;
		boolean above = (renderData & GATE_ABOVE) != 0;
		boolean open = isOpen(renderData);
		List<AxisAlignedBB> frame = new ArrayList<AxisAlignedBB>();
		switch (getType(renderData)) {
			case BarrierData.TYPE_PICKET:
				addPicket(frame, open, below, above);
				break;
			case BarrierData.TYPE_SHADOWBOX:
				addShadowbox(frame, open, below, above);
				break;
			case BarrierData.TYPE_WALL:
				addWall(frame, open, above);
				break;
			default:
				addVanilla(frame, getType(renderData), open, below, above);
				break;
		}
		EnumFacing rotation = getRotation(renderData);
		for (AxisAlignedBB box : frame) {
			boxes.add(SidedUtil.rotate(box, rotation));
		}
		return Collections.unmodifiableList(boxes);
	}

	private static void addVanilla(List<AxisAlignedBB> boxes, int type, boolean open, boolean below, boolean above) {
		double yOff = type * 0.0625;
		double postY = below ? 0 : 0.3125 - yOff;
		double yA = below ? 0 : 0.375 - yOff;
		double yB = above ? 1 : 0.9375;
		boxes.add(new AxisAlignedBB(0.4375, postY, 0, 0.5625, 1, 0.125));
		boxes.add(new AxisAlignedBB(0.4375, postY, 0.875, 0.5625, 1, 1));
		if (type == BarrierData.TYPE_VANILLA_X3) {
			double y0 = below ? 0 : 0.1875;
			if (open) {
				boxes.add(new AxisAlignedBB(0.5625, y0, 0, 0.9375, yB, 0.125));
				boxes.add(new AxisAlignedBB(0.5625, y0, 0.875, 0.9375, yB, 1));
			} else {
				boxes.add(new AxisAlignedBB(0.4375, y0, 0.125, 0.5625, yB, 0.875));
			}
			return;
		}
		if (open) {
			boxes.add(new AxisAlignedBB(0.8125, yA, 0, 0.9375, yB, 0.125));
			boxes.add(new AxisAlignedBB(0.8125, yA, 0.875, 0.9375, yB, 1));
			for (double[] z : new double[][] { { 0, 0.125 }, { 0.875, 1 } }) {
				boxes.add(new AxisAlignedBB(0.5625, 0.375 - yOff, z[0], 0.8125, 0.5625, z[1]));
				boxes.add(new AxisAlignedBB(0.5625, 0.75 - yOff, z[0], 0.8125, 0.9375, z[1]));
			}
		} else {
			boxes.add(new AxisAlignedBB(0.4375, yA, 0.375, 0.5625, yB, 0.5));
			boxes.add(new AxisAlignedBB(0.4375, yA, 0.5, 0.5625, yB, 0.625));
			for (double[] z : new double[][] { { 0.625, 0.875 }, { 0.125, 0.375 } }) {
				boxes.add(new AxisAlignedBB(0.4375, 0.375 - yOff, z[0], 0.5625, 0.5625, z[1]));
				boxes.add(new AxisAlignedBB(0.4375, 0.75 - yOff, z[0], 0.5625, 0.9375, z[1]));
			}
		}
	}

	private static void addPicket(List<AxisAlignedBB> boxes, boolean open, boolean below, boolean above) {
		double yMin = below ? 0 : 0.0625;
		double yShort = above ? 1 : 0.8125;
		double yTall = above ? 1 : 0.875;
		if (open) {
			for (double[] z : new double[][] { { 0.0625, 0.1875 }, { 0.8125, 0.9375 } }) {
				if (!above) {
					boxes.add(new AxisAlignedBB(0.5, 0.625, z[0], 1, 0.6875, z[1]));
				}
				if (!below) {
					boxes.add(new AxisAlignedBB(0.5, 0.1875, z[0], 1, 0.25, z[1]));
				}
			}
			for (double[] z : new double[][] { { 0, 0.0625 }, { 0.1875, 0.25 }, { 0.75, 0.8125 }, { 0.9375, 1 } }) {
				boxes.add(new AxisAlignedBB(0.5, yMin, z[0], 0.5625, yShort, z[1]));
				boxes.add(new AxisAlignedBB(0.6875, yMin, z[0], 0.8125, yTall, z[1]));
				boxes.add(new AxisAlignedBB(0.9375, yMin, z[0], 1, yTall, z[1]));
			}
			return;
		}
		if (!above) {
			boxes.add(new AxisAlignedBB(0.4375, 0.625, 0, 0.5625, 0.6875, 1));
		}
		if (!below) {
			boxes.add(new AxisAlignedBB(0.4375, 0.1875, 0, 0.5625, 0.25, 1));
		}
		for (double[] x : new double[][] { { 0.5625, 0.625 }, { 0.375, 0.4375 } }) {
			for (double[] z : new double[][] { { 0, 0.0625 }, { 0.9375, 1 } }) {
				boxes.add(new AxisAlignedBB(x[0], yMin, z[0], x[1], yShort, z[1]));
			}
			for (double[] z : new double[][] { { 0.1875, 0.3125 }, { 0.4375, 0.5625 }, { 0.6875, 0.8125 } }) {
				boxes.add(new AxisAlignedBB(x[0], yMin, z[0], x[1], yTall, z[1]));
			}
		}
	}

	private static void addShadowbox(List<AxisAlignedBB> boxes, boolean open, boolean below, boolean above) {
		if (open) {
			for (double[] z : new double[][] { { 0.0625, 0.1875 }, { 0.8125, 0.9375 } }) {
				if (!above) {
					boxes.add(new AxisAlignedBB(0.5, 0.75, z[0], 1, 0.875, z[1]));
				}
				if (!below) {
					boxes.add(new AxisAlignedBB(0.5, 0.125, z[0], 1, 0.25, z[1]));
				}
			}
			boxes.add(new AxisAlignedBB(0.5, 0, 0, 0.75, 1, 0.0625));
			boxes.add(new AxisAlignedBB(0.75, 0, 0.1875, 1, 1, 0.25));
			boxes.add(new AxisAlignedBB(0.5, 0, 0.75, 0.75, 1, 0.8125));
			boxes.add(new AxisAlignedBB(0.75, 0, 0.9375, 1, 1, 1));
			return;
		}
		if (!above) {
			boxes.add(new AxisAlignedBB(0.4375, 0.75, 0, 0.5625, 0.875, 1));
		}
		if (!below) {
			boxes.add(new AxisAlignedBB(0.4375, 0.125, 0, 0.5625, 0.25, 1));
		}
		boxes.add(new AxisAlignedBB(0.5625, 0, 0, 0.625, 1, 0.25));
		boxes.add(new AxisAlignedBB(0.375, 0, 0.25, 0.4375, 1, 0.5));
		boxes.add(new AxisAlignedBB(0.5625, 0, 0.5, 0.625, 1, 0.75));
		boxes.add(new AxisAlignedBB(0.375, 0, 0.75, 0.4375, 1, 1));
	}

	private static void addWall(List<AxisAlignedBB> boxes, boolean open, boolean above) {
		double yMax = above ? 1 : 0.8125;
		if (open) {
			boxes.add(new AxisAlignedBB(0.5, 0, 0, 1, yMax, 0.125));
			boxes.add(new AxisAlignedBB(0.5, 0, 0.875, 1, yMax, 1));
		} else {
			boxes.add(new AxisAlignedBB(0.4375, 0, 0, 0.5625, yMax, 1));
		}
	}

	/**
	 * Gets the footprint: thin for vanilla gates and walls, wider for the others.
	 */
	private static AxisAlignedBB getFootprint(int data, double height) {
		int type = getType(data);
		double half = type == BarrierData.TYPE_VANILLA || type == BarrierData.TYPE_WALL ? 0.0625 : 0.125;
		return isOnZ(data)
			? new AxisAlignedBB(0.5 - half, 0, 0, 0.5 + half, height, 1)
			: new AxisAlignedBB(0, 0, 0.5 - half, 1, height, 0.5 + half);
	}

	/**
	 * Gets the collision box: none when open, 1.5 blocks high when closed.
	 */
	public static List<AxisAlignedBB> getCollisionBoxes(int data) {
		return isOpen(data) ? Collections.<AxisAlignedBB>emptyList() : Collections.singletonList(getFootprint(data, 1.5));
	}

	public static AxisAlignedBB getSelectionBox(int data) {
		return getFootprint(data, 1.0);
	}

}
