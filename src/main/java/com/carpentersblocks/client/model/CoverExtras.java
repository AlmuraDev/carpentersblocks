package com.carpentersblocks.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.carpentersblocks.CbConfig;
import com.carpentersblocks.blockentity.CbRenderData;
import com.carpentersblocks.client.model.shape.BlockShapes;
import com.carpentersblocks.client.model.shape.RenderHelperSlope;
import com.carpentersblocks.util.RotationUtil.Rotation;
import com.carpentersblocks.util.attribute.AbstractAttribute;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * What a Carpenter's block draws besides its own shape, from its neighbors in the chunk being meshed (1.12.2's
 * RenderPkg): the wedge under a sloping rail above ("Rail Slope Fill") and the surface of a fluid beside it
 * ("Routable Fluids").
 */
public final class CoverExtras {

    private CoverExtras() {}

    ////////////
    // Rail slope fill
    ////////////

    /**
     * Gets the wedge drawn in the rail's space when a sloping rail sits on this block, in the block's cover and dye
     * (no overlays or designs, like 1.12.2). The quads lie above the block, so they are never culled by its sides.
     */
    public static List<BakedQuad> getRailSlopeQuads(BlockAndTintGetter level, BlockPos pos, CbRenderData data) {
        if (!CbConfig.ENABLE_RAIL_SLOPES.get()) {
            return List.of();
        }
        // A rail only stays on a sturdy top, so the rail being there is the solid top check
        BlockPos railPos = pos.above();
        BlockState railState = level.getBlockState(railPos);
        if (!(railState.getBlock() instanceof BaseRailBlock rail)) {
            return List.of();
        }
        Direction rising = getRising(rail.getRailDirection(railState, level, railPos, null));
        if (rising == null) {
            return List.of();
        }
        List<BakedQuad> quads = new ArrayList<>();
        for (BakedQuad quad : CoverQuadBuilder.build(getWedge(rising), hostOnly(data), level, pos)) {
            quads.add(raise(quad));
        }
        return quads;
    }

    @Nullable
    private static Direction getRising(RailShape shape) {
        return switch (shape) {
            case ASCENDING_EAST -> Direction.EAST;
            case ASCENDING_WEST -> Direction.WEST;
            case ASCENDING_NORTH -> Direction.NORTH;
            case ASCENDING_SOUTH -> Direction.SOUTH;
            default -> null;
        };
    }

    /**
     * The slope's wedge without its base, high side toward the rising direction.
     */
    private static QuadContainer getWedge(Direction rising) {
        TextureAtlasSprite sprite = CbSprites.get(CbSprites.UNCOVERED_FULL);
        QuadContainer wedge = new QuadContainer(EnumAttributeLocation.HOST);
        wedge.add(BlockShapes.getFullFace(Direction.NORTH, sprite));
        wedge.add(RenderHelperSlope.getWedgeXNeg());
        wedge.add(RenderHelperSlope.getWedgeXPos());
        wedge.add(Quad.getQuad(Direction.SOUTH, sprite,
            new Vec3(1.0D, 0.0D, 1.0D), new Vec3(1.0D, 1.0D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D), new Vec3(0.0D, 0.0D, 1.0D)));
        // Unrotated, the wedge's high side is north
        wedge.rotate(switch (rising) {
            case EAST -> Rotation.X0_Y90_Z0;
            case SOUTH -> Rotation.X0_Y180_Z0;
            case WEST -> Rotation.X0_Y270_Z0;
            default -> Rotation.X0_Y0_Z0;
        });
        return wedge;
    }

    /**
     * Only the block's own cover and dye.
     */
    private static CbRenderData hostOnly(CbRenderData data) {
        Map<AbstractAttribute.Key, AbstractAttribute<?>> attributes = new HashMap<>();
        data.attributes().forEach((key, attribute) -> {
            if (EnumAttributeLocation.HOST.equals(key.location())
                    && (EnumAttributeType.COVER.equals(key.type()) || EnumAttributeType.DYE.equals(key.type()))) {
                attributes.put(key, attribute);
            }
        });
        return new CbRenderData(Map.copyOf(attributes), data.cbMetadata());
    }

