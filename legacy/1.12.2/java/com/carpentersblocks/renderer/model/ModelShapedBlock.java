package com.carpentersblocks.renderer.model;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import com.carpentersblocks.block.BlockCarpentersFlowerPot;
import com.carpentersblocks.renderer.bakedblock.BakedFlowerPot;
import com.carpentersblocks.renderer.bakedblock.BakedShapedBlock;
import com.carpentersblocks.util.registry.SpriteRegistry;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

/**
 * Model for blocks drawn from their {@link com.carpentersblocks.block.IShapedBlock} boxes.
 */
public class ModelShapedBlock implements IModel {

	private final Block _block;

	public ModelShapedBlock(Block block) {
		_block = block;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Collections.emptySet();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return ImmutableSet.of(SpriteRegistry.RESOURCE_UNCOVERED_FULL);
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		if (_block instanceof BlockCarpentersFlowerPot) {
			return new BakedFlowerPot(state, format, bakedTextureGetter, _block);
		}
		return new BakedShapedBlock(state, format, bakedTextureGetter, _block);
	}

	@Override
	public IModelState getDefaultState() {
		return TRSRTransformation.identity();
	}

}
