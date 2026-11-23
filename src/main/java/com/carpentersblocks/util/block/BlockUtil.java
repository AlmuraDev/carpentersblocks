package com.carpentersblocks.util.block;

import javax.annotation.Nullable;

import com.carpentersblocks.CbConfig;
import com.carpentersblocks.block.ICoverableBlock;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.util.attribute.AttributeItemStack;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.handler.DyeHandler;
import com.carpentersblocks.util.handler.OverlayHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public final class BlockUtil {

    private BlockUtil() {}

    /**
     * Takes an ItemStack and returns its block, or air if it does not hold a block.
     */
    public static Block toBlock(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock() : Blocks.AIR;
    }

    /**
     * Gets the block state an item attribute is drawn and acts as: the state saved with a cover, else the item's
     * block in its default state, or null for items that are not blocks.
     */
    @Nullable
    public static BlockState getBlockState(AttributeItemStack attribute) {
        if (attribute.getBlockState() != null) {
            return attribute.getBlockState();
        }
        return getBlockState(attribute.getModel());
    }

    @Nullable
    public static BlockState getBlockState(ItemStack itemStack) {
        Block block = toBlock(itemStack);
        return block == Blocks.AIR ? null : block.defaultBlockState();
    }

    /**
     * Gets the location a click on a side changes: the side cover there, or the whole block.
     */
    public static EnumAttributeLocation getAttributeLocationForFacing(CbBlockEntity blockEntity, Direction facing) {
        EnumAttributeLocation side = EnumAttributeLocation.fromDirection(facing);
        return blockEntity.getAttributeHelper().hasAttribute(side, EnumAttributeType.COVER) ? side : EnumAttributeLocation.HOST;
    }

    /**
     * Returns whether an item can cover a block: full blocks, slabs, panes and glass, or the config exceptions.
     */
    public static boolean isCover(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BlockItem blockItem) || isOverlay(itemStack)) {
            return false;
        }
        Block block = blockItem.getBlock();
        if (block instanceof ICoverableBlock) {
            return false;
        }
        BlockState blockState = block.defaultBlockState();
        return blockState.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)
            || block instanceof SlabBlock
            || block instanceof IronBarsBlock
            || block instanceof HalfTransparentBlock
            || CbConfig.COVER_EXCEPTIONS.get().contains(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());
    }

    /**
     * Whether an item is glowstone dust (by tag; 1.12.2 used the ore dictionary).
     */
    public static boolean isIlluminator(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.is(Tags.Items.DUSTS_GLOWSTONE);
    }

    /**
     * Returns true if ItemStack is a dye.
     */
    public static boolean isDye(ItemStack itemStack, boolean allowWhite) {
        return DyeHandler.isDye(itemStack, allowWhite);
    }

    /**
     * Returns whether ItemStack contains a valid overlay item or block.
     */
    public static boolean isOverlay(ItemStack itemStack) {
        return OverlayHandler.isOverlay(itemStack);
    }

}
