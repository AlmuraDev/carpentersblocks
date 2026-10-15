package com.carpentersblocks.block.data;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

/**
 * Rotation for blocks attached to any face (1.7.10 BlockHandlerBase.rotateBounds).
 * <p>
 * Shapes are authored attached to {@link EnumFacing#SOUTH}, with the supporting block at z = 0,
 * and rotated to the face they are attached to.
 */
public class SidedUtil {

	/**
	 * Rotates a box authored for a south attachment.
	 */
	public static AxisAlignedBB rotate(AxisAlignedBB box, EnumFacing side) {
		Vec3d a = rotate(new Vec3d(box.minX, box.minY, box.minZ), side);
		Vec3d b = rotate(new Vec3d(box.maxX, box.maxY, box.maxZ), side);
		return new AxisAlignedBB(a.x, a.y, a.z, b.x, b.y, b.z);
	}

	/**
	 * Rotates a point authored for a south attachment.
	 */
	public static Vec3d rotate(Vec3d vec, EnumFacing side) {
		switch (side) {
			case NORTH:
				return new Vec3d(1 - vec.x, vec.y, 1 - vec.z);
			case EAST:
				return new Vec3d(vec.z, vec.y, 1 - vec.x);
			case WEST:
				return new Vec3d(1 - vec.z, vec.y, vec.x);
			case UP:
				return new Vec3d(vec.x, vec.z, vec.y);
			case DOWN:
				return new Vec3d(vec.x, 1 - vec.z, vec.y);
			default: // SOUTH
				return vec;
		}
	}

	/**
	 * Rotates cuboid corners authored for a south attachment.
	 */
	public static Vec3d[] rotate(Vec3d[] corners, EnumFacing side) {
		Vec3d[] rotated = new Vec3d[corners.length];
		for (int idx = 0; idx < corners.length; ++idx) {
			rotated[idx] = rotate(corners[idx], side);
		}
		return rotated;
	}

	/**
	 * Gets the 8 corners of a box centered on x/z = 0, bottom face first.
	 */
	public static Vec3d[] centeredCuboid(double halfWidth, double yMin, double yMax) {
		return new Vec3d[] {
			new Vec3d(-halfWidth, yMin, -halfWidth), new Vec3d(halfWidth, yMin, -halfWidth),
			new Vec3d(halfWidth, yMin, halfWidth), new Vec3d(-halfWidth, yMin, halfWidth),
			new Vec3d(-halfWidth, yMax, -halfWidth), new Vec3d(halfWidth, yMax, -halfWidth),
			new Vec3d(halfWidth, yMax, halfWidth), new Vec3d(-halfWidth, yMax, halfWidth) };
	}

	/**
	 * Applies a transform to every corner.
	 */
	public static Vec3d[] transform(Vec3d[] corners, java.util.function.UnaryOperator<Vec3d> transform) {
		Vec3d[] out = new Vec3d[corners.length];
		for (int idx = 0; idx < corners.length; ++idx) {
			out[idx] = transform.apply(corners[idx]);
		}
		return out;
	}

}
