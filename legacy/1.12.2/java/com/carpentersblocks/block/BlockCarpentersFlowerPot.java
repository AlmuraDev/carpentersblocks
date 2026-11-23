package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.api.ICarpentersHammer;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.attribute.AttributeItemStack;
import com.carpentersblocks.util.attribute.AttributeString;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.block.FlowerPotUtil;
import com.carpentersblocks.util.handler.EventHandler;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.DesignHandler.DesignType;

import net.minecraft.block.BlockCactus;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Carpenter's Flower Pot, ported from 1.7.10: holds soil and a plant, bone meal tints the plant.
 * <p>
 * Soil, plant and fertilizer are block attributes. Render data bit 16: has a plant; bit 17: the plant is tall.
 */
public class BlockCarpentersFlowerPot extends BlockCoverable implements IShapedBlock {

	public static final int HAS_PLANT = 1 << 16;
	public static final int TALL_PLANT = 1 << 17;
	private static final int DESIGN_SHIFT = 18; // 8 bits: design number, 0 for none
	private static final int DESIGN_MASK = 0xFF;

	private static final AxisAlignedBB POT_BOUNDS = new AxisAlignedBB(0.3125, 0, 0.3125, 0.6875, 0.375, 0.6875);
	private static final AxisAlignedBB SOIL_BOX = new AxisAlignedBB(0.375, 0.0625, 0.375, 0.625, 0.25, 0.625);

