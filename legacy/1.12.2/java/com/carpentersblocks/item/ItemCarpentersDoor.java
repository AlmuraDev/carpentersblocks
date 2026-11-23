package com.carpentersblocks.item;

import com.carpentersblocks.block.data.DoorData;
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
 * Places a Carpenter's Door (both halves) on top of the clicked block, like 1.7.10.
 */
public class ItemCarpentersDoor extends Item {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos clickedPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		Block door = BlockRegistry.blockCarpentersDoor;
		if (side != EnumFacing.UP || door == null) {
			return EnumActionResult.FAIL;
		}
		BlockPos bottomPos = clickedPos.up();
		BlockPos topPos = bottomPos.up();
		ItemStack itemStack = player.getHeldItem(hand);
		if (topPos.getY() >= world.getHeight()
			|| !player.canPlayerEdit(bottomPos, side, itemStack) || !player.canPlayerEdit(topPos, side, itemStack)
			|| !world.isAirBlock(bottomPos) || !world.isAirBlock(topPos)
			|| !world.isSideSolid(clickedPos, EnumFacing.UP)) {
			return EnumActionResult.FAIL;
		}
		if (world.isRemote) {
			return EnumActionResult.SUCCESS;
		}

		EnumFacing facing = player.getHorizontalFacing();
		int data = DoorData.setFacing(0, facing);
		// Hinge on the right next to a left hinged door facing the same way: a double door
		int leftData = getDoorData(world, bottomPos.offset(facing.rotateYCCW()));
		boolean hingeRight = leftData >= 0 && !DoorData.isTop(leftData) && DoorData.getFacing(leftData) == facing && !DoorData.isHingeRight(leftData);
		data = DoorData.setHingeRight(data, hingeRight);
		// Take type and redstone behavior from an adjacent door
		for (BlockPos adjacentPos : new BlockPos[] { bottomPos.west(), bottomPos.east(), bottomPos.north(), bottomPos.south() }) {
			int adjacentData = getDoorData(world, adjacentPos);
			if (adjacentData >= 0) {
				data = DoorData.setRigid(DoorData.setType(data, DoorData.getType(adjacentData)), DoorData.isRigid(adjacentData));
				break;
			}
		}

		// Place both halves before either checks for the other
		world.setBlockState(bottomPos, door.getDefaultState(), 2);
		world.setBlockState(topPos, door.getDefaultState(), 2);
		setPiece(world, bottomPos, player, itemStack, DoorData.setTop(data, false));
		setPiece(world, topPos, player, itemStack, DoorData.setTop(data, true));
		world.notifyNeighborsOfStateChange(bottomPos, door, false);
		world.notifyNeighborsOfStateChange(topPos, door, false);

		SoundType soundType = door.getSoundType(door.getDefaultState(), world, bottomPos, player);
		world.playSound(null, bottomPos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
		if (!player.capabilities.isCreativeMode) {
			itemStack.shrink(1);
		}
		return EnumActionResult.SUCCESS;
	}

	private void setPiece(World world, BlockPos blockPos, EntityPlayer player, ItemStack itemStack, int data) {
		Block door = BlockRegistry.blockCarpentersDoor;
		door.onBlockPlacedBy(world, blockPos, world.getBlockState(blockPos), player, itemStack);
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof CbTileEntity) {
			((CbTileEntity) tileEntity).setCbMetadata(data);
		}
	}

	/**
	 * Gets door data at a position, or -1 if there is no Carpenter's Door.
	 */
	private int getDoorData(World world, BlockPos blockPos) {
		if (world.getBlockState(blockPos).getBlock() != BlockRegistry.blockCarpentersDoor) {
			return -1;
		}
		TileEntity tileEntity = world.getTileEntity(blockPos);
		return tileEntity instanceof CbTileEntity ? ((CbTileEntity) tileEntity).getCbMetadata() : -1;
	}

}
