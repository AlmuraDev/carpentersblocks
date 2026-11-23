package com.carpentersblocks.block.data;

import java.util.ArrayList;
import java.util.List;

import com.carpentersblocks.block.ShapePart;
import com.carpentersblocks.block.ShapeTextures;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Carpenter's Door data and shapes, ported from 1.7.10 (data/Hinge, BlockHandlerCarpentersDoor).
 * <p>
 * Bits 0-2: type. Bit 3: hinge on the right. Bits 4-5: facing (the direction the placer looked:
 * 0 east, 1 south, 2 west, 3 north). Bit 6: open. Bit 7: top piece. Bit 8: rigid (redstone only).
 */
public class DoorData {

	public static final int TYPE_GLASS_TOP = 0;
	public static final int TYPE_GLASS_TALL = 1;
	public static final int TYPE_PANELS = 2;
	public static final int TYPE_SCREEN_TALL = 3;
	public static final int TYPE_FRENCH_GLASS = 4;
	public static final int TYPE_HIDDEN = 5;
	public static final int NUM_TYPES = 6;

	private static final int TYPE_MASK = 0x7;
	private static final int HINGE_RIGHT_BIT = 0x8;
	private static final int FACING_MASK = 0x30;
	private static final int FACING_SHIFT = 4;
	private static final int OPEN_BIT = 0x40;
	private static final int TOP_BIT = 0x80;
	private static final int RIGID_BIT = 0x100;

	private static final EnumFacing[] FACINGS = { EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH };

	public static int getType(int data) {
		int type = data & TYPE_MASK;
		return type < NUM_TYPES ? type : TYPE_GLASS_TOP;
	}

	public static int setType(int data, int type) {
		return (data & ~TYPE_MASK) | type;
	}

	public static boolean isHingeRight(int data) {
		return (data & HINGE_RIGHT_BIT) != 0;
	}

	public static int setHingeRight(int data, boolean right) {
		return right ? data | HINGE_RIGHT_BIT : data & ~HINGE_RIGHT_BIT;
	}

	/**
	 * Gets the direction the placer looked at.
	 */
	public static EnumFacing getFacing(int data) {
		return FACINGS[(data & FACING_MASK) >> FACING_SHIFT];
	}

	public static int setFacing(int data, EnumFacing facing) {
		int idx = 0;
		for (int i = 0; i < FACINGS.length; ++i) {
			if (FACINGS[i] == facing) {
				idx = i;
			}
		}
		return (data & ~FACING_MASK) | (idx << FACING_SHIFT);
	}

	public static boolean isOpen(int data) {
		return (data & OPEN_BIT) != 0;
	}

	public static int setOpen(int data, boolean open) {
		return open ? data | OPEN_BIT : data & ~OPEN_BIT;
	}

	public static boolean isTop(int data) {
		return (data & TOP_BIT) != 0;
	}

	public static int setTop(int data, boolean top) {
		return top ? data | TOP_BIT : data & ~TOP_BIT;
	}

	public static boolean isRigid(int data) {
		return (data & RIGID_BIT) != 0;
	}

	public static int setRigid(int data, boolean rigid) {
		return rigid ? data | RIGID_BIT : data & ~RIGID_BIT;
	}

	/**
	 * Gets the rotation of the door plate (authored at z 0.8125-1).
	 */
	private static EnumFacing getPlateSide(int data) {
		EnumFacing facing = getFacing(data);
		// Closed: on the placer's side
		if (!isOpen(data)) {
			return facing.getOpposite();
		}
		// Open: swung onto the hinge side
		return isHingeRight(data) ? facing.rotateY() : facing.rotateYCCW();
	}

	public static AxisAlignedBB getBounds(int data) {
		return SidedUtil.rotate(new AxisAlignedBB(0, 0, 0.8125, 1, 1, 1), getPlateSide(data));
	}

