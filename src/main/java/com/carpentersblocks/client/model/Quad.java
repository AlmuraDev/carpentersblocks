package com.carpentersblocks.client.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.util.RotationUtil;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3f;

/**
 * A face of a Carpenter's block before it is baked: its corners, side, texture, color, tint and chunk layer.
 * <p>
 * Ported from 1.12.2 with the geometry unchanged; {@link #bake} builds a 26.3 {@link BakedQuad}.
 */
public class Quad {

    private static final String GRASS_SIDE_OVERLAY = "minecraft:block/grass_block_side_overlay";
    private static final int DEFAULT_RGB = 0xFFFFFF;

    private Vec3[] _vecs;
    private Direction _facing;
    private TextureAtlasSprite _sprite;
    private int _rgb;
    private boolean _maxBrightness;
    private ChunkSectionLayer _renderLayer;
    private Vec3 _normal;
    private boolean _isOblique;
    private Vec3 _uvOffset;
    private Direction _uvFloat;
    // Tint applied by the renderer through block tint sources (see CoverTints), or -1
    private int _tintIndex = -1;
    // Side a top or bottom face was rotated to with SidedUtil; its texture turns with it (null: world aligned)
    private Direction _uvRotation;
    // Whether the sprite is one of the uncovered frames, which covers replace
    private boolean _canCover;
    // Side a sloped face is lit and mapped as (collapsible blocks: their facing), or null
    private Direction _slopedBakeFacing;
    // Part of the sprite the quad shows (u0, v0, u1, v1, 0 to 1) and its clockwise quarter turns, or null for all of it
    private float[] _spriteRegion;
    private int _spriteRegionTurns;

    private Quad() { }

    /**
     * Copy constructor
     *
     * @param src the source quad
     */
    public Quad(Quad src) {
        _vecs = Arrays.copyOf(src.getVecs(), src.getVecs().length);
        _facing = src.getFacing();
        _maxBrightness = src.isMaxBrightness();
        _renderLayer = src.getRenderLayer();
        _rgb = src.getRgb();
        _sprite = src.getSprite();
        _canCover = src._canCover;
        _normal = src.getNormal();
        _isOblique = src.isObliqueSlope();
        _uvFloat = src.getUVFloat();
        _uvOffset = src.getUVOffset();
        _tintIndex = src.getTintIndex();
        _uvRotation = src._uvRotation;
        _slopedBakeFacing = src._slopedBakeFacing;
        _spriteRegion = src._spriteRegion;
        _spriteRegionTurns = src._spriteRegionTurns;
    }

    /**
     * Shows only part of the sprite, turned clockwise by quarter turns (bed design pieces).
     */
    public void setSpriteRegion(float u0, float v0, float u1, float v1, int quarterTurns) {
        _spriteRegion = new float[] { u0, v0, u1, v1 };
        _spriteRegionTurns = quarterTurns & 3;
    }

    /**
     * Lights and maps the quad as a side if it is sloped (1.12.2 drew collapsible block tops this way).
     */
    public void setSlopedBakeFacing(Direction facing) {
        _slopedBakeFacing = facing;
    }

    public static Quad getQuad(Direction facing, Vec3... inVecs) {
        return getQuad(facing, true, CbSprites.get(CbSprites.UNCOVERED_FULL), inVecs);
    }

    public static Quad getUnsortedQuad(Direction facing, Vec3... inVecs) {
        return getQuad(facing, false, CbSprites.get(CbSprites.UNCOVERED_FULL), inVecs);
    }

    public static Quad getQuad(Direction facing, TextureAtlasSprite sprite, Vec3... inVecs) {
        return getQuad(facing, true, sprite, DEFAULT_RGB, false, ChunkSectionLayer.CUTOUT, inVecs);
    }

    public static Quad getQuad(Direction facing, boolean sortVecs, TextureAtlasSprite sprite, Vec3... inVecs) {
        return getQuad(facing, sortVecs, sprite, DEFAULT_RGB, false, ChunkSectionLayer.CUTOUT, inVecs);
    }

    public static Quad getQuad(Direction facing, TextureAtlasSprite sprite, int rgb, Vec3... inVecs) {
        return getQuad(facing, true, sprite, rgb, false, ChunkSectionLayer.CUTOUT, inVecs);
    }

    public static Quad getQuad(Direction facing, boolean sortVecs, TextureAtlasSprite sprite, int rgb, Vec3... inVecs) {
        return getQuad(facing, sortVecs, sprite, rgb, false, ChunkSectionLayer.CUTOUT, inVecs);
    }

