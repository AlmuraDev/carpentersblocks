package com.carpentersblocks.renderer.helper;

import java.util.ArrayList;
import java.util.List;

import com.carpentersblocks.renderer.Quad;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHelper {

    /**
     * Gets the six faces of a box in block coordinates (0 to 1).
     * <p>
     * Texture coordinates follow the vertex positions, so adjacent boxes line up.
     *
     * @param box the box
     * @param sprite the texture
     * @return the quads
     */
    public static List<Quad> getBoxQuads(AxisAlignedBB box, TextureAtlasSprite sprite) {
        List<Quad> quads = new ArrayList<Quad>(6);
        double x0 = box.minX, y0 = box.minY, z0 = box.minZ;
        double x1 = box.maxX, y1 = box.maxY, z1 = box.maxZ;
        quads.add(Quad.getQuad(EnumFacing.DOWN, sprite,
            new Vec3d(x0, y0, z1), new Vec3d(x0, y0, z0), new Vec3d(x1, y0, z0), new Vec3d(x1, y0, z1)));
        quads.add(Quad.getQuad(EnumFacing.UP, sprite,
            new Vec3d(x0, y1, z0), new Vec3d(x0, y1, z1), new Vec3d(x1, y1, z1), new Vec3d(x1, y1, z0)));
        quads.add(Quad.getQuad(EnumFacing.NORTH, sprite,
            new Vec3d(x1, y1, z0), new Vec3d(x1, y0, z0), new Vec3d(x0, y0, z0), new Vec3d(x0, y1, z0)));
        quads.add(Quad.getQuad(EnumFacing.SOUTH, sprite,
            new Vec3d(x0, y1, z1), new Vec3d(x0, y0, z1), new Vec3d(x1, y0, z1), new Vec3d(x1, y1, z1)));
        quads.add(Quad.getQuad(EnumFacing.WEST, sprite,
            new Vec3d(x0, y1, z0), new Vec3d(x0, y0, z0), new Vec3d(x0, y0, z1), new Vec3d(x0, y1, z1)));
        quads.add(Quad.getQuad(EnumFacing.EAST, sprite,
            new Vec3d(x1, y1, z1), new Vec3d(x1, y0, z1), new Vec3d(x1, y0, z0), new Vec3d(x1, y1, z0)));
        return quads;
    }

    // Faces of a cuboid by corner index (bottom 0-3, top 4-7)
    private static final int[][] CUBOID_FACES = {
        { 0, 1, 2, 3 }, { 7, 6, 5, 4 }, { 1, 0, 4, 5 }, { 2, 1, 5, 6 }, { 3, 2, 6, 7 }, { 0, 3, 7, 4 } };

    /**
     * Gets the six faces of a tilted cuboid.
     * <p>
     * Each face uses the block facing nearest to its normal, which decides texture mapping and shading.
     *
     * @param corners the 8 corners, bottom face 0-3 and top face 4-7
     * @param sprite the texture
     * @return the quads
     */
    public static List<Quad> getCuboidQuads(Vec3d[] corners, TextureAtlasSprite sprite) {
        List<Quad> quads = new ArrayList<Quad>(6);
        Vec3d center = Vec3d.ZERO;
        for (Vec3d corner : corners) {
            center = center.add(corner.scale(1.0D / corners.length));
        }
        for (int[] face : CUBOID_FACES) {
            Vec3d[] vecs = { corners[face[0]], corners[face[1]], corners[face[2]], corners[face[3]] };
            Vec3d faceCenter = vecs[0].add(vecs[1]).add(vecs[2]).add(vecs[3]).scale(0.25D);
            // Outward direction, whichever way the corners wind
            Vec3d outward = faceCenter.subtract(center);
            EnumFacing facing = EnumFacing.getFacingFromVector((float) outward.x, (float) outward.y, (float) outward.z);
            Quad quad = Quad.getQuad(facing, true, sprite, vecs);
            if (quad != null) {
                quads.add(quad);
            }
        }
        return quads;
    }

    /**
     * Renders the given texture to the bottom face of the block. Args: slope, x, y, z, texture
     */
    public static Quad getQuadYNeg(TextureAtlasSprite sprite) {
        return Quad.getQuad(
            EnumFacing.DOWN,
            sprite,
        	new Vec3d(0.0D, 0.0D, 1.0D),
        	new Vec3d(0.0D, 0.0D, 0.0D),
    		new Vec3d(1.0D, 0.0D, 0.0D),
    		new Vec3d(1.0D, 0.0D, 1.0D)
    	);
    }

    /**
     * Renders the given texture to the top face of the block. Args: slope, x, y, z, texture
     */
    public static Quad getQuadYPos(TextureAtlasSprite sprite) {
        return Quad.getQuad(
            EnumFacing.UP,
            sprite,
        	new Vec3d(0.0D, 1.0D, 0.0D),
        	new Vec3d(0.0D, 1.0D, 1.0D),
        	new Vec3d(1.0D, 1.0D, 1.0D),
        	new Vec3d(1.0D, 1.0D, 0.0D)
    	);
    }

    /**
     * Renders the given texture to the North face of the block.  Args: slope, x, y, z, texture
     */
    public static Quad getQuadZNeg(TextureAtlasSprite sprite) {
        return Quad.getQuad(
            EnumFacing.NORTH,
            sprite,
        	new Vec3d(1.0D, 1.0D, 0.0D),
        	new Vec3d(1.0D, 0.0D, 0.0D),
        	new Vec3d(0.0D, 0.0D, 0.0D),
        	new Vec3d(0.0D, 1.0D, 0.0D)
    	);
    }

    /**
     * Renders the given texture to the South face of the block.  Args: slope, x, y, z, texture
     */
    public static Quad getQuadZPos(TextureAtlasSprite sprite) {
        return Quad.getQuad(
            EnumFacing.SOUTH,
            sprite,
    		new Vec3d(0.0D, 1.0D, 1.0D),
    		new Vec3d(0.0D, 0.0D, 1.0D),
    		new Vec3d(1.0D, 0.0D, 1.0D),
    		new Vec3d(1.0D, 1.0D, 1.0D)
    	);
    }

    /**
     * Renders the given texture to the West face of the block.  Args: slope, x, y, z, texture
     */
    public static Quad getQuadXNeg(TextureAtlasSprite sprite) {
        return Quad.getQuad(
            EnumFacing.WEST,
            sprite,
    		new Vec3d(0.0D, 1.0D, 0.0D),
    		new Vec3d(0.0D, 0.0D, 0.0D),
    		new Vec3d(0.0D, 0.0D, 1.0D),
    		new Vec3d(0.0D, 1.0D, 1.0D)
    	);
    }

    /**
     * Renders the given texture to the East face of the block.  Args: slope, x, y, z, texture
     */
    public static Quad getQuadXPos(TextureAtlasSprite sprite) {
        return Quad.getQuad(
            EnumFacing.EAST,
            sprite,
    		new Vec3d(1.0D, 1.0D, 1.0D),
    		new Vec3d(1.0D, 0.0D, 1.0D),
    		new Vec3d(1.0D, 0.0D, 0.0D),
    		new Vec3d(1.0D, 1.0D, 0.0D)
    	);
    }

}
