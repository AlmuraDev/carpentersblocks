package com.carpentersblocks.block;

import java.util.Collections;
import java.util.List;

import com.carpentersblocks.block.data.LadderData;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.registry.ConfigRegistry;

import net.minecraft.block.SoundType;
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

public class BlockCarpentersLadder extends BlockCoverable implements IShapedBlock {

	// Direction picked in getStateForPlacement, applied once the tile entity exists
	private static final ThreadLocal<Integer> _threadLocalPlacementDir = new ThreadLocal<Integer>();

	public BlockCarpentersLadder(Material material) {
		super(material);
		setSoundType(SoundType.LADDER);
	}

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		return LadderData.getBoxes(renderData);
	}

	@Override
	public List<ShapePart> getRenderParts(int renderData) {
		return LadderData.getParts(renderData);
	}

	@Override
	public List<AxisAlignedBB> getCollisionBoxes(int renderData) {
		return Collections.singletonList(LadderData.getBounds(renderData));
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes(int renderData) {
		return Collections.singletonList(LadderData.getBounds(renderData));
	}

	/**
	 * Adds joins with adjacent ladders facing the same way, so their stiles merge.
	 */
	@Override
	public int getRenderData(IBlockAccess blockAccess, BlockPos blockPos, CbTileEntity cbTileEntity) {
		int data = cbTileEntity.getCbMetadata();
		EnumFacing lowSide = LadderData.getLowSide(data);
		int dir = LadderData.getDirection(data);
		if (isLadderWithDirection(blockAccess, blockPos.offset(lowSide), dir)) {
			data |= LadderData.JOIN_LOW;
		}
		if (isLadderWithDirection(blockAccess, blockPos.offset(lowSide.getOpposite()), dir)) {
			data |= LadderData.JOIN_HIGH;
		}
		return data;
	}

	private boolean isLadderWithDirection(IBlockAccess blockAccess, BlockPos blockPos, int dir) {
		if (blockAccess.getBlockState(blockPos).getBlock() != this) {
			return false;
		}
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		return cbTileEntity != null && LadderData.getDirection(cbTileEntity.getCbMetadata()) == dir;
	}

	@Override
	public boolean isLadder(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, EntityLivingBase entity) {
		return true;
	}

	/**
	 * Wall ladders need a solid face behind them; freestanding ladders stand on a solid
	 * face or stack on other ladders.
	 */
	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos blockPos, EnumFacing facing) {
		if (world.isSideSolid(blockPos.offset(facing.getOpposite()), facing)) {
			return true;
		}
		return facing.getAxis().isVertical() && world.getBlockState(blockPos.offset(facing.getOpposite())).getBlock() == this;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos blockPos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		int dir;
		if (facing.getAxis().isHorizontal()) {
			dir = facing.getIndex();
		} else {
			// Freestanding, across the player's view
			dir = placer.getHorizontalFacing().getAxis() == EnumFacing.Axis.X ? LadderData.DIR_ON_Z : LadderData.DIR_ON_X;
		}
		_threadLocalPlacementDir.set(dir);
		return super.getStateForPlacement(world, blockPos, facing, hitX, hitY, hitZ, meta, placer, hand);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLivingBase, ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLivingBase, itemStack);
		Integer placementDir = _threadLocalPlacementDir.get();
		_threadLocalPlacementDir.remove();
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (world.isRemote || cbTileEntity == null) {
			return;
		}
		int data = LadderData.setDirection(0, placementDir != null ? placementDir : LadderData.DIR_ON_X);
		// Unless sneaking, continue the ladder above or below (below wins)
		if (!entityLivingBase.isSneaking()) {
			for (BlockPos adjacentPos : new BlockPos[] { blockPos.up(), blockPos.down() }) {
				if (world.getBlockState(adjacentPos).getBlock() == this) {
					CbTileEntity adjacent = getTileEntity(world, adjacentPos);
					if (adjacent != null) {
						data = adjacent.getCbMetadata();
					}
				}
			}
		}
		cbTileEntity.setCbMetadata(data);
	}

	/**
	 * Wall ladders break when the block behind them is removed.
	 */
	@Override
	public void onNeighborChange(IBlockAccess blockAccess, BlockPos blockPos, BlockPos neighborBlockPos) {
		super.onNeighborChange(blockAccess, blockPos, neighborBlockPos);
		if (!(blockAccess instanceof World) || ((World)blockAccess).isRemote || ConfigRegistry.enableFreeStandingLadders) {
			return;
		}
		World world = (World) blockAccess;
		if (world.getBlockState(blockPos).getBlock() != this) {
			return;
		}
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null || LadderData.isFreestanding(cbTileEntity.getCbMetadata())) {
			return;
		}
		EnumFacing facing = LadderData.getFacing(cbTileEntity.getCbMetadata());
		if (!world.isSideSolid(blockPos.offset(facing.getOpposite()), facing)) {
			destroyBlock(world, blockPos, true);
		}
	}

	/**
	 * Hammer left click: next ladder type.
	 */
	@Override
	protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		return cbTileEntity.setCbMetadata(LadderData.getNextType(cbTileEntity.getCbMetadata(), true));
	}

	/**
	 * Hammer right click: previous ladder type.
	 */
	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		return cbTileEntity.setCbMetadata(LadderData.getNextType(cbTileEntity.getCbMetadata(), false));
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
		int dir = LadderData.getDirection(data);
		if (dir < 2) {
			dir = dir == LadderData.DIR_ON_X ? LadderData.DIR_ON_Z : LadderData.DIR_ON_X;
		} else {
			EnumFacing facing = EnumFacing.byIndex(dir);
			dir = (axis == EnumFacing.UP ? facing.rotateY() : facing.rotateYCCW()).getIndex();
		}
		return cbTileEntity.setCbMetadata(LadderData.setDirection(data, dir));
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
