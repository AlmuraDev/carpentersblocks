package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.registry.BlockRegistry;
import com.carpentersblocks.util.registry.ConfigRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Carpenter's Torch, ported from 1.7.10: vanilla style torch or lantern, put out by rain.
 * <p>
 * Data bits 0-2: attach side. Bits 3-4: state (lit, smoldering, unlit). Bits 5-8: type (torch, lantern).
 */
public class BlockCarpentersTorch extends BlockSidedShaped {

	public static final int STATE_LIT = 0;
	public static final int STATE_SMOLDERING = 1;
	public static final int STATE_UNLIT = 2;

	public static final int TYPE_VANILLA = 0;
	public static final int TYPE_LANTERN = 1;
	private static final int NUM_TYPES = 2;

	private static final int SIDE_MASK = 0x7;
	private static final int STATE_MASK = 0x18;
	private static final int STATE_SHIFT = 3;
	private static final int TYPE_MASK = 0x1e0;
	private static final int TYPE_SHIFT = 5;

	private static final int LIGHT_LIT = 14;
	private static final int LIGHT_SMOLDERING = 9;

	public BlockCarpentersTorch(Material material) {
		super(material);
		if (ConfigRegistry.enableTorchWeatherEffects) {
			setTickRandomly(true);
		}
	}

	public static int getState(int data) {
		int state = (data & STATE_MASK) >> STATE_SHIFT;
		return state <= STATE_UNLIT ? state : STATE_LIT;
	}

	public static int setState(int data, int state) {
		return (data & ~STATE_MASK) | (state << STATE_SHIFT);
	}

	public static int getType(int data) {
		int type = (data & TYPE_MASK) >> TYPE_SHIFT;
		return type < NUM_TYPES ? type : TYPE_VANILLA;
	}

	public static int setType(int data, int type) {
		return (data & ~TYPE_MASK) | (type << TYPE_SHIFT);
	}

	@Override
	public EnumFacing getAttachSide(int data) {
		int side = data & SIDE_MASK;
		return side >= 1 && side <= 5 ? EnumFacing.byIndex(side) : EnumFacing.UP;
	}

	@Override
	public int setAttachSide(int data, EnumFacing side) {
		return (data & ~SIDE_MASK) | side.getIndex();
	}

	/**
	 * Torches stand on the floor or hang on walls, never from ceilings.
	 */
	@Override
	protected boolean canAttachToSide(EnumFacing side) {
		return side != EnumFacing.DOWN;
	}

