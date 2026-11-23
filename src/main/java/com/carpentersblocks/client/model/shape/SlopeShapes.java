package com.carpentersblocks.client.model.shape;

import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.core.Direction.WEST;


import com.carpentersblocks.block.data.SlopeData;
import com.carpentersblocks.client.model.Quad;
import com.carpentersblocks.client.model.CbSprites;
import com.carpentersblocks.client.model.QuadContainer;
import com.carpentersblocks.util.RotationUtil.Rotation;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * The Carpenter's Slope shapes (1.12.2's BakedSlope): each slope type authored in one direction, then rotated.
 */
public final class SlopeShapes {

    private SlopeShapes() {}

    /**
     * Gets the uncovered slope for its block data (slope type and rotation).
     */
    public static QuadContainer create(int data) {
        QuadContainer renderPkg = new QuadContainer(EnumAttributeLocation.HOST);
        switch (SlopeData.getType(data)) {
            case WEDGE -> fillWedge(renderPkg);
            case WEDGE_INTERIOR -> fillWedgeInterior(renderPkg);
            case WEDGE_EXTERIOR -> fillWedgeExterior(renderPkg);
            case OBLIQUE_INTERIOR -> fillObliqueInterior(renderPkg);
            case OBLIQUE_EXTERIOR -> fillObliqueExterior(renderPkg);
            case PRISM_WEDGE -> fillPrismWedge(renderPkg);
            case PRISM -> fillPrism(renderPkg, 0);
            case PRISM_1P -> fillPrism(renderPkg, 1);
            case PRISM_2P -> fillPrism(renderPkg, 2);
            case PRISM_3P -> fillPrism(renderPkg, 3);
            case PRISM_4P -> fillPrism(renderPkg, 4);
            case INVERT_PRISM -> fillInvertPrism(renderPkg, 0);
            case INVERT_PRISM_1P -> fillInvertPrism(renderPkg, 1);
            case INVERT_PRISM_2P -> fillInvertPrism(renderPkg, 2);
            case INVERT_PRISM_3P -> fillInvertPrism(renderPkg, 3);
            case INVERT_PRISM_4P -> fillInvertPrism(renderPkg, 4);
        }
        renderPkg.rotate(Rotation.get(data));
        return renderPkg;
    }

