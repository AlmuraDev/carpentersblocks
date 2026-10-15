package com.carpentersblocks.block;

import java.util.Collections;
import java.util.List;

import com.carpentersblocks.block.data.BarrierData;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.registry.BlockRegistry;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCarpentersBarrier extends BlockCoverable implements IShapedBlock {

	/** How far up and down a post on a stacked barrier shows through the column. */
	private static final int MAX_POST_SCAN = 16;

	public BlockCarpentersBarrier(Material material) {
		super(material);
	}

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		return BarrierData.getBoxes(renderData);
	}

	@Override
	public List<AxisAlignedBB> getCollisionBoxes(int renderData) {
		return BarrierData.getCollisionBoxes(renderData);
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes(int renderData) {
		return Collections.singletonList(BarrierData.getSelectionBox(renderData));
	}

	@Override
	public int getInventoryRenderData(int itemDamage) {
		return BarrierData.INVENTORY;
	}

	/**
	 * Adds the connections, stacked barriers and post visibility the shape depends on.
	 */
	@Override
	public int getRenderData(IBlockAccess blockAccess, BlockPos blockPos, CbTileEntity cbTileEntity) {
		int data = getConnectionData(blockAccess, blockPos, cbTileEntity.getCbMetadata());
		BlockPos above = blockPos.up();
		if (canConnectTo(blockAccess, above, EnumFacing.DOWN, false)) {
			data |= BarrierData.CONNECT_ABOVE;
		}
		if (blockAccess.getBlockState(blockPos.down()).getBlock() == this) {
			data |= BarrierData.BARRIER_BELOW;
		}
		if (blockAccess.getBlockState(above).getBlock() == this) {
			data |= BarrierData.BARRIER_ABOVE;
		}
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			if (canConnectTo(blockAccess, above.offset(side), side.getOpposite(), false)) {
				data |= BarrierData.connectUp(side);
			}
		}
		if (isPostShown(blockAccess, blockPos, data)) {
			data |= BarrierData.SHOW_POST;
		}
		return data;
	}

	/**
	 * Gets the block data with the horizontal connections added.
	 */
	private int getConnectionData(IBlockAccess blockAccess, BlockPos blockPos, int data) {
		// A forced post only connects to other barriers
		boolean barriersOnly = BarrierData.hasPostBit(data);
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			if (canConnectTo(blockAccess, blockPos.offset(side), side.getOpposite(), barriersOnly)) {
				data |= BarrierData.connect(side);
			}
		}
		return data;
	}

	/**
	 * Barriers connect to other barriers and to solid faces.
	 *
	 * @param blockPos the neighbor position
	 * @param face the neighbor face pointing back at the barrier
	 */
	private boolean canConnectTo(IBlockAccess blockAccess, BlockPos blockPos, EnumFacing face, boolean barriersOnly) {
		IBlockState blockState = blockAccess.getBlockState(blockPos);
		if (blockState.getBlock() == this || isGate(blockState)) {
			return true;
		}
		return !barriersOnly && blockState.getBlockFaceShape(blockAccess, blockPos, face) == BlockFaceShape.SOLID;
	}

	/**
	 * A post shows when this barrier needs one, or any barrier stacked directly above or below does.
	 */
	private boolean isPostShown(IBlockAccess blockAccess, BlockPos blockPos, int renderData) {
		if (needsPost(blockAccess, blockPos, renderData)) {
			return true;
		}
		for (EnumFacing vertical : new EnumFacing[] { EnumFacing.UP, EnumFacing.DOWN }) {
			BlockPos pos = blockPos.offset(vertical);
			for (int i = 0; i < MAX_POST_SCAN && blockAccess.getBlockState(pos).getBlock() == this; ++i) {
				CbTileEntity cbTileEntity = getTileEntity(blockAccess, pos);
				if (cbTileEntity != null && needsPost(blockAccess, pos, getConnectionData(blockAccess, pos, cbTileEntity.getCbMetadata()))) {
					return true;
				}
				pos = pos.offset(vertical);
			}
		}
		return false;
	}

	private boolean needsPost(IBlockAccess blockAccess, BlockPos blockPos, int connectionData) {
		// A torch on top, or a gate alongside, needs a post
		boolean postNeeded = blockAccess.getBlockState(blockPos.up()).getMaterial() == Material.CIRCUITS;
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			postNeeded |= isGate(blockAccess.getBlockState(blockPos.offset(side)));
		}
		return BarrierData.hasPost(connectionData, postNeeded);
	}

	private static boolean isGate(IBlockState blockState) {
		return BlockRegistry.blockCarpentersGate != null && blockState.getBlock() == BlockRegistry.blockCarpentersGate;
	}

	/**
	 * Takes the type of an adjacent barrier.
	 */
	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLivingBase, ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLivingBase, itemStack);
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (world.isRemote || cbTileEntity == null) {
			return;
		}
		int type = BarrierData.TYPE_VANILLA;
		for (EnumFacing facing : new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST }) {
			BlockPos adjacentPos = blockPos.offset(facing);
			if (world.getBlockState(adjacentPos).getBlock() == this) {
				CbTileEntity adjacent = getTileEntity(world, adjacentPos);
				if (adjacent != null) {
					type = BarrierData.getType(adjacent.getCbMetadata());
				}
			}
		}
		cbTileEntity.setCbMetadata(BarrierData.setType(cbTileEntity.getCbMetadata(), type));
	}

	/**
	 * Hammer left click: toggles the post.
	 */
	@Override
	protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		return cbTileEntity.setCbMetadata(BarrierData.togglePost(cbTileEntity.getCbMetadata()));
	}

	/**
	 * Hammer right click: next type, or next vanilla variant when sneaking.
	 */
	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		int data = cbTileEntity.getCbMetadata();
		int next = entityPlayer.isSneaking() ? BarrierData.getNextVanillaType(data) : BarrierData.getNextType(data);
		return cbTileEntity.setCbMetadata(next);
	}

	@Override
	public boolean isSideSolid(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, EnumFacing facing) {
		return facing == EnumFacing.UP;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, EnumFacing facing) {
		return facing.getAxis().isVertical() ? BlockFaceShape.CENTER : BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean canPlaceTorchOnTop(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos) {
		return true;
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

	@Override
	public boolean isPassable(IBlockAccess blockAccess, BlockPos blockPos) {
		return false;
	}

}
