package com.carpentersblocks.block.data;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Carpenter's Stairs data, ported from 1.7.10 (data/Stairs, util/stairs/StairsUtil and StairsTransform).
 * <p>
 * The block data is the stairs ID (0 to 27). Facings name the cut away side of the stairs:
 * {@link EnumFacing#UP} for stairs that step up (positive), {@link EnumFacing#DOWN} for upside down stairs.
 */
public class StairsData {

	public static final int ID_MASK = 0x1f;

	public static final int ID_SIDE_SE     = 0;
	public static final int ID_SIDE_NW     = 1;
	public static final int ID_SIDE_NE     = 2;
	public static final int ID_SIDE_SW     = 3;
	public static final int ID_NEG_N       = 4;
	public static final int ID_NEG_S       = 5;
	public static final int ID_NEG_W       = 6;
	public static final int ID_NEG_E       = 7;
	public static final int ID_POS_N       = 8;
	public static final int ID_POS_S       = 9;
	public static final int ID_POS_W       = 10;
	public static final int ID_POS_E       = 11;
	public static final int ID_INT_NEG_SE  = 12;
	public static final int ID_INT_NEG_NW  = 13;
	public static final int ID_INT_NEG_NE  = 14;
	public static final int ID_INT_NEG_SW  = 15;
	public static final int ID_INT_POS_SE  = 16;
	public static final int ID_INT_POS_NW  = 17;
	public static final int ID_INT_POS_NE  = 18;
	public static final int ID_INT_POS_SW  = 19;
	public static final int ID_EXT_NEG_SE  = 20;
	public static final int ID_EXT_NEG_NW  = 21;
	public static final int ID_EXT_NEG_NE  = 22;
	public static final int ID_EXT_NEG_SW  = 23;
	public static final int ID_EXT_POS_SE  = 24;
	public static final int ID_EXT_POS_NW  = 25;
	public static final int ID_EXT_POS_NE  = 26;
	public static final int ID_EXT_POS_SW  = 27;

	private static final int NUM_IDS = 28;

	public enum Type { SIDE, NORMAL, INTERIOR, EXTERIOR }

	private static final List<List<AxisAlignedBB>> BOXES = new ArrayList<List<AxisAlignedBB>>(NUM_IDS);
	private static final List<Set<EnumFacing>> FACINGS = new ArrayList<Set<EnumFacing>>(NUM_IDS);
	private static final List<Set<EnumFacing>> FULL_FACES = new ArrayList<Set<EnumFacing>>(NUM_IDS);

	static {
		// Boxes from 1.7.10 StairsUtil.genBounds
		add(new EnumFacing[] { SOUTH, EAST },       new EnumFacing[] { NORTH, WEST },       box(0,0,0,.5,1,1), box(.5,0,0,1,1,.5));
		add(new EnumFacing[] { NORTH, WEST },       new EnumFacing[] { SOUTH, EAST },       box(.5,0,0,1,1,1), box(0,0,.5,.5,1,1));
		add(new EnumFacing[] { NORTH, EAST },       new EnumFacing[] { SOUTH, WEST },       box(0,0,0,.5,1,1), box(.5,0,.5,1,1,1));
		add(new EnumFacing[] { SOUTH, WEST },       new EnumFacing[] { NORTH, EAST },       box(.5,0,0,1,1,1), box(0,0,0,.5,1,.5));
		add(new EnumFacing[] { DOWN, NORTH },       new EnumFacing[] { UP, SOUTH },         box(0,0,.5,1,1,1), box(0,.5,0,1,1,.5));
		add(new EnumFacing[] { DOWN, SOUTH },       new EnumFacing[] { UP, NORTH },         box(0,0,0,1,1,.5), box(0,.5,.5,1,1,1));
		add(new EnumFacing[] { DOWN, WEST },        new EnumFacing[] { UP, EAST },          box(.5,0,0,1,1,1), box(0,.5,0,.5,1,1));
		add(new EnumFacing[] { DOWN, EAST },        new EnumFacing[] { UP, WEST },          box(0,0,0,.5,1,1), box(.5,.5,0,1,1,1));
		add(new EnumFacing[] { UP, NORTH },         new EnumFacing[] { DOWN, SOUTH },       box(0,0,.5,1,1,1), box(0,0,0,1,.5,.5));
		add(new EnumFacing[] { UP, SOUTH },         new EnumFacing[] { DOWN, NORTH },       box(0,0,0,1,1,.5), box(0,0,.5,1,.5,1));
		add(new EnumFacing[] { UP, WEST },          new EnumFacing[] { DOWN, EAST },        box(.5,0,0,1,1,1), box(0,0,0,.5,.5,1));
		add(new EnumFacing[] { UP, EAST },          new EnumFacing[] { DOWN, WEST },        box(0,0,0,.5,1,1), box(.5,0,0,1,.5,1));
		add(new EnumFacing[] { DOWN, SOUTH, EAST }, new EnumFacing[] { UP, NORTH, WEST },   box(0,0,0,.5,1,1), box(.5,0,0,1,1,.5), box(.5,.5,.5,1,1,1));
		add(new EnumFacing[] { DOWN, NORTH, WEST }, new EnumFacing[] { UP, SOUTH, EAST },   box(.5,0,0,1,1,1), box(0,0,.5,.5,1,1), box(0,.5,0,.5,1,.5));
		add(new EnumFacing[] { DOWN, NORTH, EAST }, new EnumFacing[] { UP, SOUTH, WEST },   box(0,0,0,.5,1,1), box(.5,0,.5,1,1,1), box(.5,.5,0,1,1,.5));
		add(new EnumFacing[] { DOWN, SOUTH, WEST }, new EnumFacing[] { UP, NORTH, EAST },   box(.5,0,0,1,1,1), box(0,0,0,.5,1,.5), box(0,.5,.5,.5,1,1));
		add(new EnumFacing[] { UP, SOUTH, EAST },   new EnumFacing[] { DOWN, NORTH, WEST }, box(0,0,0,.5,1,1), box(.5,0,0,1,1,.5), box(.5,0,.5,1,.5,1));
		add(new EnumFacing[] { UP, NORTH, WEST },   new EnumFacing[] { DOWN, SOUTH, EAST }, box(.5,0,0,1,1,1), box(0,0,.5,.5,1,1), box(0,0,0,.5,.5,.5));
		add(new EnumFacing[] { UP, NORTH, EAST },   new EnumFacing[] { DOWN, SOUTH, WEST }, box(0,0,0,.5,1,1), box(.5,0,.5,1,1,1), box(.5,0,0,1,.5,.5));
		add(new EnumFacing[] { UP, SOUTH, WEST },   new EnumFacing[] { DOWN, NORTH, EAST }, box(.5,0,0,1,1,1), box(0,0,0,.5,1,.5), box(0,0,.5,.5,.5,1));
		add(new EnumFacing[] { DOWN, SOUTH, EAST }, new EnumFacing[] { UP },                box(0,0,0,.5,1,.5), box(.5,.5,0,1,1,1), box(0,.5,.5,.5,1,1));
		add(new EnumFacing[] { DOWN, NORTH, WEST }, new EnumFacing[] { UP },                box(.5,0,.5,1,1,1), box(0,.5,0,.5,1,1), box(.5,.5,0,1,1,.5));
		add(new EnumFacing[] { DOWN, NORTH, EAST }, new EnumFacing[] { UP },                box(0,0,.5,.5,1,1), box(.5,.5,0,1,1,1), box(0,.5,0,.5,1,.5));
		add(new EnumFacing[] { DOWN, SOUTH, WEST }, new EnumFacing[] { UP },                box(.5,0,0,1,1,.5), box(0,.5,0,.5,1,1), box(.5,.5,.5,1,1,1));
		add(new EnumFacing[] { UP, SOUTH, EAST },   new EnumFacing[] { DOWN },              box(0,0,0,.5,1,.5), box(.5,0,0,1,.5,1), box(0,0,.5,.5,.5,1));
		add(new EnumFacing[] { UP, NORTH, WEST },   new EnumFacing[] { DOWN },              box(.5,0,.5,1,1,1), box(0,0,0,.5,.5,1), box(.5,0,0,1,.5,.5));
		add(new EnumFacing[] { UP, NORTH, EAST },   new EnumFacing[] { DOWN },              box(0,0,.5,.5,1,1), box(.5,0,0,1,.5,1), box(0,0,0,.5,.5,.5));
		add(new EnumFacing[] { UP, SOUTH, WEST },   new EnumFacing[] { DOWN },              box(.5,0,0,1,1,.5), box(0,0,0,.5,.5,1), box(.5,0,.5,1,.5,1));
	}

	private static AxisAlignedBB box(double x0, double y0, double z0, double x1, double y1, double z1) {
		return new AxisAlignedBB(x0, y0, z0, x1, y1, z1);
	}

	private static void add(EnumFacing[] facings, EnumFacing[] fullFaces, AxisAlignedBB... boxes) {
		List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		Collections.addAll(list, boxes);
		BOXES.add(Collections.unmodifiableList(list));
		Set<EnumFacing> facingSet = EnumSet.noneOf(EnumFacing.class);
		Collections.addAll(facingSet, facings);
		FACINGS.add(Collections.unmodifiableSet(facingSet));
		Set<EnumFacing> fullFaceSet = EnumSet.noneOf(EnumFacing.class);
		Collections.addAll(fullFaceSet, fullFaces);
		FULL_FACES.add(Collections.unmodifiableSet(fullFaceSet));
	}

	/**
	 * Gets the stairs ID from block data, clamped to a valid ID.
	 */
	public static int getId(int cbMetadata) {
		int id = cbMetadata & ID_MASK;
		return id < NUM_IDS ? id : ID_POS_N;
	}

	public static List<AxisAlignedBB> getBoxes(int id) {
		return BOXES.get(id);
	}

	public static Set<EnumFacing> getFacings(int id) {
		return FACINGS.get(id);
	}

	/**
	 * Gets whether a face of the stairs is a full, solid face.
	 */
	public static boolean isFullFace(int id, EnumFacing facing) {
		return FULL_FACES.get(id).contains(facing);
	}

	public static boolean isPositive(int id) {
		return FACINGS.get(id).contains(UP);
	}

	public static Type getType(int id) {
		if (id < ID_NEG_N) {
			return Type.SIDE;
		} else if (id < ID_INT_NEG_SE) {
			return Type.NORMAL;
		} else if (id < ID_EXT_NEG_SE) {
			return Type.INTERIOR;
		}
		return Type.EXTERIOR;
	}

	/**
	 * Hammer left click: next direction within the same type (each group of 4 IDs).
	 */
	public static int getNextDirection(int id) {
		return (id & ~3) | ((id + 1) & 3);
	}

	/**
	 * Hammer right click: next type (side, normal, interior, exterior; up then upside down).
	 */
	public static int getNextType(int id) {
		switch (getType(id)) {
			case SIDE:
				return id + 8;
			case NORMAL:
			case INTERIOR:
				return isPositive(id) ? id - 4 : id + 12;
			default:
				return isPositive(id) ? id - 4 : ID_SIDE_SE;
		}
	}

	/**
	 * Rotates stairs about the Y axis (Archimedes' Ships support).
	 *
	 * @param id the stairs ID
	 * @param clockwise UP axis is clockwise, DOWN is counter clockwise
	 */
	public static int rotate(int id, boolean clockwise) {
		int angle = id % 4;
		if (clockwise) {
			return id + new int[] { 3, 1, -2, -2 }[angle];
		}
		return id + new int[] { 2, 2, -1, -3 }[angle];
	}

	/**
	 * Gets the stairs ID for a placement.
	 *
	 * @param side the clicked face
	 * @param hitX the hit x
	 * @param hitY the hit y
	 * @param hitZ the hit z
	 * @param playerFacing the horizontal direction the player looks at
	 */
	public static int getPlacementId(EnumFacing side, float hitX, float hitY, float hitZ, EnumFacing playerFacing) {
		if (side.getAxis().isVertical()) {
			boolean upsideDown = side == DOWN;
			switch (playerFacing) {
				case SOUTH:
					return upsideDown ? ID_NEG_N : ID_POS_N;
				case WEST:
					return upsideDown ? ID_NEG_E : ID_POS_E;
				case NORTH:
					return upsideDown ? ID_NEG_S : ID_POS_S;
				default:
					return upsideDown ? ID_NEG_W : ID_POS_W;
			}
		}
		// Normalize horizontal hit across the face
		float hit;
		switch (side) {
			case NORTH:
				hit = 1.0F - hitX;
				break;
			case WEST:
				hit = hitZ;
				break;
			case EAST:
				hit = 1.0F - hitZ;
				break;
			default:
				hit = hitX;
				break;
		}
		int sideIndex = side.getIndex();
		if (hitY > 0.5F && hit > 1.0F - hitY && hit < hitY) {
			return sideIndex + 2;
		} else if (hitY < 0.5F && hit < 1.0F - hitY && hit > hitY) {
			return sideIndex + 6;
		} else if (hit < 0.2F) {
			return side == NORTH ? ID_SIDE_NW : side == SOUTH ? ID_SIDE_SE : side == WEST ? ID_SIDE_SW : ID_SIDE_NE;
		} else if (hit > 0.8F) {
			return side == NORTH ? ID_SIDE_NE : side == SOUTH ? ID_SIDE_SW : side == WEST ? ID_SIDE_NW : ID_SIDE_SE;
		}
		return hitY > 0.5F ? sideIndex + 2 : sideIndex + 6;
	}

	/**
	 * Neighboring stairs IDs, or -1 where there are no Carpenter's Stairs.
	 */
	public static class Neighbors {
		public int xn = -1, xp = -1, yn = -1, yp = -1, zn = -1, zp = -1;
	}

	private static boolean has(int id, EnumFacing facing) {
		return id >= 0 && FACINGS.get(id).contains(facing);
	}

	private static int signed(boolean positive, int posId, int negId) {
		return positive ? posId : negId;
	}

	/**
	 * Adapts a newly placed stairs ID to its neighbors (1.7.10 StairsTransform.transformStairs).
	 */
	public static int transformStairs(int id, Neighbors n) {
		// Stack onto side stairs
		if (n.yn >= 0 && getType(n.yn) == Type.SIDE) {
			return n.yn;
		}
		if (n.yp >= 0 && getType(n.yp) == Type.SIDE) {
			return n.yp;
		}

		// Corner between two stairs
		if (n.zn >= 0) {
			if (n.xn >= 0) {
				boolean pos = isPositive(n.xn) && isPositive(n.zn);
				if (has(n.zn, WEST) && has(n.xn, NORTH)) return signed(pos, ID_INT_POS_NW, ID_INT_NEG_NW);
				if (has(n.zn, EAST) && has(n.xn, SOUTH)) return signed(pos, ID_EXT_POS_SE, ID_EXT_NEG_SE);
			}
			if (n.xp >= 0) {
				boolean pos = isPositive(n.xp) && isPositive(n.zn);
				if (has(n.zn, EAST) && has(n.xp, NORTH)) return signed(pos, ID_INT_POS_NE, ID_INT_NEG_NE);
				if (has(n.zn, WEST) && has(n.xp, SOUTH)) return signed(pos, ID_EXT_POS_SW, ID_EXT_NEG_SW);
			}
		}
		if (n.zp >= 0) {
			if (n.xn >= 0) {
				boolean pos = isPositive(n.xn) && isPositive(n.zp);
				if (has(n.zp, WEST) && has(n.xn, SOUTH)) return signed(pos, ID_INT_POS_SW, ID_INT_NEG_SW);
				if (has(n.zp, EAST) && has(n.xn, NORTH)) return signed(pos, ID_EXT_POS_NE, ID_EXT_NEG_NE);
			}
			if (n.xp >= 0) {
				boolean pos = isPositive(n.xp) && isPositive(n.zp);
				if (has(n.zp, EAST) && has(n.xp, SOUTH)) return signed(pos, ID_INT_POS_SE, ID_INT_NEG_SE);
				if (has(n.zp, WEST) && has(n.xp, NORTH)) return signed(pos, ID_EXT_POS_NW, ID_EXT_NEG_NW);
			}
		}

		// Corner against a single stairs
		if (n.xn >= 0 && !has(n.xn, EAST)) {
			boolean pos = isPositive(n.xn);
			if (has(id, WEST)) {
				if (has(n.xn, SOUTH)) return signed(pos, ID_INT_POS_SW, ID_INT_NEG_SW);
				if (has(n.xn, NORTH)) return signed(pos, ID_INT_POS_NW, ID_INT_NEG_NW);
			}
			if (has(id, EAST)) {
				if (has(n.xn, SOUTH)) return signed(pos, ID_EXT_POS_SE, ID_EXT_NEG_SE);
				if (has(n.xn, NORTH)) return signed(pos, ID_EXT_POS_NE, ID_EXT_NEG_NE);
			}
		}
		if (n.xp >= 0 && !has(n.xp, WEST)) {
			boolean pos = isPositive(n.xp);
			if (has(id, WEST)) {
				if (has(n.xp, SOUTH)) return signed(pos, ID_EXT_POS_SW, ID_EXT_NEG_SW);
				if (has(n.xp, NORTH)) return signed(pos, ID_EXT_POS_NW, ID_EXT_NEG_NW);
			}
			if (has(id, EAST)) {
				if (has(n.xp, SOUTH)) return signed(pos, ID_INT_POS_SE, ID_INT_NEG_SE);
				if (has(n.xp, NORTH)) return signed(pos, ID_INT_POS_NE, ID_INT_NEG_NE);
			}
		}
		if (n.zn >= 0 && !has(n.zn, SOUTH)) {
			boolean pos = isPositive(n.zn);
			if (has(id, NORTH)) {
				if (has(n.zn, EAST)) return signed(pos, ID_INT_POS_NE, ID_INT_NEG_NE);
				if (has(n.zn, WEST)) return signed(pos, ID_INT_POS_NW, ID_INT_NEG_NW);
			}
			if (has(id, SOUTH)) {
				if (has(n.zn, EAST)) return signed(pos, ID_EXT_POS_SE, ID_EXT_NEG_SE);
				if (has(n.zn, WEST)) return signed(pos, ID_EXT_POS_SW, ID_EXT_NEG_SW);
			}
		}
		if (n.zp >= 0 && !has(n.zp, NORTH)) {
			boolean pos = isPositive(n.zp);
			if (has(id, NORTH)) {
				if (has(n.zp, EAST)) return signed(pos, ID_EXT_POS_NE, ID_EXT_NEG_NE);
				if (has(n.zp, WEST)) return signed(pos, ID_EXT_POS_NW, ID_EXT_NEG_NW);
			}
			if (has(id, SOUTH)) {
				if (has(n.zp, EAST)) return signed(pos, ID_INT_POS_SE, ID_INT_NEG_SE);
				if (has(n.zp, WEST)) return signed(pos, ID_INT_POS_SW, ID_INT_NEG_SW);
			}
		}
		return id;
	}

	/**
	 * Gets new IDs for the horizontal neighbors of newly placed stairs
	 * (1.7.10 StairsTransform.transformAdjacentStairs). Unchanged neighbors keep their ID.
	 *
	 * @return the new neighbor IDs
	 */
	public static Neighbors transformAdjacentStairs(int id, Neighbors n) {
		Neighbors out = new Neighbors();
		out.xn = n.xn;
		out.xp = n.xp;
		out.zn = n.zn;
		out.zp = n.zp;
		boolean pos = isPositive(id);
		if (has(id, WEST)) {
			if (n.zn >= 0 && pos == isPositive(n.zn)) {
				if (has(n.zn, NORTH)) out.zn = signed(pos, ID_EXT_POS_NW, ID_EXT_NEG_NW);
				if (has(n.zn, SOUTH)) out.zn = signed(pos, ID_INT_POS_SW, ID_INT_NEG_SW);
			}
			if (n.zp >= 0 && pos == isPositive(n.zp)) {
				if (has(n.zp, SOUTH)) out.zp = signed(pos, ID_EXT_POS_SW, ID_EXT_NEG_SW);
				if (has(n.zp, NORTH)) out.zp = signed(pos, ID_INT_POS_NW, ID_INT_NEG_NW);
			}
		}
		if (has(id, EAST)) {
			if (n.zn >= 0 && pos == isPositive(n.zn)) {
				if (has(n.zn, NORTH)) out.zn = signed(pos, ID_EXT_POS_NE, ID_EXT_NEG_NE);
				if (has(n.zn, SOUTH)) out.zn = signed(pos, ID_INT_POS_SE, ID_INT_NEG_SE);
			}
			if (n.zp >= 0 && pos == isPositive(n.zp)) {
				if (has(n.zp, SOUTH)) out.zp = signed(pos, ID_EXT_POS_SE, ID_EXT_NEG_SE);
				if (has(n.zp, NORTH)) out.zp = signed(pos, ID_INT_POS_NE, ID_INT_NEG_NE);
			}
		}
		if (has(id, NORTH)) {
			if (n.xn >= 0 && pos == isPositive(n.xn)) {
				if (has(n.xn, WEST)) out.xn = signed(pos, ID_EXT_POS_NW, ID_EXT_NEG_NW);
				if (has(n.xn, EAST)) out.xn = signed(pos, ID_INT_POS_NE, ID_INT_NEG_NE);
			}
			if (n.xp >= 0 && pos == isPositive(n.xp)) {
				if (has(n.xp, EAST)) out.xp = signed(pos, ID_EXT_POS_NE, ID_EXT_NEG_NE);
				if (has(n.xp, WEST)) out.xp = signed(pos, ID_INT_POS_NW, ID_INT_NEG_NW);
			}
		}
		if (has(id, SOUTH)) {
			if (n.xn >= 0 && pos == isPositive(n.xn)) {
				if (has(n.xn, WEST)) out.xn = signed(pos, ID_EXT_POS_SW, ID_EXT_NEG_SW);
				if (has(n.xn, EAST)) out.xn = signed(pos, ID_INT_POS_SE, ID_INT_NEG_SE);
			}
			if (n.xp >= 0 && pos == isPositive(n.xp)) {
				if (has(n.xp, EAST)) out.xp = signed(pos, ID_EXT_POS_SE, ID_EXT_NEG_SE);
				if (has(n.xp, WEST)) out.xp = signed(pos, ID_INT_POS_SW, ID_INT_NEG_SW);
			}
		}
		return out;
	}

}