    private static BakedQuad raise(BakedQuad quad) {
        return new BakedQuad(
            new Vector3f(quad.position0()).add(0, 1, 0), new Vector3f(quad.position1()).add(0, 1, 0),
            new Vector3f(quad.position2()).add(0, 1, 0), new Vector3f(quad.position3()).add(0, 1, 0),
            quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
            quad.direction(), quad.materialInfo(), quad.bakedNormals(), quad.bakedColors());
    }

    ////////////
    // Routable fluids
    ////////////

    private static boolean isFluid(BlockState blockState) {
        return blockState.getBlock() instanceof LiquidBlock && !blockState.getFluidState().isEmpty();
    }

    /**
     * Gets a fluid beside (or diagonally beside, past open faces) to show in this block space, or null.
     */
    @Nullable
    private static BlockPos getRoutedFluidPos(BlockAndTintGetter level, BlockPos pos, BlockState blockState) {
        if (isFluid(level.getBlockState(pos.above()))) {
            return null;
        }
        // Straight neighbors first, then diagonals reached through an open face and an open neighbor
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos fluidPos = pos.relative(side);
            if (isFluid(level.getBlockState(fluidPos)) && !blockState.isFaceSturdy(level, pos, side)) {
                return fluidPos;
            }
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            Direction turn = side.getClockWise();
            BlockPos fluidPos = pos.relative(side).relative(turn);
            if (!isFluid(level.getBlockState(fluidPos))) {
                continue;
            }
            for (Direction[] route : new Direction[][] { { side, turn }, { turn, side } }) {
                BlockPos bridgePos = pos.relative(route[0]);
                BlockState bridgeState = level.getBlockState(bridgePos);
                if (!blockState.isFaceSturdy(level, pos, route[0])
                        && !bridgeState.isFaceSturdy(level, bridgePos, route[0].getOpposite())
                        && !bridgeState.isFaceSturdy(level, bridgePos, route[1])) {
                    return fluidPos;
                }
            }
        }
        return null;
    }

    /**
     * Gets the top and underside of a fluid's surface across the block space, tinted like the fluid.
     */
    public static List<BakedQuad> getRoutedFluidQuads(BlockAndTintGetter level, BlockPos pos, BlockState blockState) {
        if (!CbConfig.ENABLE_ROUTABLE_FLUIDS.get()) {
            return List.of();
        }
        BlockPos fluidPos = getRoutedFluidPos(level, pos, blockState);
        if (fluidPos == null) {
            return List.of();
        }
        BlockState fluidBlockState = level.getBlockState(fluidPos);
        FluidState fluidState = fluidBlockState.getFluidState();
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
        TextureAtlasSprite sprite = model.stillMaterial().sprite();
        int rgb = model.fluidTintSource() != null
            ? model.fluidTintSource().colorInWorld(fluidState, fluidBlockState, level, fluidPos) & 0xFFFFFF : 0xFFFFFF;
        double height = Math.max(0.0625D, Math.min(1.0D, fluidState.getHeight(level, fluidPos)));
        List<BakedQuad> quads = new ArrayList<>(2);
        Quad top = Quad.getQuad(Direction.UP, true, sprite, rgb, false, model.layer(),
            new Vec3(0.0D, height, 0.0D), new Vec3(0.0D, height, 1.0D), new Vec3(1.0D, height, 1.0D), new Vec3(1.0D, height, 0.0D));
        Quad bottom = Quad.getQuad(Direction.DOWN, true, sprite, rgb, false, model.layer(),
            new Vec3(0.0D, height, 1.0D), new Vec3(0.0D, height, 0.0D), new Vec3(1.0D, height, 0.0D), new Vec3(1.0D, height, 1.0D));
        for (Quad quad : new Quad[] { top, bottom }) {
            if (quad != null) {
                quads.add(quad.bake(EnumAttributeLocation.HOST));
            }
        }
        return quads;
    }

}
