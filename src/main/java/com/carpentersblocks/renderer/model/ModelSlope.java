package com.carpentersblocks.renderer.model;

import java.util.Collection;
import java.util.Collections;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.renderer.BakedCollapsibleBlock;
import java.util.function.Function;
import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.carpentersblocks.Reference.*;
public class ModelSlope implements IModel
{ 
	@Override
	public Collection<ResourceLocation> getDependencies() 
	{
		return Collections.emptySet();
	}

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return ImmutableSet.of(new ResourceLocation( MOD_ID, "blocks/slope/oblique_pos"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) 
	{
		return new BakedCollapsibleBlock(state, format, bakedTextureGetter);
	}

	@Override
	public IModelState getDefaultState() 
	{
		return TRSRTransformation.identity();
	}
}
