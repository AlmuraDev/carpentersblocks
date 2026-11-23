package com.carpentersblocks.client.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.carpentersblocks.blockentity.CbRenderData;
import com.carpentersblocks.util.attribute.AttributeHelper;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.DesignHandler.DesignType;
import com.carpentersblocks.util.handler.DyeHandler;
import com.carpentersblocks.util.handler.OverlayHandler;
import com.carpentersblocks.util.handler.OverlayHandler.Overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lays a Carpenter's block's attributes over its shape (1.12.2's RenderPkg): covers lend their textures and tints to
 * the shape's uncovered faces, then chisel designs, overlays and side covers are added.
 * <p>
 * 1.12.2 was asked for one chunk layer at a time; 26.3 takes all quads at once, each with its own layer.
 */
public final class CoverQuadBuilder {

    private static final double SIDE_DEPTH = 1 / 16D;
    private static final double SNOW_SIDE_DEPTH = 1 / 8D;
    private static final int DEFAULT_RGB = 0xFFFFFF;

    private final AttributeHelper _cbAttrHelper;
    private final BlockAndTintGetter _level;
    private final BlockPos _pos;

    private CoverQuadBuilder(CbRenderData data, BlockAndTintGetter level, BlockPos pos) {
        _cbAttrHelper = data.getAttributeHelper();
        _level = level;
        _pos = pos;
    }

    /**
     * Gets a block's quads.
     *
     * @param shape the block's shape, made for this call (it is changed)
     * @param data what the block is drawn from
     */
    public static List<BakedQuad> build(QuadContainer shape, CbRenderData data, BlockAndTintGetter level, BlockPos pos) {
        return new CoverQuadBuilder(data, level, pos).getQuads(shape);
    }

    private List<BakedQuad> getQuads(QuadContainer shape) {
        List<BakedQuad> quads = new ArrayList<>(shape.getBakedQuads());
        BlockState hostBlockState = _cbAttrHelper.getBlockState(EnumAttributeLocation.HOST, EnumAttributeType.COVER);

        for (EnumAttributeLocation location : EnumAttributeLocation.values()) {
            boolean hasLocationCover = _cbAttrHelper.hasAttribute(location, EnumAttributeType.COVER);
            boolean isSideCover = !EnumAttributeLocation.HOST.equals(location);
            if (isSideCover && !hasLocationCover) {
                continue;
            }
            boolean hasOverlay = _cbAttrHelper.hasAttribute(location, EnumAttributeType.OVERLAY);
            String chiselDesign = _cbAttrHelper.getString(location, EnumAttributeType.DESIGN_CHISEL);

            QuadContainer quadContainer = isSideCover ? shape.toSideLocation(location, getSideCoverDepth(location)) : shape;

            // Cover layer
            List<Quad> coverQuads = getCoveredQuads(quadContainer, location);
            ChunkSectionLayer outermostCoverLayer = ChunkSectionLayer.CUTOUT;
            for (Quad quad : coverQuads) {
                quads.add(quad.bake(location));
                if (quad.getRenderLayer().ordinal() > outermostCoverLayer.ordinal()) {
                    outermostCoverLayer = quad.getRenderLayer();
                }
            }

            // Overlays are drawn outermost
            ChunkSectionLayer overlayLayer = hasOverlay ? ChunkSectionLayer.TRANSLUCENT : outermostCoverLayer;

            // Chisel design (a design missing from this client, from a mismatched mod version, is skipped)
            if (chiselDesign != null && !chiselDesign.isEmpty() && DesignHandler.getListForType(DesignType.CHISEL).contains(chiselDesign)) {
                TextureAtlasSprite chiselSprite = CbSprites.get(DesignHandler.getTexture(DesignType.CHISEL, chiselDesign));
                if (!CbSprites.isMissing(chiselSprite)) {
                    for (Direction facing : Direction.values()) {
                        quads.addAll(getQuadsForSide(quadContainer, facing, chiselSprite, DEFAULT_RGB, -1, ChunkSectionLayer.TRANSLUCENT));
                    }
                }
            }

            // Overlay
            if (hasOverlay) {
                Overlay overlay = OverlayHandler.getOverlayType(_cbAttrHelper.getItemStack(location, EnumAttributeType.OVERLAY));
                // Grass is tinted by the renderer (CoverTints)
                int overlayTint = Overlay.GRASS.equals(overlay) ? CoverTints.TINT_GRASS : -1;
                for (Direction facing : Direction.values()) {
                    TextureAtlasSprite overlaySprite = getOverlaySprite(overlay, facing);
                    if (overlaySprite != null) {
                        quads.addAll(getQuadsForSide(quadContainer, facing, overlaySprite, DEFAULT_RGB, overlayTint, overlayLayer));
                    }
                }
            }

            // Snow on top: a snow side cover on a block covered with something else frosts its sides
            BlockState upBlockState = _cbAttrHelper.getBlockState(EnumAttributeLocation.UP, EnumAttributeType.COVER);
            if (EnumAttributeLocation.HOST.equals(location)
                    && upBlockState != null && isSnowState(upBlockState)
                    && hostBlockState != null && !isSnowState(hostBlockState)) {
                for (Direction facing : Direction.values()) {
                    TextureAtlasSprite overlaySprite = getOverlaySprite(Overlay.SNOW, facing);
                    if (overlaySprite != null) {
                        quads.addAll(getQuadsForSide(quadContainer, facing, overlaySprite, null, -1, overlayLayer));
                    }
                }
            }
        }
        return quads;
    }

    private static boolean isSnowState(BlockState blockState) {
        return blockState.is(Blocks.SNOW_BLOCK) || blockState.is(Blocks.SNOW);
    }

    /**
     * Gets the shape's quads with the cover's textures and tints (and the dye) on the uncovered faces.
     */
    private List<Quad> getCoveredQuads(QuadContainer quadContainer, EnumAttributeLocation location) {
        List<Quad> list = new ArrayList<>();
        boolean hasDye = _cbAttrHelper.hasAttribute(location, EnumAttributeType.DYE);
        int dyeRgb = hasDye ? DyeHandler.getColor(_cbAttrHelper.getItemStack(location, EnumAttributeType.DYE)) : 0;
        BlockState blockState = _cbAttrHelper.getBlockState(location, EnumAttributeType.COVER);

        // Cover quads, to donate texture and tint
        Map<Direction, List<BakedQuad>> donorQuads = blockState != null ? getCoverQuads(blockState) : Map.of();

        for (Direction facing : Direction.values()) {
            for (Quad quad : quadContainer.getQuads(blockState, facing)) {
                if (quad.canCover() && blockState != null) {
                    for (BakedQuad bakedQuad : donorQuads.getOrDefault(facing, List.of())) {
                        BakedQuad.MaterialInfo info = bakedQuad.materialInfo();
                        Quad newQuad = new Quad(quad);
                        if (blockState.is(Blocks.GRASS_BLOCK)) {
                            // Grass keeps its green where it is green, the dye colors the rest
                            if (info.isTinted()) {
                                newQuad.setTintIndex(CoverTints.getCoverTint(location, info.tintIndex()));
                            } else if (hasDye) {
                                newQuad.setRgb(dyeRgb);
                            }
                        } else if (info.isTinted()) {
                            if (hasDye) {
                                newQuad.setRgb(dyeRgb);
                            } else {
                                newQuad.setTintIndex(CoverTints.getCoverTint(location, info.tintIndex()));
                            }
                        } else if (hasDye) {
                            newQuad.setRgb(dyeRgb);
                        }
                        newQuad.setSprite(info.sprite());
                        newQuad.setRenderLayer(info.layer());
                        float[] region = getTextureRegion(bakedQuad, facing, info.sprite());
                        if (region != null) {
                            newQuad.setSpriteRegion(region[0], region[1], region[2], region[3], 0);
                        }
                        list.add(newQuad);
                    }
                    continue;
                }
                // Dye colors the block, not fixed textures (wool, iron, glass), like 1.7.10
                if (hasDye && quad.canCover()) {
                    quad.setRgb(dyeRgb);
                }
                list.add(quad);
            }
        }
        return list;
    }

    /**
     * Gets the part of its texture a cover's face would draw across the whole side of the block, as fractions of the
     * sprite (u0, v0, u1, v1): the face's texture mapping carried out to the side's edges. Models that pick their
     * texture out of a shared sheet (Almura's OBJ blocks) draw only their part of it. Null to draw the whole texture:
     * vanilla blocks, whose faces map the whole texture across the side (a slab's half-height side comes out whole
     * too, like 1.12.2).
     */
    @Nullable
    private static float[] getTextureRegion(BakedQuad bakedQuad, Direction facing, TextureAtlasSprite sprite) {
        float spriteWidth = sprite.getU1() - sprite.getU0();
        float spriteHeight = sprite.getV1() - sprite.getV0();
        if (spriteWidth <= 0 || spriteHeight <= 0) {
            return null;
        }
        Direction.Axis axis = facing.getAxis();
        float[] a = new float[4], b = new float[4], u = new float[4], v = new float[4];
        for (int vertex = 0; vertex < 4; ++vertex) {
            org.joml.Vector3fc position = bakedQuad.position(vertex);
            // The two coordinates across the face
            a[vertex] = axis == Direction.Axis.X ? position.y() : position.x();
            b[vertex] = axis == Direction.Axis.Z ? position.y() : position.z();
            long packed = bakedQuad.packedUV(vertex);
            u[vertex] = (net.minecraft.client.model.geom.builders.UVPair.unpackU(packed) - sprite.getU0()) / spriteWidth;
            v[vertex] = (net.minecraft.client.model.geom.builders.UVPair.unpackV(packed) - sprite.getV0()) / spriteHeight;
        }
        // The mapping is affine across a flat face: solve it from three corners that are not in a line
        int[][] triples = { { 0, 1, 2 }, { 0, 1, 3 }, { 0, 2, 3 }, { 1, 2, 3 } };
        for (int[] t : triples) {
            int i = t[0], j = t[1], k = t[2];
            double det = a[i] * (b[j] - b[k]) - b[i] * (a[j] - a[k]) + (a[j] * b[k] - a[k] * b[j]);
            if (Math.abs(det) < 1.0E-6D) {
                continue;
            }
            double[] uMap = solve(a, b, u, i, j, k, det);
            double[] vMap = solve(a, b, v, i, j, k, det);
            double uMin = Double.MAX_VALUE, uMax = -Double.MAX_VALUE, vMin = Double.MAX_VALUE, vMax = -Double.MAX_VALUE;
            for (int corner = 0; corner < 4; ++corner) {
                double ca = corner & 1, cb = corner >> 1;
                double cu = uMap[0] * ca + uMap[1] * cb + uMap[2];
                double cv = vMap[0] * ca + vMap[1] * cb + vMap[2];
                uMin = Math.min(uMin, cu);
                uMax = Math.max(uMax, cu);
                vMin = Math.min(vMin, cv);
                vMax = Math.max(vMax, cv);
            }
            float[] region = { clamp(uMin), clamp(vMin), clamp(uMax), clamp(vMax) };
            float epsilon = 1.0E-3F;
            boolean wholeTexture = region[0] < epsilon && region[1] < epsilon && region[2] > 1 - epsilon && region[3] > 1 - epsilon;
            boolean empty = region[2] - region[0] < epsilon || region[3] - region[1] < epsilon;
            return wholeTexture || empty ? null : region;
        }
        return null;
    }

    /**
     * Solves value = p * a + q * b + r through three corners (Cramer's rule).
     */
    private static double[] solve(float[] a, float[] b, float[] value, int i, int j, int k, double det) {
        double p = (value[i] * (b[j] - b[k]) - b[i] * (value[j] - value[k]) + (value[j] * b[k] - value[k] * b[j])) / det;
        double q = (a[i] * (value[j] - value[k]) - value[i] * (a[j] - a[k]) + (a[j] * value[k] - a[k] * value[j])) / det;
        double r = (a[i] * (b[j] * value[k] - b[k] * value[j]) - b[i] * (a[j] * value[k] - a[k] * value[j])
            + value[i] * (a[j] * b[k] - a[k] * b[j])) / det;
        return new double[] { p, q, r };
    }

    private static float clamp(double value) {
        return (float) Math.max(0.0D, Math.min(1.0D, value));
    }

    /**
     * Gets a cover block's quads by side. Faces inside the block (a slab's top) count for the side they face.
     */
    private Map<Direction, List<BakedQuad>> getCoverQuads(BlockState blockState) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(_level, _pos, blockState, RandomSource.create(blockState.getSeed(_pos)), parts);
        Map<Direction, List<BakedQuad>> map = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.values()) {
            List<BakedQuad> sided = new ArrayList<>();
            for (BlockStateModelPart part : parts) {
                sided.addAll(part.getQuads(facing));
            }
            if (sided.isEmpty()) {
                for (BlockStateModelPart part : parts) {
                    for (BakedQuad bakedQuad : part.getQuads(null)) {
                        if (bakedQuad.direction() == facing) {
                            sided.add(bakedQuad);
                        }
                    }
                }
            }
            map.put(facing, sided);
        }
        return map;
    }

    /**
     * Gets the shape's coverable faces on a side, with another texture (a design or an overlay).
     */
    private static List<BakedQuad> getQuadsForSide(QuadContainer quadContainer, Direction facing, TextureAtlasSprite spriteOverride, @Nullable Integer rgbOverride, int tintIndex, ChunkSectionLayer layer) {
        List<Quad> destQuads = new ArrayList<>();
        for (Quad quad : quadContainer.getQuads(null, facing)) {
            if (!quad.canCover()) {
                continue;
            }
            Quad newQuad = new Quad(quad);
            newQuad.setSprite(spriteOverride);
            newQuad.setTintIndex(tintIndex);
            newQuad.setRenderLayer(layer);
            if (rgbOverride != null) {
                newQuad.setRgb(rgbOverride);
            }
            destQuads.add(newQuad);
        }
        return quadContainer.bakeQuads(destQuads);
    }

    /**
     * Returns the texture of an overlay on a side, or null if the overlay leaves the side bare.
     */
    @Nullable
    private TextureAtlasSprite getOverlaySprite(Overlay overlay, Direction facing) {
        return switch (overlay) {
            case GRASS -> facing == Direction.DOWN ? null
                : CbSprites.get(facing == Direction.UP ? CbSprites.GRASS_TOP : CbSprites.GRASS_SIDE_OVERLAY);
            case SNOW, HAY, MYCELIUM -> {
                if (facing == Direction.DOWN) {
                    yield null;
                } else if (facing == Direction.UP) {
                    yield getParticleSprite(overlay.getBlock().defaultBlockState());
                }
                yield CbSprites.get(switch (overlay) {
                    case SNOW -> CbSprites.OVERLAY_SNOW_SIDE;
                    case HAY -> CbSprites.OVERLAY_HAY_SIDE;
                    default -> CbSprites.OVERLAY_MYCELIUM_SIDE;
                });
            }
            case WEB, VINE -> getParticleSprite(overlay.getBlock().defaultBlockState());
            default -> null;
        };
    }

    private static TextureAtlasSprite getParticleSprite(BlockState blockState) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(blockState).sprite();
    }

    private double getSideCoverDepth(EnumAttributeLocation location) {
        BlockState blockState = _cbAttrHelper.getBlockState(location, EnumAttributeType.COVER);
        return EnumAttributeLocation.UP.equals(location) && blockState != null && isSnowState(blockState) ? SNOW_SIDE_DEPTH : SIDE_DEPTH;
    }

}
