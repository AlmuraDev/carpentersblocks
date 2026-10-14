package com.carpentersblocks.util.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.carpentersblocks.block.data.SlopeData.Type;
import com.carpentersblocks.util.RotationUtil.Rotation;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Builds stepped collision boxes for slopes.
 * <p>
 * Each slope type is described in its unrotated form (as drawn by BakedSlope), sampled on a
 * voxel grid, rotated the same way the model is rotated, and merged into boxes. Steps are
 * 1/{@value #RES} block high, so entities walk up slopes like stairs.
 */
public class SlopeShape {

	/** Voxels per block side. */
	private static final int RES = 8;

	private static final Map<Integer, List<AxisAlignedBB>> CACHE = new ConcurrentHashMap<Integer, List<AxisAlignedBB>>();

	/**
	 * Gets collision boxes for a slope, relative to the block position.
	 *
	 * @param type the slope type
	 * @param rotation the slope rotation
	 * @return the boxes
	 */
	public static List<AxisAlignedBB> getBoxes(Type type, Rotation rotation) {
		int key = type.ordinal() << 8 | rotation.ordinal();
		List<AxisAlignedBB> boxes = CACHE.get(key);
		if (boxes == null) {
			boxes = Collections.unmodifiableList(buildBoxes(type, rotation));
			CACHE.put(key, boxes);
		}
		return boxes;
	}

	private static List<AxisAlignedBB> buildBoxes(Type type, Rotation rotation) {
		boolean[][][] solid = new boolean[RES][RES][RES];
		for (int x = 0; x < RES; ++x) {
			for (int y = 0; y < RES; ++y) {
				for (int z = 0; z < RES; ++z) {
					double cx = (x + 0.5D) / RES;
					double cy = (y + 0.5D) / RES;
					double cz = (z + 0.5D) / RES;
					if (isInside(type, cx, cy, cz)) {
						double[] rot = rotate(cx, cy, cz, rotation);
						solid[toIndex(rot[0])][toIndex(rot[1])][toIndex(rot[2])] = true;
					}
				}
			}
		}
		return mergeBoxes(solid);
	}

	private static int toIndex(double coord) {
		return Math.min(RES - 1, Math.max(0, (int) Math.floor(coord * RES)));
	}

	/**
	 * Whether a point lies inside the unrotated slope.
	 */
	private static boolean isInside(Type type, double x, double y, double z) {
		switch (type) {
			case WEDGE:
				return y <= 1.0D - z;
			case WEDGE_INTERIOR:
				return y <= 1.0D - Math.min(x, z);
			case WEDGE_EXTERIOR:
				return y <= 1.0D - Math.max(x, z);
			case OBLIQUE_INTERIOR:
				return y <= 2.0D - x - z;
			case OBLIQUE_EXTERIOR:
				return y <= 1.0D - x - z;
			case PRISM_WEDGE:
				// Wedge with a half height ridge running to the low side
				return y <= 1.0D - z || y <= Math.min(x, 1.0D - x);
			case PRISM:
				return y <= getPrismHeight(x, z, false, false, false, false);
			case PRISM_1P:
				return y <= getPrismHeight(x, z, false, true, false, false);
			case PRISM_2P:
				return y <= getPrismHeight(x, z, true, true, false, false);
			case PRISM_3P:
				return y <= getPrismHeight(x, z, false, true, true, true);
			case PRISM_4P:
				return y <= getPrismHeight(x, z, true, true, true, true);
			case INVERT_PRISM:
				return y <= 1.0D - getPrismHeight(x, z, false, false, false, false);
			case INVERT_PRISM_1P:
				return y <= 1.0D - getPrismHeight(x, z, false, true, false, false);
			case INVERT_PRISM_2P:
				return y <= 1.0D - getPrismHeight(x, z, true, true, false, false);
			case INVERT_PRISM_3P:
				return y <= 1.0D - getPrismHeight(x, z, false, true, true, true);
			case INVERT_PRISM_4P:
				return y <= 1.0D - getPrismHeight(x, z, true, true, true, true);
			default:
				return true;
		}
	}

	/**
	 * Height of a half block pyramid, with ridges ("points") running out to the given sides.
	 */
	private static double getPrismHeight(double x, double z, boolean zNeg, boolean zPos, boolean xNeg, boolean xPos) {
		double height = Math.min(Math.min(x, 1.0D - x), Math.min(z, 1.0D - z));
		if ((zNeg && z <= 0.5D) || (zPos && z >= 0.5D)) {
			height = Math.max(height, Math.min(x, 1.0D - x));
		}
		if ((xNeg && x <= 0.5D) || (xPos && x >= 0.5D)) {
			height = Math.max(height, Math.min(z, 1.0D - z));
		}
		return height;
	}

	/**
	 * Rotates a point about the block center, matching Quad.rotate (X, then Y, then Z).
	 */
	private static double[] rotate(double x, double y, double z, Rotation rotation) {
		double rad = rotation.getRadians(Axis.X);
		if (rad > 0) {
			double ny = 0.5D + (z - 0.5D) * Math.sin(rad) + (y - 0.5D) * Math.cos(rad);
			double nz = 0.5D + (z - 0.5D) * Math.cos(rad) - (y - 0.5D) * Math.sin(rad);
			y = ny;
			z = nz;
		}
		rad = rotation.getRadians(Axis.Y);
		if (rad > 0) {
			double nz = 0.5D + (x - 0.5D) * Math.sin(rad) + (z - 0.5D) * Math.cos(rad);
			double nx = 0.5D + (x - 0.5D) * Math.cos(rad) - (z - 0.5D) * Math.sin(rad);
			x = nx;
			z = nz;
		}
		rad = rotation.getRadians(Axis.Z);
		if (rad > 0) {
			double nx = 0.5D + (y - 0.5D) * Math.sin(rad) + (x - 0.5D) * Math.cos(rad);
			double ny = 0.5D + (y - 0.5D) * Math.cos(rad) - (x - 0.5D) * Math.sin(rad);
			x = nx;
			y = ny;
		}
		return new double[] { x, y, z };
	}

	/**
	 * Greedily merges solid voxels into as few boxes as possible.
	 */
	private static List<AxisAlignedBB> mergeBoxes(boolean[][][] solid) {
		List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
		boolean[][][] used = new boolean[RES][RES][RES];
		for (int y = 0; y < RES; ++y) {
			for (int z = 0; z < RES; ++z) {
				for (int x = 0; x < RES; ++x) {
					if (!solid[x][y][z] || used[x][y][z]) {
						continue;
					}
					// Grow along x, then z, then y
					int x2 = x;
					while (x2 + 1 < RES && isFree(solid, used, x2 + 1, x2 + 1, y, y, z, z)) {
						++x2;
					}
					int z2 = z;
					while (z2 + 1 < RES && isFree(solid, used, x, x2, y, y, z2 + 1, z2 + 1)) {
						++z2;
					}
					int y2 = y;
					while (y2 + 1 < RES && isFree(solid, used, x, x2, y2 + 1, y2 + 1, z, z2)) {
						++y2;
					}
					for (int ux = x; ux <= x2; ++ux) {
						for (int uy = y; uy <= y2; ++uy) {
							for (int uz = z; uz <= z2; ++uz) {
								used[ux][uy][uz] = true;
							}
						}
					}
					boxes.add(new AxisAlignedBB(
						(double) x / RES, (double) y / RES, (double) z / RES,
						(double) (x2 + 1) / RES, (double) (y2 + 1) / RES, (double) (z2 + 1) / RES));
				}
			}
		}
		return boxes;
	}

	private static boolean isFree(boolean[][][] solid, boolean[][][] used, int x1, int x2, int y1, int y2, int z1, int z2) {
		for (int x = x1; x <= x2; ++x) {
			for (int y = y1; y <= y2; ++y) {
				for (int z = z1; z <= z2; ++z) {
					if (!solid[x][y][z] || used[x][y][z]) {
						return false;
					}
				}
			}
		}
		return true;
	}

}
