package com.carpentersblocks.block.data;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Rotation for blocks attached to any face (1.7.10 BlockHandlerBase.rotateBounds).
 * <p>
 * Shapes are authored attached to {@link Direction#SOUTH}, with the supporting block at z = 0,
 * and rotated to the face they are attached to.
 */
public class SidedUtil {

    /**
     * Rotates a box authored for a south attachment.
     */
    public static AABB rotate(AABB box, Direction side) {
        Vec3 a = rotate(new Vec3(box.minX, box.minY, box.minZ), side);
        Vec3 b = rotate(new Vec3(box.maxX, box.maxY, box.maxZ), side);
        return new AABB(a.x, a.y, a.z, b.x, b.y, b.z);
    }

    /**
     * Rotates a point authored for a south attachment.
     */
    public static Vec3 rotate(Vec3 vec, Direction side) {
        switch (side) {
            case NORTH:
                return new Vec3(1 - vec.x, vec.y, 1 - vec.z);
            case EAST:
                return new Vec3(vec.z, vec.y, 1 - vec.x);
            case WEST:
                return new Vec3(1 - vec.z, vec.y, vec.x);
            case UP:
                return new Vec3(vec.x, vec.z, vec.y);
            case DOWN:
                return new Vec3(vec.x, 1 - vec.z, vec.y);
            default: // SOUTH
                return vec;
        }
    }

    /**
     * Rotates cuboid corners authored for a south attachment.
     */
    public static Vec3[] rotate(Vec3[] corners, Direction side) {
        Vec3[] rotated = new Vec3[corners.length];
        for (int idx = 0; idx < corners.length; ++idx) {
            rotated[idx] = rotate(corners[idx], side);
        }
        return rotated;
    }

    /**
     * Gets the 8 corners of a box centered on x/z = 0, bottom face first.
     */
    public static Vec3[] centeredCuboid(double halfWidth, double yMin, double yMax) {
        return new Vec3[] {
            new Vec3(-halfWidth, yMin, -halfWidth), new Vec3(halfWidth, yMin, -halfWidth),
            new Vec3(halfWidth, yMin, halfWidth), new Vec3(-halfWidth, yMin, halfWidth),
            new Vec3(-halfWidth, yMax, -halfWidth), new Vec3(halfWidth, yMax, -halfWidth),
            new Vec3(halfWidth, yMax, halfWidth), new Vec3(-halfWidth, yMax, halfWidth) };
    }

    /**
     * Applies a transform to every corner.
     */
    public static Vec3[] transform(Vec3[] corners, java.util.function.UnaryOperator<Vec3> transform) {
        Vec3[] out = new Vec3[corners.length];
        for (int idx = 0; idx < corners.length; ++idx) {
            out[idx] = transform.apply(corners[idx]);
        }
        return out;
    }

}
