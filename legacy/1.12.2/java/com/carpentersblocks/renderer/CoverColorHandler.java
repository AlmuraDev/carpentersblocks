package com.carpentersblocks.renderer;

import java.util.Map;

import com.carpentersblocks.block.state.Property;
import com.carpentersblocks.util.attribute.AbstractAttribute;
import com.carpentersblocks.util.attribute.AbstractAttribute.Key;
import com.carpentersblocks.util.attribute.AttributeHelper;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.block.BlockUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Colors tinted cover quads (grass, leaves, vines...) when the renderer draws them.
 * <p>
 * Tints are looked up by the renderer with the chunk snapshot it builds from, instead of the live
 * world from the chunk builder thread. The cover comes from the block's extended state, so the
 * tile entity isn't read either.
 */
@SideOnly(Side.CLIENT)
public class CoverColorHandler implements IBlockColor {

	/** Grass color, for the grass overlay. */
	public static final int TINT_GRASS = 0x7000;

	/** Biome color of a routed liquid (water). */
	public static final int TINT_FLUID = 0x7001;

	/** Base of cover tints: base + location * 256 + the cover quad's own tint index. */
	private static final int TINT_COVER = 0x8000;

	/** Base of flower pot plant tints: base + 256 for the upper half + the plant quad's own tint index. */
	private static final int TINT_PLANT = 0x6000;

	/** Plant tint when fertilized (1.7.10: 0.45, 0.80, 0.30). */
	private static final int FERTILIZED_COLOR = 0x73CC4D;

	public static int getCoverTint(EnumAttributeLocation location, int coverTintIndex) {
		return TINT_COVER + (location.ordinal() << 8) + (coverTintIndex & 0xFF);
	}

	public static int getPlantTint(boolean upperHalf, int plantTintIndex) {
		return TINT_PLANT + (upperHalf ? 0x100 : 0) + (plantTintIndex & 0xFF);
	}

	@Override
	@SuppressWarnings("unchecked")
	public int colorMultiplier(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, int tintIndex) {
		if (tintIndex == TINT_GRASS) {
			return Minecraft.getMinecraft().getBlockColors().colorMultiplier(Blocks.GRASS.getDefaultState(), blockAccess, blockPos, 0);
		}
		if (tintIndex < TINT_PLANT || !(blockState instanceof IExtendedBlockState)) {
			return -1;
		}
		if (tintIndex == TINT_FLUID) {
			IBlockState fluidState = ((IExtendedBlockState) blockState).getValue(Property.ROUTED_FLUID);
			return fluidState == null ? -1 : Minecraft.getMinecraft().getBlockColors().colorMultiplier(fluidState, blockAccess, blockPos, 0);
		}
		Map<Key, AbstractAttribute> attributes = (Map<Key, AbstractAttribute>) ((IExtendedBlockState) blockState).getValue(Property.ATTR_MAP);
		if (tintIndex < TINT_GRASS) {
			return getPlantColor((IExtendedBlockState) blockState, attributes, blockAccess, blockPos, tintIndex - TINT_PLANT);
		}
		int locationIdx = (tintIndex - TINT_COVER) >> 8;
		if (attributes == null || tintIndex < TINT_COVER || locationIdx >= EnumAttributeLocation.values().length) {
			return -1;
		}
		IBlockState coverState = BlockUtil.getAttributeBlockState(new AttributeHelper(attributes), EnumAttributeLocation.values()[locationIdx], EnumAttributeType.COVER);
		if (coverState == null) {
			return -1;
		}
		return Minecraft.getMinecraft().getBlockColors().colorMultiplier(coverState, blockAccess, blockPos, (tintIndex - TINT_COVER) & 0xFF);
	}

	private static int getPlantColor(IExtendedBlockState blockState, Map<Key, AbstractAttribute> attributes, IBlockAccess blockAccess, BlockPos blockPos, int plantTint) {
		IBlockState plantState = blockState.getValue(Property.PLANT);
		if (plantState != null && (plantTint & 0x100) != 0) {
			plantState = com.carpentersblocks.util.block.FlowerPotUtil.getUpperHalf(plantState);
		}
		if (plantState == null) {
			return -1;
		}
		int color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(plantState, blockAccess, blockPos, plantTint & 0xFF);
		boolean fertilized = attributes != null && new AttributeHelper(attributes).hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.FERTILIZER);
		if (fertilized && (color & 0xFFFFFF) != 0xFFFFFF) {
			color = FERTILIZED_COLOR;
		}
		return color;
	}

}
