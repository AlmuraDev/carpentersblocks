package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.attribute.AttributeItemStack;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.registry.ItemRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Carpenter's Bed, ported from 1.7.10: two blocks, placed with {@link com.carpentersblocks.item.ItemCarpentersBed}.
 * <p>
 * Data bits 0-1: direction from head to foot (horizontal index). Bit 4: occupied. Bit 15: head.
 * Dye the foot to color the blanket.
 */
public class BlockCarpentersBed extends BlockCoverable implements IShapedBlock {

	private static final int DIR_MASK = 0x3;
	private static final int OCCUPIED_BIT = 0x10;
	private static final int HEAD_BIT = 0x8000;

	// Render data
	private static final int PARALLEL_NEG = 1 << 16;
	private static final int PARALLEL_POS = 1 << 17;
	private static final int BLANKET_COLOR_SHIFT = 18; // 5 bits: dye color + 1, 0 for white
	private static final int INVENTORY = 1 << 23;

	public BlockCarpentersBed(Material material) {
		super(material);
	}

	/**
	 * Gets the direction from the head to the foot (toward whoever placed it).
	 */
	public static EnumFacing getDirection(int data) {
		return EnumFacing.byHorizontalIndex(data & DIR_MASK);
	}

	public static int setDirection(int data, EnumFacing direction) {
		return (data & ~DIR_MASK) | direction.getHorizontalIndex();
	}

	public static boolean isHead(int data) {
		return (data & HEAD_BIT) != 0;
	}

	public static int setHead(int data, boolean head) {
		return head ? data | HEAD_BIT : data & ~HEAD_BIT;
	}

	private static boolean isOccupied(int data) {
		return (data & OCCUPIED_BIT) != 0;
	}

	private BlockPos getOtherHalfPos(BlockPos blockPos, int data) {
		EnumFacing direction = getDirection(data);
		return isHead(data) ? blockPos.offset(direction) : blockPos.offset(direction.getOpposite());
	}

	@Nullable
	private CbTileEntity getOtherHalf(IBlockAccess blockAccess, BlockPos blockPos, int data) {
		BlockPos otherPos = getOtherHalfPos(blockPos, data);
		if (blockAccess.getBlockState(otherPos).getBlock() != this) {
			return null;
		}
		CbTileEntity other = getTileEntity(blockAccess, otherPos);
		return other != null && isHead(other.getCbMetadata()) != isHead(data) ? other : null;
	}