    private static void fillWedge(QuadContainer renderPkg) {
        renderPkg.add(BlockShapes.getFullFace(Direction.DOWN, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        renderPkg.add(BlockShapes.getFullFace(Direction.NORTH, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        renderPkg.add(RenderHelperSlope.getWedgeXNeg());
        renderPkg.add(RenderHelperSlope.getWedgeXPos());
        renderPkg.add(
            Quad.getQuad(
                SOUTH,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(1.0D, 0.0D, 1.0D),
                new Vec3(1.0D, 1.0D, 0.0D),
                new Vec3(0.0D, 1.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D)));
    }
    
    private static void fillWedgeExterior(QuadContainer renderPkg) {
        renderPkg.add(BlockShapes.getFullFace(Direction.DOWN, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        renderPkg.add(RenderHelperSlope.getWedgeXNeg());
        renderPkg.add(RenderHelperSlope.getWedgeExteriorZNeg());
        renderPkg.add(
            Quad.getQuad(
                SOUTH,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(0.0D, 1.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D),
                new Vec3(1.0D, 0.0D, 1.0D)));
        renderPkg.add(
            Quad.getQuad(
                EAST,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(1.0D, 0.0D, 1.0D),
                new Vec3(1.0D, 0.0D, 0.0D),
                new Vec3(0.0D, 1.0D, 0.0D)));
    }

    private static void fillWedgeInterior(QuadContainer renderPkg) {
        renderPkg.add(BlockShapes.getFullFace(Direction.DOWN, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        renderPkg.add(BlockShapes.getFullFace(Direction.WEST, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        renderPkg.add(BlockShapes.getFullFace(Direction.NORTH, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        renderPkg.add(RenderHelperSlope.getWedgeXPos());
        renderPkg.add(RenderHelperSlope.getWedgeInteriorZPos());
        renderPkg.add(
            Quad.getQuad(
                SOUTH,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(0.0D, 1.0D, 0.0D),
                new Vec3(1.0D, 0.0D, 1.0D),
                new Vec3(1.0D, 1.0D, 0.0D)));
        renderPkg.add(
            Quad.getQuad(
                EAST,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(0.0D, 1.0D, 1.0D),
                new Vec3(1.0D, 0.0D, 1.0D),
                new Vec3(0.0D, 1.0D, 0.0D)));
    }
    
    private static void fillObliqueInterior(QuadContainer renderPkg) {
        renderPkg.add(BlockShapes.getFullFace(Direction.DOWN, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        renderPkg.add(BlockShapes.getFullFace(Direction.NORTH, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        renderPkg.add(BlockShapes.getFullFace(Direction.WEST, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        renderPkg.add(RenderHelperSlope.getWedgeInteriorZPos());
        renderPkg.add(RenderHelperSlope.getWedgeXPos());
        renderPkg.add(
            Quad.getQuad(
                UP,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(0.0D, 1.0D, 0.0D),
                new Vec3(0.0D, 1.0D, 1.0D),
                new Vec3(1.0D, 1.0D, 0.0D)));
        // Right oblique slope part
        renderPkg.add(
            Quad.getQuad(
                SOUTH,
                CbSprites.get(CbSprites.UNCOVERED_OBLIQUE_POS),
                new Vec3(0.0D, 1.0D, 1.0D),
                new Vec3(1.0D, 0.0D, 1.0D),
                new Vec3(0.5D, 1.0D, 0.5D)));
        // Left oblique slope part
        renderPkg.add(
            Quad.getQuad(
                SOUTH,
                CbSprites.get(CbSprites.UNCOVERED_OBLIQUE_POS),
                new Vec3(0.5D, 1.0D, 0.5D),
                new Vec3(1.0D, 0.0D, 1.0D),
                new Vec3(1.0D, 1.0D, 0.0D)));
    }
    
    private static void fillObliqueExterior(QuadContainer renderPkg) {
        renderPkg.add(
            Quad.getQuad(
                DOWN,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(0.0D, 0.0D, 1.0D),
                new Vec3(0.0D, 0.0D, 0.0D),
                new Vec3(1.0D, 0.0D, 0.0D)));
        renderPkg.add(
            Quad.getQuad(
                SOUTH,
                CbSprites.get(CbSprites.UNCOVERED_OBLIQUE_NEG),
                new Vec3(0.0D, 1.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D),
                new Vec3(1.0D, 0.0D, 0.0D)));
        renderPkg.add(RenderHelperSlope.getWedgeExteriorZNeg());
        renderPkg.add(
            Quad.getQuad(
                WEST,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(0.0D, 1.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D)));
    }
    
    private static void fillPrismWedge(QuadContainer renderPkg) {
        renderPkg.add(BlockShapes.getFullFace(Direction.DOWN, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        renderPkg.add(BlockShapes.getFullFace(Direction.NORTH, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        renderPkg.add(RenderHelperSlope.getWedgeXNeg());
        renderPkg.add(RenderHelperSlope.getWedgeXPos());
        renderPkg.add(
            Quad.getQuad(
                SOUTH,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(0.0D, 1.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D),
                new Vec3(0.5D, 0.5D, 0.5D),
                new Vec3(0.5D, 1.0D, 0.0D)));
        renderPkg.add(
            Quad.getQuad(
                SOUTH,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(0.5D, 1.0D, 0.0D),
                new Vec3(0.5D, 0.5D, 0.5D),
                new Vec3(1.0D, 0.0D, 1.0D),
                new Vec3(1.0D, 1.0D, 0.0D)));
        renderPkg.add(
            Quad.getQuad(
                WEST,
                CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                new Vec3(0.5D, 0.5D, 0.5D),
                new Vec3(0.0D, 0.0D, 1.0D),
                new Vec3(0.5D, 0.5D, 1.0D)));
        renderPkg.add(
            Quad.getQuad(
                EAST,
                CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                new Vec3(0.5D, 0.5D, 1.0D),
                new Vec3(1.0D, 0.0D, 1.0D),
                new Vec3(0.5D, 0.5D, 0.5D)));
        renderPkg.add(
            Quad.getQuad(
                SOUTH,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(0.5D, 0.5D, 1.0D),
                new Vec3(0.0D, 0.0D, 1.0D),
                new Vec3(0.5D, 0.0D, 1.0D)));
        renderPkg.add(
            Quad.getQuad(
                SOUTH,
                CbSprites.get(CbSprites.UNCOVERED_FULL),
                new Vec3(0.5D, 0.5D, 1.0D),
                new Vec3(0.5D, 0.0D, 1.0D),
                new Vec3(1.0D, 0.0D, 1.0D)));
    }
    
    private static void fillPrism(QuadContainer renderPkg, int numPoints) {
        renderPkg.add(BlockShapes.getFullFace(Direction.DOWN, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        switch (numPoints) {
            case 0:
                renderPkg.add(RenderHelperSlope.getPrismSlopeZNeg());
                renderPkg.add(
                    Quad.getQuad(
                        SOUTH,
                        CbSprites.get(CbSprites.UNCOVERED_FULL),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(0.0D, 0.0D, 1.0D),
                        new Vec3(1.0D, 0.0D, 1.0D)));
                renderPkg.add(
                    Quad.getQuad(
                        WEST,
                        CbSprites.get(CbSprites.UNCOVERED_FULL),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(0.0D, 0.0D, 0.0D),
                        new Vec3(0.0D, 0.0D, 1.0D)));
                renderPkg.add(
                    Quad.getQuad(
                        EAST,
                        CbSprites.get(CbSprites.UNCOVERED_FULL),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(1.0D, 0.0D, 1.0D),
                        new Vec3(1.0D, 0.0D, 0.0D)));
                break;
            case 1:
                renderPkg.add(RenderHelperSlope.getPrismSlopeZNeg());
                renderPkg.add(RenderHelperSlope.getPrismZPosXNeg());
                renderPkg.add(RenderHelperSlope.getPrismZPosXPos());
                renderPkg.add(
                    Quad.getQuad(
                        WEST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(0.0D, 0.0D, 0.0D),
                        new Vec3(0.0D, 0.0D, 1.0D),
                        new Vec3(0.5D, 0.5D, 1.0D)));
                renderPkg.add(
                    Quad.getQuad(
                        EAST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.5D, 0.5D, 1.0D),
                        new Vec3(1.0D, 0.0D, 1.0D),
                        new Vec3(1.0D, 0.0D, 0.0D),
                        new Vec3(0.5D, 0.5D, 0.5D)));
                break;
            case 2:
                renderPkg.add(RenderHelperSlope.getPrismZNegXNeg());
                renderPkg.add(RenderHelperSlope.getPrismZNegXPos());
                renderPkg.add(RenderHelperSlope.getPrismZPosXNeg());
                renderPkg.add(RenderHelperSlope.getPrismZPosXPos());
                renderPkg.add(
                    Quad.getQuad(
                        WEST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.5D, 0.5D, 0.0D),
                        new Vec3(0.0D, 0.0D, 0.0D),
                        new Vec3(0.0D, 0.0D, 1.0D),
                        new Vec3(0.5D, 0.5D, 1.0D)));
                renderPkg.add(
                    Quad.getQuad(
                        EAST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.5D, 0.5D, 1.0D),
                        new Vec3(1.0D, 0.0D, 1.0D),
                        new Vec3(1.0D, 0.0D, 0.0D),
                        new Vec3(0.5D, 0.5D, 0.0D)));
                break;
            case 3:
                renderPkg.add(
                    Quad.getQuad(
                        NORTH,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(1.0D, 0.5D, 0.5D),
                        new Vec3(1.0D, 0.0D, 0.0D),
                        new Vec3(0.0D, 0.0D, 0.0D),
                        new Vec3(0.0D, 0.5D, 0.5D)));
                renderPkg.add(RenderHelperSlope.getPrismSlopedZPosXNeg());
                renderPkg.add(RenderHelperSlope.getPrismSlopedZPosXPos());
                renderPkg.add(RenderHelperSlope.getPrismZPosXNeg());
                renderPkg.add(RenderHelperSlope.getPrismZPosXPos());
                renderPkg.add(RenderHelperSlope.getPrismXNegZNeg());
                renderPkg.add(RenderHelperSlope.getPrismXNegZPos());
                renderPkg.add(RenderHelperSlope.getPrismSlopedXNegZPos());
                renderPkg.add(RenderHelperSlope.getPrismXPosZPos());
                renderPkg.add(RenderHelperSlope.getPrismXPosZNeg());
                renderPkg.add(RenderHelperSlope.getPrismSlopedXPosZPos());
                break;
            case 4:
                renderPkg.add(RenderHelperSlope.getPrismZNegXNeg());
                renderPkg.add(RenderHelperSlope.getPrismZNegXPos());
                renderPkg.add(RenderHelperSlope.getPrismZPosXNeg());
                renderPkg.add(RenderHelperSlope.getPrismZPosXPos());
                renderPkg.add(RenderHelperSlope.getPrismXNegZNeg());
                renderPkg.add(RenderHelperSlope.getPrismXNegZPos());
                renderPkg.add(RenderHelperSlope.getPrismXPosZPos());
                renderPkg.add(RenderHelperSlope.getPrismXPosZNeg());
                // Sloped faces
                renderPkg.add(
                    Quad.getQuad(
                        NORTH,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(1.0D, 0.5D, 0.5D),
                        new Vec3(1.0D, 0.0D, 0.0D),
                        new Vec3(0.5D, 0.5D, 0.5D)));
                renderPkg.add(
                    Quad.getQuad(
                        NORTH,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(0.0D, 0.0D, 0.0D),
                        new Vec3(0.0D, 0.5D, 0.5D)));
                renderPkg.add(RenderHelperSlope.getPrismSlopedZPosXNeg());
                renderPkg.add(RenderHelperSlope.getPrismSlopedZPosXPos());
                renderPkg.add(
                    Quad.getQuad(
                        WEST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.5D, 0.5D, 0.0D),
                        new Vec3(0.0D, 0.0D, 0.0D),
                        new Vec3(0.5D, 0.5D, 0.5D)));
                renderPkg.add(RenderHelperSlope.getPrismSlopedXNegZPos());
                renderPkg.add(RenderHelperSlope.getPrismSlopedXPosZPos());
                renderPkg.add(
                    Quad.getQuad(
                        EAST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(1.0D, 0.0D, 0.0D),
                        new Vec3(0.5D, 0.5D, 0.0D)));
                break;
        }
    }
    
    private static void fillInvertPrism(QuadContainer renderPkg, int numPoints) {
        renderPkg.add(BlockShapes.getFullFace(Direction.DOWN, CbSprites.get(CbSprites.UNCOVERED_FULL)));
        switch (numPoints) {
            case 0:
                renderPkg.add(BlockShapes.getFullFace(Direction.NORTH, CbSprites.get(CbSprites.UNCOVERED_FULL)));
                renderPkg.add(BlockShapes.getFullFace(Direction.SOUTH, CbSprites.get(CbSprites.UNCOVERED_FULL)));
                renderPkg.add(BlockShapes.getFullFace(Direction.WEST, CbSprites.get(CbSprites.UNCOVERED_FULL)));
                renderPkg.add(BlockShapes.getFullFace(Direction.EAST, CbSprites.get(CbSprites.UNCOVERED_FULL)));
                renderPkg.add(
                    Quad.getQuad(
                        NORTH,
                        CbSprites.get(CbSprites.UNCOVERED_FULL),
                        new Vec3(1.0D, 1.0D, 1.0D),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(0.0D, 1.0D, 1.0D)));
                renderPkg.add(RenderHelperSlope.getInvertPrismSlopedZPos());
                renderPkg.add(
                    Quad.getQuad(
                        WEST,
                        CbSprites.get(CbSprites.UNCOVERED_FULL),
                        new Vec3(1.0D, 1.0D, 0.0D),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(1.0D, 1.0D, 1.0D)));
                renderPkg.add(
                    Quad.getQuad(
                        EAST,
                        CbSprites.get(CbSprites.UNCOVERED_FULL),
                        new Vec3(0.0D, 1.0D, 1.0D),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(0.0D, 1.0D, 0.0D)));
                break;
            case 1:
                renderPkg.add(BlockShapes.getFullFace(Direction.NORTH, CbSprites.get(CbSprites.UNCOVERED_FULL)));
                renderPkg.add(BlockShapes.getFullFace(Direction.WEST, CbSprites.get(CbSprites.UNCOVERED_FULL)));
                renderPkg.add(BlockShapes.getFullFace(Direction.EAST, CbSprites.get(CbSprites.UNCOVERED_FULL)));
                renderPkg.add(RenderHelperSlope.getInvertPrismZPosXNeg());
                renderPkg.add(RenderHelperSlope.getInvertPrismZPosXPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismSlopedZPos());
                renderPkg.add(
                    Quad.getQuad(
                        WEST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(1.0D, 1.0D, 0.0D),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(0.5D, 0.5D, 1.0D),
                        new Vec3(1.0D, 1.0D, 1.0D)));
                renderPkg.add(
                    Quad.getQuad(
                        EAST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.0D, 1.0D, 1.0D),
                        new Vec3(0.5D, 0.5D, 1.0D),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(0.0D, 1.0D, 0.0D)));
                break;
            case 2:
                renderPkg.add(BlockShapes.getFullFace(Direction.WEST, CbSprites.get(CbSprites.UNCOVERED_FULL)));
                renderPkg.add(BlockShapes.getFullFace(Direction.EAST, CbSprites.get(CbSprites.UNCOVERED_FULL)));
                renderPkg.add(RenderHelperSlope.getInvertPrismZPosXNeg());
                renderPkg.add(RenderHelperSlope.getInvertPrismZPosXPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismZNegXPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismZNegXNeg());
                renderPkg.add(
                    Quad.getQuad(
                        WEST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(1.0D, 1.0D, 0.0D),
                        new Vec3(0.5D, 0.5D, 0.0D),
                        new Vec3(0.5D, 0.5D, 1.0D),
                        new Vec3(1.0D, 1.0D, 1.0D)));
                renderPkg.add(
                    Quad.getQuad(
                        EAST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.0D, 1.0D, 1.0D),
                        new Vec3(0.5D, 0.5D, 1.0D),
                        new Vec3(0.5D, 0.5D, 0.0D),
                        new Vec3(0.0D, 1.0D, 0.0D)));
                break;
            case 3:
                renderPkg.add(BlockShapes.getFullFace(Direction.NORTH, CbSprites.get(CbSprites.UNCOVERED_FULL)));
                renderPkg.add(RenderHelperSlope.getInvertPrismZPosXNeg());
                renderPkg.add(RenderHelperSlope.getInvertPrismZPosXPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismXNegZNeg());
                renderPkg.add(RenderHelperSlope.getInvertPrismXNegZPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismXPosZPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismXPosZNeg());
                // Sloped faces
                renderPkg.add(
                    Quad.getQuad(
                        SOUTH,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.0D, 1.0D, 0.0D),
                        new Vec3(0.0D, 0.5D, 0.5D),
                        new Vec3(1.0D, 0.5D, 0.5D),
                        new Vec3(1.0D, 1.0D, 0.0D)));
                renderPkg.add(RenderHelperSlope.getInvertPrismSlopeZNegXPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismSlopeZNegXNeg());
                renderPkg.add(RenderHelperSlope.getInvertPrismSlopeXNegZPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismSlopeXPosZPos());
                break;
            case 4:
                renderPkg.add(RenderHelperSlope.getInvertPrismZNegXPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismZNegXNeg());
                renderPkg.add(RenderHelperSlope.getInvertPrismZPosXNeg());
                renderPkg.add(RenderHelperSlope.getInvertPrismZPosXPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismXNegZNeg());
                renderPkg.add(RenderHelperSlope.getInvertPrismXNegZPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismXPosZPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismXPosZNeg());
                // Sloped faces
                renderPkg.add(RenderHelperSlope.getInvertPrismSlopeZNegXPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismSlopeZNegXNeg());
                renderPkg.add(
                    Quad.getQuad(
                        SOUTH,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.0D, 1.0D, 0.0D),
                        new Vec3(0.0D, 0.5D, 0.5D),
                        new Vec3(0.5D, 0.5D, 0.5D)));
                renderPkg.add(
                    Quad.getQuad(
                        SOUTH,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(1.0D, 0.5D, 0.5D),
                        new Vec3(1.0D, 1.0D, 0.0D)));
                renderPkg.add(
                    Quad.getQuad(
                        WEST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(1.0D, 1.0D, 0.0D),
                        new Vec3(0.5D, 0.5D, 0.0D),
                        new Vec3(0.5D, 0.5D, 0.5D)));
                renderPkg.add(RenderHelperSlope.getInvertPrismSlopeXNegZPos());
                renderPkg.add(RenderHelperSlope.getInvertPrismSlopeXPosZPos());
                renderPkg.add(
                    Quad.getQuad(
                        EAST,
                        CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
                        new Vec3(0.5D, 0.5D, 0.5D),
                        new Vec3(0.5D, 0.5D, 0.0D),
                        new Vec3(0.0D, 1.0D, 0.0D)));
                break;
        }
    }
    
}