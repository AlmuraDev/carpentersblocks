package com.carpentersblocks.client.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.blockentity.CbRenderData;
import com.carpentersblocks.client.model.shape.BlockShapes;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Vector3fc;

/**
 * The block model of a Carpenter's block (1.12.2's AbstractBakedModel): its shape with its attributes laid over it,
 * built from the block entity's {@link CbRenderData} when the chunk is meshed.
 * <p>
 * Named in the blockstate file as <code>{"type": "carpentersblocks:coverable", "shape": "block"}</code>.
 */
public final class CoverableModel implements DynamicBlockStateModel {

    public static final Identifier TYPE = CarpentersBlocks.id("coverable");
    private static final float EPSILON = 1.0E-4F;
    private static final CbRenderData UNCOVERED = new CbRenderData(Map.of(), 0);

    private final String _shapeName;
    private final BlockShapes.Shape _shape;

    private CoverableModel(String shapeName, BlockShapes.Shape shape) {
        _shapeName = shapeName;
        _shape = shape;
    }

    private static CbRenderData getRenderData(BlockAndTintGetter level, BlockPos pos) {
        CbRenderData data = level.getModelData(pos).get(CbRenderData.PROPERTY);
        return data != null ? data : UNCOVERED;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        CbRenderData data = getRenderData(level, pos);
        List<BakedQuad> quads = CoverQuadBuilder.build(_shape.create(data), data, level, pos);
        parts.add(new Part(quads, getParticle(data)));
    }

    /**
     * Breaking particles look like the cover.
     */
    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return getParticle(getRenderData(level, pos));
    }

    @Override
    public Material.Baked particleMaterial() {
        return getParticle(UNCOVERED);
    }

    private static Material.Baked getParticle(CbRenderData data) {
        BlockState cover = data.getAttributeHelper().getBlockState(EnumAttributeLocation.HOST, EnumAttributeType.COVER);
        if (cover != null) {
            return Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(cover);
        }
        return new Material.Baked(CbSprites.get(CbSprites.UNCOVERED_FULL), false);
    }

    /**
     * Covers may be translucent (glass, ice).
     */
    @Override
    public int materialFlags() {
        return BakedQuad.FLAG_TRANSLUCENT;
    }

    @Override
    public String toString() {
        return "CoverableModel[" + _shapeName + "]";
    }

    /**
     * A block's quads, split by the side they lie flat on so the renderer can skip those hidden by a neighbor. Quads
     * not on the block boundary are unsided.
     */
    private static final class Part implements BlockStateModelPart {

        private final List<BakedQuad> _unsided = new ArrayList<>();
        private final Map<Direction, List<BakedQuad>> _sided = new EnumMap<>(Direction.class);
        private final Material.Baked _particle;
        private final int _flags;

        Part(List<BakedQuad> quads, Material.Baked particle) {
            _particle = particle;
            int flags = 0;
            for (BakedQuad quad : quads) {
                flags |= quad.materialInfo().flags();
                Direction face = quad.direction();
                if (isOnBoundary(quad, face)) {
                    _sided.computeIfAbsent(face, key -> new ArrayList<>()).add(quad);
                } else {
                    _unsided.add(quad);
                }
            }
            _flags = flags;
        }

        @Override
        public List<BakedQuad> getQuads(Direction direction) {
            if (direction == null) {
                return _unsided;
            }
            return _sided.getOrDefault(direction, List.of());
        }

        @Override
        @Deprecated
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public Material.Baked particleMaterial() {
            return _particle;
        }

        @Override
        public int materialFlags() {
            return _flags;
        }

        /**
         * Whether all corners lie on the block face the quad points out of.
         */
        private static boolean isOnBoundary(BakedQuad quad, Direction face) {
            int axis = face.getAxis().ordinal(); // X, Y, Z
            float bound = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0F : 0.0F;
            for (int vertex = 0; vertex < 4; ++vertex) {
                Vector3fc position = quad.position(vertex);
                float value = axis == 0 ? position.x() : axis == 1 ? position.y() : position.z();
                if (Math.abs(value - bound) > EPSILON) {
                    return false;
                }
            }
            return true;
        }

    }

    /**
     * The blockstate file's model.
     *
     * @param shape the block shape, see {@link BlockShapes}
     */
    public record Unbaked(String shape) implements CustomUnbakedBlockStateModel {

        public static final MapCodec<Unbaked> CODEC = Codec.STRING.fieldOf("shape").xmap(Unbaked::new, Unbaked::shape);

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public CoverableModel bake(ModelBaker modelBakery) {
            Supplier<IllegalArgumentException> unknown = () -> new IllegalArgumentException("Unknown Carpenter's block shape " + shape);
            return new CoverableModel(shape, BlockShapes.get(shape).orElseThrow(unknown));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {}

    }

}
