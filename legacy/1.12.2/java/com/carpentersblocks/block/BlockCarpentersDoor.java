package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.carpentersblocks.block.data.DoorData;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.handler.ChatHandler;
import com.carpentersblocks.util.registry.ItemRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Carpenter's Door, ported from 1.7.10 (BlockHinged, BlockCarpentersDoor): two blocks high,
 * placed with {@link com.carpentersblocks.item.ItemCarpentersDoor}, pairs into double doors.
 */
public class BlockCarpentersDoor extends BlockCoverable implements IShapedBlock {

	private static final int SOUND_OPEN_WOOD = 1006;
	private static final int SOUND_CLOSE_WOOD = 1012;
	private static final int SOUND_OPEN_IRON = 1005;
	private static final int SOUND_CLOSE_IRON = 1011;

	public BlockCarpentersDoor(Material material) {
		super(material);
	}

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		return Collections.singletonList(DoorData.getBounds(renderData));
	}

	@Override
	public List<ShapePart> getRenderParts(int renderData) {
		return DoorData.getParts(renderData);
	}

	////////////
	// Door pieces
	////////////

	private CbTileEntity getDoor(World world, BlockPos blockPos) {
		return world.getBlockState(blockPos).getBlock() == this ? getTileEntity(world, blockPos) : null;
	}

	/**
	 * Gets the door paired with this one into a double door: on the side away from the hinge,
	 * same piece and facing, opposite hinge.
	 */
	private CbTileEntity getPairedDoor(World world, BlockPos blockPos, int data) {
		EnumFacing facing = DoorData.getFacing(data);
		boolean right = DoorData.isHingeRight(data);
		BlockPos pairPos = blockPos.offset(right ? facing.rotateYCCW() : facing.rotateY());
		CbTileEntity pair = getDoor(world, pairPos);
		if (pair != null) {
			int pairData = pair.getCbMetadata();
			if (DoorData.isTop(pairData) == DoorData.isTop(data) && DoorData.getFacing(pairData) == facing && DoorData.isHingeRight(pairData) != right) {
				return pair;
			}
		}
		return null;
	}

	/**
	 * Gets this door piece, its other half, and both halves of a paired door.
	 */
	private List<CbTileEntity> getPieces(CbTileEntity cbTileEntity) {
		List<CbTileEntity> pieces = new ArrayList<CbTileEntity>();
		World world = cbTileEntity.getWorld();
		addHalves(world, cbTileEntity, pieces);
		CbTileEntity pair = getPairedDoor(world, cbTileEntity.getPos(), cbTileEntity.getCbMetadata());
		if (pair != null) {
			addHalves(world, pair, pieces);
		}
		return pieces;
	}

	private void addHalves(World world, CbTileEntity cbTileEntity, List<CbTileEntity> pieces) {
		pieces.add(cbTileEntity);
		boolean top = DoorData.isTop(cbTileEntity.getCbMetadata());
		CbTileEntity other = getDoor(world, top ? cbTileEntity.getPos().down() : cbTileEntity.getPos().up());
		if (other != null) {
			pieces.add(other);
		}
	}

	private CbTileEntity getOtherHalf(CbTileEntity cbTileEntity) {
		boolean top = DoorData.isTop(cbTileEntity.getCbMetadata());
		return getDoor(cbTileEntity.getWorld(), top ? cbTileEntity.getPos().down() : cbTileEntity.getPos().up());
	}

	/**
	 * Copies the shared door properties to the other half.
	 */
	private void updateOtherHalf(CbTileEntity cbTileEntity) {
		CbTileEntity other = getOtherHalf(cbTileEntity);
		if (other != null) {
			int data = cbTileEntity.getCbMetadata();
			other.setCbMetadata(DoorData.setTop(data, DoorData.isTop(other.getCbMetadata())));
		}
	}

	private void setOpen(CbTileEntity clicked, boolean open) {
		for (CbTileEntity piece : getPieces(clicked)) {
			piece.setCbMetadata(DoorData.setOpen(piece.getCbMetadata(), open));
		}
		boolean rigid = DoorData.isRigid(clicked.getCbMetadata());
		int sound = open ? (rigid ? SOUND_OPEN_IRON : SOUND_OPEN_WOOD) : (rigid ? SOUND_CLOSE_IRON : SOUND_CLOSE_WOOD);
		clicked.getWorld().playEvent(null, sound, clicked.getPos(), 0);
	}

	////////////
	// Interaction
	////////////

	@Override
	protected void postOnBlockActivated(CbTileEntity cbTileEntity, EntityPlayer entityPlayer, EnumFacing facing, float hitX, float hitY, float hitZ, ActionResult actionResult) {
		int data = cbTileEntity.getCbMetadata();
		if (!DoorData.isRigid(data)) {
			setOpen(cbTileEntity, !DoorData.isOpen(data));
			actionResult.setAltered().setNoSound();
		}
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
		int data = cbTileEntity.getCbMetadata();
		// Pieces without their other half go away; the bottom drops the door without a floor
		if (DoorData.isTop(data)) {
			if (getDoor(world, blockPos.down()) == null) {
				world.setBlockToAir(blockPos);
				return;
			}
		} else {
			if (getDoor(world, blockPos.up()) == null) {
				world.setBlockToAir(blockPos);
				return;
			}
			if (!world.isSideSolid(blockPos.down(), EnumFacing.UP)) {
				world.setBlockToAir(blockPos.up());
				destroyBlock(world, blockPos, true);
				return;
			}
		}
		// Redstone opens and closes wooden and rigid doors alike
		if (neighborBlock.getDefaultState().canProvidePower()) {
			boolean powered = false;
			for (CbTileEntity piece : getPieces(cbTileEntity)) {
				powered |= world.isBlockPowered(piece.getPos());
			}
			if (powered != DoorData.isOpen(data)) {
				setOpen(cbTileEntity, powered);
			}
		}
	}

	/**
	 * Hammer left click: moves the hinge to the other side.
	 */
	@Override
	protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		int data = cbTileEntity.getCbMetadata();
		cbTileEntity.setCbMetadata(DoorData.setHingeRight(data, !DoorData.isHingeRight(data)));
		updateOtherHalf(cbTileEntity);
		return true;
	}

	/**
	 * Hammer right click: next type, or toggles redstone only when sneaking.
	 */
	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		int data = cbTileEntity.getCbMetadata();
		if (entityPlayer.isSneaking()) {
			boolean rigid = !DoorData.isRigid(data);
			for (CbTileEntity piece : getPieces(cbTileEntity)) {
				piece.setCbMetadata(DoorData.setRigid(piece.getCbMetadata(), rigid));
			}
			ChatHandler.sendMessageToPlayer(rigid ? "message.activation_iron.name" : "message.activation_wood.name", entityPlayer);
			return true;
		}
		cbTileEntity.setCbMetadata(DoorData.setType(data, (DoorData.getType(data) + 1) % DoorData.NUM_TYPES));
		updateOtherHalf(cbTileEntity);
		return true;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos blockPos, EnumFacing axis) {
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null || axis.getAxis() != EnumFacing.Axis.Y) {
			return false;
		}
		int data = cbTileEntity.getCbMetadata();
		EnumFacing facing = DoorData.getFacing(data);
		cbTileEntity.setCbMetadata(DoorData.setFacing(data, axis == EnumFacing.UP ? facing.rotateY() : facing.rotateYCCW()));
		updateOtherHalf(cbTileEntity);
		return true;
	}

	////////////
	// Drops
	////////////

	@Override
	public Item getItemDropped(IBlockState blockState, Random random, int fortune) {
		return ItemRegistry.itemCarpentersDoor != null ? ItemRegistry.itemCarpentersDoor : Items.AIR;
	}

	@Override
	public ItemStack getPickBlock(IBlockState blockState, RayTraceResult target, World world, BlockPos blockPos, EntityPlayer player) {
		return new ItemStack(getItemDropped(blockState, world.rand, 0));
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
		return cbTileEntity != null && DoorData.isOpen(cbTileEntity.getCbMetadata());
	}

}
