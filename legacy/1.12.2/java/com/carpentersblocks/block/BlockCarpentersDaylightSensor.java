package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.handler.ChatHandler;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

/**
 * Carpenter's Daylight Sensor, ported from 1.7.10.
 * <p>
 * Data bits 0-3: light level. Bit 4: inverted polarity. Bits 5-6: sensitivity. Bits 7-9: attach side.
 */
public class BlockCarpentersDaylightSensor extends BlockSidedShaped {

	public static final int SENSITIVITY_SLEEP = 0;
	public static final int SENSITIVITY_MONSTERS = 1;
	public static final int SENSITIVITY_DYNAMIC = 2;

	private static final int LIGHT_MASK = 0xf;
	private static final int POLARITY_BIT = 0x10;
	private static final int SENSITIVITY_MASK = 0x60;
	private static final int SENSITIVITY_SHIFT = 5;
	private static final int SIDE_MASK = 0x380;
	private static final int SIDE_SHIFT = 7;

	private static final int UPDATE_TICKS = 20;

	public BlockCarpentersDaylightSensor(Material material) {
		super(material);
	}

	@Override
	public EnumFacing getAttachSide(int data) {
		int side = (data & SIDE_MASK) >> SIDE_SHIFT;
		return side >= 1 && side <= 5 ? EnumFacing.byIndex(side) : EnumFacing.UP;
	}

	@Override
	public int setAttachSide(int data, EnumFacing side) {
		return (data & ~SIDE_MASK) | (side.getIndex() << SIDE_SHIFT);
	}

	@Override
	protected boolean canAttachToSide(EnumFacing side) {
		return side != EnumFacing.DOWN;
	}

	private static int getLight(int data) {
		return data & LIGHT_MASK;
	}

	private static boolean isInverted(int data) {
		return (data & POLARITY_BIT) != 0;
	}

	private static int getSensitivity(int data) {
		int sensitivity = (data & SENSITIVITY_MASK) >> SENSITIVITY_SHIFT;
		return sensitivity <= SENSITIVITY_DYNAMIC ? sensitivity : SENSITIVITY_SLEEP;
	}

	@Override
	protected int getPowerOutput(int data) {
		int light = getLight(data);
		boolean inverted = isInverted(data);
		switch (getSensitivity(data)) {
			case SENSITIVITY_DYNAMIC:
				return inverted ? 15 - light : light;
			case SENSITIVITY_MONSTERS:
				return (light > 7) != inverted ? 15 : 0;
			default: // SLEEP
				return (light > 11) != inverted ? 15 : 0;
		}
	}

	@Override
	public boolean canProvidePower(IBlockState blockState) {
		return true;
	}