	////////////
	// Shape
	////////////

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		return Collections.singletonList(new AxisAlignedBB(0, 0, 0, 1, 0.625, 1));
	}

	@Override
	public int getInventoryRenderData(int itemDamage) {
		return INVENTORY | HEAD_BIT;
	}

	@Override
	public int getRenderData(IBlockAccess blockAccess, BlockPos blockPos, CbTileEntity cbTileEntity) {
		int data = cbTileEntity.getCbMetadata();
		EnumFacing direction = getDirection(data);
		if (isParallelBed(blockAccess, blockPos.offset(direction.rotateY()), data)) {
			data |= PARALLEL_NEG;
		}
		if (isParallelBed(blockAccess, blockPos.offset(direction.rotateYCCW()), data)) {
			data |= PARALLEL_POS;
		}
		CbTileEntity other = getOtherHalf(blockAccess, blockPos, cbTileEntity.getCbMetadata());
		CbTileEntity foot = isHead(data) ? other : cbTileEntity;
		if (other != null && isOccupied(other.getCbMetadata())) {
			data |= OCCUPIED_BIT;
		}
		EnumDyeColor color = foot != null ? getDyeColor(foot) : null;
		if (color != null) {
			data |= (color.getMetadata() + 1) << BLANKET_COLOR_SHIFT;
		}
		return data;
	}

	private boolean isParallelBed(IBlockAccess blockAccess, BlockPos blockPos, int data) {
		if (blockAccess.getBlockState(blockPos).getBlock() != this) {
			return false;
		}
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		if (cbTileEntity == null) {
			return false;
		}
		int otherData = cbTileEntity.getCbMetadata();
		return isHead(otherData) == isHead(data) && getDirection(otherData) == getDirection(data);
	}

	@Nullable
	private static EnumDyeColor getDyeColor(CbTileEntity cbTileEntity) {
		if (!cbTileEntity.getAttributeHelper().hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.DYE)) {
			return null;
		}
		ItemStack dye = ((AttributeItemStack) cbTileEntity.getAttributeHelper().getAttribute(EnumAttributeLocation.HOST, EnumAttributeType.DYE)).getModel();
		return dye.getItem() == Items.DYE ? EnumDyeColor.byDyeDamage(dye.getMetadata()) : null;
	}

	@Override
	public List<ShapePart> getRenderParts(int renderData) {
		boolean head = isHead(renderData);
		boolean inventory = (renderData & INVENTORY) != 0;
		boolean parallelNeg = (renderData & PARALLEL_NEG) != 0;
		boolean parallelPos = (renderData & PARALLEL_POS) != 0;
		EnumFacing direction = inventory ? EnumFacing.SOUTH : getDirection(renderData);
		int colorIdx = (renderData >> BLANKET_COLOR_SHIFT) & 0x1f;
		ResourceLocation blanket = colorIdx == 0 ? ShapeTextures.WOOL_WHITE
			: new ResourceLocation("minecraft", "blocks/wool_colored_" + EnumDyeColor.byMetadata(colorIdx - 1).getName());
		double yTop = isOccupied(renderData) ? 0.875 : 0.625;

		// Authored for a bed whose foot is to the south
		List<ShapePart> parts = new ArrayList<ShapePart>();
		List<AxisAlignedBB> frame = new ArrayList<AxisAlignedBB>();
		List<AxisAlignedBB> blanketBoxes = new ArrayList<AxisAlignedBB>();
		double mattressX0 = parallelNeg ? 0 : 0.0625;
		double mattressX1 = parallelPos ? 1 : 0.9375;
		AxisAlignedBB mattress;
		if (head) {
			frame.add(new AxisAlignedBB(0.125, 0.1875, 0, 0.875, 0.875, 0.125));
			frame.add(new AxisAlignedBB(0, 0.1875, 0.125, 1, 0.3125, 1));
			frame.add(new AxisAlignedBB(0, parallelNeg ? 0.1875 : 0, 0, 0.125, parallelNeg ? 0.875 : 1, 0.125));
			frame.add(new AxisAlignedBB(0.875, parallelPos ? 0.1875 : 0, 0, 1, parallelPos ? 0.875 : 1, 0.125));
			blanketBoxes.add(new AxisAlignedBB(0.0625, yTop - 0.0625, 0.5, 0.9375, yTop, 1));
			blanketBoxes.add(new AxisAlignedBB(0, yTop - 0.3125, 0.5, 0.0625, yTop, 1));
			blanketBoxes.add(new AxisAlignedBB(0.9375, yTop - 0.3125, 0.5, 1, yTop, 1));
			mattress = new AxisAlignedBB(mattressX0, 0.3125, 0.125, mattressX1, 0.5625, 1);
			parts.add(ShapePart.textured(SidedUtil.rotate(new AxisAlignedBB(0.125, 0.5625, 0.1875, 0.875, 0.6875, 0.5625), direction), ShapeTextures.BED_PILLOW));
		} else {
			frame.add(new AxisAlignedBB(0, 0.1875, 0, 1, 0.3125, 1));
			if (!parallelNeg) {
				frame.add(new AxisAlignedBB(0, 0, 0.875, 0.125, 0.1875, 1));
			}
			if (!parallelPos) {
				frame.add(new AxisAlignedBB(0.875, 0, 0.875, 1, 0.1875, 1));
			}
			blanketBoxes.add(new AxisAlignedBB(0.0625, yTop - 0.0625, 0, 0.9375, yTop, 0.9375));
			blanketBoxes.add(new AxisAlignedBB(0, yTop - 0.3125, 0, 0.0625, yTop, 1));
			blanketBoxes.add(new AxisAlignedBB(0.9375, yTop - 0.3125, 0, 1, yTop, 1));
			blanketBoxes.add(new AxisAlignedBB(0.0625, yTop - 0.3125, 0.9375, 0.9375, yTop, 1));
			mattress = new AxisAlignedBB(mattressX0, 0.3125, 0, mattressX1, 0.5625, 0.9375);
		}
		for (AxisAlignedBB box : frame) {
			parts.add(ShapePart.covered(SidedUtil.rotate(box, direction)));
		}
		for (AxisAlignedBB box : blanketBoxes) {
			parts.add(ShapePart.textured(SidedUtil.rotate(box, direction), blanket));
		}
		parts.add(ShapePart.textured(SidedUtil.rotate(mattress, direction), ShapeTextures.WOOL_WHITE));
		return parts;
	}

	////////////
	// Sleeping
	////////////

	@Override
	public boolean isBed(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, @Nullable Entity player) {
		return true;
	}

	/**
	 * Direction from the foot to the head, like vanilla beds.
	 */
	@Override
	public EnumFacing getBedDirection(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos) {
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		return cbTileEntity != null ? getDirection(cbTileEntity.getCbMetadata()).getOpposite() : EnumFacing.NORTH;
	}

	@Override
	public boolean isBedFoot(IBlockAccess blockAccess, BlockPos blockPos) {
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		return cbTileEntity != null && !isHead(cbTileEntity.getCbMetadata());
	}

	@Override
	public void setBedOccupied(IBlockAccess blockAccess, BlockPos blockPos, EntityPlayer player, boolean occupied) {
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		if (cbTileEntity == null) {
			return;
		}
		setOccupied(cbTileEntity, occupied);
		CbTileEntity other = getOtherHalf(blockAccess, blockPos, cbTileEntity.getCbMetadata());
		if (other != null) {
			setOccupied(other, occupied);
		}
	}

	private static void setOccupied(CbTileEntity cbTileEntity, boolean occupied) {
		int data = cbTileEntity.getCbMetadata();
		cbTileEntity.setCbMetadata(occupied ? data | OCCUPIED_BIT : data & ~OCCUPIED_BIT);
	}

	/**
	 * Finds a safe spot next to the bed to wake up or respawn at (vanilla checks for its own facing property).
	 */
	@Override
	@Nullable
	public BlockPos getBedSpawnPosition(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, @Nullable EntityPlayer player) {
		if (!(blockAccess instanceof World)) {
			return null;
		}
		World world = (World) blockAccess;
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		BlockPos head = blockPos;
		BlockPos foot = blockPos;
		if (cbTileEntity != null) {
			int data = cbTileEntity.getCbMetadata();
			BlockPos other = getOtherHalfPos(blockPos, data);
			head = isHead(data) ? blockPos : other;
			foot = isHead(data) ? other : blockPos;
		}
		for (BlockPos center : new BlockPos[] { head, foot }) {
			for (int dx = -1; dx <= 1; ++dx) {
				for (int dz = -1; dz <= 1; ++dz) {
					for (int dy = 0; dy <= 1; ++dy) {
						BlockPos pos = center.add(dx, dy, dz);
						if (world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP)
							&& !world.getBlockState(pos).getMaterial().isSolid()
							&& !world.getBlockState(pos.up()).getMaterial().isSolid()
							&& world.getBlockState(pos).getBlock() != this) {
							return pos;
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	protected void postOnBlockActivated(CbTileEntity cbTileEntity, EntityPlayer entityPlayer, EnumFacing facing, float hitX, float hitY, float hitZ, ActionResult actionResult) {
		World world = cbTileEntity.getWorld();
		int data = cbTileEntity.getCbMetadata();
		BlockPos headPos = isHead(data) ? cbTileEntity.getPos() : getOtherHalfPos(cbTileEntity.getPos(), data);
		actionResult.setAltered().setNoSound();
		if (!world.provider.canRespawnHere() || world.getBiome(headPos) == Biomes.HELL) {
			// Beds explode where you can't sleep, like vanilla
			world.setBlockToAir(cbTileEntity.getPos());
			world.setBlockToAir(getOtherHalfPos(cbTileEntity.getPos(), data));
			world.newExplosion(null, headPos.getX() + 0.5D, headPos.getY() + 0.5D, headPos.getZ() + 0.5D, 5.0F, true, true);
			return;
		}
		if (isOccupied(data)) {
			for (EntityPlayer player : world.playerEntities) {
				if (player.isPlayerSleeping() && headPos.equals(player.bedLocation)) {
					entityPlayer.sendStatusMessage(new TextComponentTranslation("tile.bed.occupied"), true);
					return;
				}
			}
			setBedOccupied(world, cbTileEntity.getPos(), entityPlayer, false);
		}
		EntityPlayer.SleepResult result = entityPlayer.trySleep(headPos);
		if (result == EntityPlayer.SleepResult.OK) {
			setBedOccupied(world, headPos, entityPlayer, true);
		} else if (result == EntityPlayer.SleepResult.NOT_POSSIBLE_NOW) {
			entityPlayer.sendStatusMessage(new TextComponentTranslation("tile.bed.noSleep"), true);
		} else if (result == EntityPlayer.SleepResult.NOT_SAFE) {
			entityPlayer.sendStatusMessage(new TextComponentTranslation("tile.bed.notSafe"), true);
		}
	}

	/**
	 * A half without its other half goes away, dropping only its attributes.
	 */
	@Override
	public void neighborChanged(IBlockState blockState, World world, BlockPos blockPos, Block neighborBlock, BlockPos neighborBlockPos) {
		super.neighborChanged(blockState, world, blockPos, neighborBlock, neighborBlockPos);
		if (world.isRemote || world.getBlockState(blockPos).getBlock() != this) {
			return;
		}
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity != null && getOtherHalf(world, blockPos, cbTileEntity.getCbMetadata()) == null) {
			destroyBlock(world, blockPos, false);
		}
	}

	@Override
	public Item getItemDropped(IBlockState blockState, Random random, int fortune) {
		return ItemRegistry.itemCarpentersBed != null ? ItemRegistry.itemCarpentersBed : Items.AIR;
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

}
