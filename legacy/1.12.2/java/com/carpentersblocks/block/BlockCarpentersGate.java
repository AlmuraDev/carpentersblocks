package com.carpentersblocks.block;

import java.util.Collections;
import java.util.List;

import com.carpentersblocks.block.data.BarrierData;
import com.carpentersblocks.block.data.GateData;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.registry.BlockRegistry;

import net.minecraft.block.Block;
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

/**
 * Carpenter's Gate, ported from 1.7.10: a fence gate in every barrier style, opening away from the player.
 */
public class BlockCarpentersGate extends BlockCoverable implements IShapedBlock {

	private static final int SOUND_OPEN = 1008;
	private static final int SOUND_CLOSE = 1014;

	public BlockCarpentersGate(Material material) {
		super(material);
	}

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		return GateData.getBoxes(renderData);
	}

	@Override
	public List<AxisAlignedBB> getCollisionBoxes(int renderData) {
		return GateData.getCollisionBoxes(renderData);
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes(int renderData) {
		return Collections.singletonList(GateData.getSelectionBox(renderData));
	}

	@Override
	public int getInventoryRenderData(int itemDamage) {
		return GateData.INVENTORY;
	}

	/**
	 * Stacked gates merge into one tall gate.
	 */
	@Override
	public int getRenderData(IBlockAccess blockAccess, BlockPos blockPos, CbTileEntity cbTileEntity) {
		int data = cbTileEntity.getCbMetadata();
		if (blockAccess.getBlockState(blockPos.down()).getBlock() == this) {
			data |= GateData.GATE_BELOW;
		}
		if (blockAccess.getBlockState(blockPos.up()).getBlock() == this) {
			data |= GateData.GATE_ABOVE;
		}
		return data;
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos blockPos) {
		return world.getBlockState(blockPos.down()).getMaterial().isSolid() && super.canPlaceBlockAt(world, blockPos);
	}

	/**
	 * Faces across the player's view, and takes the type of an adjacent gate or barrier.
	 */
	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLivingBase, ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLivingBase, itemStack);
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (world.isRemote || cbTileEntity == null) {
			return;
		}
		int data = GateData.setOnZ(0, entityLivingBase.getHorizontalFacing().getAxis() == EnumFacing.Axis.X);
		for (EnumFacing facing : EnumFacing.VALUES) {
			BlockPos adjacentPos = blockPos.offset(facing);
			Block block = world.getBlockState(adjacentPos).getBlock();
			if (block == this || block == BlockRegistry.blockCarpentersBarrier) {
				CbTileEntity adjacent = getTileEntity(world, adjacentPos);
				if (adjacent != null) {
					data = GateData.setType(data, BarrierData.getType(adjacent.getCbMetadata()));
				}
			}
		}
		cbTileEntity.setCbMetadata(data);
	}

	/**
	 * Opens away from the player, or closes.
	 */
	@Override
	protected void postOnBlockActivated(CbTileEntity cbTileEntity, EntityPlayer entityPlayer, EnumFacing facing, float hitX, float hitY, float hitZ, ActionResult actionResult) {
		int data = cbTileEntity.getCbMetadata();
		if (GateData.isOpen(data)) {
			data = GateData.setOpen(data, false);
		} else {
			EnumFacing look = entityPlayer.getHorizontalFacing();
			boolean negative = GateData.isOnZ(data) ? look != EnumFacing.EAST : look != EnumFacing.SOUTH;
			data = GateData.setOpen(GateData.setOpensNegative(data, negative), true);
		}
		setGate(cbTileEntity, data, true);
		actionResult.setAltered().setNoSound();
	}

	/**
	 * Sets the gate, and the gate stacked on it (below first, else above) if it faces the same way.
	 */
	private void setGate(CbTileEntity cbTileEntity, int data, boolean playSound) {
		World world = cbTileEntity.getWorld();
		BlockPos blockPos = cbTileEntity.getPos();
		cbTileEntity.setCbMetadata(data);
		if (playSound) {
			world.playEvent(null, GateData.isOpen(data) ? SOUND_OPEN : SOUND_CLOSE, blockPos, 0);
		}
		BlockPos neighborPos = world.getBlockState(blockPos.down()).getBlock() == this ? blockPos.down() : blockPos.up();
		if (world.getBlockState(neighborPos).getBlock() == this) {
			CbTileEntity neighbor = getTileEntity(world, neighborPos);
			if (neighbor != null && GateData.isOnZ(neighbor.getCbMetadata()) == GateData.isOnZ(data)) {
				int neighborData = GateData.setOpen(GateData.setOpensNegative(neighbor.getCbMetadata(), GateData.opensNegative(data)), GateData.isOpen(data));
				neighbor.setCbMetadata(neighborData);
			}
		}
	}

	/**
	 * Redstone opens and closes the gate.
	 */
	@Override
	public void neighborChanged(IBlockState blockState, World world, BlockPos blockPos, Block neighborBlock, BlockPos neighborBlockPos) {
		super.neighborChanged(blockState, world, blockPos, neighborBlock, neighborBlockPos);
		if (world.isRemote) {
			return;
		}
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null) {
			return;
		}
		boolean powered = world.isBlockPowered(blockPos);
		if (powered || neighborBlock.getDefaultState().canProvidePower()) {
			int data = cbTileEntity.getCbMetadata();
			if (powered != GateData.isOpen(data)) {
				setGate(cbTileEntity, GateData.setOpen(data, powered), true);
			}
		}
	}

	/**
	 * Hammer right click: next style, or the next vanilla variant when sneaking.
	 */
	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		int data = cbTileEntity.getCbMetadata();
		int type = GateData.getType(data);
		if (entityPlayer.isSneaking()) {
			if (type > BarrierData.TYPE_VANILLA_X3) {
				return true;
			}
			type = (type + 1) % (BarrierData.TYPE_VANILLA_X3 + 1);
		} else {
			type = type <= BarrierData.TYPE_VANILLA_X3 ? BarrierData.TYPE_PICKET : (type + 1) % (BarrierData.TYPE_WALL + 1);
		}
		return cbTileEntity.setCbMetadata(GateData.setType(data, type));
	}

	@Override
	public boolean rotateBlock(World world, BlockPos blockPos, EnumFacing axis) {
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null || axis.getAxis() != EnumFacing.Axis.Y) {
			return false;
		}
		int data = cbTileEntity.getCbMetadata();
		return cbTileEntity.setCbMetadata(GateData.setOnZ(data, !GateData.isOnZ(data)));
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

	@Override
	public boolean isPassable(IBlockAccess blockAccess, BlockPos blockPos) {
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		return cbTileEntity != null && GateData.isOpen(cbTileEntity.getCbMetadata());
	}

}
