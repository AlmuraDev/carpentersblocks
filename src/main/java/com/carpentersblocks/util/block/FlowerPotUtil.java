package com.carpentersblocks.util.block;

import javax.annotation.Nullable;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.common.IShearable;

/**
 * Soil and plant rules for the Carpenter's Flower Pot, ported from 1.7.10 (util/flowerpot).
 * <p>
 * 1.12.2 went by block materials and IPlantable; 26.3 has neither, so soil goes by the dirt and sand tags and plants by
 * their block class.
 */
public final class FlowerPotUtil {

    private FlowerPotUtil() {}

    @Nullable
    private static Block getBlock(ItemStack itemStack) {
        return itemStack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock() : null;
    }

    /**
     * Soil: a plain block of grass, dirt, sand or gravel.
     */
    public static boolean isSoil(ItemStack itemStack) {
        Block block = getBlock(itemStack);
        if (block == null || block instanceof EntityBlock) {
            return false;
        }
        BlockState blockState = block.defaultBlockState();
        return blockState.is(BlockTags.DIRT) || blockState.is(BlockTags.SAND) || block == Blocks.GRAVEL;
    }

    /**
     * Plants: flowers, saplings, crops (their seeds), mushrooms, cacti, sugar cane, leaves and other shearable plants,
     * plus wheat.
     */
    public static boolean isPlant(ItemStack itemStack) {
        if (itemStack.is(Items.WHEAT)) {
            return true;
        }
        Block block = getBlock(itemStack);
        return block != null && block != Blocks.AIR && !(block instanceof EntityBlock)
            && (block instanceof VegetationBlock || block instanceof CactusBlock || block instanceof SugarCaneBlock
                || block instanceof LeavesBlock || block instanceof IShearable);
    }

    /**
     * Gets the block state a plant item shows as. Crops are fully grown.
     */
    @Nullable
    public static BlockState getPlantState(ItemStack itemStack) {
        BlockState blockState;
        if (itemStack.is(Items.WHEAT)) {
            blockState = Blocks.WHEAT.defaultBlockState();
        } else {
            Block block = getBlock(itemStack);
            if (block == null) {
                return null;
            }
            blockState = block.defaultBlockState();
        }
        if (blockState.getBlock() instanceof CropBlock crops) {
            blockState = crops.getStateForAge(crops.getMaxAge());
        }
        return blockState;
    }

    /**
     * Gets the top half of a double plant, or null for single plants.
     */
    @Nullable
    public static BlockState getUpperHalf(BlockState plantState) {
        if (plantState.getBlock() instanceof DoublePlantBlock) {
            return plantState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
        }
        return null;
    }

    /**
     * Cacti and leaves fill the pot almost to the top of the block.
     */
    public static boolean isTall(BlockState plantState) {
        Block block = plantState.getBlock();
        return block instanceof CactusBlock || block instanceof LeavesBlock;
    }

}
