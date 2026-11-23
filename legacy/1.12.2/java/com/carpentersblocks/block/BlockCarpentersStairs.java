package com.carpentersblocks.block;

import java.util.List;

import com.carpentersblocks.block.data.StairsData;
import com.carpentersblocks.tileentity.CbTileEntity;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCarpentersStairs extends BlockCoverable implements IShapedBlock {

	// Stairs ID picked in getStateForPlacement, applied once the tile entity exists
	private static final ThreadLocal<Integer> _threadLocalPlacementId = new ThreadLocal<Integer>();

	public BlockCarpentersStairs(Material material) {
		super(material);
	}

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		return StairsData.getBoxes(StairsData.getId(renderData));
	}

	@Override
	public int getInventoryRenderData(int itemDamage) {
		return StairsData.ID_POS_N;
	}

	@Override
	public boolean supportsSideCovers() {
		return true;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos blockPos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		_threadLocalPlacementId.set(StairsData.getPlacementId(facing, hitX, hitY, hitZ, placer.getHorizontalFacing()));
		return super.getStateForPlacement(world, blockPos, facing, hitX, hitY, hitZ, meta, placer, hand);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLivingBase, ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLivingBase, itemStack);
		Integer placementId = _threadLocalPlacementId.get();
		_threadLocalPlacementId.remove();
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (world.isRemote || cbTileEntity == null) {
			return;
		}
		int id = placementId != null ? placementId : StairsData.ID_POS_N;
		// Sneaking skips connecting to adjacent stairs
		if (!entityLivingBase.isSneaking()) {
			StairsData.Neighbors neighbors = getNeighbors(world, blockPos);
			id = StairsData.transformStairs(id, neighbors);
			StairsData.Neighbors adjusted = StairsData.transformAdjacentStairs(id, neighbors);
			setStairsId(world, blockPos.west(), adjusted.xn);
			setStairsId(world, blockPos.east(), adjusted.xp);
			setStairsId(world, blockPos.north(), adjusted.zn);
			setStairsId(world, blockPos.south(), adjusted.zp);
		}
		cbTileEntity.setCbMetadata(id);
	}

	private StairsData.Neighbors getNeighbors(World world, BlockPos blockPos) {
		StairsData.Neighbors neighbors = new StairsData.Neighbors();
		neighbors.xn = getStairsId(world, blockPos.west());
		neighbors.xp = getStairsId(world, blockPos.east());
		neighbors.yn = getStairsId(world, blockPos.down());
		neighbors.yp = getStairsId(world, blockPos.up());
		neighbors.zn = getStairsId(world, blockPos.north());
		neighbors.zp = getStairsId(world, blockPos.south());
		return neighbors;
	}

	/**
	 * Gets the stairs ID at a position, or -1 if there are no Carpenter's Stairs.
	 */
	private int getStairsId(World world, BlockPos blockPos) {
		if (world.getBlockState(blockPos).getBlock() != this) {
			return -1;
		}
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		return cbTileEntity != null ? StairsData.getId(cbTileEntity.getCbMetadata()) : -1;
	}

	private void setStairsId(World world, BlockPos blockPos, int id) {
		if (id >= 0 && world.getBlockState(blockPos).getBlock() == this) {
			CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
			if (cbTileEntity != null) {
				cbTileEntity.setCbMetadata(id);
			}
		}
	}

	/**
	 * Hammer left click: rotates the stairs.
	 */
	@Override
	protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		int id = StairsData.getId(cbTileEntity.getCbMetadata());
		return cbTileEntity.setCbMetadata(StairsData.getNextDirection(id));
	}

	/**
	 * Hammer right click: changes the stairs type.
	 */
	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		int id = StairsData.getId(cbTileEntity.getCbMetadata());
		return cbTileEntity.setCbMetadata(StairsData.getNextType(id));
	}

	/**
	 * Damages the hammer with a chance to not damage.
	 */
	@Override
	protected void damageItemWithChance(World world, EntityPlayer entityPlayer, EnumHand hand) {
		if (world.rand.nextFloat() <= com.carpentersblocks.util.registry.ConfigRegistry.itemHammerDamageChanceFromStairs) {
			super.damageItemWithChance(world, entityPlayer, hand);
		}
	}

	@Override
	public boolean rotateBlock(World world, BlockPos blockPos, EnumFacing axis) {
		if (axis.getAxis() != EnumFacing.Axis.Y) {
			return false;
		}
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null) {
			return false;
		}
		int id = StairsData.getId(cbTileEntity.getCbMetadata());
		return cbTileEntity.setCbMetadata(StairsData.rotate(id, axis == EnumFacing.UP));
	}

	@Override
	public boolean isSideSolid(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, EnumFacing facing) {
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		return cbTileEntity != null
			&& isBlockSolid(blockAccess, blockPos)
			&& StairsData.isFullFace(StairsData.getId(cbTileEntity.getCbMetadata()), facing);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, EnumFacing facing) {
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		if (cbTileEntity != null && StairsData.isFullFace(StairsData.getId(cbTileEntity.getCbMetadata()), facing)) {
			return BlockFaceShape.SOLID;
		}
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState blockState) {
		return false;
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, EnumFacing facing) {
		return false;
	}

}
