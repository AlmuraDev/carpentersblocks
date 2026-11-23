package com.carpentersblocks.client.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.BlockCoverable;
import com.carpentersblocks.client.model.shape.BlockShapes;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

/**
 * A Carpenter's block item: the block's shape, uncovered, in its inventory pose (1.12.2's getInventoryQuads).
 * <p>
 * Named in item definitions as <code>{"type": "carpentersblocks:coverable", "shape": "shaped"}</code>; the block comes
 * from the stack.
 */
public final class CoverableItemModel implements ItemModel {

    public static final Identifier TYPE = CarpentersBlocks.id("coverable");
    /** Block items use the block display transforms. */
    private static final Identifier TRANSFORMS_FROM = Identifier.withDefaultNamespace("block/block");

    private record Geometry(ItemQuads quads, Supplier<Vector3fc[]> extents, Material.Baked particle) {}

    private final BlockShapes.Shape _shape;
    private final ItemTransforms _transforms;
    private final Matrix4fc _transformation;
    // Baked on first use by render data, once the block atlas is loaded; a resource reload bakes a new model
    private final Map<Integer, Geometry> _geometry = new ConcurrentHashMap<>();

    private CoverableItemModel(BlockShapes.Shape shape, ItemTransforms transforms, Matrix4fc transformation) {
        _shape = shape;
        _transforms = transforms;
        _transformation = transformation;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
            @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        if (!(item.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        BlockState blockState = blockItem.getBlock().defaultBlockState();
        int renderData = blockState.getBlock() instanceof BlockCoverable block ? block.getInventoryRenderData(item) : 0;
        Geometry geometry = _geometry.computeIfAbsent(renderData, data -> bake(blockState, data));
        output.appendModelIdentityElement(this);
        output.appendModelIdentityElement(renderData);
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        if (item.hasFoil()) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            output.setAnimated();
            output.appendModelIdentityElement(ItemStackRenderState.FoilType.STANDARD);
        }
        layer.setExtents(geometry.extents());
        layer.setLocalTransform(_transformation);
        new ModelRenderProperties(true, geometry.particle(), _transforms).applyToLayer(layer, displayContext);
        layer.setQuads(geometry.quads());
    }

    private Geometry bake(BlockState blockState, int renderData) {
        List<BakedQuad> quads = List.copyOf(_shape.create(blockState, renderData, null).bakeQuads());
        Material.Baked particle = new Material.Baked(CbSprites.get(CbSprites.UNCOVERED_FULL), false);
        return new Geometry(ItemQuads.split(quads), Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(quads)), particle);
    }

    /**
     * The item definition's model.
     *
     * @param shape the block shape, see {@link BlockShapes}
     */
    public record Unbaked(String shape) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> CODEC = Codec.STRING.fieldOf("shape").xmap(Unbaked::new, Unbaked::shape);

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(TRANSFORMS_FROM);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ItemTransforms transforms = context.blockModelBaker().getModel(TRANSFORMS_FROM).getTopTransforms();
            BlockShapes.Shape blockShape = BlockShapes.get(shape).orElseThrow(() -> new IllegalArgumentException("Unknown Carpenter's block shape " + shape));
            return new CoverableItemModel(blockShape, transforms, transformation);
        }

    }

}
