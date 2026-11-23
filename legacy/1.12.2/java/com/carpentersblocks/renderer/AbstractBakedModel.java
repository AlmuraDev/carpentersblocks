package com.carpentersblocks.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import com.carpentersblocks.util.IConstants;
import com.carpentersblocks.util.block.BlockUtil;
import com.carpentersblocks.util.registry.ConfigRegistry;
import com.carpentersblocks.util.registry.SpriteRegistry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.model.IModelState;

public abstract class AbstractBakedModel implements IBakedModel {

	private static final float EPSILON = 1.0E-4F;

	// Quads of the block being drawn on this thread, so the 7 getQuads calls per layer build them once
	private static final ThreadLocal<CachedQuads> _cachedQuads = new ThreadLocal<CachedQuads>();

    private VertexFormat _vertexFormat;
    private VertexFormat _brightnessVertexFormat;

    public AbstractBakedModel(IModelState modelState, VertexFormat vertexFormat, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
    	this._vertexFormat = vertexFormat;
    	this._brightnessVertexFormat = new VertexFormat(vertexFormat).addElement(DefaultVertexFormats.TEX_2S);
    }

    public VertexFormat getVertexFormat() {
    	// TEX_2S lets quads set their own brightness, but only Forge's light pipeline converts it;
    	// vanilla's renderer (pipeline off) and Optifine copy the vertex data as is and garble it
    	if (ConfigRegistry.enableOptifineCompatibility || !ForgeModContainer.forgeLightPipelineEnabled) {
    		return _vertexFormat;
    	}
    	return _brightnessVertexFormat;
    }

    public abstract List<BakedQuad> getInventoryQuads(RenderPkg renderPkg);

    @Override
    public List<BakedQuad> getQuads(IBlockState blockState, EnumFacing facing, long rand) {
    	if (blockState == null) {
    		// Items ask once per side and once unsided: draw them once
    		if (facing != null) {
    			return Collections.emptyList();
    		}
    		RenderPkg renderPkg = new RenderPkg(_vertexFormat, facing, rand);
    		return this.getInventoryQuads(renderPkg);
    	}
    	if (!BlockUtil.validateBlockState(blockState)) {
    		return Collections.emptyList();
    	}
    	BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();
    	if (renderLayer == null) {
    		// Breaking cracks (ForgeHooksClient.getDamageModel) retexture these quads, so only the shape matters
    		if (facing != null) {
    			return Collections.emptyList();
    		}
    		try {
	    		RenderPkg renderPkg = new RenderPkg(_vertexFormat, blockState, facing, rand);
	    		fillQuads(renderPkg);
	    		return renderPkg.getInventoryQuads();
    		} finally {
    			RenderPkg.clearThreadedState();
    		}
    	}
    	CachedQuads cached = _cachedQuads.get();
    	if (cached == null || cached.model != this || cached.blockState != blockState || cached.renderLayer != renderLayer) {
    		try {
	    		RenderPkg renderPkg = new RenderPkg(this.getVertexFormat(), blockState, null, rand);
	    		fillQuads(renderPkg);
	    		cached = new CachedQuads(this, blockState, renderLayer, renderPkg.getQuads());
    		} finally {
    			RenderPkg.clearThreadedState();
    		}
    		_cachedQuads.set(cached);
    	}
    	if (facing == null) {
    		// Unsided quads are asked for last; don't hold the block past it
    		_cachedQuads.remove();
    	}
    	return cached.get(facing);
    }

    /**
     * A block's quads split by the side they lie flat on, so the renderer can skip
     * those hidden by a neighbor. Quads not on the block boundary are unsided.
     */
    private static class CachedQuads {

    	private final AbstractBakedModel model;
    	private final IBlockState blockState;
    	private final BlockRenderLayer renderLayer;
    	private final List<BakedQuad> unsided = new ArrayList<BakedQuad>();
    	private final Map<EnumFacing, List<BakedQuad>> sided = new EnumMap<EnumFacing, List<BakedQuad>>(EnumFacing.class);

    	CachedQuads(AbstractBakedModel model, IBlockState blockState, BlockRenderLayer renderLayer, List<BakedQuad> quads) {
    		this.model = model;
    		this.blockState = blockState;
    		this.renderLayer = renderLayer;
    		for (BakedQuad quad : quads) {
    			EnumFacing face = quad.getFace();
    			if (face != null && isOnBoundary(quad, face)) {
    				sided.computeIfAbsent(face, key -> new ArrayList<BakedQuad>()).add(quad);
    			} else {
    				unsided.add(quad);
    			}
    		}
    	}

    	List<BakedQuad> get(EnumFacing facing) {
    		if (facing == null) {
    			return unsided;
    		}
    		List<BakedQuad> quads = sided.get(facing);
    		return quads != null ? quads : Collections.<BakedQuad>emptyList();
    	}

    	/**
    	 * Whether all corners lie on the block face the quad points out of.
    	 */
    	private static boolean isOnBoundary(BakedQuad quad, EnumFacing face) {
    		VertexFormat format = quad.getFormat();
    		int positionOffset = -1;
    		for (int idx = 0; idx < format.getElementCount(); ++idx) {
    			if (format.getElement(idx).getUsage() == VertexFormatElement.EnumUsage.POSITION) {
    				positionOffset = format.getOffset(idx) / 4;
    				break;
    			}
    		}
    		if (positionOffset < 0) {
    			return false;
    		}
    		int[] data = quad.getVertexData();
    		int stride = format.getIntegerSize();
    		int axis = face.getAxis().ordinal(); // X, Y, Z
    		float bound = face.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1.0F : 0.0F;
    		for (int vertex = 0; vertex < 4; ++vertex) {
    			float value = Float.intBitsToFloat(data[vertex * stride + positionOffset + axis]);
    			if (Math.abs(value - bound) > EPSILON) {
    				return false;
    			}
    		}
    		return true;
    	}

    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
    	return SpriteRegistry.sprite_uncovered_full;
    }

    /**
     * Fills quad container with all block quads.
     *
     * @param quadContainer the quad container
     */
    protected abstract void fillQuads(RenderPkg renderPkg);

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
    	return Pair.of(this, IConstants.perspectiveMatrix[cameraTransformType.ordinal()]);
    }

}
