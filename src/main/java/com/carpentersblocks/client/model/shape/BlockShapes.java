package com.carpentersblocks.client.model.shape;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.carpentersblocks.blockentity.CbRenderData;
import com.carpentersblocks.client.model.CbSprites;
import com.carpentersblocks.client.model.Quad;
import com.carpentersblocks.client.model.QuadContainer;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * The shapes Carpenter's blocks are drawn with, by the name their blockstate files give (1.12.2's baked block
 * classes). Shapes are added here as blocks are ported.
 */
public final class BlockShapes {

    /**
     * Makes a block's uncovered quads from its render data. Called on chunk meshing threads.
     */
    @FunctionalInterface
    public interface Shape {
        QuadContainer create(CbRenderData data);
    }

    private static final Map<String, Shape> SHAPES = new HashMap<>();

    static {
        // The Carpenter's Block: a full block (1.12.2's BakedBlock)
        SHAPES.put("block", data -> {
            QuadContainer container = new QuadContainer(EnumAttributeLocation.HOST);
            TextureAtlasSprite sprite = CbSprites.get(CbSprites.UNCOVERED_QUARTERED);
            for (Direction facing : Direction.values()) {
                container.add(getFullFace(facing, sprite));
            }
            return container;
        });
    }

    private BlockShapes() {}

    public static Optional<Shape> get(String name) {
        return Optional.ofNullable(SHAPES.get(name));
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

}
