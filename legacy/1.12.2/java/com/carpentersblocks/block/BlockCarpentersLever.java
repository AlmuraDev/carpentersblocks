package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carpentersblocks.block.data.RedstoneData;
import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.handler.ChatHandler;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Carpenter's Lever, ported from 1.7.10: toggles, any face, invertible polarity.
 */
public class BlockCarpentersLever extends BlockSidedShaped {

	private static final double HANDLE_TILT = 2.0D * Math.PI / 9.0D;

	public BlockCarpentersLever(Material material) {
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

	/**
	 * Floor and ceiling levers point across the player's view.
	 */
	@Override
	protected int onSidedBlockPlaced(int data, EntityLivingBase placer) {
		EnumFacing side = RedstoneData.getSide(data);
		boolean axisZ = side.getAxis().isVertical()
			? placer.getHorizontalFacing().getAxis() == EnumFacing.Axis.Z
			: side.getAxis() == EnumFacing.Axis.Z;
		return RedstoneData.setAxisZ(data, axisZ);
	}

	private static AxisAlignedBB getBase(int data) {
		EnumFacing side = RedstoneData.getSide(data);
		if (side.getAxis().isVertical()) {
			double y0 = side == EnumFacing.UP ? 0 : 0.8125;
			double y1 = y0 + 0.1875;
			return RedstoneData.isAxisZ(data)
				? new AxisAlignedBB(0.3125, y0, 0.2, 0.6875, y1, 0.8)
				: new AxisAlignedBB(0.2, y0, 0.3125, 0.8, y1, 0.6875);
		}
		return SidedUtil.rotate(new AxisAlignedBB(0.3125, 0.2, 0, 0.6875, 0.8, 0.1875), side);
	}

	/**
	 * Gets the tilted handle (1.7.10 BlockHandlerCarpentersLever).
	 */
	private static Vec3d[] getHandle(int data) {
		EnumFacing side = RedstoneData.getSide(data);
		boolean on = RedstoneData.isOn(data);
		Vec3d[] corners = SidedUtil.centeredCuboid(0.0625, 0, 0.625);
		corners = SidedUtil.transform(corners, vec -> new Vec3d(vec.x, vec.y, vec.z + (on ? -0.0625 : 0.0625)).rotatePitch((float) (on ? HANDLE_TILT : -HANDLE_TILT)));
		if (side.getAxis().isVertical()) {
			boolean axisX = !RedstoneData.isAxisZ(data);
			double yOffset = side == EnumFacing.UP ? 0.125 : 0.875;
			return SidedUtil.transform(corners, vec -> {
				if (side == EnumFacing.DOWN) {
					vec = new Vec3d(-vec.x, -vec.y, vec.z);
				}
				if (axisX) {
					vec = vec.rotateYaw((float) (Math.PI / 2));
				}
				return vec.add(0.5, yOffset, 0.5);
			});
		}
		float yaw = side == EnumFacing.NORTH ? 0 : side == EnumFacing.SOUTH ? (float) Math.PI : side == EnumFacing.WEST ? (float) (Math.PI / 2) : (float) (-Math.PI / 2);
		return SidedUtil.transform(corners, vec -> vec.add(0, -0.375, 0).rotatePitch((float) (Math.PI / 2)).rotateYaw(yaw).add(0.5, 0.5, 0.5));
	}

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		return Collections.singletonList(getBase(renderData));
	}

	@Override
	public List<ShapePart> getRenderParts(int renderData) {
		List<ShapePart> parts = new ArrayList<ShapePart>(2);
		parts.add(ShapePart.covered(getBase(renderData)));
		parts.add(ShapePart.cuboid(getHandle(renderData), ShapeTextures.LEVER, false));
		return parts;
	}

	@Override
	public List<AxisAlignedBB> getCollisionBoxes(int renderData) {
		return Collections.emptyList();
	}

	@Override
	public int getInventoryRenderData(int itemDamage) {
		return RedstoneData.setSide(0, EnumFacing.UP);
	}

	/**
	 * Toggles the lever.
	 */
	@Override
	protected void postOnBlockActivated(CbTileEntity cbTileEntity, EntityPlayer entityPlayer, EnumFacing facing, float hitX, float hitY, float hitZ, ActionResult actionResult) {
		World world = cbTileEntity.getWorld();
		BlockPos blockPos = cbTileEntity.getPos();
		int data = cbTileEntity.getCbMetadata();
		boolean on = !RedstoneData.isOn(data);
		data = RedstoneData.setOn(data, on);
		cbTileEntity.setCbMetadata(data);
		world.playSound(null, blockPos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, on ? 0.6F : 0.5F);
		notifyPowerChange(world, blockPos, data);
		actionResult.setAltered().setNoSound();
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
	public boolean isPassable(IBlockAccess blockAccess, BlockPos blockPos) {
		return true;
	}

}
