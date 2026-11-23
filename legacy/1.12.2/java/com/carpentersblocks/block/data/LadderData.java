package com.carpentersblocks.block.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carpentersblocks.block.ShapePart;
import com.carpentersblocks.block.ShapeTextures;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Carpenter's Ladder data and shapes, ported from 1.7.10 (data/Ladder, BlockHandlerCarpentersLadder).
 * <p>
 * Data bits 0-2: direction. {@link #DIR_ON_X} and {@link #DIR_ON_Z} are freestanding ladders,
 * 2 to 5 are wall ladders facing that direction (attached to the block behind).
 * Data bits 3-6: type. Render data bits 16-17 hold the joins with adjacent ladders.
 */
public class LadderData {

	public static final int DIR_ON_X = 0;
	public static final int DIR_ON_Z = 1;

	public static final int TYPE_DEFAULT = 0;
	public static final int TYPE_RAIL    = 1;
	public static final int TYPE_POLE    = 2;
	private static final int NUM_TYPES   = 3;

	private static final int DIR_MASK = 0x7;
	private static final int TYPE_MASK = 0x78;
	private static final int TYPE_SHIFT = 3;

	/** Render data: joined with a ladder on the low side of the ladder width (in the south frame). */
	public static final int JOIN_LOW = 1 << 16;
	/** Render data: joined with a ladder on the high side of the ladder width (in the south frame). */
	public static final int JOIN_HIGH = 1 << 17;

	private static final double[][] RUNGS = { { 0.125, 0.1875 }, { 0.375, 0.4375 }, { 0.625, 0.6875 }, { 0.875, 0.9375 } };
	private static final double[][] RAIL_RUNGS = { { 0.0625, 0.1875 }, { 0.3125, 0.4375 }, { 0.5625, 0.6875 }, { 0.8125, 0.9375 } };

	public static int getDirection(int data) {
		int dir = data & DIR_MASK;
		return dir <= 5 ? dir : DIR_ON_X;
	}

	public static int setDirection(int data, int dir) {
		return (data & ~DIR_MASK) | dir;
	}

	public static int getType(int data) {
		int type = (data & TYPE_MASK) >> TYPE_SHIFT;
		return type < NUM_TYPES ? type : TYPE_DEFAULT;
	}

	public static int setType(int data, int type) {
		return (data & ~TYPE_MASK) | (type << TYPE_SHIFT);
	}

	public static int getNextType(int data, boolean forward) {
		int type = getType(data);
		type = forward ? (type + 1) % NUM_TYPES : (type + NUM_TYPES - 1) % NUM_TYPES;
		return setType(data, type);
	}

	public static boolean isFreestanding(int data) {
		return getDirection(data) < 2;
	}

	/**
	 * Gets the wall facing of a wall ladder.
	 */
	public static EnumFacing getFacing(int data) {
		return EnumFacing.byIndex(getDirection(data));
	}

	/**
	 * Gets the horizontal direction of the low side of the ladder width, used to find ladders to join.
	 */
	public static EnumFacing getLowSide(int data) {
		switch (getDirection(data)) {
			case DIR_ON_X:
				return EnumFacing.WEST;
			case DIR_ON_Z:
				return EnumFacing.NORTH;
			default:
				return lowSideForWall(getFacing(data));
		}
	}

	private static EnumFacing lowSideForWall(EnumFacing facing) {
		switch (facing) {
			case NORTH:
				return EnumFacing.EAST;
			case EAST:
				return EnumFacing.SOUTH;
			case WEST:
				return EnumFacing.NORTH;
			default: // SOUTH
				return EnumFacing.WEST;
		}
	}

	/**
	 * Gets the collision and outline box.
	 */
	public static AxisAlignedBB getBounds(int data) {
		switch (getDirection(data)) {
			case DIR_ON_X:
				return new AxisAlignedBB(0, 0, 0.375, 1, 1, 0.625);
			case DIR_ON_Z:
				return new AxisAlignedBB(0.375, 0, 0, 0.625, 1, 1);
			default:
				return rotate(new AxisAlignedBB(0, 0, 0, 1, 1, 0.1875), getFacing(data));
		}
	}

	/**
	 * Gets the boxes the ladder is drawn with.
	 */
	public static List<AxisAlignedBB> getBoxes(int renderData) {
		List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
		boolean joinLow = (renderData & JOIN_LOW) != 0;
		boolean joinHigh = (renderData & JOIN_HIGH) != 0;
		int dir = getDirection(renderData);
		switch (getType(renderData)) {
			case TYPE_RAIL:
				addRail(boxes, dir);
				break;
			case TYPE_POLE:
				addPole(boxes, dir);
				break;
			default:
				addDefault(boxes, dir, joinLow, joinHigh);
				break;
		}
		return Collections.unmodifiableList(boxes);
	}

	/**
	 * Gets the parts the ladder is drawn with: rail ladders have iron rails, like 1.7.10.
	 */
	public static List<ShapePart> getParts(int renderData) {
		List<AxisAlignedBB> boxes = getBoxes(renderData);
		// addRail adds the rails before the rungs
		int railCount = getType(renderData) != TYPE_RAIL ? 0 : isFreestanding(renderData) ? 8 : 4;
		List<ShapePart> parts = new ArrayList<ShapePart>(boxes.size());
		for (int idx = 0; idx < boxes.size(); ++idx) {
			parts.add(idx < railCount ? ShapePart.textured(boxes.get(idx), ShapeTextures.IRON_BLOCK) : ShapePart.covered(boxes.get(idx)));
		}
		return parts;
	}

	private static void addDefault(List<AxisAlignedBB> boxes, int dir, boolean joinLow, boolean joinHigh) {
		// Authored on X: freestanding ladders span z 0.375-0.625, wall ladders z 0-0.1875 in the south frame
		boolean freestanding = dir < 2;
		double z0 = freestanding ? 0.375 : 0.0;
		double z1 = freestanding ? 0.625 : 0.1875;
		double rungZ0 = freestanding ? 0.4375 : 0.0;
		double rungZ1 = freestanding ? 0.5625 : 0.1875;
		List<AxisAlignedBB> frame = new ArrayList<AxisAlignedBB>();
		if (!joinLow) {
			frame.add(new AxisAlignedBB(0, 0, z0, 0.125, 1, z1));
		}
		if (!joinHigh) {
			frame.add(new AxisAlignedBB(0.875, 0, z0, 1, 1, z1));
		}
		double x0 = joinLow ? 0.0 : 0.125;
		double x1 = joinHigh ? 1.0 : 0.875;
		for (double[] rung : RUNGS) {
			frame.add(new AxisAlignedBB(x0, rung[0], rungZ0, x1, rung[1], rungZ1));
		}
		addOriented(boxes, frame, dir);
	}

	private static void addRail(List<AxisAlignedBB> boxes, int dir) {
		List<AxisAlignedBB> frame = new ArrayList<AxisAlignedBB>();
		if (dir < 2) {
			// Authored on Z, rails on both sides of the rungs
			AxisAlignedBB[] rails = {
				new AxisAlignedBB(0.3125, 0, 0.1875, 0.375, 1, 0.25),
				new AxisAlignedBB(0.375, 0, 0.125, 0.4375, 1, 0.25),
				new AxisAlignedBB(0.625, 0, 0.1875, 0.6875, 1, 0.25),
				new AxisAlignedBB(0.5625, 0, 0.125, 0.625, 1, 0.25) };
			for (AxisAlignedBB rail : rails) {
				frame.add(rail);
				frame.add(rotate(rail, EnumFacing.NORTH));
			}
			for (double[] rung : RUNGS) {
				frame.add(new AxisAlignedBB(0.4375, rung[0], 0, 0.5625, rung[1], 1));
			}
			// Authored on Z, so swap for ladders on X
			for (AxisAlignedBB box : frame) {
				boxes.add(dir == DIR_ON_Z ? box : swapXZ(box));
			}
			return;
		}
		frame.add(new AxisAlignedBB(0.125, 0, 0.0625, 0.25, 1, 0.125));
		frame.add(new AxisAlignedBB(0.1875, 0, 0.125, 0.25, 1, 0.1875));
		frame.add(new AxisAlignedBB(0.75, 0, 0.0625, 0.875, 1, 0.125));
		frame.add(new AxisAlignedBB(0.75, 0, 0.125, 0.8125, 1, 0.1875));
		for (double[] rung : RAIL_RUNGS) {
			frame.add(new AxisAlignedBB(0, rung[0], 0, 1, rung[1], 0.0625));
		}
		addOriented(boxes, frame, dir);
	}

	private static void addPole(List<AxisAlignedBB> boxes, int dir) {
		if (dir < 2) {
			boxes.add(new AxisAlignedBB(0.375, 0, 0.375, 0.625, 1, 0.625));
			// Pegs authored on Z
			AxisAlignedBB[] pegs = {
				new AxisAlignedBB(0.4375, 0.125, 0.625, 0.5625, 0.1875, 0.9375),
				new AxisAlignedBB(0.4375, 0.1875, 0.875, 0.5625, 0.25, 0.9375),
				new AxisAlignedBB(0.4375, 0.75, 0.0625, 0.5625, 0.8125, 0.375),
				new AxisAlignedBB(0.4375, 0.8125, 0.0625, 0.5625, 0.875, 0.125) };
			for (AxisAlignedBB peg : pegs) {
				boxes.add(dir == DIR_ON_Z ? peg : swapXZ(peg));
			}
			return;
		}
		List<AxisAlignedBB> frame = new ArrayList<AxisAlignedBB>();
		for (double[] rung : RUNGS) {
			double y0 = rung[0];
			double y1 = rung[0] + 0.0625;
			frame.add(new AxisAlignedBB(0.125, y0, 0, 0.1875, y1, 0.125));
			frame.add(new AxisAlignedBB(0.8125, y0, 0, 0.875, y1, 0.125));
			frame.add(new AxisAlignedBB(0.125, y0, 0.125, 0.875, y1, 0.1875));
		}
		addOriented(boxes, frame, dir);
	}

	/**
	 * Adds boxes authored on X (freestanding) or in the south frame (wall).
	 */
	private static void addOriented(List<AxisAlignedBB> boxes, List<AxisAlignedBB> frame, int dir) {
		for (AxisAlignedBB box : frame) {
			switch (dir) {
				case DIR_ON_X:
					boxes.add(box);
					break;
				case DIR_ON_Z:
					boxes.add(swapXZ(box));
					break;
				default:
					boxes.add(rotate(box, EnumFacing.byIndex(dir)));
					break;
			}
		}
	}

	private static AxisAlignedBB swapXZ(AxisAlignedBB box) {
		return new AxisAlignedBB(box.minZ, box.minY, box.minX, box.maxZ, box.maxY, box.maxX);
	}

	/**
	 * Rotates a box authored in the south frame to a horizontal facing (1.7.10 rotateBounds).
	 */
	public static AxisAlignedBB rotate(AxisAlignedBB box, EnumFacing facing) {
		switch (facing) {
			case NORTH:
				return new AxisAlignedBB(1 - box.maxX, box.minY, 1 - box.maxZ, 1 - box.minX, box.maxY, 1 - box.minZ);
			case EAST:
				return new AxisAlignedBB(box.minZ, box.minY, 1 - box.maxX, box.maxZ, box.maxY, 1 - box.minX);
			case WEST:
				return new AxisAlignedBB(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX);
			default: // SOUTH
				return box;
		}
	}

}
