package com.carpentersblocks.block;

import com.carpentersblocks.tileentity.CbTileEntity;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * A shaped block attached to one face of the block next to it (1.7.10 BlockSided):
 * buttons, levers, torches, daylight sensors.
 * <p>
 * The attach side is the face that was clicked, pointing away from the supporting block.
 * Redstone output is weak on every side and strong into the supporting block.
 */
public abstract class BlockSidedShaped extends BlockCoverable implements IShapedBlock {

	// Clicked face picked in getStateForPlacement, applied once the tile entity exists
	private static final ThreadLocal<EnumFacing> _threadLocalAttachSide = new ThreadLocal<EnumFacing>();

	public BlockSidedShaped(Material material) {
		super(material);
	}

	/**
	 * Gets the side the block is attached to, from its data.
	 */
	public abstract EnumFacing getAttachSide(int data);

	/**
	 * Sets the side the block is attached to.
	 */
	public abstract int setAttachSide(int data, EnumFacing side);

	/**
	 * Whether the block can attach to a side.
	 */
	protected boolean canAttachToSide(EnumFacing side) {
		return true;
	}

	/**
	 * Gets the redstone output, 0 to 15.
	 */
	protected int getPowerOutput(int data) {
		return 0;
	}

	/**
	 * Sets up the data of a newly placed block.
	 *
	 * @param data the data, with the attach side set
	 * @param placer the placer
	 * @return the data
	 */
	protected int onSidedBlockPlaced(int data, EntityLivingBase placer) {
		return data;
	}

	protected boolean isSupported(IBlockAccess blockAccess, BlockPos blockPos, EnumFacing side) {
		return blockAccess.isSideSolid(blockPos.offset(side.getOpposite()), side, false);
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos blockPos, EnumFacing side) {
		return canAttachToSide(side) && isSupported(world, blockPos, side);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos blockPos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		_threadLocalAttachSide.set(facing);
		return super.getStateForPlacement(world, blockPos, facing, hitX, hitY, hitZ, meta, placer, hand);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLivingBase, ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLivingBase, itemStack);
		EnumFacing side = _threadLocalAttachSide.get();
		_threadLocalAttachSide.remove();
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (world.isRemote || cbTileEntity == null) {
			return;
		}
		if (side == null || !canAttachToSide(side)) {
			side = EnumFacing.UP;
		}
		int data = onSidedBlockPlaced(setAttachSide(cbTileEntity.getCbMetadata(), side), entityLivingBase);
		cbTileEntity.setCbMetadata(data);
		notifyPowerChange(world, blockPos, data);
	}

	/**
	 * Breaks the block when its support is removed.
	 */
	@Override
	public void onNeighborChange(IBlockAccess blockAccess, BlockPos blockPos, BlockPos neighborBlockPos) {
		super.onNeighborChange(blockAccess, blockPos, neighborBlockPos);
		if (!(blockAccess instanceof World) || ((World)blockAccess).isRemote) {
			return;
		}
		World world = (World) blockAccess;
		if (world.getBlockState(blockPos).getBlock() != this) {
			return;
		}
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity != null && !isSupported(world, blockPos, getAttachSide(cbTileEntity.getCbMetadata()))) {
			destroyBlock(world, blockPos, true);
		}
	}

	/**
	 * Updates the block and the supporting block after the redstone output changed.
	 */
	protected void notifyPowerChange(World world, BlockPos blockPos, int data) {
		world.notifyNeighborsOfStateChange(blockPos, this, false);
		world.notifyNeighborsOfStateChange(blockPos.offset(getAttachSide(data).getOpposite()), this, false);
	}

	@Override
	public void breakBlock(World world, BlockPos blockPos, IBlockState blockState) {
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		boolean powered = cbTileEntity != null && getPowerOutput(cbTileEntity.getCbMetadata()) > 0;
		EnumFacing side = cbTileEntity != null ? getAttachSide(cbTileEntity.getCbMetadata()) : null;
		super.breakBlock(world, blockPos, blockState);
		if (powered) {
			world.notifyNeighborsOfStateChange(blockPos, this, false);
			world.notifyNeighborsOfStateChange(blockPos.offset(side.getOpposite()), this, false);
		}
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, EnumFacing facing) {
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		return cbTileEntity != null ? getPowerOutput(cbTileEntity.getCbMetadata()) : 0;
	}

	/**
	 * Strong power goes into the supporting block, like vanilla buttons.
	 */
	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, EnumFacing facing) {
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		if (cbTileEntity == null) {
			return 0;
		}
		int data = cbTileEntity.getCbMetadata();
		return facing == getAttachSide(data) ? getPowerOutput(data) : 0;
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
		int data = cbTileEntity.getCbMetadata();
		EnumFacing side = getAttachSide(data);
		if (side.getAxis().isVertical()) {
			return false;
		}
		side = axis == EnumFacing.UP ? side.rotateY() : side.rotateYCCW();
		return cbTileEntity.setCbMetadata(setAttachSide(data, side));
	}

	@Override
	public boolean isSideSolid(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, EnumFacing facing) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, EnumFacing facing) {
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
