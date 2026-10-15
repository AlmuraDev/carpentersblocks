package com.carpentersblocks.block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.handler.ChatHandler;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Carpenter's Garage Door, ported from 1.7.10 (data/GarageDoor, BlockCarpentersGarageDoor).
 * <p>
 * One item fills a column from the placed block down to the floor. When open, only the top
 * (host) piece shows, rolled up to half height.
 * <p>
 * Data bits 0-3: type. Bits 4-6: facing. Bit 7: open. Bit 8: rigid (redstone only). Bit 9: host (top piece).
 */
public class BlockCarpentersGarageDoor extends BlockCoverable implements IShapedBlock {

	public static final int TYPE_DEFAULT = 0;
	public static final int TYPE_GLASS_TOP = 1;
	public static final int TYPE_GLASS = 2;
	public static final int TYPE_SIDING = 3;
	public static final int TYPE_HIDDEN = 4;
	private static final int NUM_TYPES = 5;

	private static final int TYPE_MASK = 0xf;
	private static final int FACING_MASK = 0x70;
	private static final int FACING_SHIFT = 4;
	private static final int OPEN_BIT = 0x80;
	private static final int RIGID_BIT = 0x100;
	private static final int HOST_BIT = 0x200;

	/** Render data: no garage door below (the bottom piece). */
	private static final int BOTTOM = 1 << 16;
	private static final int INVENTORY = 1 << 17;

	private static final int MAX_PIECES = 500;
	private static final int SOUND_OPEN = 1006;
	private static final int SOUND_CLOSE = 1012;

	public BlockCarpentersGarageDoor(Material material) {
		super(material);
	}

	private static int getType(int data) {
		int type = data & TYPE_MASK;
		return type < NUM_TYPES ? type : TYPE_DEFAULT;
	}

	private static EnumFacing getFacing(int data) {
		int facing = (data & FACING_MASK) >> FACING_SHIFT;
		return facing >= 2 && facing <= 5 ? EnumFacing.byIndex(facing) : EnumFacing.SOUTH;
	}

	private static int setFacing(int data, EnumFacing facing) {
		return (data & ~FACING_MASK) | (facing.getIndex() << FACING_SHIFT);
	}

	private static boolean isOpen(int data) {
		return (data & OPEN_BIT) != 0;
	}

	private static boolean isRigid(int data) {
		return (data & RIGID_BIT) != 0;
	}

	private static boolean isHost(int data) {
		return (data & HOST_BIT) != 0;
	}

	/**
	 * Open pieces below the host are rolled up into it.
	 */
	private static boolean isVisible(int data) {
		return !isOpen(data) || isHost(data);
	}

