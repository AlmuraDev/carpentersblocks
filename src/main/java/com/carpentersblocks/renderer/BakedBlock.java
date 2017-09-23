package com.carpentersblocks.renderer;

import com.carpentersblocks.renderer.helper.RenderHelper;
import com.carpentersblocks.util.registry.SpriteRegistry;
import com.google.common.base.Function;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BakedBlock extends AbstractBakedModel 
{ 
    public BakedBlock(IModelState state, VertexFormat format, java.util.function.Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
		super(state, format, bakedTextureGetter);
	}
    
	@SideOnly(Side.CLIENT)
	@Override
	protected void fillQuads(QuadContainer quadContainer)
	{
		RenderHelper renderHelper = new RenderHelper();
		quadContainer.add(renderHelper.getQuadYNeg());
		quadContainer.add(renderHelper.getQuadYPos());
		quadContainer.add(renderHelper.getQuadZNeg());
		quadContainer.add(renderHelper.getQuadZPos());
		quadContainer.add(renderHelper.getQuadXNeg());
		quadContainer.add(renderHelper.getQuadXPos());
    }

	@SideOnly(Side.CLIENT)
    @Override
	protected TextureAtlasSprite getUncoveredSprite() 
	{
		return SpriteRegistry.sprite_uncovered_quartered;
	} 
}
