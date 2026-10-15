package com.carpentersblocks.renderer.bakedblock;

import java.util.List;
import java.util.function.Function;

import com.carpentersblocks.block.IShapedBlock;
import com.carpentersblocks.block.ShapePart;
import com.carpentersblocks.renderer.AbstractBakedModel;
import com.carpentersblocks.renderer.Quad;
import com.carpentersblocks.renderer.RenderPkg;
import com.carpentersblocks.renderer.helper.RenderHelper;
import com.carpentersblocks.util.registry.SpriteRegistry;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Draws an {@link IShapedBlock} from its parts.
 */
@SideOnly(Side.CLIENT)
public class BakedShapedBlock extends AbstractBakedModel {

	private final IShapedBlock _shapedBlock;

	// Per instance: models are baked again on resource reload, so this never holds stale sprites
	private List<BakedQuad> _inventoryBakedQuads;

	public BakedShapedBlock(IModelState modelState, VertexFormat vertexFormat, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, Block block) {
		super(modelState, vertexFormat, bakedTextureGetter);
		_shapedBlock = (IShapedBlock) block;
	}

	@Override
	public List<BakedQuad> getInventoryQuads(RenderPkg renderPkg) {
		if (_inventoryBakedQuads == null) {
			addParts(renderPkg, _shapedBlock.getInventoryRenderData(0));
			_inventoryBakedQuads = renderPkg.getInventoryQuads();
		}
		return _inventoryBakedQuads;
	}

	@Override
	protected void fillQuads(RenderPkg renderPkg) {
		addParts(renderPkg, renderPkg.getData());
	}

	private void addParts(RenderPkg renderPkg, int renderData) {
		for (ShapePart part : _shapedBlock.getRenderParts(renderData)) {
			TextureAtlasSprite sprite = part.getTexture() == null
				? SpriteRegistry.sprite_uncovered_solid
				: Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(part.getTexture().toString());
			List<Quad> quads = part.getCorners() != null
				? RenderHelper.getCuboidQuads(part.getCorners(), sprite)
				: RenderHelper.getBoxQuads(part.getBox(), sprite);
			for (Quad quad : quads) {
				ResourceLocation faceTexture = part.getTexture(quad.getFacing());
				if (faceTexture != null && !faceTexture.equals(part.getTexture())) {
					quad.setSprite(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(faceTexture.toString()));
				}
				quad.setUVRotation(part.getUVRotation());
				// Panes blend on the translucent pass, or are cut out when "Enable Pane Alpha Rendering" is off
				if (part.isTranslucent() && com.carpentersblocks.util.registry.ConfigRegistry.enableAlphaPanes) {
					quad.setRenderLayer(BlockRenderLayer.TRANSLUCENT);
				}
				if (part.isFullBright()) {
					quad.setMaxBrightness(true);
				}
				if (part.getColor() != -1) {
					quad.setRgb(part.getColor());
				}
				renderPkg.add(quad);
			}
		}
	}

}
