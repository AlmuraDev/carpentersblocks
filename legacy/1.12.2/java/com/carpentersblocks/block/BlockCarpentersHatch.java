package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.handler.ChatHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Carpenter's Hatch, ported from 1.7.10 (data/Hatch, BlockHandlerCarpentersHatch).
 * <p>
 * Data bits 0-2: type. Bit 3: hangs high. Bit 4: open. Bits 5-6: side of the supporting block
 * (0 south, 1 north, 2 east, 3 west). Bit 7: rigid (redstone only).
 */
public class BlockCarpentersHatch extends BlockCoverable implements IShapedBlock {

	public static final int TYPE_HIDDEN = 0;
	public static final int TYPE_WINDOW = 1;
	public static final int TYPE_SCREEN = 2;
	public static final int TYPE_FRENCH_WINDOW = 3;
	public static final int TYPE_PANEL = 4;
	private static final int NUM_TYPES = 5;

	private static final int TYPE_MASK = 0x7;
	private static final int HIGH_BIT = 0x8;
	private static final int OPEN_BIT = 0x10;
	private static final int DIR_MASK = 0x60;
	private static final int DIR_SHIFT = 5;
	private static final int RIGID_BIT = 0x80;

	private static final int SOUND_OPEN = 1007;
	private static final int SOUND_CLOSE = 1013;

	// Data picked in getStateForPlacement, applied once the tile entity exists
	private static final ThreadLocal<Integer> _threadLocalPlacement = new ThreadLocal<Integer>();

	public BlockCarpentersHatch(Material material) {
		super(material);
	}

	private static int getType(int data) {
		int type = data & TYPE_MASK;
		return type < NUM_TYPES ? type : TYPE_HIDDEN;
	}

	private static boolean isHigh(int data) {
		return (data & HIGH_BIT) != 0;
	}

	private static boolean isOpen(int data) {
		return (data & OPEN_BIT) != 0;
	}

	private static int setOpen(int data, boolean open) {
		return open ? data | OPEN_BIT : data & ~OPEN_BIT;
	}

	private static int getDir(int data) {
		return (data & DIR_MASK) >> DIR_SHIFT;
	}

	private static int setDir(int data, int dir) {
		return (data & ~DIR_MASK) | (dir << DIR_SHIFT);
	}

	private static boolean isRigid(int data) {
		return (data & RIGID_BIT) != 0;
	}

	/**
	 * Gets the direction from the hatch to its supporting block.
	 */
	private static EnumFacing getSupportSide(int dir) {
		switch (dir) {
			case 0:
				return EnumFacing.SOUTH;
			case 1:
				return EnumFacing.NORTH;
			case 2:
				return EnumFacing.EAST;
			default:
				return EnumFacing.WEST;
		}
	}

