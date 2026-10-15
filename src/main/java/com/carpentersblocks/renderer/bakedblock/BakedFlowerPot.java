package com.carpentersblocks.renderer.bakedblock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.carpentersblocks.block.BlockCarpentersFlowerPot;
import com.carpentersblocks.renderer.Quad;
import com.carpentersblocks.renderer.RenderPkg;
import com.carpentersblocks.renderer.helper.RenderHelper;
import com.carpentersblocks.util.attribute.AttributeHelper;
import com.carpentersblocks.util.attribute.AttributeItemStack;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.block.FlowerPotUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Draws the flower pot, its soil, and the plant from the plant's own model, scaled into the pot.
 */
@SideOnly(Side.CLIENT)
public class BakedFlowerPot extends BakedShapedBlock {

	/** Plant tint when fertilized (1.7.10: 0.45, 0.80, 0.30). */
	private static final int FERTILIZED_COLOR = 0x73CC4D;

	public BakedFlowerPot(IModelState modelState, VertexFormat vertexFormat, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, Block block) {
		super(modelState, vertexFormat, bakedTextureGetter, block);
	}

	@Override
	protected void fillQuads(RenderPkg renderPkg) {
		super.fillQuads(renderPkg);
		AttributeHelper attributes = renderPkg.getAttributeHelper();
		if (attributes == null) {
			return;
		}
		World world = Minecraft.getMinecraft().world;
		BlockPos blockPos = renderPkg.getBlockPos();

		ItemStack soil = getStack(attributes, EnumAttributeType.SOIL);
		if (!soil.isEmpty() && soil.getItem() instanceof ItemBlock) {
			@SuppressWarnings("deprecation")
			IBlockState soilState = ((ItemBlock) soil.getItem()).getBlock().getStateFromMeta(soil.getMetadata());
			TextureAtlasSprite soilSprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(soilState);
			for (Quad quad : RenderHelper.getBoxQuads(BlockCarpentersFlowerPot.getSoilBox(), soilSprite)) {
				renderPkg.add(quad);
			}
		}

		ItemStack plant = getStack(attributes, EnumAttributeType.PLANT);
		if (plant.isEmpty() || world == null) {
			return;
		}
		IBlockState plantState = FlowerPotUtil.getPlantState(plant, world, blockPos);
		if (plantState == null) {
			return;
		}
		boolean fertilized = !getStack(attributes, EnumAttributeType.FERTILIZER).isEmpty();
		Block plantBlock = plantState.getBlock();
		// Flowers and mushrooms at full size, other plants smaller, like 1.7.10
		double scale = plantBlock instanceof BlockFlower || plantBlock instanceof BlockMushroom ? 1.0D : 0.75D;
		BlockRenderLayer layer = plantBlock.getRenderLayer();
		addPlantQuads(renderPkg, world, blockPos, plantState, scale, 0.25D, fertilized, layer);
		IBlockState upper = FlowerPotUtil.getUpperHalf(plantState);
		if (upper != null) {
			addPlantQuads(renderPkg, world, blockPos, upper, scale, 0.25D + scale, fertilized, layer);
		}
	}

	private static ItemStack getStack(AttributeHelper attributes, EnumAttributeType type) {
		if (!attributes.hasAttribute(EnumAttributeLocation.HOST, type)) {
			return ItemStack.EMPTY;
		}
		return ((AttributeItemStack) attributes.getAttribute(EnumAttributeLocation.HOST, type)).getModel();
	}

	private void addPlantQuads(RenderPkg renderPkg, World world, BlockPos blockPos, IBlockState plantState, double scale, double yOffset, boolean fertilized, BlockRenderLayer layer) {
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(plantState);
		List<BakedQuad> quads = new ArrayList<BakedQuad>(model.getQuads(plantState, null, 0L));
		for (EnumFacing facing : EnumFacing.VALUES) {
			quads.addAll(model.getQuads(plantState, facing, 0L));
		}
		for (BakedQuad quad : quads) {
			int color = -1;
			if (quad.hasTintIndex()) {
				color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(plantState, world, blockPos, quad.getTintIndex());
				if (fertilized && (color & 0xFFFFFF) != 0xFFFFFF) {
					color = FERTILIZED_COLOR;
				}
			}
			renderPkg.addBakedQuad(transform(quad, scale, yOffset, color), layer);
		}
	}

	/**
	 * Scales a quad about the bottom center of the block, raises it, and bakes in a tint.
	 */
	private static BakedQuad transform(BakedQuad quad, double scale, double yOffset, int color) {
		VertexFormat format = quad.getFormat();
		int[] data = quad.getVertexData().clone();
		int stride = format.getIntegerSize();
		int positionOffset = -1;
		int colorOffset = -1;
		for (int idx = 0; idx < format.getElementCount(); ++idx) {
			VertexFormatElement element = format.getElement(idx);
			if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
				positionOffset = format.getOffset(idx) / 4;
			} else if (element.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
				colorOffset = format.getOffset(idx) / 4;
			}
		}
		for (int vertex = 0; vertex < 4; ++vertex) {
			int base = vertex * stride;
			if (positionOffset >= 0) {
				float x = Float.intBitsToFloat(data[base + positionOffset]);
				float y = Float.intBitsToFloat(data[base + positionOffset + 1]);
				float z = Float.intBitsToFloat(data[base + positionOffset + 2]);
				data[base + positionOffset] = Float.floatToRawIntBits((float) (0.5D + (x - 0.5D) * scale));
				data[base + positionOffset + 1] = Float.floatToRawIntBits((float) (y * scale + yOffset));
				data[base + positionOffset + 2] = Float.floatToRawIntBits((float) (0.5D + (z - 0.5D) * scale));
			}
			if (color != -1 && colorOffset >= 0) {
				data[base + colorOffset] = multiplyColor(data[base + colorOffset], color);
			}
		}
		return new BakedQuad(data, -1, quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), format);
	}

	/**
	 * Multiplies a vertex color (bytes R, G, B, A) by an RGB tint.
	 */
	private static int multiplyColor(int vertexColor, int tint) {
		int r = (vertexColor & 0xFF) * ((tint >> 16) & 0xFF) / 255;
		int g = ((vertexColor >> 8) & 0xFF) * ((tint >> 8) & 0xFF) / 255;
		int b = ((vertexColor >> 16) & 0xFF) * (tint & 0xFF) / 255;
		return (vertexColor & 0xFF000000) | (b << 16) | (g << 8) | r;
	}

}
