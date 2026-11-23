package com.carpentersblocks.util.block;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.carpentersblocks.block.BlockCarpentersSlope;
import com.carpentersblocks.block.data.SlopeData;
import com.carpentersblocks.block.data.SlopeData.Type;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.util.RotationUtil.Rotation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Joins a newly placed slope to the slopes beside it, from 1.7.10 (1.12.2 dropped it): a wedge between two slopes at
 * a right angle becomes the inside or outside corner between them and turns the wedges beside it into corners, and
 * a prism grows ridges toward the prisms beside it and they toward it. Sneaking places a slope as it is.
 * <p>
 * 1.7.10 named every slope; here a slope is its type and rotation, and what it is compared by is its facings: the
 * directions its sloped faces look (up or down, and the sides).
 */
public final class SlopeTransform {

    // The rotations upright and upside down slopes are placed with
    private static final Rotation[] UPRIGHT = { Rotation.X0_Y0_Z0, Rotation.X0_Y90_Z0, Rotation.X0_Y180_Z0, Rotation.X0_Y270_Z0 };
    private static final Rotation[] UPSIDE_DOWN = { Rotation.X180_Y0_Z0, Rotation.X180_Y90_Z0, Rotation.X0_Y0_Z180, Rotation.X180_Y270_Z0 };

    private SlopeTransform() {}

    /**
     * A slope in the world, as compared.
     *
     * @param facings the directions its sloped faces look, rotated
     */
    private record Slope(Type type, Set<Direction> facings) {

        boolean isPositive() {
            return facings.contains(Direction.UP);
        }

        boolean has(Direction direction) {
            return facings.contains(direction);
        }

        /**
         * A wedge sloping up or down (not one on its side), 1.7.10's primary type WEDGE.
         */
        boolean isUprightWedge() {
            return type == Type.WEDGE && (facings.contains(Direction.UP) || facings.contains(Direction.DOWN));
        }

        boolean isPrism() {
            return type == Type.PRISM || type == Type.PRISM_1P || type == Type.PRISM_2P || type == Type.PRISM_3P || type == Type.PRISM_4P;
        }
    }

