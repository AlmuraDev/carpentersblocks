package com.carpentersblocks.util.block;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.carpentersblocks.block.BlockCarpentersSlope;
import com.carpentersblocks.block.data.SlopeData;
import com.carpentersblocks.block.data.SlopeData.Type;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.RotationUtil.Rotation;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Joins a newly placed slope to the slopes beside it, from 1.7.10: a wedge between two slopes at a right angle
 * becomes the inside or outside corner between them and turns the wedges beside it into corners, and a prism grows
 * ridges toward the prisms beside it and they toward it. Sneaking places a slope as it is.
 * <p>
 * 1.7.10 named every slope; here a slope is its type and rotation, and what it is compared by is its facings: the
 * directions its sloped faces look (up or down, and the sides).
 */
public final class SlopeTransform {

	// The rotations upright and upside down slopes are placed with
	private static final Rotation[] UPRIGHT = { Rotation.X0_Y0_Z0, Rotation.X0_Y90_Z0, Rotation.X0_Y180_Z0, Rotation.X0_Y270_Z0 };
	private static final Rotation[] UPSIDE_DOWN = { Rotation.X180_Y0_Z0, Rotation.X180_Y90_Z0, Rotation.X0_Y0_Z180, Rotation.X180_Y270_Z0 };
	private static final int TYPE_BITSHIFT = 6;

	private SlopeTransform() {}

	/**
	 * A slope in the world, as compared.
	 */
	private static final class Slope {

		final Type type;
		// The directions its sloped faces look, rotated
		final Set<EnumFacing> facings;

		Slope(Type type, Set<EnumFacing> facings) {
			this.type = type;
			this.facings = facings;
		}

		boolean isPositive() {
			return facings.contains(EnumFacing.UP);
		}

		boolean has(EnumFacing facing) {
			return facings.contains(facing);
		}

		/**
		 * A wedge sloping up or down (not one on its side), 1.7.10's primary type WEDGE.
		 */
		boolean isUprightWedge() {
			return type == Type.WEDGE && (facings.contains(EnumFacing.UP) || facings.contains(EnumFacing.DOWN));
		}

		boolean isPrism() {
			return type == Type.PRISM || type == Type.PRISM_1P || type == Type.PRISM_2P || type == Type.PRISM_3P || type == Type.PRISM_4P;
		}
	}

	/**
	 * The facings of each type unrotated, as BakedSlope draws them.
	 */
	private static Set<EnumFacing> unrotatedFacings(Type type) {
		switch (type) {
			case WEDGE:
			case PRISM_WEDGE:
			case PRISM_1P:
				return EnumSet.of(EnumFacing.UP, EnumFacing.SOUTH);
			case WEDGE_INTERIOR:
			case WEDGE_EXTERIOR:
			case OBLIQUE_INTERIOR:
			case OBLIQUE_EXTERIOR:
				return EnumSet.of(EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.EAST);
			case PRISM:
				return EnumSet.of(EnumFacing.UP);
			case PRISM_2P:
				return EnumSet.of(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH);
			case PRISM_3P:
				return EnumSet.of(EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST);
			case PRISM_4P:
				return EnumSet.of(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST);
			default:
				return EnumSet.noneOf(EnumFacing.class);
		}
	}

	private static Set<EnumFacing> facings(Type type, Rotation rotation) {
		Set<EnumFacing> facings = EnumSet.noneOf(EnumFacing.class);
		for (EnumFacing facing : unrotatedFacings(type)) {
			facings.add(rotation.getRotatedFacing(facing));
		}
		return facings;
	}

	private static Slope of(int cbMetadata) {
		Type type = SlopeData.getType(cbMetadata);
		return new Slope(type, facings(type, Rotation.get(cbMetadata)));
	}

	@Nullable
	private static CbTileEntity slopeAt(World world, BlockPos pos) {
		if (!(world.getBlockState(pos).getBlock() instanceof BlockCarpentersSlope)) {
			return null;
		}
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof CbTileEntity ? (CbTileEntity) tileEntity : null;
	}

	@Nullable
	private static Slope slope(World world, BlockPos pos) {
		CbTileEntity tileEntity = slopeAt(world, pos);
		return tileEntity != null ? of(tileEntity.getCbMetadata()) : null;
	}

	/**
	 * Gets the block data of a slope of a type, upright or upside down, whose sloped faces look to the sides given;
	 * or -1 when no placed rotation makes one.
	 */
	private static int make(Type type, boolean positive, EnumFacing... sides) {
		Set<EnumFacing> wanted = EnumSet.of(positive ? EnumFacing.UP : EnumFacing.DOWN);
		for (EnumFacing side : sides) {
			wanted.add(side);
		}
		for (Rotation rotation : positive ? UPRIGHT : UPSIDE_DOWN) {
			if (facings(type, rotation).equals(wanted)) {
				return rotation.asInt() | type.ordinal() << TYPE_BITSHIFT;
			}
		}
		return -1;
	}

