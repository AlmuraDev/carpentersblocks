package com.carpentersblocks.client.model.shape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.carpentersblocks.CbConfig;
import com.carpentersblocks.blockentity.CbRenderData;
import com.carpentersblocks.block.BlockCarpentersCollapsibleBlock;
import com.carpentersblocks.util.block.CollapsibleUtil;
import com.carpentersblocks.block.IShapedBlock;
import com.carpentersblocks.block.ShapePart;
import com.carpentersblocks.client.model.CbSprites;
import com.carpentersblocks.client.model.Quad;
import com.carpentersblocks.client.model.QuadContainer;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The shapes Carpenter's blocks are drawn with, by the name their blockstate files and item definitions give (1.12.2's
 * baked block classes). Shapes are added here as blocks are ported.
 */
public final class BlockShapes {

    /**
     * Makes a block's uncovered quads. Called on chunk meshing threads.
     */
    @FunctionalInterface
    public interface Shape {

        /**
         * @param blockState the block
         * @param renderData the block's render data (BlockCoverable.getRenderData), or its inventory render data
         * @param data what the block is drawn from, or null for an item
         */
        QuadContainer create(BlockState blockState, int renderData, @Nullable CbRenderData data);

    }

    private static final Map<String, Shape> SHAPES = new HashMap<>();

    static {
        // The Carpenter's Block: a full block (1.12.2's BakedBlock)
        SHAPES.put("block", (blockState, renderData, data) -> {
            QuadContainer container = new QuadContainer(EnumAttributeLocation.HOST);
            TextureAtlasSprite sprite = CbSprites.get(CbSprites.UNCOVERED_QUARTERED);
            for (Direction facing : Direction.values()) {
                container.add(getFullFace(facing, sprite));
            }
            return container;
        });
        // Blocks made of boxes: barriers, gates, ladders, buttons, levers, torches... (1.12.2's BakedShapedBlock)
        SHAPES.put("shaped", (blockState, renderData, data) -> createShaped(blockState, renderData));
        // The Carpenter's Flower Pot, with its soil and plant (1.12.2's BakedFlowerPot)
        SHAPES.put("flower_pot", FlowerPotShape::create);
        // The Carpenter's Slope (1.12.2's BakedSlope)
        SHAPES.put("slope", (blockState, renderData, data) -> SlopeShapes.create(renderData));
        // The Carpenter's Collapsible Block (1.12.2's BakedCollapsibleBlock)
        SHAPES.put("collapsible", (blockState, renderData, data) -> createCollapsible(renderData));
    }

    private static QuadContainer createCollapsible(int renderData) {
        QuadContainer container = new QuadContainer(EnumAttributeLocation.HOST);
        TextureAtlasSprite sprite = CbSprites.get(CbSprites.UNCOVERED_SOLID);
        if (renderData == BlockCarpentersCollapsibleBlock.INVENTORY) {
            container.add(getFullFace(Direction.UP, sprite).offset(0.0D, -1.0D, 0.0D));
            container.add(getFullFace(Direction.DOWN, sprite));
            return container;
        }
        CollapsibleUtil util = new CollapsibleUtil(renderData);
        util.computeOffsets();
        List<Quad> quads = new ArrayList<>();
        if (!util.isFullCube()) {
            quads.add(RenderHelperCollapsible.getQuadYPosZNeg(util, sprite));
            quads.add(RenderHelperCollapsible.getQuadYPosZPos(util, sprite));
            quads.add(RenderHelperCollapsible.getQuadXNegYPos(util, sprite));
            quads.add(RenderHelperCollapsible.getQuadXPosYPos(util, sprite));
        } else {
            quads.add(getFullFace(Direction.UP, sprite));
        }
        quads.add(getFullFace(Direction.DOWN, sprite));
        quads.add(RenderHelperCollapsible.getQuadZNeg(util, sprite));
        quads.add(RenderHelperCollapsible.getQuadZPos(util, sprite));
        quads.add(RenderHelperCollapsible.getQuadXNeg(util, sprite));
        quads.add(RenderHelperCollapsible.getQuadXPos(util, sprite));
        for (Quad quad : quads) {
            if (quad != null) {
                quad.setSlopedBakeFacing(util.getFacing());
                container.add(quad);
            }
        }
        return container;
    }

    private BlockShapes() {}

    public static Optional<Shape> get(String name) {
        return Optional.ofNullable(SHAPES.get(name));
    }

    static QuadContainer createShaped(BlockState blockState, int renderData) {
        QuadContainer container = new QuadContainer(EnumAttributeLocation.HOST);
        if (!(blockState.getBlock() instanceof IShapedBlock shapedBlock)) {
            return container;
        }
        for (ShapePart part : shapedBlock.getRenderParts(renderData)) {
            TextureAtlasSprite sprite = part.getTexture() == null ? CbSprites.get(CbSprites.UNCOVERED_SOLID) : CbSprites.get(part.getTexture());
            List<Quad> quads = part.getCorners() != null ? getCuboidQuads(part.getCorners(), sprite) : getBoxQuads(part.getBox(), sprite);
            for (Quad quad : quads) {
                Identifier faceTexture = part.getTexture(quad.getFacing());
                if (faceTexture != null && !faceTexture.equals(part.getTexture())) {
                    quad.setSprite(CbSprites.get(faceTexture));
                }
                quad.setUVRotation(part.getUVRotation());
                int designPiece = part.getDesignPiece(quad.getFacing());
                if (designPiece >= 0) {
                    BedDesignPieces.apply(quad, designPiece);
                }
                // Panes blend on the translucent layer, or are cut out when "enableAlphaPanes" is off
                if (part.isTranslucent() && CbConfig.ENABLE_ALPHA_PANES.get()) {
                    quad.setRenderLayer(ChunkSectionLayer.TRANSLUCENT);
                }
                if (part.isFullBright()) {
                    quad.setMaxBrightness(true);
                }
                if (part.getColor() != -1) {
                    quad.setRgb(part.getColor());
                }
                container.add(quad);
            }
        }
        return container;
    }