	public static List<ShapePart> getParts(int data) {
		EnumFacing side = getPlateSide(data);
		int type = getType(data);
		boolean top = isTop(data);
		List<AxisAlignedBB> covered = new ArrayList<AxisAlignedBB>();
		AxisAlignedBB pane = null;
		ResourceLocation paneTexture = null;
		if (type == TYPE_HIDDEN) {
			covered.add(new AxisAlignedBB(0, 0, 0.8125, 1, 1, 1));
		} else {
			covered.add(new AxisAlignedBB(0, 0, 0.8125, 0.1875, 1, 1));
			covered.add(new AxisAlignedBB(0.8125, 0, 0.8125, 1, 1, 1));
			switch (type) {
				case TYPE_GLASS_TOP:
				case TYPE_PANELS:
					if (top && type == TYPE_GLASS_TOP) {
						covered.add(new AxisAlignedBB(0.1875, 0.8125, 0.8125, 0.8125, 1, 1));
						covered.add(new AxisAlignedBB(0.1875, 0, 0.8125, 0.8125, 0.1875, 1));
						pane = new AxisAlignedBB(0.1875, 0.1875, 0.90625, 0.8125, 0.8125, 0.921875);
						paneTexture = ShapeTextures.DOOR_GLASS_TOP;
					} else if (top) {
						covered.add(new AxisAlignedBB(0.1875, 0, 0.875, 0.8125, 0.8125, 0.9375));
						covered.add(new AxisAlignedBB(0.1875, 0.8125, 0.8125, 0.8125, 1, 1));
						covered.add(new AxisAlignedBB(0.1875, 0.0625, 0.8125, 0.8125, 0.25, 1));
						covered.add(new AxisAlignedBB(0.3125, 0.375, 0.8125, 0.6875, 0.6875, 1));
					} else {
						covered.add(new AxisAlignedBB(0.1875, 0.1875, 0.875, 0.8125, 1, 0.9375));
						covered.add(new AxisAlignedBB(0.1875, 0, 0.8125, 0.8125, 0.1875, 1));
						covered.add(new AxisAlignedBB(0.3125, 0.3125, 0.8125, 0.6875, type == TYPE_PANELS ? 0.9375 : 0.875, 1));
					}
					break;
				case TYPE_GLASS_TALL:
				case TYPE_SCREEN_TALL:
					if (top) {
						covered.add(new AxisAlignedBB(0.1875, 0.8125, 0.8125, 0.8125, 1, 1));
						pane = new AxisAlignedBB(0.1875, 0, 0.90625, 0.8125, 0.8125, 0.921875);
						paneTexture = type == TYPE_SCREEN_TALL ? ShapeTextures.DOOR_SCREEN_TALL : ShapeTextures.DOOR_GLASS_TALL_TOP;
					} else {
						covered.add(new AxisAlignedBB(0.1875, 0, 0.8125, 0.8125, 0.1875, 1));
						pane = new AxisAlignedBB(0.1875, 0.1875, 0.90625, 0.8125, 1, 0.921875);
						paneTexture = type == TYPE_SCREEN_TALL ? ShapeTextures.DOOR_SCREEN_TALL : ShapeTextures.DOOR_GLASS_TALL_BOTTOM;
					}
					break;
				default: // FRENCH_GLASS
					if (top) {
						covered.add(new AxisAlignedBB(0.4375, 0.0625, 0.875, 0.5625, 0.375, 0.9375));
						covered.add(new AxisAlignedBB(0.4375, 0.5, 0.875, 0.5625, 0.8125, 0.9375));
						covered.add(new AxisAlignedBB(0.1875, 0.8125, 0.8125, 0.8125, 1, 1));
						covered.add(new AxisAlignedBB(0.1875, 0, 0.875, 0.8125, 0.0625, 0.9375));
						covered.add(new AxisAlignedBB(0.1875, 0.375, 0.875, 0.8125, 0.5, 0.9375));
						pane = new AxisAlignedBB(0.1875, 0, 0.90625, 0.8125, 0.8125, 0.921875);
						paneTexture = ShapeTextures.DOOR_FRENCH_GLASS_TOP;
					} else {
						covered.add(new AxisAlignedBB(0.4375, 0.1875, 0.875, 0.5625, 0.5, 0.9375));
						covered.add(new AxisAlignedBB(0.4375, 0.625, 0.875, 0.5625, 0.9375, 0.9375));
						covered.add(new AxisAlignedBB(0.1875, 0, 0.8125, 0.8125, 0.1875, 1));
						covered.add(new AxisAlignedBB(0.1875, 0.5, 0.875, 0.8125, 0.625, 0.9375));
						covered.add(new AxisAlignedBB(0.1875, 0.9375, 0.875, 0.8125, 1, 0.9375));
						pane = new AxisAlignedBB(0.1875, 0.1875, 0.90625, 0.8125, 1, 0.921875);
						paneTexture = ShapeTextures.DOOR_FRENCH_GLASS_BOTTOM;
					}
					break;
			}
		}
		List<ShapePart> parts = new ArrayList<ShapePart>();
		for (AxisAlignedBB box : covered) {
			parts.add(ShapePart.covered(SidedUtil.rotate(box, side)));
		}
		if (pane != null) {
			parts.add(ShapePart.translucent(SidedUtil.rotate(pane, side), paneTexture));
		}
		// Pull handles on both faces of the door (1.7.10 BlockHandlerCarpentersDoor). The handle on
		// the plate's outer face reaches into the next block space (z beyond 1 before rotating).
		// Hidden doors only have the inner handle.
		boolean open = isOpen(data);
		boolean right = isHingeRight(data);
		double xMin = open ? (right ? 0.875 : 0.0625) : (right ? 0.0625 : 0.875);
		double xMax = xMin + 0.0625;
		double yMin = top ? 0.0625 : 0.875;
		double yMax = yMin + 0.0625;
		double yMinOff = top ? 0 : 0.875;
		double yMaxOff = top ? 0.125 : 1;
		boolean innerHandle = true;
		boolean outerHandle = type != TYPE_HIDDEN;
		if ((innerHandle && !open) || (outerHandle && open)) {
			parts.add(ShapePart.textured(SidedUtil.rotate(new AxisAlignedBB(xMin, yMin, 0.75, xMax, yMax, 0.8125), side), ShapeTextures.IRON_BLOCK));
			parts.add(ShapePart.textured(SidedUtil.rotate(new AxisAlignedBB(xMin, yMinOff, 0.6875, xMax, yMaxOff, 0.75), side), ShapeTextures.IRON_BLOCK));
		}
		if ((outerHandle && !open) || (innerHandle && open)) {
			parts.add(ShapePart.textured(SidedUtil.rotate(new AxisAlignedBB(xMin, yMin, 1, xMax, yMax, 1.0625), side), ShapeTextures.IRON_BLOCK));
			parts.add(ShapePart.textured(SidedUtil.rotate(new AxisAlignedBB(xMin, yMinOff, 1.0625, xMax, yMaxOff, 1.125), side), ShapeTextures.IRON_BLOCK));
		}
		return parts;
	}

}