	////////////
	// Shape
	////////////

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		return Collections.singletonList(getBounds(renderData));
	}

	private AxisAlignedBB getBounds(int data) {
		EnumFacing side = getAttachSide(data);
		if (getType(data) == TYPE_LANTERN) {
			return side == EnumFacing.UP
				? new AxisAlignedBB(0.1875, 0, 0.1875, 0.8125, 0.9375, 0.8125)
				: SidedUtil.rotate(new AxisAlignedBB(0.1875, 0.25, 0, 0.8125, 0.9375, 0.8125), side);
		}
		return side == EnumFacing.UP
			? new AxisAlignedBB(0.4, 0, 0.4, 0.6, 0.6, 0.6)
			: SidedUtil.rotate(new AxisAlignedBB(0.35, 0.2, 0, 0.65, 0.8, 0.3), side);
	}

	@Override
	public List<AxisAlignedBB> getCollisionBoxes(int renderData) {
		return getType(renderData) == TYPE_LANTERN ? getShapeBoxes(renderData) : Collections.<AxisAlignedBB>emptyList();
	}

	@Override
	public int getInventoryRenderData(int itemDamage) {
		return setAttachSide(0, EnumFacing.UP);
	}

	@Override
	public List<ShapePart> getRenderParts(int renderData) {
		return getType(renderData) == TYPE_LANTERN ? getLanternParts(renderData) : getTorchParts(renderData);
	}

	private List<ShapePart> getTorchParts(int data) {
		EnumFacing side = getAttachSide(data);
		int state = getState(data);
		ResourceLocation head = state == STATE_LIT ? ShapeTextures.TORCH_HEAD_LIT : state == STATE_SMOLDERING ? ShapeTextures.TORCH_HEAD_SMOLDERING : ShapeTextures.TORCH_HEAD_UNLIT;
		List<ShapePart> parts = new ArrayList<ShapePart>(2);
		parts.add(ShapePart.cuboid(placeTorch(SidedUtil.centeredCuboid(0.0625, 0, 0.5), side), null, false));
		parts.add(ShapePart.cuboid(placeTorch(SidedUtil.centeredCuboid(0.0625, 0.5, 0.625), side), head, state == STATE_LIT));
		return parts;
	}

	/**
	 * Places the upright torch: standing on the floor, or leaning out from a wall (1.7.10 BlockHandlerCarpentersTorch).
	 */
	private static Vec3d[] placeTorch(Vec3d[] corners, EnumFacing side) {
		if (side == EnumFacing.UP) {
			return SidedUtil.transform(corners, vec -> vec.add(0.5, 0, 0.5));
		}
		float yaw = side == EnumFacing.NORTH ? 0 : side == EnumFacing.SOUTH ? (float) Math.PI : side == EnumFacing.WEST ? (float) (Math.PI / 2) : (float) (-Math.PI / 2);
		float tilt = (float) (-Math.PI * 3.4D / 9.0D);
		return SidedUtil.transform(corners, vec -> vec.add(0, 0, 0.0625).rotatePitch(tilt).add(0, -0.4375, 0)
			.rotatePitch((float) (Math.PI / 2)).rotateYaw(yaw).add(0.5, 0.1875, 0.5));
	}

	private List<ShapePart> getLanternParts(int data) {
		EnumFacing side = getAttachSide(data);
		List<ShapePart> parts = new ArrayList<ShapePart>();
		if (side == EnumFacing.UP) {
			parts.add(ShapePart.covered(new AxisAlignedBB(0.375, 0, 0.375, 0.625, 0.375, 0.625)));
		} else {
			parts.add(ShapePart.covered(SidedUtil.rotate(new AxisAlignedBB(0.4375, 0.375, 0, 0.5625, 0.75, 0.25), side)));
			parts.add(ShapePart.covered(SidedUtil.rotate(new AxisAlignedBB(0.375, 0.375, 0.25, 0.625, 0.75, 0.375), side)));
			parts.add(ShapePart.covered(new AxisAlignedBB(0.375, 0.25, 0.375, 0.625, 0.375, 0.625)));
		}
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			parts.add(ShapePart.covered(SidedUtil.rotate(new AxisAlignedBB(0.25, 0.25, 0.25, 0.375, 0.75, 0.375), facing)));
			parts.add(ShapePart.covered(SidedUtil.rotate(new AxisAlignedBB(0.25, 0.25, 0.375, 0.375, 0.375, 0.625), facing)));
			if (facing != side) {
				parts.add(ShapePart.translucent(SidedUtil.rotate(new AxisAlignedBB(0.375, 0.375, 0.3125, 0.625, 0.75, 0.328125), facing), ShapeTextures.LANTERN_GLASS));
			}
		}
		parts.add(ShapePart.covered(new AxisAlignedBB(0.1875, 0.75, 0.1875, 0.8125, 0.875, 0.8125)));
		parts.add(ShapePart.covered(new AxisAlignedBB(0.3125, 0.875, 0.3125, 0.6875, 0.9375, 0.6875)));
		parts.add(ShapePart.glowing(new AxisAlignedBB(0.4375, 0.375, 0.4375, 0.5625, 0.5, 0.5625), ShapeTextures.TORCH_HEAD_LIT));
		return parts;
	}

	////////////
	// Light and weather
	////////////

	@Override
	public int getLightValue(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos) {
		if (blockAccess.getBlockState(blockPos).getBlock() != this) {
			return super.getLightValue(blockState, blockAccess, blockPos);
		}
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		if (cbTileEntity == null) {
			return LIGHT_LIT;
		}
		int data = cbTileEntity.getCbMetadata();
		// Lanterns always burn
		if (getType(data) == TYPE_LANTERN || getState(data) == STATE_LIT) {
			return LIGHT_LIT;
		}
		int coverLight = super.getLightValue(blockState, blockAccess, blockPos);
		return getState(data) == STATE_SMOLDERING ? Math.max(LIGHT_SMOLDERING, coverLight) : coverLight;
	}

	/**
	 * Rain puts torches out one step at a time; they come back once it stops.
	 */
	@Override
	public void updateTick(World world, BlockPos blockPos, IBlockState blockState, Random random) {
		if (world.isRemote || !ConfigRegistry.enableTorchWeatherEffects) {
			return;
		}
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null) {
			return;
		}
		int data = cbTileEntity.getCbMetadata();
		if (getType(data) != TYPE_VANILLA) {
			return;
		}
		boolean wet = world.isRainingAt(blockPos.up()) || world.isRainingAt(blockPos);
		int state = getState(data);
		int newState = state;
		switch (state) {
			case STATE_LIT:
				newState = wet ? STATE_SMOLDERING : STATE_LIT;
				break;
			case STATE_SMOLDERING:
				newState = wet ? STATE_UNLIT : STATE_LIT;
				break;
			default:
				newState = wet ? STATE_UNLIT : STATE_SMOLDERING;
				break;
		}
		setTorchState(cbTileEntity, newState);
	}

	private void setTorchState(CbTileEntity cbTileEntity, int state) {
		int data = cbTileEntity.getCbMetadata();
		int oldState = getState(data);
		if (state == oldState) {
			return;
		}
		if (state > oldState) {
			World world = cbTileEntity.getWorld();
			world.playSound(null, cbTileEntity.getPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
		}
		cbTileEntity.setCbMetadata(setState(data, state));
	}

	/**
	 * Relights the torch when right clicked with a torch.
	 */
	@Override
	protected void preOnBlockActivated(CbTileEntity cbTileEntity, EntityPlayer entityPlayer, EnumFacing facing, float hitX, float hitY, float hitZ, ActionResult actionResult) {
		ItemStack itemStack = entityPlayer.getHeldItemMainhand();
		Block heldBlock = Block.getBlockFromItem(itemStack.getItem());
		if ((heldBlock == Blocks.TORCH || heldBlock == BlockRegistry.blockCarpentersTorch) && getState(cbTileEntity.getCbMetadata()) != STATE_LIT) {
			setTorchState(cbTileEntity, STATE_LIT);
			actionResult.setAltered();
		}
	}

	/**
	 * Hammer clicks switch between torch and lantern.
	 */
	@Override
	protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		int data = cbTileEntity.getCbMetadata();
		return cbTileEntity.setCbMetadata(setType(data, (getType(data) + 1) % NUM_TYPES));
	}

	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		return onHammerLeftClick(cbTileEntity, entityPlayer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState blockState, World world, BlockPos blockPos, Random random) {
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null) {
			return;
		}
		int data = cbTileEntity.getCbMetadata();
		int state = getType(data) == TYPE_LANTERN ? STATE_LIT : getState(data);
		if (state == STATE_UNLIT) {
			return;
		}
		Vec3d head = getHeadPosition(data);
		double x = blockPos.getX() + head.x, y = blockPos.getY() + head.y, z = blockPos.getZ() + head.z;
		world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0, 0, 0);
		if (state == STATE_LIT) {
			world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
		}
	}

	private Vec3d getHeadPosition(int data) {
		if (getType(data) == TYPE_LANTERN) {
			return new Vec3d(0.5, 0.5625, 0.5);
		}
		switch (getAttachSide(data)) {
			case NORTH:
				return new Vec3d(0.5, 0.92, 0.77);
			case SOUTH:
				return new Vec3d(0.5, 0.92, 0.23);
			case WEST:
				return new Vec3d(0.77, 0.92, 0.5);
			case EAST:
				return new Vec3d(0.23, 0.92, 0.5);
			default:
				return new Vec3d(0.5, 0.7, 0.5);
		}
	}

	@Override
	public boolean isPassable(IBlockAccess blockAccess, BlockPos blockPos) {
		return true;
	}

}
