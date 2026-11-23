package com.carpentersblocks.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.carpentersblocks.block.data.RedstoneData;
import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.handler.ChatHandler;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Carpenter's Button, ported from 1.7.10: pressed for one second, any face, invertible polarity.
 */
public class BlockCarpentersButton extends BlockSidedShaped {

	private static final int PRESS_TICKS = 20;

	public BlockCarpentersButton(Material material) {
		super(material);
	}

	@Override
	public EnumFacing getAttachSide(int data) {
		return RedstoneData.getSide(data);
	}

	@Override
	public int setAttachSide(int data, EnumFacing side) {
		return RedstoneData.setSide(data, side);
	}

	@Override
	protected int getPowerOutput(int data) {
		return RedstoneData.getPowerOutput(data);
	}

	@Override
	public boolean canProvidePower(IBlockState blockState) {
		return true;
	}

	private static AxisAlignedBB getBox(int data) {
		double depth = RedstoneData.isOn(data) ? 0.0625 : 0.125;
		return SidedUtil.rotate(new AxisAlignedBB(0.3125, 0.375, 0, 0.6875, 0.625, depth), RedstoneData.getSide(data));
	}

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		if (renderData == -1) {
			return Collections.singletonList(new AxisAlignedBB(0.3125, 0.375, 0.375, 0.6875, 0.625, 0.625));
		}
		return Collections.singletonList(getBox(renderData));
	}

	@Override
	public List<AxisAlignedBB> getCollisionBoxes(int renderData) {
		return Collections.emptyList();
	}

	@Override
	public int getInventoryRenderData(int itemDamage) {
		return -1;
	}

	/**
	 * Presses the button.
	 */
	@Override
	protected void postOnBlockActivated(CbTileEntity cbTileEntity, EntityPlayer entityPlayer, EnumFacing facing, float hitX, float hitY, float hitZ, ActionResult actionResult) {
		int data = cbTileEntity.getCbMetadata();
		if (RedstoneData.isOn(data)) {
			return;
		}
		World world = cbTileEntity.getWorld();
		BlockPos blockPos = cbTileEntity.getPos();
		data = RedstoneData.setOn(data, true);
		cbTileEntity.setCbMetadata(data);
		playClick(world, blockPos, true);
		notifyPowerChange(world, blockPos, data);
		world.scheduleUpdate(blockPos, this, PRESS_TICKS);
		actionResult.setAltered().setNoSound();
	}

	/**
	 * Releases the button.
	 */
	@Override
	public void updateTick(World world, BlockPos blockPos, IBlockState blockState, Random random) {
		if (world.isRemote) {
			return;
		}
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null || !RedstoneData.isOn(cbTileEntity.getCbMetadata())) {
			return;
		}
		int data = RedstoneData.setOn(cbTileEntity.getCbMetadata(), false);
		cbTileEntity.setCbMetadata(data);
		playClick(world, blockPos, false);
		notifyPowerChange(world, blockPos, data);
	}

	private void playClick(World world, BlockPos blockPos, boolean on) {
		world.playSound(null, blockPos, on ? SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON : SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, on ? 0.6F : 0.5F);
	}

	/**
	 * Hammer left click: inverts the redstone output.
	 */
	@Override
	protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		int data = RedstoneData.togglePolarity(cbTileEntity.getCbMetadata());
		cbTileEntity.setCbMetadata(data);
		notifyPowerChange(cbTileEntity.getWorld(), cbTileEntity.getPos(), data);
		ChatHandler.sendMessageToPlayer(RedstoneData.isInverted(data) ? "message.polarity_neg.name" : "message.polarity_pos.name", entityPlayer);
		return true;
	}

	@Override
	public boolean isPassable(net.minecraft.world.IBlockAccess blockAccess, BlockPos blockPos) {
		return true;
	}

}
