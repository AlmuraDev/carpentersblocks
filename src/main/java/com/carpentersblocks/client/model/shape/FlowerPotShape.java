package com.carpentersblocks.client.model.shape;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.block.BlockCarpentersFlowerPot;
import com.carpentersblocks.blockentity.CbRenderData;
import com.carpentersblocks.client.model.CoverTints;
import com.carpentersblocks.client.model.Quad;
import com.carpentersblocks.client.model.QuadContainer;
import com.carpentersblocks.util.attribute.AttributeHelper;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.block.FlowerPotUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Draws the flower pot, its soil, and the plant from the plant's own model scaled into the pot (1.12.2's
 * BakedFlowerPot).
 */
final class FlowerPotShape {

    private FlowerPotShape() {}

    static QuadContainer create(BlockState blockState, int renderData, @Nullable CbRenderData data) {
        QuadContainer container = BlockShapes.createShaped(blockState, renderData);
        if (data == null) {
            return container;
        }
        AttributeHelper attributes = data.getAttributeHelper();
        ItemStack soil = attributes.getItemStack(EnumAttributeLocation.HOST, EnumAttributeType.SOIL);
        BlockState soilState = soil.isEmpty() ? null : com.carpentersblocks.util.block.BlockUtil.getBlockState(soil);
        if (soilState != null) {
            TextureAtlasSprite soilSprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(soilState).sprite();
            for (Quad quad : BlockShapes.getBoxQuads(BlockCarpentersFlowerPot.getSoilBox(), soilSprite)) {
                container.add(quad);
            }
        }
        ItemStack plant = attributes.getItemStack(EnumAttributeLocation.HOST, EnumAttributeType.PLANT);
        BlockState plantState = plant.isEmpty() ? null : FlowerPotUtil.getPlantState(plant);
        if (plantState == null) {
            return container;
        }
        Block plantBlock = plantState.getBlock();
        // Flowers and mushrooms at full size, other plants smaller, like 1.7.10
        double scale = plantBlock instanceof FlowerBlock || plantBlock instanceof MushroomBlock ? 1.0D : 0.75D;
        addPlantQuads(container, plantState, false, scale, 0.25D);
        BlockState upper = FlowerPotUtil.getUpperHalf(plantState);
        if (upper != null) {
            addPlantQuads(container, upper, true, scale, 0.25D + scale);
        }
        return container;
    }

    private static void addPlantQuads(QuadContainer container, BlockState plantState, boolean upper, double scale, double yOffset) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(plantState);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(42L), parts);
        for (BlockStateModelPart part : parts) {
            for (BakedQuad quad : part.getQuads(null)) {
                container.addBaked(transform(quad, scale, yOffset, upper));
            }
            for (Direction direction : Direction.values()) {
                for (BakedQuad quad : part.getQuads(direction)) {
                    container.addBaked(transform(quad, scale, yOffset, upper));
                }
            }
        }
    }

    /**
     * Scales a quad about the bottom center of the block and raises it. Its tint goes through the pot's plant tints
     * (biome colors, fertilizer).
     */
    private static BakedQuad transform(BakedQuad quad, double scale, double yOffset, boolean upper) {
        Vector3f[] positions = new Vector3f[4];
        for (int vertex = 0; vertex < 4; ++vertex) {
            Vector3fc position = quad.position(vertex);
            positions[vertex] = new Vector3f(
                (float) (0.5D + (position.x() - 0.5D) * scale),
                (float) (position.y() * scale + yOffset),
                (float) (0.5D + (position.z() - 0.5D) * scale));
        }
        BakedQuad.MaterialInfo info = quad.materialInfo();
        int tintIndex = info.isTinted() ? CoverTints.getPlantTint(upper, info.tintIndex()) : -1;
        BakedQuad.MaterialInfo tinted = new BakedQuad.MaterialInfo(info.sprite(), info.layer(), info.itemRenderType(), info.itemGlintRenderType(),
            info.itemGlintSpecialRenderType(), tintIndex, info.shadeDirectionOverride(), info.lightEmission(), info.ambientOcclusion());
        return new BakedQuad(positions[0], positions[1], positions[2], positions[3],
            quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
            quad.direction(), tinted, quad.bakedNormals(), quad.bakedColors());
    }

}
