package com.carpentersblocks.block.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Carpenter's Barrier data and shapes, ported from 1.7.10 (data/Barrier, BlockHandlerCarpentersBarrier).
 * <p>
 * Data bits 0-3: type. Bit 4: forced post.
 * Render data adds the connections and neighbors the shape depends on (bits 16 and up).
 */
public class BarrierData {

	public static final int TYPE_VANILLA    = 0;
	public static final int TYPE_VANILLA_X1 = 1;
	public static final int TYPE_VANILLA_X2 = 2;
	public static final int TYPE_VANILLA_X3 = 3;
	public static final int TYPE_PICKET     = 4;
	public static final int TYPE_SHADOWBOX  = 5;
	public static final int TYPE_WALL       = 6;

	private static final int TYPE_MASK = 0xf;
	private static final int POST_BIT = 0x10;

	/** Render data: connected on a horizontal side, shifted by {@link #sideIndex}. */
	private static final int CONNECT_SHIFT = 16;
	/** Render data: the neighbor one block up on that side is connectable, shifted by {@link #sideIndex}. */
	private static final int CONNECT_UP_SHIFT = 20;
	public static final int CONNECT_ABOVE = 1 << 24;
	public static final int BARRIER_BELOW = 1 << 25;
	public static final int BARRIER_ABOVE = 1 << 26;
	public static final int SHOW_POST     = 1 << 27;
	public static final int INVENTORY     = 1 << 28;

	public static int getType(int data) {
		int type = data & TYPE_MASK;
		return type <= TYPE_WALL ? type : TYPE_VANILLA;
	}

	public static int setType(int data, int type) {
		return (data & ~TYPE_MASK) | type;
	}

	public static boolean isVanilla(int data) {
		return getType(data) <= TYPE_VANILLA_X3;
	}

	public static boolean hasPostBit(int data) {
		return (data & POST_BIT) != 0;
	}

	public static int togglePost(int data) {
		return data ^ POST_BIT;
	}

	/**
	 * Hammer right click: vanilla, picket, shadowbox, wall.
	 */
	public static int getNextType(int data) {
		switch (getType(data)) {
			case TYPE_PICKET:
				return setType(data, TYPE_SHADOWBOX);
			case TYPE_SHADOWBOX:
				return setType(data, TYPE_WALL);
			case TYPE_WALL:
				return setType(data, TYPE_VANILLA);
			default:
				return setType(data, TYPE_PICKET);
		}
	}

	/**
	 * Hammer sneak right click: cycles the vanilla fence variants.
	 */
	public static int getNextVanillaType(int data) {
		if (!isVanilla(data)) {
			return data;
		}
		return setType(data, (getType(data) + 1) % (TYPE_VANILLA_X3 + 1));
	}

	public static int sideIndex(EnumFacing side) {
		return side.getHorizontalIndex();
	}

	public static int connect(EnumFacing side) {
		return 1 << (CONNECT_SHIFT + sideIndex(side));
	}

	public static int connectUp(EnumFacing side) {
		return 1 << (CONNECT_UP_SHIFT + sideIndex(side));
	}

	private static boolean isConnected(int renderData, EnumFacing side) {
		return (renderData & connect(side)) != 0;
	}

	private static boolean has(int renderData, int flag) {
		return (renderData & flag) != 0;
	}

	/**
	 * Gets whether the post is drawn, from the barrier's own data and connections
	 * (the caller also checks barriers stacked above and below).
	 *
	 * @param renderData the render data, with connections set
	 * @param circuitAbove whether a torch or similar sits on top
	 */
	public static boolean hasPost(int renderData, boolean circuitAbove) {
		if (hasPostBit(renderData) || circuitAbove) {
			return true;
		}
		boolean xn = isConnected(renderData, EnumFacing.WEST);
		boolean xp = isConnected(renderData, EnumFacing.EAST);
		boolean zn = isConnected(renderData, EnumFacing.NORTH);
		boolean zp = isConnected(renderData, EnumFacing.SOUTH);
		boolean pathOnX = xn && xp;
		boolean pathOnZ = zn && zp;
		// Only a straight run hides the post
		return pathOnX == pathOnZ || ((xn || xp) && (zn || zp));
	}