    public static Quad getQuad(Direction facing, boolean sortVecs, TextureAtlasSprite sprite, int rgb, boolean maxBrightness, ChunkSectionLayer renderLayer, Vec3... inVecs) {
        Quad quad = new Quad();
        quad._vecs = Arrays.copyOf(inVecs, inVecs.length);
        quad.setSprite(sprite);
        quad._canCover = isUncoveredSprite(sprite);
        quad.setMaxBrightness(maxBrightness);
        quad.setRenderLayer(renderLayer);
        quad.setRgb(rgb);
        if (!quad.applyFacing(sortVecs, facing)) {
            return null;
        }
        return quad;
    }

    private static boolean isUncoveredSprite(TextureAtlasSprite sprite) {
        return sprite != null && sprite.contents().name().getNamespace().equals(CarpentersBlocks.MOD_ID)
            && sprite.contents().name().getPath().contains("uncovered_");
    }

    /**
     * Copy constructor with offset.
     */
    public Quad offset(double x, double y, double z) {
        Quad quad = new Quad(this);
        for (int i = 0; i < quad._vecs.length; ++i) {
            quad._vecs[i] = quad._vecs[i].add(x, y, z);
        }
        quad.setUVOffset(new Vec3(-x, -y, -z));
        return quad;
    }

    public void setUVRotation(Direction side) {
        _uvRotation = side;
    }

    public int getTintIndex() {
        return _tintIndex;
    }

    public void setTintIndex(int tintIndex) {
        _tintIndex = tintIndex;
    }

    public Direction getFacing() {
        return _facing;
    }

    public Vec3[] getVecs() {
        return _vecs;
    }

    public TextureAtlasSprite getSprite() {
        return _sprite;
    }

    /**
     * Sets the texture. Whether a cover can replace the quad's texture stays as it was.
     */
    public void setSprite(TextureAtlasSprite sprite) {
        _sprite = sprite;
    }

    public int getRgb() {
        return _rgb;
    }

    public void setRgb(int rgb) {
        _rgb = rgb;
    }

    public boolean isMaxBrightness() {
        return _maxBrightness;
    }

    public void setMaxBrightness(boolean maxBrightness) {
        _maxBrightness = maxBrightness;
    }

    public ChunkSectionLayer getRenderLayer() {
        return _renderLayer;
    }

    public void setRenderLayer(ChunkSectionLayer renderLayer) {
        _renderLayer = renderLayer;
    }

    /**
     * Whether a cover replaces this quad's texture (it shows an uncovered frame).
     */
    public boolean canCover() {
        return _canCover;
    }

    /**
     * Bakes the quad for the chunk renderer.
     *
     * @param location where the quad's attribute sits, for oblique slope texture mapping
     */
    public BakedQuad bake(EnumAttributeLocation location) {
        String spriteName = _sprite.contents().name().toString();
        boolean isFloatingOverlay = spriteName.equals(GRASS_SIDE_OVERLAY)
            || spriteName.contains("overlay/overlay_") && spriteName.endsWith("_side");

        if (_slopedBakeFacing != null && (isSloped(Axis.X) || isSloped(Axis.Y) || isSloped(Axis.Z))) {
            _facing = _slopedBakeFacing;
            _isOblique = false;
            isFloatingOverlay = false;
        }

        // The chunk renderer's flat and AO lighters switch on the quad side, so it can't be missing
        Vec3 normal = _normal != null ? _normal : Vec3.atLowerCornerOf((_facing != null ? _facing : Direction.UP).getUnitVec3i());
        Direction direction = Direction.getApproximateNearest(normal.x, normal.y, normal.z);

        UV[] uv = rotateUV(QuadUtil.getUV(this, isFloatingOverlay, location));
        Vector3f[] positions = new Vector3f[4];
        long[] uvs = new long[4];
        for (int i = 0; i < 4; ++i) {
            positions[i] = new Vector3f((float) _vecs[i].x, (float) _vecs[i].y, (float) _vecs[i].z);
            float u = (float) uv[i].getU() / 16.0F;
            float v = (float) uv[i].getV() / 16.0F;
            if (_spriteRegion != null) {
                // The piece is shown turned clockwise: look up where each point comes from in the unturned piece
                float su = u, sv = v;
                switch (_spriteRegionTurns) {
                    case 1 -> { su = v; sv = 1.0F - u; }
                    case 2 -> { su = 1.0F - u; sv = 1.0F - v; }
                    case 3 -> { su = 1.0F - v; sv = u; }
                    default -> { }
                }
                u = _spriteRegion[0] + su * (_spriteRegion[2] - _spriteRegion[0]);
                v = _spriteRegion[1] + sv * (_spriteRegion[3] - _spriteRegion[1]);
            }
            uvs[i] = UVPair.pack(_sprite.getU(u), _sprite.getV(v));
        }
        int argb = 0xFF000000 | (_rgb & 0xFFFFFF);
        int packedNormal = BakedNormals.pack((float) normal.x, (float) normal.y, (float) normal.z);
        return new BakedQuad(
            positions[0], positions[1], positions[2], positions[3],
            uvs[0], uvs[1], uvs[2], uvs[3],
            direction,
            materialInfo(_sprite, _renderLayer, _tintIndex, _maxBrightness ? 15 : 0),
            BakedNormals.of(packedNormal),
            BakedColors.of(argb));
    }