	////////////
	// Shape
	////////////

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		if ((renderData & INVENTORY) == 0 && !isVisible(renderData)) {
			return Collections.emptyList();
		}
		if ((renderData & INVENTORY) != 0) {
			return getInventoryBoxes();
		}
		double yMin = isHost(renderData) && isOpen(renderData) ? 0.5 : 0;
		AxisAlignedBB box = getType(renderData) == TYPE_HIDDEN ? new AxisAlignedBB(0, yMin, 0, 1, 1, 0.125) : new AxisAlignedBB(0, yMin, 0.125, 1, 1, 0.25);
		return Collections.singletonList(SidedUtil.rotate(box, getFacing(renderData)));
	}

	private static List<AxisAlignedBB> getInventoryBoxes() {
		List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
		boxes.add(new AxisAlignedBB(0.4375, 0.75, 0, 0.5625, 1, 1));
		boxes.add(new AxisAlignedBB(0.4375, 0.625, 0, 0.5625, 0.75, 0.3125));
		boxes.add(new AxisAlignedBB(0.4375, 0.625, 0.6875, 0.5625, 0.75, 1));
		boxes.add(new AxisAlignedBB(0.4375, 0, 0, 0.5625, 0.625, 1));
		return boxes;
	}

	@Override
	public int getInventoryRenderData(int itemDamage) {
		return INVENTORY;
	}

	@Override
	public int getRenderData(IBlockAccess blockAccess, BlockPos blockPos, CbTileEntity cbTileEntity) {
		int data = cbTileEntity.getCbMetadata();
		if (blockAccess.getBlockState(blockPos.down()).getBlock() != this) {
			data |= BOTTOM;
		}
		return data;
	}

	@Override
	public List<ShapePart> getRenderParts(int renderData) {
		List<ShapePart> parts = new ArrayList<ShapePart>();
		if ((renderData & INVENTORY) != 0) {
			for (AxisAlignedBB box : getInventoryBoxes()) {
				parts.add(ShapePart.covered(box));
			}
			return parts;
		}
		if (!isVisible(renderData)) {
			return parts;
		}
		boolean open = isOpen(renderData);
		boolean host = isHost(renderData);
		boolean bottom = (renderData & BOTTOM) != 0;
		EnumFacing facing = getFacing(renderData);
		List<AxisAlignedBB> covered = new ArrayList<AxisAlignedBB>();
		List<AxisAlignedBB> iron = new ArrayList<AxisAlignedBB>();
		AxisAlignedBB pane = null;
		ResourceLocation paneTexture = null;
		switch (getType(renderData)) {
			case TYPE_HIDDEN:
				covered.add(new AxisAlignedBB(0, open ? 0.5 : 0, 0, 1, 1, 0.125));
				break;
			case TYPE_SIDING:
				if (open) {
					iron.add(new AxisAlignedBB(0, 0.5, 0.1875, 1, 1, 0.25));
					covered.add(new AxisAlignedBB(0, 0.5, 0.125, 1, 0.9375, 0.1875));
				} else {
					iron.add(new AxisAlignedBB(0, 0, 0.1875, 1, 1, 0.25));
					covered.add(new AxisAlignedBB(0, 0, 0.125, 1, 0.4375, 0.1875));
					covered.add(new AxisAlignedBB(0, 0.5, 0.125, 1, 0.9375, 0.1875));
				}
				break;
			case TYPE_DEFAULT:
				if (open) {
					covered.add(new AxisAlignedBB(0, 0.5, 0.125, 1, 1, 0.25));
				} else {
					covered.add(new AxisAlignedBB(0, 0, 0.125, 0.3125, 1, 0.25));
					covered.add(new AxisAlignedBB(0.3125, 0.75, 0.125, 0.6875, 1, 0.25));
					covered.add(new AxisAlignedBB(0.3125, 0, 0.125, 0.6875, 0.625, 0.25));
					covered.add(new AxisAlignedBB(0.6875, 0, 0.125, 1, 1, 0.25));
				}
				break;
			default: // GLASS_TOP, GLASS
				if (open) {
					addOpenPanels(covered);
				} else {
					boolean glassTop = getType(renderData) == TYPE_GLASS_TOP ? host : bottom;
					if (getType(renderData) == TYPE_GLASS && !bottom) {
						covered.add(new AxisAlignedBB(0, 0, 0.125, 0.125, 1, 0.25));
						covered.add(new AxisAlignedBB(0.125, 0.875, 0.125, 0.875, 1, 0.25));
						covered.add(new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.125, 0.25));
						covered.add(new AxisAlignedBB(0.875, 0, 0.125, 1, 1, 0.25));
						pane = new AxisAlignedBB(0.125, 0.125, 0.1875, 0.875, 0.875, 0.203125);
						paneTexture = ShapeTextures.GARAGE_GLASS;
					} else {
						addPanels(covered, glassTop);
						if (glassTop) {
							pane = new AxisAlignedBB(0.125, 0.5625, 0.1875, 0.875, 0.875, 0.203125);
							paneTexture = ShapeTextures.GARAGE_GLASS_TOP;
						}
					}
				}
				break;
		}
		for (AxisAlignedBB box : covered) {
			parts.add(ShapePart.covered(SidedUtil.rotate(box, facing)));
		}
		for (AxisAlignedBB box : iron) {
			parts.add(ShapePart.textured(SidedUtil.rotate(box, facing), ShapeTextures.IRON_BLOCK));
		}
		if (pane != null) {
			parts.add(ShapePart.translucent(SidedUtil.rotate(pane, facing), paneTexture));
		}
		return parts;
	}

	private static void addPanels(List<AxisAlignedBB> boxes, boolean glassTop) {
		boxes.add(new AxisAlignedBB(0, 0, 0.125, 0.125, 1, 0.25));
		boxes.add(new AxisAlignedBB(0.125, 0.875, 0.125, 0.875, 1, 0.25));
		boxes.add(new AxisAlignedBB(0.125, 0.4375, 0.125, 0.875, 0.5625, 0.25));
		boxes.add(new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.125, 0.25));
		boxes.add(new AxisAlignedBB(0.875, 0, 0.125, 1, 1, 0.25));
		boxes.add(glassTop ? new AxisAlignedBB(0.125, 0.125, 0.1875, 0.875, 0.4375, 0.25) : new AxisAlignedBB(0, 0, 0.1875, 1, 1, 0.25));
	}

	private static void addOpenPanels(List<AxisAlignedBB> boxes) {
		boxes.add(new AxisAlignedBB(0, 0.5, 0.1875, 1, 1, 0.25));
		boxes.add(new AxisAlignedBB(0, 0.5, 0.125, 0.125, 1, 0.1875));
		boxes.add(new AxisAlignedBB(0.125, 0.9375, 0.125, 0.875, 1, 0.1875));
		boxes.add(new AxisAlignedBB(0.125, 0.5, 0.125, 0.875, 0.625, 0.1875));
		boxes.add(new AxisAlignedBB(0.875, 0.5, 0.125, 1, 1, 0.1875));
	}

	////////////
	// Linked pieces
	////////////

	private CbTileEntity getPiece(World world, BlockPos blockPos) {
		return world.getBlockState(blockPos).getBlock() == this ? getTileEntity(world, blockPos) : null;
	}

	/**
	 * Gets every piece of the door: connected up, down, and across, facing the same way.
	 */
	private List<CbTileEntity> getLinkedPieces(CbTileEntity start) {
		World world = start.getWorld();
		EnumFacing facing = getFacing(start.getCbMetadata());
		EnumFacing[] directions = { EnumFacing.UP, EnumFacing.DOWN, facing.rotateY(), facing.rotateYCCW() };
		List<CbTileEntity> pieces = new ArrayList<CbTileEntity>();
		Set<BlockPos> visited = new HashSet<BlockPos>();
		Deque<BlockPos> queue = new ArrayDeque<BlockPos>();
		queue.add(start.getPos());
		visited.add(start.getPos());
		while (!queue.isEmpty() && pieces.size() < MAX_PIECES) {
			BlockPos pos = queue.poll();
			CbTileEntity piece = getPiece(world, pos);
			if (piece == null || getFacing(piece.getCbMetadata()) != facing) {
				continue;
			}
			pieces.add(piece);
			for (EnumFacing direction : directions) {
				BlockPos next = pos.offset(direction);
				if (visited.add(next)) {
					queue.add(next);
				}
			}
		}
		return pieces;
	}

	private void setOpen(CbTileEntity clicked, boolean open) {
		for (CbTileEntity piece : getLinkedPieces(clicked)) {
			int data = piece.getCbMetadata();
			piece.setCbMetadata(open ? data | OPEN_BIT : data & ~OPEN_BIT);
		}
		clicked.getWorld().playEvent(null, open ? SOUND_OPEN : SOUND_CLOSE, clicked.getPos(), 0);
	}

	////////////
	// Placement
	////////////

	private boolean hasSupportAbove(World world, BlockPos blockPos) {
		return world.isSideSolid(blockPos.up(), EnumFacing.DOWN) || world.getBlockState(blockPos.up()).getBlock() == this;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos blockPos, EnumFacing side) {
		return world.isSideSolid(blockPos.up(), EnumFacing.DOWN) && super.canPlaceBlockAt(world, blockPos);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos blockPos) {
		return world.getBlockState(blockPos).getBlock().isReplaceable(world, blockPos) && hasSupportAbove(world, blockPos);
	}

	/**
	 * The placed piece becomes the host; matches a door alongside and fills down to the floor.
	 */
	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLivingBase, ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLivingBase, itemStack);
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (world.isRemote || cbTileEntity == null) {
			return;
		}
		EnumFacing facing = entityLivingBase.getHorizontalFacing();
		int data = setFacing(HOST_BIT, facing);
		for (EnumFacing side : new EnumFacing[] { facing.rotateY(), facing.rotateYCCW() }) {
			CbTileEntity reference = getPiece(world, blockPos.offset(side));
			if (reference != null && getFacing(reference.getCbMetadata()) == facing) {
				data = (reference.getCbMetadata() & ~HOST_BIT) | HOST_BIT;
				break;
			}
		}
		cbTileEntity.setCbMetadata(data);
		fillDown(cbTileEntity);
	}

	/**
	 * Adds pieces below until something is in the way.
	 */
	private void fillDown(CbTileEntity host) {
		World world = host.getWorld();
		int pieceData = host.getCbMetadata() & ~HOST_BIT;
		BlockPos pos = host.getPos().down();
		for (int count = 0; count < MAX_PIECES && pos.getY() > 0 && canPlaceBlockAt(world, pos); ++count) {
			world.setBlockState(pos, getDefaultState(), 3);
			CbTileEntity piece = getTileEntity(world, pos);
			if (piece != null) {
				piece.copyOwner(host);
				piece.setCbMetadata(pieceData);
			}
			pos = pos.down();
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
		// The host needs something to hang from
		if (isHost(data) && !hasSupportAbove(world, blockPos)) {
			destroyColumn(world, cbTileEntity, true);
			return;
		}
		// Fill a gap opened up below
		if (neighborBlock != this && world.isAirBlock(blockPos.down())) {
			CbTileEntity host = findHost(cbTileEntity);
			if (host != null) {
				fillDown(host);
			}
		}
		// Redstone
		if (neighborBlock.getDefaultState().canProvidePower()) {
			boolean powered = world.isBlockPowered(blockPos);
			if (powered && !isOpen(data)) {
				setOpen(cbTileEntity, true);
			} else if (!powered && isOpen(data)) {
				boolean anyPowered = false;
				for (CbTileEntity piece : getLinkedPieces(cbTileEntity)) {
					anyPowered |= world.isBlockPowered(piece.getPos());
				}
				if (!anyPowered) {
					setOpen(cbTileEntity, false);
				}
			}
		}
	}

	private CbTileEntity findHost(CbTileEntity piece) {
		World world = piece.getWorld();
		BlockPos pos = piece.getPos();
		for (int count = 0; count < MAX_PIECES; ++count) {
			CbTileEntity current = getPiece(world, pos);
			if (current == null) {
				return null;
			}
			if (isHost(current.getCbMetadata())) {
				return current;
			}
			pos = pos.up();
		}
		return null;
	}

	////////////
	// Interaction
	////////////

	@Override
	protected void postOnBlockActivated(CbTileEntity cbTileEntity, EntityPlayer entityPlayer, EnumFacing facing, float hitX, float hitY, float hitZ, ActionResult actionResult) {
		int data = cbTileEntity.getCbMetadata();
		if (!isRigid(data)) {
			setOpen(cbTileEntity, !isOpen(data));
			actionResult.setAltered().setNoSound();
		}
	}

	private void setTypeAll(CbTileEntity cbTileEntity, int type) {
		for (CbTileEntity piece : getLinkedPieces(cbTileEntity)) {
			piece.setCbMetadata((piece.getCbMetadata() & ~TYPE_MASK) | type);
		}
	}

	/**
	 * Hammer left click: next type.
	 */
	@Override
	protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		setTypeAll(cbTileEntity, (getType(cbTileEntity.getCbMetadata()) + 1) % NUM_TYPES);
		return true;
	}

	/**
	 * Hammer right click: previous type, or toggles redstone only when sneaking.
	 */
	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		if (entityPlayer.isSneaking()) {
			boolean rigid = !isRigid(cbTileEntity.getCbMetadata());
			for (CbTileEntity piece : getLinkedPieces(cbTileEntity)) {
				int data = piece.getCbMetadata();
				piece.setCbMetadata(rigid ? data | RIGID_BIT : data & ~RIGID_BIT);
			}
			ChatHandler.sendMessageToPlayer(rigid ? "message.activation_iron.name" : "message.activation_wood.name", entityPlayer);
			return true;
		}
		setTypeAll(cbTileEntity, (getType(cbTileEntity.getCbMetadata()) + NUM_TYPES - 1) % NUM_TYPES);
		return true;
	}

	////////////
	// Breaking
	////////////

	/**
	 * Breaking any piece takes down its column; one item drops, plus every attribute.
	 */
	@Override
	public boolean removedByPlayer(IBlockState blockState, World world, BlockPos blockPos, EntityPlayer entityPlayer, boolean willHarvest) {
		if (world.isRemote) {
			return super.removedByPlayer(blockState, world, blockPos, entityPlayer, willHarvest);
		}
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null || suppressDestroyBlock(entityPlayer)) {
			return super.removedByPlayer(blockState, world, blockPos, entityPlayer, willHarvest);
		}
		destroyColumn(world, cbTileEntity, !entityPlayer.capabilities.isCreativeMode);
		return true;
	}

	/**
	 * Only the host drops the door item (explosions break every piece).
	 */
	@Override
	public List<ItemStack> getDrops(IBlockAccess blockAccess, BlockPos blockPos, IBlockState blockState, int fortune) {
		List<ItemStack> drops = new ArrayList<ItemStack>(super.getDrops(blockAccess, blockPos, blockState, fortune));
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		if (cbTileEntity != null && !isHost(cbTileEntity.getCbMetadata())) {
			drops.removeIf(itemStack -> Block.getBlockFromItem(itemStack.getItem()) == this);
		}
		return drops;
	}

	private void destroyColumn(World world, CbTileEntity piece, boolean dropItem) {
		List<BlockPos> column = new ArrayList<BlockPos>();
		BlockPos pos = piece.getPos();
		while (world.getBlockState(pos).getBlock() == this && column.size() < MAX_PIECES) {
			column.add(pos);
			pos = pos.up();
		}
		pos = piece.getPos().down();
		while (world.getBlockState(pos).getBlock() == this && column.size() < MAX_PIECES) {
			column.add(pos);
			pos = pos.down();
		}
		BlockPos dropPos = piece.getPos();
		if (dropItem) {
			spawnAsEntity(world, dropPos, new ItemStack(this));
		}
		for (BlockPos columnPos : column) {
			CbTileEntity columnPiece = getTileEntity(world, columnPos);
			if (columnPiece != null) {
				for (ItemStack attribute : columnPiece.getAllDroppableAttributes()) {
					spawnAsEntity(world, dropPos, attribute);
				}
			}
		}
		for (BlockPos columnPos : column) {
			world.setBlockState(columnPos, net.minecraft.init.Blocks.AIR.getDefaultState(), 2);
		}
		for (BlockPos columnPos : column) {
			world.notifyNeighborsOfStateChange(columnPos, this, false);
		}
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