	/**
	 * Joins a slope just placed (not sneaking) to the slopes beside it, and them to it.
	 */
	public static void onPlaced(World world, BlockPos pos, CbTileEntity tileEntity) {
		Slope slope = of(tileEntity.getCbMetadata());
		if (slope.isUprightWedge()) {
			int data = transformWedge(world, pos, slope);
			if (data >= 0) {
				tileEntity.setCbMetadata(data);
				slope = of(data);
			}
			transformAdjacentWedges(world, pos, slope);
		} else if (slope.type == Type.PRISM && slope.isPositive()) {
			int data = transformPrism(world, pos);
			if (data >= 0) {
				tileEntity.setCbMetadata(data);
			}
			transformAdjacentPrisms(world, pos);
		}
	}

	////////////
	// Wedges
	////////////

	/**
	 * Gets the corner a wedge becomes between the slopes beside it, or -1 to stay as it is.
	 */
	private static int transformWedge(World world, BlockPos pos, Slope slope) {
		Slope north = slope(world, pos.north());
		Slope south = slope(world, pos.south());
		Slope west = slope(world, pos.west());
		Slope east = slope(world, pos.east());

		// Between two slopes at a right angle: an inside corner where both slope toward this block's corner, an
		// outside corner where both slope away
		int data;
		if ((data = corner(north, EnumFacing.NORTH, west, EnumFacing.WEST)) >= 0
				|| (data = corner(north, EnumFacing.NORTH, east, EnumFacing.EAST)) >= 0
				|| (data = corner(south, EnumFacing.SOUTH, west, EnumFacing.WEST)) >= 0
				|| (data = corner(south, EnumFacing.SOUTH, east, EnumFacing.EAST)) >= 0) {
			return data;
		}

		// Beside one slope that turns at a right angle to this one
		if ((data = besideTurn(slope, west, EnumFacing.WEST)) >= 0
				|| (data = besideTurn(slope, east, EnumFacing.EAST)) >= 0
				|| (data = besideTurn(slope, north, EnumFacing.NORTH)) >= 0
				|| (data = besideTurn(slope, south, EnumFacing.SOUTH)) >= 0) {
			return data;
		}
		return -1;
	}

	/**
	 * Two slopes on sides at a right angle: an inside corner when each slopes toward the other's side, an outside
	 * corner when each slopes away from it.
	 */
	private static int corner(@Nullable Slope first, EnumFacing firstSide, @Nullable Slope second, EnumFacing secondSide) {
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
	private static int besideTurn(Slope slope, @Nullable Slope neighbor, EnumFacing side) {
		if (neighbor == null || neighbor.has(side.getOpposite())) {
			return -1;
		}
		// 1.7.10 tried the facing along the side first (the side itself, then its opposite), then the turn
		// (south or east before north or west)
		for (EnumFacing facing : new EnumFacing[] { side, side.getOpposite() }) {
			if (!slope.has(facing)) {
				continue;
			}
			Type type = facing == side ? Type.WEDGE_INTERIOR : Type.WEDGE_EXTERIOR;
			EnumFacing[] turns = side.getAxis() == EnumFacing.Axis.X
				? new EnumFacing[] { EnumFacing.SOUTH, EnumFacing.NORTH } : new EnumFacing[] { EnumFacing.EAST, EnumFacing.WEST };
			for (EnumFacing turn : turns) {
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
	private static void transformAdjacentWedges(World world, BlockPos pos, Slope slope) {
		if (!slope.isUprightWedge()) {
			return;
		}
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			if (!slope.has(facing)) {
				continue;
			}
			for (EnumFacing side : new EnumFacing[] { facing.rotateY(), facing.rotateYCCW() }) {
				CbTileEntity tileEntity = slopeAt(world, pos.offset(side));
				if (tileEntity == null) {
					continue;
				}
				Slope neighbor = of(tileEntity.getCbMetadata());
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
					tileEntity.setCbMetadata(data);
				}
			}
		}
	}

	////////////
	// Prisms
	////////////

	/**
	 * Gets the prism with ridges toward the prisms beside it, or -1 to stay as it is (none beside, or two at a right
	 * angle, which has no shape here).
	 */
	private static int transformPrism(World world, BlockPos pos) {
		Set<EnumFacing> ridges = EnumSet.noneOf(EnumFacing.class);
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			Slope neighbor = slope(world, pos.offset(side));
			if (neighbor != null && neighbor.isPositive()
					&& (neighbor.isPrism() || neighbor.type == Type.PRISM_WEDGE && neighbor.has(side.getOpposite()))) {
				ridges.add(side);
			}
		}
		Type type;
		switch (ridges.size()) {
			case 1: type = Type.PRISM_1P; break;
			case 2: type = Type.PRISM_2P; break;
			case 3: type = Type.PRISM_3P; break;
			case 4: type = Type.PRISM_4P; break;
			default: return -1;
		}
		return make(type, true, ridges.toArray(new EnumFacing[0]));
	}

	/**
	 * Grows the ridges of the prisms beside a prism just placed toward their prism neighbors.
	 */
	private static void transformAdjacentPrisms(World world, BlockPos pos) {
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			BlockPos neighborPos = pos.offset(side);
			CbTileEntity tileEntity = slopeAt(world, neighborPos);
			if (tileEntity != null) {
				Slope neighbor = of(tileEntity.getCbMetadata());
				if (neighbor.isPrism() && neighbor.isPositive()) {
					int data = transformPrism(world, neighborPos);
					if (data >= 0) {
						tileEntity.setCbMetadata(data);
					}
				}
			}
		}
	}

}