    /**
     * Gets how a quad is drawn: its texture, chunk layer, tint and emitted light.
     */
    static BakedQuad.MaterialInfo materialInfo(TextureAtlasSprite sprite, ChunkSectionLayer layer, int tintIndex, int lightEmission) {
        boolean translucent = layer.translucent();
        return new BakedQuad.MaterialInfo(
            sprite,
            layer,
            translucent ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet(),
            translucent ? Sheets.translucentBlockItemGlintSheet() : Sheets.cutoutBlockItemGlintSheet(),
            translucent ? Sheets.translucentBlockItemGlintSpecialSheet() : Sheets.cutoutBlockItemGlintSpecialSheet(),
            tintIndex,
            null,
            lightEmission,
            true);
    }

    /**
     * Turns top and bottom face UVs back into the frame a part was authored in (see SidedUtil.rotate),
     * so the texture turns with the part.
     */
    private UV[] rotateUV(UV[] uv) {
        if (_uvRotation == null || (_facing != Direction.UP && _facing != Direction.DOWN)) {
            return uv;
        }
        boolean down = _facing == Direction.DOWN;
        UV[] out = new UV[uv.length];
        for (int i = 0; i < uv.length; ++i) {
            double x = uv[i].getU() / 16.0D;
            double z = down ? 1.0D - uv[i].getV() / 16.0D : uv[i].getV() / 16.0D;
            double ax, az;
            switch (_uvRotation) {
                case NORTH: ax = 1.0D - x; az = 1.0D - z; break;
                case EAST:  ax = 1.0D - z; az = x; break;
                case WEST:  ax = z; az = 1.0D - x; break;
                default:    ax = x; az = z; break;
            }
            out[i] = new UV(ax, down ? 1.0D - az : az);
        }
        return out;
    }

    /**
     * Rotates quads about axis. Positive axis is clockwise rotation.
     * Negative axis is counter-clockwise rotation.
     *
     * @param rotation the rotation enum
     */
    public void rotate(RotationUtil.Rotation rotation) {
        Vec3[] newVecs = new Vec3[_vecs.length];
        Direction newFacing = rotation.getRotatedFacing(_facing);
        for (int i = 0; i < _vecs.length; ++i) {
            Vec3 vecRot = _vecs[i];
            if (rotation.getRadians(Axis.X) > 0) {
                vecRot = rotateAroundX(vecRot, rotation.getRadians(Axis.X));
            }
            if (rotation.getRadians(Axis.Y) > 0) {
                vecRot = rotateAroundY(vecRot, rotation.getRadians(Axis.Y));
            }
            if (rotation.getRadians(Axis.Z) > 0) {
                vecRot = rotateAroundZ(vecRot, rotation.getRadians(Axis.Z));
            }
            newVecs[i] = vecRot;
        }
        _vecs = newVecs;
        applyFacing(true, newFacing);
    }

    private static Vec3 rotateAroundX(Vec3 vec, double radians) {
        double y = 0.5D + (vec.z - 0.5D) * Math.sin(radians) + (vec.y - 0.5D) * Math.cos(radians);
        double z = 0.5D + (vec.z - 0.5D) * Math.cos(radians) - (vec.y - 0.5D) * Math.sin(radians);
        return new Vec3(vec.x, y, z);
    }

    private static Vec3 rotateAroundY(Vec3 vec, double radians) {
        double z = 0.5D + (vec.x - 0.5D) * Math.sin(radians) + (vec.z - 0.5D) * Math.cos(radians);
        double x = 0.5D + (vec.x - 0.5D) * Math.cos(radians) - (vec.z - 0.5D) * Math.sin(radians);
        return new Vec3(x, vec.y, z);
    }

    private static Vec3 rotateAroundZ(Vec3 vec, double radians) {
        double x = 0.5D + (vec.y - 0.5D) * Math.sin(radians) + (vec.x - 0.5D) * Math.cos(radians);
        double y = 0.5D + (vec.y - 0.5D) * Math.cos(radians) - (vec.x - 0.5D) * Math.sin(radians);
        return new Vec3(x, y, vec.z);
    }