    /**
     * Gets a whole face of the block (1.12.2's RenderHelper.getQuadYNeg and the like).
     */
    public static Quad getFullFace(Direction facing, TextureAtlasSprite sprite) {
        return switch (facing) {
            case DOWN -> Quad.getQuad(facing, sprite,
                new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 0.0D, 0.0D), new Vec3(1.0D, 0.0D, 0.0D), new Vec3(1.0D, 0.0D, 1.0D));
            case UP -> Quad.getQuad(facing, sprite,
                new Vec3(0.0D, 1.0D, 0.0D), new Vec3(0.0D, 1.0D, 1.0D), new Vec3(1.0D, 1.0D, 1.0D), new Vec3(1.0D, 1.0D, 0.0D));
            case NORTH -> Quad.getQuad(facing, sprite,
                new Vec3(1.0D, 1.0D, 0.0D), new Vec3(1.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D));
            case SOUTH -> Quad.getQuad(facing, sprite,
                new Vec3(0.0D, 1.0D, 1.0D), new Vec3(0.0D, 0.0D, 1.0D), new Vec3(1.0D, 0.0D, 1.0D), new Vec3(1.0D, 1.0D, 1.0D));
            case WEST -> Quad.getQuad(facing, sprite,
                new Vec3(0.0D, 1.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 1.0D), new Vec3(0.0D, 1.0D, 1.0D));
            case EAST -> Quad.getQuad(facing, sprite,
                new Vec3(1.0D, 1.0D, 1.0D), new Vec3(1.0D, 0.0D, 1.0D), new Vec3(1.0D, 0.0D, 0.0D), new Vec3(1.0D, 1.0D, 0.0D));
        };
    }

    /**
     * Gets the six faces of a box in block coordinates (0 to 1). Texture coordinates follow the vertex positions, so
     * adjacent boxes line up (1.12.2's RenderHelper.getBoxQuads).
     */
    public static List<Quad> getBoxQuads(AABB box, TextureAtlasSprite sprite) {
        List<Quad> quads = new ArrayList<>(6);
        double x0 = box.minX, y0 = box.minY, z0 = box.minZ;
        double x1 = box.maxX, y1 = box.maxY, z1 = box.maxZ;
        addIfValid(quads, Quad.getQuad(Direction.DOWN, sprite,
            new Vec3(x0, y0, z1), new Vec3(x0, y0, z0), new Vec3(x1, y0, z0), new Vec3(x1, y0, z1)));
        addIfValid(quads, Quad.getQuad(Direction.UP, sprite,
            new Vec3(x0, y1, z0), new Vec3(x0, y1, z1), new Vec3(x1, y1, z1), new Vec3(x1, y1, z0)));
        addIfValid(quads, Quad.getQuad(Direction.NORTH, sprite,
            new Vec3(x1, y1, z0), new Vec3(x1, y0, z0), new Vec3(x0, y0, z0), new Vec3(x0, y1, z0)));
        addIfValid(quads, Quad.getQuad(Direction.SOUTH, sprite,
            new Vec3(x0, y1, z1), new Vec3(x0, y0, z1), new Vec3(x1, y0, z1), new Vec3(x1, y1, z1)));
        addIfValid(quads, Quad.getQuad(Direction.WEST, sprite,
            new Vec3(x0, y1, z0), new Vec3(x0, y0, z0), new Vec3(x0, y0, z1), new Vec3(x0, y1, z1)));
        addIfValid(quads, Quad.getQuad(Direction.EAST, sprite,
            new Vec3(x1, y1, z1), new Vec3(x1, y0, z1), new Vec3(x1, y0, z0), new Vec3(x1, y1, z0)));
        return quads;
    }

    // Faces of a cuboid by corner index (bottom 0-3, top 4-7)
    private static final int[][] CUBOID_FACES = {
        { 0, 1, 2, 3 }, { 7, 6, 5, 4 }, { 1, 0, 4, 5 }, { 2, 1, 5, 6 }, { 3, 2, 6, 7 }, { 0, 3, 7, 4 } };

    /**
     * Gets the six faces of a tilted cuboid. Each face uses the block side nearest to its normal, which decides texture
     * mapping and shading (1.12.2's RenderHelper.getCuboidQuads).
     *
     * @param corners the 8 corners, bottom face 0-3 and top face 4-7
     */
    public static List<Quad> getCuboidQuads(Vec3[] corners, TextureAtlasSprite sprite) {
        List<Quad> quads = new ArrayList<>(6);
        Vec3 center = Vec3.ZERO;
        for (Vec3 corner : corners) {
            center = center.add(corner.scale(1.0D / corners.length));
        }
        for (int[] face : CUBOID_FACES) {
            Vec3[] vecs = { corners[face[0]], corners[face[1]], corners[face[2]], corners[face[3]] };
            Vec3 faceCenter = vecs[0].add(vecs[1]).add(vecs[2]).add(vecs[3]).scale(0.25D);
            // Outward direction, whichever way the corners wind
            Vec3 outward = faceCenter.subtract(center);
            Direction facing = Direction.getApproximateNearest(outward.x, outward.y, outward.z);
            addIfValid(quads, Quad.getQuad(facing, true, sprite, vecs));
        }
        return quads;
    }

    private static void addIfValid(List<Quad> quads, Quad quad) {
        if (quad != null) {
            quads.add(quad);
        }
    }

}