	/**
	 * Gets the boxes the barrier is drawn with.
	 */
	public static List<AxisAlignedBB> getBoxes(int renderData) {
		List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
		if (has(renderData, INVENTORY)) {
			boxes.add(new AxisAlignedBB(0.125, 0, 0.375, 0.375, 1, 0.625));
			boxes.add(new AxisAlignedBB(0.625, 0, 0.375, 0.875, 1, 0.625));
			boxes.add(new AxisAlignedBB(0, 0.8125, 0.4375, 1, 0.9375, 0.5625));
			boxes.add(new AxisAlignedBB(0, 0.4375, 0.4375, 1, 0.5625, 0.5625));
			return Collections.unmodifiableList(boxes);
		}
		int type = getType(renderData);
		boolean showPost = has(renderData, SHOW_POST);
		switch (type) {
			case TYPE_PICKET:
				addPicket(boxes, renderData, showPost);
				break;
			case TYPE_SHADOWBOX:
				addShadowbox(boxes, renderData, showPost);
				break;
			case TYPE_WALL:
				addWall(boxes, renderData, showPost);
				break;
			default:
				addVanilla(boxes, renderData, type, showPost);
				break;
		}
		return Collections.unmodifiableList(boxes);
	}

	private static void addVanilla(List<AxisAlignedBB> boxes, int renderData, int type, boolean showPost) {
		if (showPost) {
			boxes.add(new AxisAlignedBB(0.375, 0, 0.375, 0.625, 1, 0.625));
		}
		double offset = type * 0.0625;
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			if (!isConnected(renderData, side)) {
				continue;
			}
			if (type == TYPE_VANILLA_X3) {
				addPlank(boxes, renderData, side, 0.0625, 0.1875, 0.9375, true);
			} else {
				addPlank(boxes, renderData, side, 0.0625, 0.75 - offset, 0.9375, true);
				addPlank(boxes, renderData, side, 0.0625, 0.375 - offset, 0.5625, true);
			}
		}
	}

	private static void addPicket(List<AxisAlignedBB> boxes, int renderData, boolean showPost) {
		if (showPost) {
			boxes.add(new AxisAlignedBB(0.4375, 0, 0.4375, 0.5625, 1, 0.5625));
		}
		double yMin = has(renderData, BARRIER_BELOW) ? 0.0 : 0.0625;
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			if (isConnected(renderData, side)) {
				boolean tall = has(renderData, connectUp(side)) && has(renderData, CONNECT_ABOVE);
				double inner = tall ? 1.0 : 0.875;
				double outer = tall ? 1.0 : 0.8125;
				addArm(boxes, side, new AxisAlignedBB(0.5625, yMin, 0.6875, 0.625, inner, 0.8125));
				addArm(boxes, side, new AxisAlignedBB(0.375, yMin, 0.6875, 0.4375, inner, 0.8125));
				addArm(boxes, side, new AxisAlignedBB(0.5625, yMin, 0.9375, 0.625, outer, 1.0));
				addArm(boxes, side, new AxisAlignedBB(0.375, yMin, 0.9375, 0.4375, outer, 1.0));
				addPlank(boxes, renderData, side, 0.0625, 0.625, 0.6875, false);
				addPlank(boxes, renderData, side, 0.0625, 0.1875, 0.25, false);
			} else {
				addArm(boxes, side, new AxisAlignedBB(0.4375, yMin, 0.5625, 0.5625, 1.0, 0.625));
			}
		}
	}

	private static void addShadowbox(List<AxisAlignedBB> boxes, int renderData, boolean showPost) {
		if (showPost) {
			boxes.add(new AxisAlignedBB(0.4375, 0, 0.4375, 0.5625, 1, 0.5625));
		}
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			if (isConnected(renderData, side)) {
				addPlank(boxes, renderData, side, 0.0625, 0.75, 0.875, false);
				addPlank(boxes, renderData, side, 0.0625, 0.125, 0.25, false);
				addArm(boxes, side, new AxisAlignedBB(0.5625, 0, 0.5, 0.625, 1, 0.75));
				addArm(boxes, side, new AxisAlignedBB(0.375, 0, 0.75, 0.4375, 1, 1));
			}
		}
	}

	private static void addWall(List<AxisAlignedBB> boxes, int renderData, boolean showPost) {
		if (showPost) {
			boxes.add(new AxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 0.75));
		}
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			if (isConnected(renderData, side)) {
				boolean tall = has(renderData, connectUp(side)) && has(renderData, CONNECT_ABOVE);
				addArm(boxes, side, new AxisAlignedBB(0.3125, 0, 0.5, 0.6875, tall ? 1.0 : 0.8125, 1));
			}
		}
	}

	/**
	 * Adds a rail from the center to a side. Top rails are left out under a connected barrier
	 * and bottom rails over a barrier, so stacked barriers merge (unless enforced).
	 */
	private static void addPlank(List<AxisAlignedBB> boxes, int renderData, EnumFacing side, double radius, double yMin, double yMax, boolean enforce) {
		boolean isTop = yMax - 0.5 > 0.5 - yMin;
		if (!enforce) {
			if (isTop && has(renderData, connectUp(side)) && has(renderData, BARRIER_ABOVE)) {
				return;
			}
			if (!isTop && has(renderData, BARRIER_BELOW)) {
				return;
			}
		}
		addArm(boxes, side, new AxisAlignedBB(0.5 - radius, yMin, 0.5, 0.5 + radius, yMax, 1));
	}

	/**
	 * Adds a box authored toward south (z = 1), turned to the side.
	 */
	private static void addArm(List<AxisAlignedBB> boxes, EnumFacing side, AxisAlignedBB box) {
		boxes.add(LadderData.rotate(box, side));
	}

	/**
	 * Gets the collision box: 1.5 blocks high like vanilla fences, the same for all types.
	 */
	public static List<AxisAlignedBB> getCollisionBoxes(int renderData) {
		boolean zn = isConnected(renderData, EnumFacing.NORTH);
		boolean zp = isConnected(renderData, EnumFacing.SOUTH);
		boolean xn = isConnected(renderData, EnumFacing.WEST);
		boolean xp = isConnected(renderData, EnumFacing.EAST);
		List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>(2);
		if (zn || zp) {
			boxes.add(new AxisAlignedBB(0.375, 0, zn ? 0 : 0.375, 0.625, 1.5, zp ? 1 : 0.625));
		}
		if (xn || xp || !(zn || zp)) {
			boxes.add(new AxisAlignedBB(xn ? 0 : 0.375, 0, 0.375, xp ? 1 : 0.625, 1.5, 0.625));
		}
		return boxes;
	}

	/**
	 * Gets the outline and click box.
	 */
	public static AxisAlignedBB getSelectionBox(int renderData) {
		boolean zn = isConnected(renderData, EnumFacing.NORTH);
		boolean zp = isConnected(renderData, EnumFacing.SOUTH);
		boolean xn = isConnected(renderData, EnumFacing.WEST);
		boolean xp = isConnected(renderData, EnumFacing.EAST);
		double low = isVanilla(renderData) ? 0.375 : 0.25;
		double high = 1.0 - low;
		double x0 = low, x1 = high, z0 = low, z1 = high;
		if (!isVanilla(renderData)) {
			// Straight runs are thinner
			if (zn && zp && !xn && !xp) {
				x0 = 0.3125;
				x1 = 0.6875;
			} else if (xn && xp && !zn && !zp) {
				z0 = 0.3125;
				z1 = 0.6875;
			}
		}
		return new AxisAlignedBB(xn ? 0 : x0, 0, zn ? 0 : z0, xp ? 1 : x1, 1, zp ? 1 : z1);
	}

}
