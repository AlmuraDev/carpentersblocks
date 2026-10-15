package com.carpentersblocks.block;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

/**
 * A drawn box of an {@link IShapedBlock}.
 * <p>
 * Parts without a texture take the block cover. Parts with a texture (iron rails, torch heads,
 * glass panes...) always show that texture. A part is either an axis aligned box, or a tilted
 * cuboid given by its 8 corners (lever handles, wall torches).
 */
public class ShapePart {

	private final AxisAlignedBB _box;
	private final Vec3d[] _corners;
	private final ResourceLocation _texture;
	private final boolean _translucent;
	private final boolean _fullBright;
	private int _rgb = -1;

	private ShapePart(AxisAlignedBB box, @Nullable Vec3d[] corners, @Nullable ResourceLocation texture, boolean translucent, boolean fullBright) {
		_box = box;
		_corners = corners;
		_texture = texture;
		_translucent = translucent;
		_fullBright = fullBright;
	}

	/**
	 * A part covered like the rest of the block.
	 */
	public static ShapePart covered(AxisAlignedBB box) {
		return new ShapePart(box, null, null, false, false);
	}

	/**
	 * A part that always shows a texture.
	 */
	public static ShapePart textured(AxisAlignedBB box, ResourceLocation texture) {
		return new ShapePart(box, null, texture, false, false);
	}

	/**
	 * A part that always shows a see-through texture (glass).
	 */
	public static ShapePart translucent(AxisAlignedBB box, ResourceLocation texture) {
		return new ShapePart(box, null, texture, true, false);
	}

	/**
	 * A part that always shows a texture at full brightness (flames, lights).
	 */
	public static ShapePart glowing(AxisAlignedBB box, ResourceLocation texture) {
		return new ShapePart(box, null, texture, false, true);
	}

	/**
	 * A tilted cuboid. Corners 0-3 are the bottom face and 4-7 the top face, each going around
	 * the same way, with corner 4 above corner 0.
	 *
	 * @param corners the 8 corners
	 * @param texture the texture, or null to take the cover
	 * @param fullBright whether drawn at full brightness
	 */
	public static ShapePart cuboid(Vec3d[] corners, @Nullable ResourceLocation texture, boolean fullBright) {
		double x0 = Double.MAX_VALUE, y0 = Double.MAX_VALUE, z0 = Double.MAX_VALUE;
		double x1 = -Double.MAX_VALUE, y1 = -Double.MAX_VALUE, z1 = -Double.MAX_VALUE;
		for (Vec3d corner : corners) {
			x0 = Math.min(x0, corner.x);
			y0 = Math.min(y0, corner.y);
			z0 = Math.min(z0, corner.z);
			x1 = Math.max(x1, corner.x);
			y1 = Math.max(y1, corner.y);
			z1 = Math.max(z1, corner.z);
		}
		return new ShapePart(new AxisAlignedBB(x0, y0, z0, x1, y1, z1), corners.clone(), texture, false, fullBright);
	}

	/**
	 * Tints the part with a color (0xRRGGBB).
	 */
	public ShapePart withColor(int rgb) {
		_rgb = rgb;
		return this;
	}

	/**
	 * Gets the tint, or -1 for none.
	 */
	public int getColor() {
		return _rgb;
	}

	/**
	 * Gets the box, or the bounds of the cuboid corners.
	 */
	public AxisAlignedBB getBox() {
		return _box;
	}

	/**
	 * Gets the cuboid corners, or null for an axis aligned box.
	 */
	@Nullable
	public Vec3d[] getCorners() {
		return _corners;
	}

	@Nullable
	public ResourceLocation getTexture() {
		return _texture;
	}

	public boolean isTranslucent() {
		return _translucent;
	}

	public boolean isFullBright() {
		return _fullBright;
	}

}