	public BlockCarpentersFlowerPot(Material material) {
		super(material);
	}

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();
		boxes.add(new AxisAlignedBB(0.375, 0, 0.375, 0.625, 0.0625, 0.625));
		boxes.add(new AxisAlignedBB(0.375, 0, 0.3125, 0.625, 0.375, 0.375));
		boxes.add(new AxisAlignedBB(0.375, 0, 0.625, 0.625, 0.375, 0.6875));
		boxes.add(new AxisAlignedBB(0.3125, 0, 0.3125, 0.375, 0.375, 0.6875));
		boxes.add(new AxisAlignedBB(0.625, 0, 0.3125, 0.6875, 0.375, 0.6875));
		return boxes;
	}

	@Override
	public List<AxisAlignedBB> getCollisionBoxes(int renderData) {
		return Collections.singletonList((renderData & TALL_PLANT) != 0 ? POT_BOUNDS.setMaxY(0.99) : POT_BOUNDS);
	}

	@Override
	public List<AxisAlignedBB> getSelectionBoxes(int renderData) {
		double maxY = (renderData & TALL_PLANT) != 0 ? 0.99 : (renderData & HAS_PLANT) != 0 ? 0.75 : 0.375;
		return Collections.singletonList(POT_BOUNDS.setMaxY(maxY));
	}

	public static AxisAlignedBB getSoilBox() {
		return SOIL_BOX;
	}

	@Override
	public int getRenderData(IBlockAccess blockAccess, BlockPos blockPos, CbTileEntity cbTileEntity) {
		int data = cbTileEntity.getCbMetadata();
		ItemStack plant = getAttributeStack(cbTileEntity, EnumAttributeType.PLANT);
		if (!plant.isEmpty()) {
			data |= HAS_PLANT;
			IBlockState plantState = FlowerPotUtil.getPlantState(plant, blockAccess, blockPos);
			if (plantState != null && FlowerPotUtil.isTall(plantState)) {
				data |= TALL_PLANT;
			}
		}
		data |= DesignHandler.getDesignNumber(DesignType.FLOWERPOT, getDesign(cbTileEntity)) << DESIGN_SHIFT;
		return data;
	}

	////////////
	// Designs
	////////////

	@Nullable
	private static String getDesign(CbTileEntity cbTileEntity) {
		if (!cbTileEntity.getAttributeHelper().hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC)) {
			return null;
		}
		return ((AttributeString) cbTileEntity.getAttributeHelper().getAttribute(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC)).getModel();
	}

	/**
	 * Sets the pot design, pushing out the cover (a designed pot takes no cover), like 1.7.10.
	 */
	private static boolean setDesign(CbTileEntity cbTileEntity, @Nullable String design) {
		String current = getDesign(cbTileEntity);
		if (design == null ? current == null : design.equals(current)) {
			return false;
		}
		cbTileEntity.removeAttribute(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC);
		if (design != null) {
			cbTileEntity.addAttribute(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC, design);
			cbTileEntity.dropAttribute(EnumAttributeLocation.HOST, EnumAttributeType.COVER);
		}
		cbTileEntity.update(true);
		return true;
	}

	/**
	 * Hammer left click: previous design.
	 */
	@Override
	protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		return setDesign(cbTileEntity, DesignHandler.getPrev(DesignType.FLOWERPOT, getDesign(cbTileEntity)));
	}

	/**
	 * Hammer right click: next design, or none when sneaking.
	 */
	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		return setDesign(cbTileEntity, entityPlayer.isSneaking() ? null : DesignHandler.getNext(DesignType.FLOWERPOT, getDesign(cbTileEntity)));
	}

	/**
	 * A pot with a design takes no cover.
	 */
	@Override
	protected boolean canSupportCover(CbTileEntity cbTileEntity, EnumAttributeLocation location) {
		return EnumAttributeLocation.HOST.equals(location) && getDesign(cbTileEntity) == null;
	}

	@Override
	public List<ShapePart> getRenderParts(int renderData) {
		String design = DesignHandler.getDesign(DesignType.FLOWERPOT, (renderData >> DESIGN_SHIFT) & DESIGN_MASK);
		List<ShapePart> parts = new ArrayList<ShapePart>();
		for (AxisAlignedBB box : getShapeBoxes(renderData)) {
			parts.add(design == null ? ShapePart.covered(box) : ShapePart.textured(box, DesignHandler.getFlowerPotTexture(design)));
		}
		return parts;
	}

	/**
	 * Adds the plant's block state, worked out here with the chunk snapshot rather than by the model.
	 */
	@Override
	public IBlockState getExtendedState(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos) {
		IBlockState extendedState = super.getExtendedState(blockState, blockAccess, blockPos);
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		if (cbTileEntity == null || !(extendedState instanceof net.minecraftforge.common.property.IExtendedBlockState)) {
			return extendedState;
		}
		ItemStack plant = getAttributeStack(cbTileEntity, EnumAttributeType.PLANT);
		IBlockState plantState = plant.isEmpty() ? null : FlowerPotUtil.getPlantState(plant, blockAccess, blockPos);
		return ((net.minecraftforge.common.property.IExtendedBlockState) extendedState).withProperty(com.carpentersblocks.block.state.Property.PLANT, plantState);
	}

	private static ItemStack getAttributeStack(CbTileEntity cbTileEntity, EnumAttributeType type) {
		if (!cbTileEntity.getAttributeHelper().hasAttribute(EnumAttributeLocation.HOST, type)) {
			return ItemStack.EMPTY;
		}
		return ((AttributeItemStack) cbTileEntity.getAttributeHelper().getAttribute(EnumAttributeLocation.HOST, type)).getModel();
	}

	////////////
	// Placement
	////////////

	private boolean hasSupport(World world, BlockPos blockPos) {
		BlockPos below = blockPos.down();
		IBlockState belowState = world.getBlockState(below);
		return world.isSideSolid(below, EnumFacing.UP) || belowState.getBlock().canPlaceTorchOnTop(belowState, world, below);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos blockPos) {
		return super.canPlaceBlockAt(world, blockPos) && hasSupport(world, blockPos);
	}

	@Override
	public void onNeighborChange(IBlockAccess blockAccess, BlockPos blockPos, BlockPos neighborBlockPos) {
		super.onNeighborChange(blockAccess, blockPos, neighborBlockPos);
		if (blockAccess instanceof World && !((World) blockAccess).isRemote) {
			World world = (World) blockAccess;
			if (world.getBlockState(blockPos).getBlock() == this && !hasSupport(world, blockPos)) {
				destroyBlock(world, blockPos, true);
			}
		}
	}

	////////////
	// Planting
	////////////

	/**
	 * Adds soil, then a plant; bone meal tints the plant.
	 */
	@Override
	protected void preOnBlockActivated(CbTileEntity cbTileEntity, EntityPlayer entityPlayer, EnumFacing facing, float hitX, float hitY, float hitZ, ActionResult actionResult) {
		ItemStack itemStack = entityPlayer.getHeldItemMainhand();
		if (itemStack.isEmpty()) {
			return;
		}
		World world = cbTileEntity.getWorld();
		BlockPos blockPos = cbTileEntity.getPos();
		boolean hasSoil = !getAttributeStack(cbTileEntity, EnumAttributeType.SOIL).isEmpty();
		boolean hasPlant = !getAttributeStack(cbTileEntity, EnumAttributeType.PLANT).isEmpty();
		boolean soilArea = facing == EnumFacing.UP && hitX > 0.375F && hitX < 0.625F && hitZ > 0.375F && hitZ < 0.625F;

		// Bone meal
		if (hasPlant && itemStack.getItem() == Items.DYE && EnumDyeColor.byDyeDamage(itemStack.getMetadata()) == EnumDyeColor.WHITE) {
			if (getAttributeStack(cbTileEntity, EnumAttributeType.FERTILIZER).isEmpty()) {
				cbTileEntity.addAttribute(EnumAttributeLocation.HOST, EnumAttributeType.FERTILIZER, itemStack);
				world.playEvent(2005, blockPos, 0);
				actionResult.setAltered().decInventory().setNoSound();
			}
			return;
		}
		if (hasSoil) {
			if (!hasPlant && FlowerPotUtil.isPlant(itemStack) && FlowerPotUtil.getPlantState(itemStack, world, blockPos) != null) {
				cbTileEntity.addAttribute(EnumAttributeLocation.HOST, EnumAttributeType.PLANT, itemStack);
				world.playSound(null, blockPos, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
				actionResult.setAltered().decInventory().setNoSound();
			}
		} else if (FlowerPotUtil.isSoil(itemStack) && soilArea) {
			cbTileEntity.addAttribute(EnumAttributeLocation.HOST, EnumAttributeType.SOIL, itemStack);
			world.playSound(null, blockPos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
			actionResult.setAltered().decInventory().setNoSound();
		}
	}

	/**
	 * Sneak left click with a hammer takes out the fertilizer or plant (clicked high), or the soil.
	 */
	@Override
	protected void preOnBlockClicked(CbTileEntity cbTileEntity, EntityPlayer entityPlayer, ActionResult actionResult) {
		if (!entityPlayer.isSneaking() || !(entityPlayer.getHeldItemMainhand().getItem() instanceof ICarpentersHammer)) {
			return;
		}
		Vec3d hit = EventHandler.getEventHitVector();
		boolean high = hit == null || hit.y > 0.375;
		EnumAttributeType[] order = high ? new EnumAttributeType[] { EnumAttributeType.FERTILIZER, EnumAttributeType.PLANT } : new EnumAttributeType[] { EnumAttributeType.SOIL };
		for (EnumAttributeType type : order) {
			ItemStack stack = getAttributeStack(cbTileEntity, type).copy();
			if (!stack.isEmpty()) {
				cbTileEntity.removeAttribute(EnumAttributeLocation.HOST, type);
				cbTileEntity.update(true);
				spawnAsEntity(cbTileEntity.getWorld(), cbTileEntity.getPos(), stack);
				actionResult.setAltered();
				return;
			}
		}
	}

	/**
	 * Cacti still hurt.
	 */
	@Override
	public void onEntityCollision(World world, BlockPos blockPos, IBlockState blockState, Entity entity) {
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity == null) {
			return;
		}
		ItemStack plant = getAttributeStack(cbTileEntity, EnumAttributeType.PLANT);
		if (!plant.isEmpty()) {
			IBlockState plantState = FlowerPotUtil.getPlantState(plant, world, blockPos);
			if (plantState != null && plantState.getBlock() instanceof BlockCactus) {
				entity.attackEntityFrom(DamageSource.CACTUS, 1.0F);
			}
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
