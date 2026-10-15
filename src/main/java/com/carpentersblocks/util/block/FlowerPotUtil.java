package com.carpentersblocks.util.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;

/**
 * Soil and plant rules for the Carpenter's Flower Pot, ported from 1.7.10 (util/flowerpot).
 */
public class FlowerPotUtil {

	/**
	 * Soil: a plain block of grass, dirt or sand.
	 */
	public static boolean isSoil(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemBlock)) {
			return false;
		}
		Block block = ((ItemBlock) itemStack.getItem()).getBlock();
		if (block.hasTileEntity(block.getDefaultState())) {
			return false;
		}
		Material material = block.getDefaultState().getMaterial();
		return material == Material.GRASS || material == Material.GROUND || material == Material.SAND;
	}

	/**
	 * Plants: plantable or shearable blocks without a tile entity, plus seeds and crops.
	 */
	public static boolean isPlant(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item == Items.REEDS || item == Items.WHEAT || item instanceof IPlantable) {
			return true;
		}
		if (!(item instanceof ItemBlock)) {
			return false;
		}
		Block block = ((ItemBlock) item).getBlock();
		return block != Blocks.AIR && !block.hasTileEntity(block.getDefaultState())
			&& (block instanceof IPlantable || block instanceof IShearable);
	}

	/**
	 * Gets the block state a plant item shows as. Crops are fully grown.
	 */
	@SuppressWarnings("deprecation")
	@Nullable
	public static IBlockState getPlantState(ItemStack itemStack, IBlockAccess blockAccess, BlockPos blockPos) {
		Item item = itemStack.getItem();
		IBlockState blockState;
		if (item == Items.REEDS) {
			blockState = Blocks.REEDS.getDefaultState();
		} else if (item == Items.WHEAT) {
			blockState = Blocks.WHEAT.getDefaultState();
		} else if (item instanceof ItemBlock) {
			blockState = ((ItemBlock) item).getBlock().getStateFromMeta(item.getMetadata(itemStack.getMetadata()));
		} else if (item instanceof IPlantable) {
			blockState = ((IPlantable) item).getPlant(blockAccess, blockPos);
		} else {
			return null;
		}
		if (blockState != null && blockState.getBlock() instanceof BlockCrops) {
			BlockCrops crops = (BlockCrops) blockState.getBlock();
			blockState = crops.withAge(crops.getMaxAge());
		}
		return blockState;
	}

	/**
	 * Gets the top half of a double plant, or null for single plants.
	 */
	@Nullable
	public static IBlockState getUpperHalf(IBlockState plantState) {
		if (plantState.getBlock() instanceof BlockDoublePlant) {
			return plantState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);
		}
		return null;
	}

	/**
	 * Cacti and leaves fill the pot almost to the top of the block.
	 */
	public static boolean isTall(IBlockState plantState) {
		Block block = plantState.getBlock();
		return block instanceof BlockCactus || block instanceof BlockLeaves || plantState.getMaterial() == Material.LEAVES;
	}

}