    public Direction getYSlope() {
        return _normal.y > 0.0D ? Direction.UP : Direction.DOWN;
    }

    public boolean isSloped(Axis axis) {
        double axis1 = Axis.X.equals(axis) ? _normal.x : Axis.Y.equals(axis) ? _normal.y : _normal.z;
        return QuadUtil.compare(axis1, -1.0D) > 0 &&
                QuadUtil.compare(axis1, 0.0D) != 0 &&
                QuadUtil.compare(axis1, 1.0D) < 0;
    }

    /**
     * Return quad facing as NWSE component.
     */
    public Direction getCardinalFacing() {
        if (!isSloped(Axis.Y)) {
            CarpentersBlocks.LOGGER.warn("getCardinalFacing called for non-sloping Y-axis");
        }
        return Direction.getApproximateNearest(_normal.x, 0, _normal.z);
    }

    /**
     * Applies facing to quad with <code>sort</code> parameter set to <code>true</code>.
     */
    public void applyFacing(Direction facing) {
        applyFacing(true, facing);
    }

    /**
     * Applies facing and calculates properties.
     * <p>
     * Quad must be rotated to match facing prior to calling this or runtime exception may occur.
     *
     * @param facing the new facing
     * @return <code>true</code> if transformation succeeds, <code>false</code> if it fails.
     */
    public boolean applyFacing(boolean sortVecs, Direction facing) {
        // The side the quad faces: its current winding once it has one (rotated or relabeled quads),
        // otherwise the facing it is built with
        Vec3[] previousVecs = _vecs;
        Vec3 front = _facing != null && _vecs != null ? computeNormal(_vecs) : null;
        boolean hadWinding = front != null;
        if (front == null) {
            front = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        }
        if (sortVecs) {
            _vecs = QuadUtil.sortVec3dsByFacing(facing, _vecs);
        }
        // Sorting by a facing that is not the face's own (sloped faces) can wind a face backwards or
        // scramble a triangle's corners, which hides it. A quad that already had a correct winding keeps it.
        if (sortVecs && hadWinding && (!QuadUtil.isValid(this) || isMisSorted(_vecs, front))) {
            _vecs = previousVecs;
        }
        if (!QuadUtil.isValid(this)) {
            return false;
        }
        _facing = facing;
        _normal = computeNormal(_vecs);
        _isOblique = isSloped(Axis.X) && isSloped(Axis.Y) && isSloped(Axis.Z);
        return true;
    }

    /**
     * Whether sorted vertices face away from the front, or repeat a triangle corner in non adjacent
     * slots (a twisted quad).
     */
    private static boolean isMisSorted(Vec3[] vecs, Vec3 front) {
        Vec3 normal = computeNormal(vecs);
        if (normal == null || normal.dot(front) < 0.0D) {
            return true;
        }
        return vecs.length == 4 && (vecs[0].equals(vecs[2]) || vecs[1].equals(vecs[3]));
    }

    /**
     * Gets the front normal of vertices (counter clockwise seen from the front), or null if degenerate.
     */
    private static Vec3 computeNormal(Vec3[] vecs) {
        Vec3[] unique = new LinkedHashSet<>(Arrays.asList(vecs)).toArray(new Vec3[0]);
        if (unique.length < 3 || unique[0] == null || unique[1] == null || unique[2] == null) {
            return null;
        }
        return (unique[1].subtract(unique[0])).cross(unique[2].subtract(unique[1])).normalize();
    }

    public Vec3 getNormal() {
        return _normal;
    }

    public boolean isObliqueSlope() {
        return _isOblique;
    }

    @SuppressWarnings("unused")
    private boolean isTriangle() {
        return new HashSet<>(Arrays.asList(this.getVecs())).size() < 4;
    }

    public Direction getSideCoverAltFacing() {
        if (isSloped(Axis.Y)) {
            return Double.compare(_normal.y, 0.0D) > 0 ? Direction.UP : Direction.DOWN;
        }
        return _facing;
    }

    public Vec3 getUVOffset() {
        return _uvOffset == null ? Vec3.ZERO : _uvOffset;
    }

    public void setUVOffset(Vec3 uvOffset) {
        _uvOffset = uvOffset;
    }

    public Direction getUVFloat() {
        return _uvFloat;
    }

    public Quad setUVFloat(Direction uvFloat) {
        _uvFloat = uvFloat;
        return this;
    }

    public boolean hasFloatingUV() {
        return _uvFloat != null;
    }

}
