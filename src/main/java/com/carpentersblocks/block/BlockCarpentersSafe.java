package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.tileentity.TECarpentersSafe;
import com.carpentersblocks.util.attribute.AttributeItemStack;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.handler.ChatHandler;
import com.carpentersblocks.util.protection.PlayerPermissions;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Carpenter's Safe, ported from 1.7.10: a lockable chest with a sliding door and status lights.
 * <p>
 * Data bits 0-1: facing (toward whoever placed it). Bit 2: open. Bit 3: unlocked. Bits 4-5: automation.
 * A new safe is locked, with automation off.
 */
public class BlockCarpentersSafe extends BlockCoverable implements IShapedBlock {

	public static final int AUTOMATION_DISABLED = 0;
	public static final int AUTOMATION_SEND = 1;
	public static final int AUTOMATION_RECEIVE = 2;
	public static final int AUTOMATION_ALL = 3;

	private static final int FACING_MASK = 0x3;
	private static final int OPEN_BIT = 0x4;
	private static final int UNLOCKED_BIT = 0x8;
	private static final int AUTOMATION_MASK = 0x30;
	private static final int AUTOMATION_SHIFT = 4;

	// Render data
	private static final int LIT_CELLS_SHIFT = 16; // 4 bits
	private static final int UPGRADE_SHIFT = 20;   // 2 bits: none, gold, diamond, emerald
	private static final int INVENTORY = 1 << 22;

	private static final String[] UPGRADE_ORES = { "ingotGold", "gemDiamond", "gemEmerald" };

	public BlockCarpentersSafe(Material material) {
		super(material);
	}

	public static EnumFacing getFacing(int data) {
		return EnumFacing.byHorizontalIndex(data & FACING_MASK);
	}

	private static int setFacing(int data, EnumFacing facing) {
		return (data & ~FACING_MASK) | facing.getHorizontalIndex();
	}

	private static boolean isOpen(int data) {
		return (data & OPEN_BIT) != 0;
	}

	private static boolean isUnlocked(int data) {
		return (data & UNLOCKED_BIT) != 0;
	}

	public static int getAutomation(int data) {
		return (data & AUTOMATION_MASK) >> AUTOMATION_SHIFT;
	}

	private static int setAutomation(int data, int automation) {
		return (data & ~AUTOMATION_MASK) | (automation << AUTOMATION_SHIFT);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState blockState) {
		return new TECarpentersSafe();
	}

	private TECarpentersSafe getSafe(IBlockAccess blockAccess, BlockPos blockPos) {
		TileEntity tileEntity = blockAccess.getTileEntity(blockPos);
		return tileEntity instanceof TECarpentersSafe ? (TECarpentersSafe) tileEntity : null;
	}

