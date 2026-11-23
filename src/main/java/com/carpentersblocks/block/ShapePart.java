package com.carpentersblocks.block;

import javax.annotation.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A drawn box of an {@link IShapedBlock}.
 * <p>
 * Parts without a texture take the block cover. Parts with a texture (iron rails, torch heads,
 * glass panes...) always show that texture. A part is either an axis aligned box, or a tilted
 * cuboid given by its 8 corners (lever handles, wall torches).
 */
public class ShapePart {

    private final AABB _box;
    private final Vec3[] _corners;
    private final Identifier _texture;
    private final boolean _translucent;
    private final boolean _fullBright;
    private int _rgb = -1;
    // Textures for single faces, over the part texture (bed designs)
    private java.util.Map<net.minecraft.core.Direction, Identifier> _faceTextures;
    // Side the part was rotated to with SidedUtil, so top and bottom textures turn with it (null: world aligned)
    private net.minecraft.core.Direction _uvRotation;
    // Piece (0-7) of a bed design texture each face shows, or none (1.12.2 cut the designs into 8 textures)
    private int _designPiece = -1;
    private java.util.Map<net.minecraft.core.Direction, Integer> _faceDesignPieces;

    private ShapePart(AABB box, @Nullable Vec3[] corners, @Nullable Identifier texture, boolean translucent, boolean fullBright) {
        _box = box;
        _corners = corners;
        _texture = texture;
        _translucent = translucent;
        _fullBright = fullBright;
    }

    /**
     * A part covered like the rest of the block.
     */
    public static ShapePart covered(AABB box) {
        return new ShapePart(box, null, null, false, false);
    }

    /**
     * A part that always shows a texture.
     */
    public static ShapePart textured(AABB box, Identifier texture) {
        return new ShapePart(box, null, texture, false, false);
    }

    /**
     * A part that always shows a see-through texture (glass).
     */
    public static ShapePart translucent(AABB box, Identifier texture) {
        return new ShapePart(box, null, texture, true, false);
    }

    /**
     * A part that always shows a texture at full brightness (flames, lights).
     */
    public static ShapePart glowing(AABB box, Identifier texture) {
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
    public static ShapePart cuboid(Vec3[] corners, @Nullable Identifier texture, boolean fullBright) {
        double x0 = Double.MAX_VALUE, y0 = Double.MAX_VALUE, z0 = Double.MAX_VALUE;
        double x1 = -Double.MAX_VALUE, y1 = -Double.MAX_VALUE, z1 = -Double.MAX_VALUE;
        for (Vec3 corner : corners) {
            x0 = Math.min(x0, corner.x);
            y0 = Math.min(y0, corner.y);
            z0 = Math.min(z0, corner.z);
            x1 = Math.max(x1, corner.x);
            y1 = Math.max(y1, corner.y);
            z1 = Math.max(z1, corner.z);
        }
        return new ShapePart(new AABB(x0, y0, z0, x1, y1, z1), corners.clone(), texture, false, fullBright);
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
    public AABB getBox() {
        return _box;
    }

    /**
     * Gets the cuboid corners, or null for an axis aligned box.
     */
    @Nullable
    public Vec3[] getCorners() {
        return _corners;
    }

    @Nullable
    public Identifier getTexture() {
        return _texture;
    }

    /**
     * Gets the texture of one face: its own texture if set, otherwise the part texture.
     */
    @Nullable
    public Identifier getTexture(net.minecraft.core.Direction face) {
        if (_faceTextures != null && _faceTextures.containsKey(face)) {
            return _faceTextures.get(face);
        }
        return _texture;
    }

    /**
     * Sets textures for single faces (world facing), over the part texture.
     */
    public ShapePart withFaceTextures(java.util.Map<net.minecraft.core.Direction, Identifier> faceTextures) {
        _faceTextures = new java.util.EnumMap<net.minecraft.core.Direction, Identifier>(faceTextures);
        return this;
    }

    /**
     * Turns top and bottom textures with a part rotated by {@link com.carpentersblocks.block.data.SidedUtil#rotate} to a side.
     */
    /**
     * Shows one piece of a bed design texture (the part texture), on every face or per face (world facing).
     */
    public ShapePart withDesignPieces(int piece, @Nullable java.util.Map<net.minecraft.core.Direction, Integer> facePieces) {
        _designPiece = piece;
        _faceDesignPieces = facePieces == null ? null : new java.util.EnumMap<net.minecraft.core.Direction, Integer>(facePieces);
        return this;
    }

    /**
     * Gets the bed design piece a face shows, or -1 for the whole texture.
     */
    public int getDesignPiece(net.minecraft.core.Direction face) {
        if (_faceDesignPieces != null && _faceDesignPieces.containsKey(face)) {
            return _faceDesignPieces.get(face);
        }
        return _designPiece;
    }

    public ShapePart withUVRotation(net.minecraft.core.Direction side) {
        _uvRotation = side;
        return this;
    }

    @Nullable
    public net.minecraft.core.Direction getUVRotation() {
        return _uvRotation;
    }

    public boolean isTranslucent() {
        return _translucent;
    }

    public boolean isFullBright() {
        return _fullBright;
    }

}
