package com.carpentersblocks.item;

import com.carpentersblocks.block.BlockCarpentersBed;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.registry.BlockRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Places a Carpenter's Bed: the foot on the clicked block, the head away from the player, like 1.7.10.
 */
public class ItemCarpentersBed extends Item {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos clickedPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		Block bed = BlockRegistry.blockCarpentersBed;
		if (side != EnumFacing.UP || bed == null) {
			return EnumActionResult.FAIL;
		}
		EnumFacing look = player.getHorizontalFacing();
		BlockPos footPos = clickedPos.up();
		BlockPos headPos = footPos.offset(look);
		ItemStack itemStack = player.getHeldItem(hand);
		if (!player.canPlayerEdit(footPos, side, itemStack) || !player.canPlayerEdit(headPos, side, itemStack)
			|| !world.isAirBlock(footPos) || !world.isAirBlock(headPos)
			|| !world.isSideSolid(footPos.down(), EnumFacing.UP) || !world.isSideSolid(headPos.down(), EnumFacing.UP)) {
			return EnumActionResult.FAIL;
		}
		if (world.isRemote) {
			return EnumActionResult.SUCCESS;
		}
		// Head to foot points back at the player
		int data = BlockCarpentersBed.setDirection(0, look.getOpposite());
		world.setBlockState(footPos, bed.getDefaultState(), 2);
		world.setBlockState(headPos, bed.getDefaultState(), 2);
		setPiece(world, footPos, player, itemStack, BlockCarpentersBed.setHead(data, false));
		setPiece(world, headPos, player, itemStack, BlockCarpentersBed.setHead(data, true));
		world.notifyNeighborsOfStateChange(footPos, bed, false);
		world.notifyNeighborsOfStateChange(headPos, bed, false);

		SoundType soundType = bed.getSoundType(bed.getDefaultState(), world, footPos, player);
		world.playSound(null, footPos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
		if (!player.capabilities.isCreativeMode) {
			itemStack.shrink(1);
		}
		return EnumActionResult.SUCCESS;
	}

	private void setPiece(World world, BlockPos blockPos, EntityPlayer player, ItemStack itemStack, int data) {
		Block bed = BlockRegistry.blockCarpentersBed;
		bed.onBlockPlacedBy(world, blockPos, world.getBlockState(blockPos), player, itemStack);
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof CbTileEntity) {
			((CbTileEntity) tileEntity).setCbMetadata(data);
		}
	}

}