	////////////
	// Shape
	////////////

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		return Collections.singletonList(getBounds(renderData));
	}

	private static AxisAlignedBB getBounds(int data) {
		double t = getType(data) == TYPE_HIDDEN ? 0.125 : 0.1875;
		if (!isOpen(data)) {
			return isHigh(data) ? new AxisAlignedBB(0, 1 - t, 0, 1, 1, 1) : new AxisAlignedBB(0, 0, 0, 1, t, 1);
		}
		return SidedUtil.rotate(new AxisAlignedBB(0, 0, 1 - t, 1, 1, 1), getSupportSide(getDir(data)));
	}

	@Override
	public int getInventoryRenderData(int itemDamage) {
		return -1;
	}

	@Override
	public List<ShapePart> getRenderParts(int renderData) {
		List<ShapePart> parts = new ArrayList<ShapePart>();
		if (renderData == -1) {
			parts.add(ShapePart.covered(new AxisAlignedBB(0, 0.4375, 0, 1, 0.5625, 1)));
			parts.add(ShapePart.textured(new AxisAlignedBB(0.0625, 0.5625, 0.375, 0.125, 0.625, 0.4375), ShapeTextures.IRON_BLOCK));
			parts.add(ShapePart.textured(new AxisAlignedBB(0.0625, 0.5625, 0.5625, 0.125, 0.625, 0.625), ShapeTextures.IRON_BLOCK));
			parts.add(ShapePart.textured(new AxisAlignedBB(0.0625, 0.625, 0.375, 0.125, 0.6875, 0.625), ShapeTextures.IRON_BLOCK));
			return parts;
		}
		int type = getType(renderData);
		boolean open = isOpen(renderData);
		boolean high = isHigh(renderData);
		EnumFacing baseDir = getSupportSide(getDir(renderData));
		EnumFacing side = open ? baseDir : high ? EnumFacing.UP : EnumFacing.DOWN;

		// Panel, authored at z 0.8125-1
		List<AxisAlignedBB> covered = new ArrayList<AxisAlignedBB>();
		ResourceLocation pane = null;
		if (type == TYPE_HIDDEN) {
			covered.add(new AxisAlignedBB(0, 0, 0.875, 1, 1, 1));
		} else {
			covered.add(new AxisAlignedBB(0, 0, 0.8125, 0.1875, 1, 1));
			covered.add(new AxisAlignedBB(0.1875, 0.8125, 0.8125, 0.8125, 1, 1));
			covered.add(new AxisAlignedBB(0.1875, 0, 0.8125, 0.8125, 0.1875, 1));
			covered.add(new AxisAlignedBB(0.8125, 0, 0.8125, 1, 1, 1));
			switch (type) {
				case TYPE_WINDOW:
					pane = ShapeTextures.HATCH_GLASS;
					break;
				case TYPE_SCREEN:
					pane = ShapeTextures.HATCH_SCREEN;
					break;
				case TYPE_FRENCH_WINDOW:
					covered.add(new AxisAlignedBB(0.1875, 0.4375, 0.875, 0.8125, 0.5625, 0.9375));
					covered.add(new AxisAlignedBB(0.4375, 0.1875, 0.875, 0.5625, 0.4375, 0.9375));
					covered.add(new AxisAlignedBB(0.4375, 0.5625, 0.875, 0.5625, 0.8125, 0.9375));
					pane = ShapeTextures.HATCH_FRENCH_GLASS;
					break;
				default: // PANEL
					covered.add(new AxisAlignedBB(0.1875, 0.1875, 0.875, 0.8125, 0.8125, 0.9375));
					covered.add(new AxisAlignedBB(0.3125, 0.3125, 0.8125, 0.6875, 0.6875, 1));
					break;
			}
		}
		for (AxisAlignedBB box : covered) {
			parts.add(ShapePart.covered(SidedUtil.rotate(box, side)));
		}
		if (pane != null) {
			parts.add(ShapePart.translucent(SidedUtil.rotate(new AxisAlignedBB(0.1875, 0.1875, 0.90625, 0.8125, 0.8125, 0.921875), side), pane));
		}

		// Handle on the face that is inside this block space: the outer face when open, the inner face when closed
		double zo = type == TYPE_HIDDEN ? 0.125 : 0.1875;
		double yMin = high ? 0.0625 : 0.875;
		double yMax = yMin + 0.0625;
		AxisAlignedBB[] handle = {
			new AxisAlignedBB(0.375, yMin, 0.9375 - zo, 0.4375, yMax, 1 - zo),
			new AxisAlignedBB(0.5625, yMin, 0.9375 - zo, 0.625, yMax, 1 - zo),
			new AxisAlignedBB(0.375, yMin, 0.875 - zo, 0.625, yMax, 0.9375 - zo) };
		if (open && type != TYPE_HIDDEN) {
			for (AxisAlignedBB box : handle) {
				parts.add(ShapePart.textured(SidedUtil.rotate(box, baseDir), ShapeTextures.IRON_BLOCK));
			}
		} else if (!open) {
			// Closed hatches are seen from both sides (the room and through the opening), so the
			// handle is mirrored onto the far face too. Open ones rest their far face on the support.
			EnumFacing turn = high ? baseDir : baseDir.getOpposite();
			double mirror = 2 - zo; // twice the panel's middle depth
			for (AxisAlignedBB box : handle) {
				AxisAlignedBB far = new AxisAlignedBB(box.minX, box.minY, mirror - box.maxZ, box.maxX, box.maxY, mirror - box.minZ);
				parts.add(ShapePart.textured(SidedUtil.rotate(SidedUtil.rotate(box, side), turn), ShapeTextures.IRON_BLOCK));
				parts.add(ShapePart.textured(SidedUtil.rotate(SidedUtil.rotate(far, side), turn), ShapeTextures.IRON_BLOCK));
			}
		}
		return parts;
	}

	////////////
	// Placement and support
	////////////

	/**
	 * Hatches hang off the side of a block: solid faces, slabs, stairs, Carpenter's Blocks and glowstone.
	 */
	private boolean isValidSupport(IBlockAccess blockAccess, BlockPos hatchPos, int dir) {
		EnumFacing toSupport = getSupportSide(dir);
		BlockPos supportPos = hatchPos.offset(toSupport);
		IBlockState supportState = blockAccess.getBlockState(supportPos);
		Block block = supportState.getBlock();
		return block == Blocks.GLOWSTONE || block instanceof BlockSlab || block instanceof BlockStairs
			|| block instanceof BlockCarpentersStairs || block instanceof BlockCarpentersBlock
			|| blockAccess.isSideSolid(supportPos, toSupport.getOpposite(), false);
	}

	private static int dirForClickedSide(EnumFacing side) {
		return side.getIndex() - 2;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos blockPos, EnumFacing side) {
		return side.getAxis().isHorizontal() && isValidSupport(world, blockPos, dirForClickedSide(side));
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos blockPos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		int data = 0;
		if (facing.getAxis().isHorizontal()) {
			data = setDir(data, dirForClickedSide(facing));
		}
		if (hitY > 0.5F) {
			data |= HIGH_BIT;
		}
		_threadLocalPlacement.set(data);
		return super.getStateForPlacement(world, blockPos, facing, hitX, hitY, hitZ, meta, placer, hand);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLivingBase, ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLivingBase, itemStack);
		Integer data = _threadLocalPlacement.get();
		_threadLocalPlacement.remove();
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (!world.isRemote && cbTileEntity != null && data != null) {
			cbTileEntity.setCbMetadata(data);
		}
	}

	/**
	 * Turns to the next side with a valid support.
	 *
	 * @return whether a supported side was found
	 */
	private boolean findNextSupport(CbTileEntity cbTileEntity) {
		int data = cbTileEntity.getCbMetadata();
		int dir = getDir(data);
		for (int count = 0; count < 4; ++count) {
			dir = (dir + 1) % 4;
			if (isValidSupport(cbTileEntity.getWorld(), cbTileEntity.getPos(), dir)) {
				cbTileEntity.setCbMetadata(setDir(data, dir));
				return true;
			}
		}
		return false;
	}

	@Override
	public void neighborChanged(IBlockState blockState, World world, BlockPos blockPos, Block neighborBlock, BlockPos neighborBlockPos) {
		super.neighborChanged(blockState, world, blockPos, neighborBlock, neighborBlockPos);
		if (world.isRemote || world.getBlockState(blockPos).getBlock() != this) {
			return;
		}
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null) {
			return;
		}
		if (!isValidSupport(world, blockPos, getDir(cbTileEntity.getCbMetadata())) && !findNextSupport(cbTileEntity)) {
			destroyBlock(world, blockPos, true);
			return;
		}
		// Redstone works on rigid hatches too
		boolean powered = world.isBlockPowered(blockPos);
		int data = cbTileEntity.getCbMetadata();
		if (neighborBlock.getDefaultState().canProvidePower() && powered != isOpen(data)) {
			setHatchOpen(cbTileEntity, powered);
		}
	}

	private void setHatchOpen(CbTileEntity cbTileEntity, boolean open) {
		cbTileEntity.setCbMetadata(setOpen(cbTileEntity.getCbMetadata(), open));
		cbTileEntity.getWorld().playEvent(null, open ? SOUND_OPEN : SOUND_CLOSE, cbTileEntity.getPos(), 0);
	}

	@Override
	protected void postOnBlockActivated(CbTileEntity cbTileEntity, EntityPlayer entityPlayer, EnumFacing facing, float hitX, float hitY, float hitZ, ActionResult actionResult) {
		int data = cbTileEntity.getCbMetadata();
		if (!isRigid(data)) {
			setHatchOpen(cbTileEntity, !isOpen(data));
			actionResult.setAltered().setNoSound();
		}
	}

	/**
	 * Hidden hatches hanging high act as ladders when open.
	 */
	@Override
	public boolean isLadder(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, EntityLivingBase entity) {
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		if (cbTileEntity == null) {
			return false;
		}
		int data = cbTileEntity.getCbMetadata();
		return getType(data) == TYPE_HIDDEN && isHigh(data) && isOpen(data);
	}

	/**
	 * Hammer left click: turns to the next supported side.
	 */
	@Override
	protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		return findNextSupport(cbTileEntity);
	}

	/**
	 * Hammer right click: next type, or toggles redstone only when sneaking.
	 */
	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		int data = cbTileEntity.getCbMetadata();
		if (entityPlayer.isSneaking()) {
			data ^= RIGID_BIT;
			ChatHandler.sendMessageToPlayer(isRigid(data) ? "message.activation_iron.name" : "message.activation_wood.name", entityPlayer);
		} else {
			data = (data & ~TYPE_MASK) | ((getType(data) + 1) % NUM_TYPES);
		}
		return cbTileEntity.setCbMetadata(data);
	}

	@Override
	public boolean rotateBlock(World world, BlockPos blockPos, EnumFacing axis) {
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null || axis.getAxis() != EnumFacing.Axis.Y) {
			return false;
		}
		int data = cbTileEntity.getCbMetadata();
		int[] up = { 3, 2, 0, 1 };
		int[] down = { 2, 3, 1, 0 };
		int dir = getDir(data);
		return cbTileEntity.setCbMetadata(setDir(data, axis == EnumFacing.UP ? up[dir] : down[dir]));
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