	////////////
	// Shape
	////////////

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		return Collections.singletonList(SidedUtil.rotate(new AxisAlignedBB(0, 0, 0, 1, 1, 0.25), getAttachSide(renderData)));
	}

	@Override
	public int getInventoryRenderData(int itemDamage) {
		return setAttachSide(0, EnumFacing.UP);
	}

	@Override
	public List<ShapePart> getRenderParts(int renderData) {
		EnumFacing side = getAttachSide(renderData);
		boolean active = getPowerOutput(renderData) > 0;
		List<ShapePart> parts = new ArrayList<ShapePart>();
		// Frame
		parts.add(ShapePart.covered(SidedUtil.rotate(new AxisAlignedBB(0.0625, 0.0625, 0, 0.9375, 0.9375, 0.0625), side)));
		parts.add(ShapePart.covered(SidedUtil.rotate(new AxisAlignedBB(0, 0, 0, 0.0625, 1, 0.25), side)));
		parts.add(ShapePart.covered(SidedUtil.rotate(new AxisAlignedBB(0.0625, 0, 0, 0.9375, 0.0625, 0.25), side)));
		parts.add(ShapePart.covered(SidedUtil.rotate(new AxisAlignedBB(0.0625, 0.9375, 0, 0.9375, 1, 0.25), side)));
		parts.add(ShapePart.covered(SidedUtil.rotate(new AxisAlignedBB(0.9375, 0, 0, 1, 1, 0.25), side)));
		// Lapis inlay, ringed with redstone that glows while the sensor outputs
		parts.add(ShapePart.textured(SidedUtil.rotate(new AxisAlignedBB(0.125, 0.125, 0.0625, 0.875, 0.875, 0.1875), side), ShapeTextures.LAPIS_BLOCK));
		AxisAlignedBB[] redstone = {
			new AxisAlignedBB(0.0625, 0.0625, 0.0625, 0.125, 0.9375, 0.1875),
			new AxisAlignedBB(0.125, 0.0625, 0.0625, 0.875, 0.125, 0.1875),
			new AxisAlignedBB(0.125, 0.875, 0.0625, 0.875, 0.9375, 0.1875),
			new AxisAlignedBB(0.875, 0.0625, 0.0625, 0.9375, 0.9375, 0.1875) };
		for (AxisAlignedBB box : redstone) {
			AxisAlignedBB rotated = SidedUtil.rotate(box, side);
			parts.add(active ? ShapePart.glowing(rotated, ShapeTextures.REDSTONE_BLOCK) : ShapePart.textured(rotated, ShapeTextures.REDSTONE_BLOCK));
		}
		// Glass top
		parts.add(ShapePart.translucent(SidedUtil.rotate(new AxisAlignedBB(0.0625, 0.0625, 0.234375, 0.9375, 0.9375, 0.25), side), ShapeTextures.DAYLIGHT_SENSOR_GLASS_TOP));
		return parts;
	}

	////////////
	// Sensing
	////////////

	@Override
	public void onBlockAdded(World world, BlockPos blockPos, IBlockState blockState) {
		super.onBlockAdded(world, blockPos, blockState);
		if (!world.isRemote) {
			world.scheduleUpdate(blockPos, this, UPDATE_TICKS);
		}
	}

	@Override
	public void updateTick(World world, BlockPos blockPos, IBlockState blockState, Random random) {
		if (world.isRemote) {
			return;
		}
		world.scheduleUpdate(blockPos, this, UPDATE_TICKS);
		if (!world.provider.hasSkyLight()) {
			return;
		}
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null) {
			return;
		}
		int data = cbTileEntity.getCbMetadata();
		int light = MathHelper.clamp(computeLight(world, blockPos, data), 0, 15);
		if (light != getLight(data)) {
			data = (data & ~LIGHT_MASK) | light;
			cbTileEntity.setCbMetadata(data);
			notifyPowerChange(world, blockPos, data);
		}
	}

	private int computeLight(World world, BlockPos blockPos, int data) {
		int skyLight = world.getLightFor(EnumSkyBlock.SKY, blockPos) - world.getSkylightSubtracted();
		switch (getSensitivity(data)) {
			case SENSITIVITY_DYNAMIC:
				float angle = world.getCelestialAngleRadians(1.0F);
				if (angle > 1.67F && angle < 4.62F) {
					return 0;
				}
				switch (getAttachSide(data)) {
					case NORTH:
					case SOUTH:
						return (int) (skyLight * 0.6F);
					case EAST:
						return celestialLight(skyLight, angle + (float) (Math.PI / 2));
					case WEST:
						return celestialLight(skyLight, angle - (float) (Math.PI / 2));
					default:
						return celestialLight(skyLight, angle);
				}
			case SENSITIVITY_MONSTERS:
				return world.isThundering() ? 7 : skyLight;
			default:
				return skyLight;
		}
	}

	/**
	 * Light falling on the sensor for a sun angle (vanilla daylight sensor formula).
	 */
	private static int celestialLight(int skyLight, float angle) {
		float pi2 = (float) (Math.PI * 2);
		angle += angle < (float) Math.PI ? (0.0F - angle) * 0.2F : (pi2 - angle) * 0.2F;
		return MathHelper.clamp(Math.round(skyLight * MathHelper.cos(angle)), 0, 15);
	}

	/**
	 * Hammer left click: inverts the redstone output.
	 */
	@Override
	protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		int data = cbTileEntity.getCbMetadata() ^ POLARITY_BIT;
		cbTileEntity.setCbMetadata(data);
		notifyPowerChange(cbTileEntity.getWorld(), cbTileEntity.getPos(), data);
		ChatHandler.sendMessageToPlayer(isInverted(data) ? "message.polarity_neg.name" : "message.polarity_pos.name", entityPlayer);
		return true;
	}

	/**
	 * Hammer right click: next sensitivity.
	 */
	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		int data = cbTileEntity.getCbMetadata();
		int sensitivity = (getSensitivity(data) + 1) % (SENSITIVITY_DYNAMIC + 1);
		data = (data & ~SENSITIVITY_MASK) | (sensitivity << SENSITIVITY_SHIFT);
		cbTileEntity.setCbMetadata(data);
		notifyPowerChange(cbTileEntity.getWorld(), cbTileEntity.getPos(), data);
		String[] messages = { "message.sensitivity_sleep.name", "message.sensitivity_monsters.name", "message.sensitivity_dynamic.name" };
		ChatHandler.sendMessageToPlayer(messages[sensitivity], entityPlayer);
		return true;
	}

}