	/**
	 * Gets the upgrade (1 gold, 2 diamond, 3 emerald), or 0 for none.
	 */
	private static int getUpgradeIndex(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return 0;
		}
		for (int id : OreDictionary.getOreIDs(itemStack)) {
			String name = OreDictionary.getOreName(id);
			for (int idx = 0; idx < UPGRADE_ORES.length; ++idx) {
				if (UPGRADE_ORES[idx].equals(name)) {
					return idx + 1;
				}
			}
		}
		return 0;
	}

	////////////
	// Shape
	////////////

	@Override
	public List<AxisAlignedBB> getShapeBoxes(int renderData) {
		return Collections.singletonList(FULL_BLOCK_AABB);
	}

	@Override
	public int getInventoryRenderData(int itemDamage) {
		return INVENTORY | UNLOCKED_BIT;
	}

	@Override
	public int getRenderData(IBlockAccess blockAccess, BlockPos blockPos, CbTileEntity cbTileEntity) {
		int data = cbTileEntity.getCbMetadata();
		if (cbTileEntity instanceof TECarpentersSafe) {
			TECarpentersSafe safe = (TECarpentersSafe) cbTileEntity;
			// 9 lights, each a ninth of the slots
			int litCells = Math.min(9, safe.getFilledSlots() * 9 / safe.getSizeInventory());
			data |= litCells << LIT_CELLS_SHIFT;
			if (safe.hasUpgrade()) {
				ItemStack upgrade = ((AttributeItemStack) safe.getAttributeHelper().getAttribute(EnumAttributeLocation.HOST, EnumAttributeType.SAFE_UPGRADE)).getModel();
				data |= getUpgradeIndex(upgrade) << UPGRADE_SHIFT;
			}
		}
		return data;
	}

	@Override
	public List<ShapePart> getRenderParts(int renderData) {
		EnumFacing facing = (renderData & INVENTORY) != 0 ? EnumFacing.WEST : getFacing(renderData);
		boolean open = isOpen(renderData);
		boolean unlocked = isUnlocked(renderData);
		int litCells = (renderData >> LIT_CELLS_SHIFT) & 0xf;
		int upgrade = (renderData >> UPGRADE_SHIFT) & 0x3;
		ResourceLocation panel = upgrade == 1 ? ShapeTextures.GOLD_BLOCK : upgrade == 2 ? ShapeTextures.DIAMOND_BLOCK : upgrade == 3 ? ShapeTextures.EMERALD_BLOCK : ShapeTextures.IRON_BLOCK;

		// Authored with the front to the south
		List<ShapePart> parts = new ArrayList<ShapePart>();
		AxisAlignedBB[] shell = {
			new AxisAlignedBB(0, 0, 0, 0.0625, 1, 1),
			new AxisAlignedBB(0.9375, 0, 0, 1, 1, 1),
			new AxisAlignedBB(0.0625, 0, 0.0625, 0.3125, 0.0625, 1),
			new AxisAlignedBB(0.375, 0, 0.0625, 0.9375, 0.0625, 1),
			new AxisAlignedBB(0.0625, 0.9375, 0.0625, 0.3125, 1, 1),
			new AxisAlignedBB(0.375, 0.9375, 0.0625, 0.9375, 1, 1),
			new AxisAlignedBB(0.0625, 0, 0, 0.3125, 1, 0.0625),
			new AxisAlignedBB(0.375, 0, 0, 0.9375, 1, 0.0625),
			new AxisAlignedBB(0.3125, 0, 0, 0.375, 1, 1),
			new AxisAlignedBB(0.375, 0.625, 0.0625, 0.9375, 0.6875, 0.875),
			new AxisAlignedBB(0.375, 0.3125, 0.0625, 0.9375, 0.375, 0.875),
			// Sliding door
			new AxisAlignedBB(0.375, 0.0625, 0.875, open ? 0.5625 : 0.9375, 0.9375, 0.9375) };
		for (AxisAlignedBB box : shell) {
			parts.add(ShapePart.covered(SidedUtil.rotate(box, facing)));
		}
		AxisAlignedBB[] panelBoxes = {
			new AxisAlignedBB(0.125, 0.875, 0.9375, 0.25, 0.9375, 1),
			new AxisAlignedBB(0.0625, 0.0625, 0.9375, 0.125, 0.9375, 1),
			new AxisAlignedBB(0.25, 0.0625, 0.9375, 0.3125, 0.9375, 1),
			new AxisAlignedBB(0.125, 0.6875, 0.9375, 0.25, 0.75, 1),
			new AxisAlignedBB(0.125, 0.0625, 0.9375, 0.25, 0.125, 1),
			new AxisAlignedBB(0.0625, 0.0625, 0.875, 0.3125, 0.9375, 0.9375),
			// Handle
			open ? new AxisAlignedBB(0.4375, 0.375, 0.9375, 0.5, 0.625, 1) : new AxisAlignedBB(0.8125, 0.375, 0.9375, 0.875, 0.625, 1) };
		for (AxisAlignedBB box : panelBoxes) {
			parts.add(ShapePart.textured(SidedUtil.rotate(box, facing), panel));
		}
		// Lock lights and the fill gauge
		parts.add(light(new AxisAlignedBB(0.125, 0.8125, 0.9375, 0.25, 0.875, 1), facing, unlocked, 0x00FF00, 0x367E36));
		parts.add(light(new AxisAlignedBB(0.125, 0.75, 0.9375, 0.25, 0.8125, 1), facing, !unlocked, 0xFF0000, 0x7E3636));
		for (int cell = 0; cell < 9; ++cell) {
			double y0 = 0.125 + 0.0625 * cell;
			parts.add(light(new AxisAlignedBB(0.125, y0, 0.9375, 0.25, y0 + 0.0625, 1), facing, cell < litCells, 0x0000FF, 0x383884));
		}
		return parts;
	}

	private static ShapePart light(AxisAlignedBB box, EnumFacing facing, boolean on, int onColor, int offColor) {
		AxisAlignedBB rotated = SidedUtil.rotate(box, facing);
		return (on ? ShapePart.glowing(rotated, ShapeTextures.SAFE_LIGHT) : ShapePart.textured(rotated, ShapeTextures.SAFE_LIGHT)).withColor(on ? onColor : offColor);
	}

	////////////
	// Use
	////////////

	@Override
	public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLivingBase, ItemStack itemStack) {
		super.onBlockPlacedBy(world, blockPos, blockState, entityLivingBase, itemStack);
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (!world.isRemote && cbTileEntity != null) {
			cbTileEntity.setCbMetadata(setFacing(cbTileEntity.getCbMetadata(), entityLivingBase.getHorizontalFacing().getOpposite()));
		}
	}

	/**
	 * Locked safes open only for their owner (and operators).
	 */
	private boolean canOpen(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		return isUnlocked(cbTileEntity.getCbMetadata()) || PlayerPermissions.hasElevatedPermission(cbTileEntity, entityPlayer, true);
	}

	@Override
	protected void postOnBlockActivated(CbTileEntity cbTileEntity, EntityPlayer entityPlayer, EnumFacing facing, float hitX, float hitY, float hitZ, ActionResult actionResult) {
		actionResult.setAltered().setNoSound();
		if (!(cbTileEntity instanceof TECarpentersSafe)) {
			return;
		}
		TECarpentersSafe safe = (TECarpentersSafe) cbTileEntity;
		if (!canOpen(safe, entityPlayer)) {
			ChatHandler.sendMessageToPlayer("message.block_lock.name", entityPlayer);
			return;
		}
		// Sneak with gold, a diamond or an emerald to double the space
		ItemStack held = entityPlayer.getHeldItemMainhand();
		if (entityPlayer.isSneaking() && !safe.hasUpgrade() && getUpgradeIndex(held) > 0) {
			safe.addAttribute(EnumAttributeLocation.HOST, EnumAttributeType.SAFE_UPGRADE, held);
			actionResult.decInventory();
			return;
		}
		entityPlayer.displayGUIChest(safe);
	}

	/**
	 * Slides the door and plays the door sound.
	 */
	public void setOpen(TECarpentersSafe safe, boolean open) {
		World world = safe.getWorld();
		if (world == null || world.isRemote) {
			return;
		}
		int data = safe.getCbMetadata();
		safe.setCbMetadata(open ? data | OPEN_BIT : data & ~OPEN_BIT);
		world.playSound(null, safe.getPos(), open ? SoundEvents.BLOCK_WOODEN_DOOR_OPEN : SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
	}

	/**
	 * Hammer left click: turns the front toward the player.
	 */
	@Override
	protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		EnumFacing facing = entityPlayer.getHorizontalFacing().getOpposite();
		return cbTileEntity.setCbMetadata(setFacing(cbTileEntity.getCbMetadata(), facing));
	}

	/**
	 * Hammer right click: next automation mode, or toggles the lock when sneaking.
	 */
	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
		if (!PlayerPermissions.hasElevatedPermission(cbTileEntity, entityPlayer, true)) {
			return false;
		}
		int data = cbTileEntity.getCbMetadata();
		if (entityPlayer.isSneaking()) {
			if (isUnlocked(data)) {
				data = setAutomation(data & ~UNLOCKED_BIT, AUTOMATION_DISABLED);
				ChatHandler.sendMessageToPlayer("message.safe_lock.name", entityPlayer);
			} else {
				data = setAutomation(data | UNLOCKED_BIT, AUTOMATION_ALL);
				ChatHandler.sendMessageToPlayer("message.safe_unlock.name", entityPlayer);
			}
		} else {
			int automation = (getAutomation(data) + 1) % 4;
			data = setAutomation(data, automation);
			String[] messages = { "message.automation_disabled.name", "message.automation_extract.name", "message.automation_insert.name", "message.automation_all.name" };
			ChatHandler.sendMessageToPlayer(messages[automation], entityPlayer);
		}
		return cbTileEntity.setCbMetadata(data);
	}

	////////////
	// Protection
	////////////

	/**
	 * Can't be broken while open, or by players who don't own it.
	 */
	@Override
	public float getPlayerRelativeBlockHardness(IBlockState blockState, EntityPlayer entityPlayer, World world, BlockPos blockPos) {
		CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
		if (cbTileEntity != null && (isOpen(cbTileEntity.getCbMetadata()) || !PlayerPermissions.hasElevatedPermission(cbTileEntity, entityPlayer, true))) {
			return 0.0F;
		}
		return super.getPlayerRelativeBlockHardness(blockState, entityPlayer, world, blockPos);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos blockPos, Entity exploder, Explosion explosion) {
		return 6000000.0F;
	}

	@Override
	public void breakBlock(World world, BlockPos blockPos, IBlockState blockState) {
		TECarpentersSafe safe = getSafe(world, blockPos);
		if (safe != null) {
			InventoryHelper.dropInventoryItems(world, blockPos, safe);
		}
		super.breakBlock(world, blockPos, blockState);
	}

	@Override
	public boolean isSideSolid(IBlockState blockState, IBlockAccess blockAccess, BlockPos blockPos, EnumFacing facing) {
		CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
		return cbTileEntity != null && facing != getFacing(cbTileEntity.getCbMetadata()) && isBlockSolid(blockAccess, blockPos);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, EnumFacing facing) {
		return isSideSolid(blockState, blockAccess, blockPos, facing) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
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