    /**
     * The facings of each type unrotated, as SlopeShapes draws them.
     */
    private static Set<Direction> unrotatedFacings(Type type) {
        return switch (type) {
            case WEDGE, PRISM_WEDGE, PRISM_1P -> EnumSet.of(Direction.UP, Direction.SOUTH);
            case WEDGE_INTERIOR, WEDGE_EXTERIOR, OBLIQUE_INTERIOR, OBLIQUE_EXTERIOR -> EnumSet.of(Direction.UP, Direction.SOUTH, Direction.EAST);
            case PRISM -> EnumSet.of(Direction.UP);
            case PRISM_2P -> EnumSet.of(Direction.UP, Direction.NORTH, Direction.SOUTH);
            case PRISM_3P -> EnumSet.of(Direction.UP, Direction.SOUTH, Direction.WEST, Direction.EAST);
            case PRISM_4P -> EnumSet.of(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
            default -> EnumSet.noneOf(Direction.class);
        };
    }

    private static Set<Direction> facings(Type type, Rotation rotation) {
        Set<Direction> facings = EnumSet.noneOf(Direction.class);
        for (Direction facing : unrotatedFacings(type)) {
            facings.add(rotation.getRotatedFacing(facing));
        }
        return facings;
    }

    private static Slope of(int cbMetadata) {
        Type type = SlopeData.getType(cbMetadata);
        return new Slope(type, facings(type, Rotation.get(cbMetadata)));
    }

    @Nullable
    private static CbBlockEntity slopeAt(Level level, BlockPos pos) {
        if (!(level.getBlockState(pos).getBlock() instanceof BlockCarpentersSlope)) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof CbBlockEntity cbBlockEntity ? cbBlockEntity : null;
    }

    @Nullable
    private static Slope slope(Level level, BlockPos pos) {
        CbBlockEntity blockEntity = slopeAt(level, pos);
        return blockEntity != null ? of(blockEntity.getCbMetadata()) : null;
    }

    /**
     * Gets the block data of a slope of a type, upright or upside down, whose sloped faces look to the sides given;
     * or -1 when no placed rotation makes one.
     */
    private static int make(Type type, boolean positive, Direction... sides) {
        Set<Direction> wanted = EnumSet.of(positive ? Direction.UP : Direction.DOWN);
        for (Direction side : sides) {
            wanted.add(side);
        }
        for (Rotation rotation : positive ? UPRIGHT : UPSIDE_DOWN) {
            if (facings(type, rotation).equals(wanted)) {
                return SlopeData.withType(rotation.asInt(), type);
            }
        }
        return -1;
    }

    /**
     * Joins a slope just placed (not sneaking) to the slopes beside it, and them to it.
     */
    public static void onPlaced(Level level, BlockPos pos, CbBlockEntity blockEntity) {
        Slope slope = of(blockEntity.getCbMetadata());
        if (slope.isUprightWedge()) {
            int data = transformWedge(level, pos, slope);
            if (data >= 0) {
                blockEntity.setCbMetadata(data);
                slope = of(data);
            }
            transformAdjacentWedges(level, pos, slope);
        } else if (slope.type() == Type.PRISM && slope.isPositive()) {
            int data = transformPrism(level, pos);
            if (data >= 0) {
                blockEntity.setCbMetadata(data);
            }
            transformAdjacentPrisms(level, pos);
        }
    }

    ////////////
    // Wedges
    ////////////

    /**
     * Gets the corner a wedge becomes between the slopes beside it, or -1 to stay as it is.
     */
    private static int transformWedge(Level level, BlockPos pos, Slope slope) {
        Slope north = slope(level, pos.north());
        Slope south = slope(level, pos.south());
        Slope west = slope(level, pos.west());
        Slope east = slope(level, pos.east());

        // Between two slopes at a right angle: an inside corner where both slope toward this block's corner, an
        // outside corner where both slope away
        int data;
        if ((data = corner(north, Direction.NORTH, west, Direction.WEST)) >= 0
                || (data = corner(north, Direction.NORTH, east, Direction.EAST)) >= 0
                || (data = corner(south, Direction.SOUTH, west, Direction.WEST)) >= 0
                || (data = corner(south, Direction.SOUTH, east, Direction.EAST)) >= 0) {
            return data;
        }

        // Beside one slope that turns at a right angle to this one
        if ((data = besideTurn(slope, west, Direction.WEST)) >= 0
                || (data = besideTurn(slope, east, Direction.EAST)) >= 0
                || (data = besideTurn(slope, north, Direction.NORTH)) >= 0
                || (data = besideTurn(slope, south, Direction.SOUTH)) >= 0) {
            return data;
        }
        return -1;
    }

    /**
     * Two slopes on sides at a right angle: an inside corner when each slopes toward the other's side, an outside
     * corner when each slopes away from it.
     */
    private static int corner(@Nullable Slope first, Direction firstSide, @Nullable Slope second, Direction secondSide) {
        if (first == null || second == null) {
            return -1;
        }
        boolean positive = first.isPositive() && second.isPositive();
        if (first.has(secondSide) && second.has(firstSide)) {
            return make(Type.WEDGE_INTERIOR, positive, firstSide, secondSide);
        }
        if (first.has(secondSide.getOpposite()) && second.has(firstSide.getOpposite())) {
            return make(Type.WEDGE_EXTERIOR, positive, firstSide.getOpposite(), secondSide.getOpposite());
        }
        return -1;
    }

    /**
     * A slope on one side, sloping at a right angle to that side and not away from this block: a wedge sloping
     * toward it becomes an inside corner, one sloping away an outside corner.
     */
    private static int besideTurn(Slope slope, @Nullable Slope neighbor, Direction side) {
        if (neighbor == null || neighbor.has(side.getOpposite())) {
            return -1;
        }
        // 1.7.10 tried the facing along the side first (the side itself, then its opposite), then the turn
        // (south or east before north or west)
        for (Direction facing : new Direction[] { side, side.getOpposite() }) {
            if (!slope.has(facing)) {
                continue;
            }
            Type type = facing == side ? Type.WEDGE_INTERIOR : Type.WEDGE_EXTERIOR;
            for (Direction turn : side.getAxis() == Direction.Axis.X
                    ? new Direction[] { Direction.SOUTH, Direction.NORTH } : new Direction[] { Direction.EAST, Direction.WEST }) {
                if (neighbor.has(turn)) {
                    return make(type, neighbor.isPositive(), turn, facing);
                }
            }
        }
        return -1;
    }

    /**
     * Turns the upright wedges beside a wedge just placed, sloping at a right angle to it, into the corners joining
     * them to it.
     */
    private static void transformAdjacentWedges(Level level, BlockPos pos, Slope slope) {
        if (!slope.isUprightWedge()) {
            return;
        }
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            if (!slope.has(facing)) {
                continue;
            }
            for (Direction side : new Direction[] { facing.getClockWise(), facing.getCounterClockWise() }) {
                BlockPos neighborPos = pos.relative(side);
                CbBlockEntity blockEntity = slopeAt(level, neighborPos);
                if (blockEntity == null) {
                    continue;
                }
                Slope neighbor = of(blockEntity.getCbMetadata());
                if (!neighbor.isUprightWedge() || neighbor.isPositive() != slope.isPositive()) {
                    continue;
                }
                int data = -1;
                if (neighbor.has(side)) {
                    data = make(Type.WEDGE_EXTERIOR, slope.isPositive(), side, facing);
                } else if (neighbor.has(side.getOpposite())) {
                    data = make(Type.WEDGE_INTERIOR, slope.isPositive(), side.getOpposite(), facing);
                }
                if (data >= 0) {
                    blockEntity.setCbMetadata(data);
                }
            }
        }
    }

    ////////////
    // Prisms
    ////////////

    /**
     * Gets the prism with ridges toward the prisms beside it, or -1 to stay as it is (none beside, or two at a right
     * angle, which has no shape since 1.12.2).
     */
    private static int transformPrism(Level level, BlockPos pos) {
        Set<Direction> ridges = EnumSet.noneOf(Direction.class);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            Slope neighbor = slope(level, pos.relative(side));
            if (neighbor != null && neighbor.isPositive()
                    && (neighbor.isPrism() || neighbor.type() == Type.PRISM_WEDGE && neighbor.has(side.getOpposite()))) {
                ridges.add(side);
            }
        }
        Type type = switch (ridges.size()) {
            case 1 -> Type.PRISM_1P;
            case 2 -> Type.PRISM_2P;
            case 3 -> Type.PRISM_3P;
            case 4 -> Type.PRISM_4P;
            default -> null;
        };
        if (type == null) {
            return -1;
        }
        // The sides with ridges, as the shapes are told apart: a 3 point prism by its sides with ridges too
        return make(type, true, ridges.toArray(new Direction[0]));
    }

    /**
     * Grows the ridges of the prisms beside a prism just placed toward their prism neighbors.
     */
    private static void transformAdjacentPrisms(Level level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(side);
            CbBlockEntity blockEntity = slopeAt(level, neighborPos);
            if (blockEntity != null) {
                Slope neighbor = of(blockEntity.getCbMetadata());
                if (neighbor.isPrism() && neighbor.isPositive()) {
                    int data = transformPrism(level, neighborPos);
                    if (data >= 0) {
                        blockEntity.setCbMetadata(data);
                    }
                }
            }
        }
    }

}
