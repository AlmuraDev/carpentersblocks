package com.carpentersblocks.item;

import java.util.List;

import com.carpentersblocks.entity.EntityCarpentersTile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Places a Carpenter's Tile on the clicked face, taking dye, rotation and design from a tile next to it
 * (unless sneaking), like 1.7.10.
 */
public class ItemCarpentersTile extends Item {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos clickedPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		BlockPos tilePos = clickedPos.offset(side);
		ItemStack itemStack = player.getHeldItem(hand);
		if (!player.canPlayerEdit(tilePos, side, itemStack) || !world.isSideSolid(clickedPos, side, false)) {
			return EnumActionResult.FAIL;
		}
		if (world.isRemote) {
			return EnumActionResult.SUCCESS;
		}
		AxisAlignedBB box = EntityCarpentersTile.getLocalBox(side).offset(tilePos);
		// One tile per face
		for (EntityCarpentersTile existing : world.getEntitiesWithinAABB(EntityCarpentersTile.class, box.shrink(0.01D))) {
			if (existing.getDirection() == side) {
				return EnumActionResult.FAIL;
			}
		}
		EntityCarpentersTile tile = new EntityCarpentersTile(world, player, tilePos, side);
		if (!player.isSneaking()) {
			copyFromNeighbor(world, tile, tilePos, side);
		}
		world.spawnEntity(tile);
		world.playSound(null, tilePos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
		if (!player.capabilities.isCreativeMode) {
			itemStack.shrink(1);
		}
		return EnumActionResult.SUCCESS;
	}

	/**
	 * Takes dye, rotation and design from a tile on the same face next to this one.
	 */
	private void copyFromNeighbor(World world, EntityCarpentersTile tile, BlockPos tilePos, EnumFacing side) {
		for (EnumFacing direction : EnumFacing.VALUES) {
			if (direction.getAxis() == side.getAxis()) {
				continue;
			}
			BlockPos neighborPos = tilePos.offset(direction);
			List<EntityCarpentersTile> neighbors = world.getEntitiesWithinAABB(EntityCarpentersTile.class, EntityCarpentersTile.getLocalBox(side).offset(neighborPos).shrink(0.01D));
			for (EntityCarpentersTile neighbor : neighbors) {
				if (neighbor.getDirection() == side) {
					tile.setDye(neighbor.getDye());
					tile.setRotation(neighbor.getRotation());
					tile.setDesign(neighbor.getDesign());
					return;
				}
			}
		}
	}

}
