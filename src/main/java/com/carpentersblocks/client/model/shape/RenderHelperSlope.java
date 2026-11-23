package com.carpentersblocks.client.model.shape;

import com.carpentersblocks.client.model.Quad;
import com.carpentersblocks.client.model.CbSprites;
import com.carpentersblocks.client.model.QuadContainer;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class RenderHelperSlope {

    public static Quad getWedgeXNeg() {
        return Quad.getQuad(
            Direction.WEST,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.0D, 1.0D, 0.0D),
            new Vec3(0.0D, 0.0D, 0.0D),
            new Vec3(0.0D, 0.0D, 1.0D));
    }
    
    public static Quad getWedgeXPos() {
        return Quad.getQuad(
            Direction.EAST,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(1.0D, 0.0D, 1.0D),
            new Vec3(1.0D, 0.0D, 0.0D),
            new Vec3(1.0D, 1.0D, 0.0D));
    }
    
    public static Quad getWedgeInteriorZPos() {
        return Quad.getQuad(
            Direction.SOUTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.0D, 1.0D, 1.0D),
            new Vec3(0.0D, 0.0D, 1.0D),
            new Vec3(1.0D, 0.0D, 1.0D));
    }
    
    public static Quad getWedgeExteriorZNeg() {
        return Quad.getQuad(
            Direction.NORTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(1.0D, 0.0D, 0.0D),
            new Vec3(0.0D, 0.0D, 0.0D),
            new Vec3(0.0D, 1.0D, 0.0D));
    }
    
    public static Quad getPrismSlopeZNeg() {
        return Quad.getQuad(
            Direction.NORTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.5D, 0.5D, 0.5D),
            new Vec3(1.0D, 0.0D, 0.0D),
            new Vec3(0.0D, 0.0D, 0.0D));
    }
    
    public static Quad getPrismZPosXNeg() {
        return Quad.getQuad(
            Direction.SOUTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.5D, 0.5D, 1.0D),
            new Vec3(0.0D, 0.0D, 1.0D),
            new Vec3(0.5D, 0.0D, 1.0D));
    }
    
    public static Quad getPrismZPosXPos() {
        return Quad.getQuad(
            Direction.SOUTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.5D, 0.5D, 1.0D),
            new Vec3(0.5D, 0.0D, 1.0D),
            new Vec3(1.0D, 0.0D, 1.0D));
    }
    
    public static Quad getPrismZNegXNeg() {
        return Quad.getQuad(
            Direction.NORTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.5D, 0.5D, 0.0D),
            new Vec3(0.5D, 0.0D, 0.0D),
            new Vec3(0.0D, 0.0D, 0.0D));
    }
    
    public static Quad getPrismZNegXPos() {
        return Quad.getQuad(
            Direction.NORTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.5D, 0.5D, 0.0D),
            new Vec3(1.0D, 0.0D, 0.0D),
            new Vec3(0.5D, 0.0D, 0.0D));
    }
    
    public static Quad getPrismSlopedZPosXNeg() {
        return Quad.getQuad(
            Direction.SOUTH,
            CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
            new Vec3(0.0D, 0.5D, 0.5D),
            new Vec3(0.0D, 0.0D, 1.0D),
            new Vec3(0.5D, 0.5D, 0.5D));
    }
    
    public static Quad getPrismSlopedZPosXPos() {
        return Quad.getQuad(
            Direction.SOUTH,
            CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
            new Vec3(0.5D, 0.5D, 0.5D),
            new Vec3(1.0D, 0.0D, 1.0D),
            new Vec3(1.0D, 0.5D, 0.5D));
    }
    
    public static Quad getPrismXNegZNeg() {
        return Quad.getQuad(
            Direction.WEST,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.0D, 0.0D, 0.0D),
            new Vec3(0.0D, 0.0D, 0.5D),
            new Vec3(0.0D, 0.5D, 0.5D));
    }
    
    public static Quad getPrismXNegZPos() {
        return Quad.getQuad(
            Direction.WEST,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.0D, 0.5D, 0.5D),
            new Vec3(0.0D, 0.0D, 0.5D),
            new Vec3(0.0D, 0.0D, 1.0D));
    }
    
    public static Quad getPrismSlopedXNegZPos() {
        return Quad.getQuad(
            Direction.WEST,
            CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
            new Vec3(0.5D, 0.5D, 0.5D),
            new Vec3(0.0D, 0.0D, 1.0D),
            new Vec3(0.5D, 0.5D, 1.0D));
    }
    
    public static Quad getPrismXPosZPos() {
        return Quad.getQuad(
            Direction.EAST,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(1.0D, 0.0D, 1.0D),
            new Vec3(1.0D, 0.0D, 0.5D),
            new Vec3(1.0D, 0.5D, 0.5D));
    }
    
    public static Quad getPrismXPosZNeg() {
        return Quad.getQuad(
            Direction.EAST,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(1.0D, 0.5D, 0.5D),
            new Vec3(1.0D, 0.0D, 0.5D),
            new Vec3(1.0D, 0.0D, 0.0D));
    }
    
    public static Quad getPrismSlopedXPosZPos() {
        return Quad.getQuad(
            Direction.EAST,
            CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
            new Vec3(0.5D, 0.5D, 1.0D),
            new Vec3(1.0D, 0.0D, 1.0D),
            new Vec3(0.5D, 0.5D, 0.5D));
    }
    
    public static Quad getInvertPrismSlopedZPos() {
        return Quad.getQuad(
            Direction.SOUTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.0D, 1.0D, 0.0D),
            new Vec3(0.5D, 0.5D, 0.5D),
            new Vec3(1.0D, 1.0D, 0.0D));
    }
    
    public static Quad getInvertPrismZPosXNeg() {
        return Quad.getQuad(
            Direction.SOUTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.0D, 1.0D, 1.0D),
            new Vec3(0.0D, 0.0D, 1.0D),
            new Vec3(0.5D, 0.0D, 1.0D),
            new Vec3(0.5D, 0.5D, 1.0D));
    }
    
    public static Quad getInvertPrismZPosXPos() {
        return Quad.getQuad(
            Direction.SOUTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.5D, 0.5D, 1.0D),
            new Vec3(0.5D, 0.0D, 1.0D),
            new Vec3(1.0D, 0.0D, 1.0D),
            new Vec3(1.0D, 1.0D, 1.0D));
    }
    
    public static Quad getInvertPrismZNegXPos() {
        return Quad.getQuad(
            Direction.NORTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(1.0D, 1.0D, 0.0D),
            new Vec3(1.0D, 0.0D, 0.0D),
            new Vec3(0.5D, 0.0D, 0.0D),
            new Vec3(0.5D, 0.5D, 0.0D));
    }
    
    public static Quad getInvertPrismZNegXNeg() {
        return Quad.getQuad(
            Direction.NORTH,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.5D, 0.5D, 0.0D),
            new Vec3(0.5D, 0.0D, 0.0D),
            new Vec3(0.0D, 0.0D, 0.0D),
            new Vec3(0.0D, 1.0D, 0.0D));
    }
    
    public static Quad getInvertPrismXNegZNeg() {
        return Quad.getQuad(
            Direction.WEST,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.0D, 1.0D, 0.0D),
            new Vec3(0.0D, 0.0D, 0.0D),
            new Vec3(0.0D, 0.0D, 0.5D),
            new Vec3(0.0D, 0.5D, 0.5D));
    }
    
    public static Quad getInvertPrismXNegZPos() {
        return Quad.getQuad(
            Direction.WEST,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(0.0D, 0.5D, 0.5D),
            new Vec3(0.0D, 0.0D, 0.5D),
            new Vec3(0.0D, 0.0D, 1.0D),
            new Vec3(0.0D, 1.0D, 1.0D));
    }
    
    public static Quad getInvertPrismXPosZPos() {
        return Quad.getQuad(
            Direction.EAST,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(1.0D, 1.0D, 1.0D),
            new Vec3(1.0D, 0.0D, 1.0D),
            new Vec3(1.0D, 0.0D, 0.5D),
            new Vec3(1.0D, 0.5D, 0.5D));
    }
    
    public static Quad getInvertPrismXPosZNeg() {
        return Quad.getQuad(
            Direction.EAST,
            CbSprites.get(CbSprites.UNCOVERED_FULL),
            new Vec3(1.0D, 0.5D, 0.5D),
            new Vec3(1.0D, 0.0D, 0.5D),
            new Vec3(1.0D, 0.0D, 0.0D),
            new Vec3(1.0D, 1.0D, 0.0D));
    }
    
    public static Quad getInvertPrismSlopeZNegXPos() {
        return Quad.getQuad(
            Direction.NORTH,
            CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
            new Vec3(1.0D, 1.0D, 1.0D),
            new Vec3(1.0D, 0.5D, 0.5D),
            new Vec3(0.5D, 0.5D, 0.5D));
    }
    
    public static Quad getInvertPrismSlopeZNegXNeg() {
        return Quad.getQuad(
            Direction.NORTH,
            CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
            new Vec3(0.5D, 0.5D, 0.5D),
            new Vec3(0.0D, 0.5D, 0.5D),
            new Vec3(0.0D, 1.0D, 1.0D));
    }
    
    public static Quad getInvertPrismSlopeXNegZPos() {
        return Quad.getQuad(
            Direction.WEST,
            CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
            new Vec3(0.5D, 0.5D, 0.5D),
            new Vec3(0.5D, 0.5D, 1.0D),
            new Vec3(1.0D, 1.0D, 1.0D));
    }
    
    public static Quad getInvertPrismSlopeXPosZPos() {
        return Quad.getQuad(
            Direction.EAST,
            CbSprites.get(CbSprites.UNCOVERED_QUARTERED),
            new Vec3(0.0D, 1.0D, 1.0D),
            new Vec3(0.5D, 0.5D, 1.0D),
            new Vec3(0.5D, 0.5D, 0.5D));
    }
    
}