package com.carpentersblocks.tileentity;

import com.carpentersblocks.block.BlockCarpentersSafe;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * Carpenter's Safe inventory, ported from 1.7.10: 27 slots, 54 with an upgrade.
 * Hoppers and pipes reach every slot from any side but the front, as the automation mode allows.
 */
public class TECarpentersSafe extends CbTileEntity implements ISidedInventory {

	private static final int MAX_SIZE = 54;
	private static final int BASE_SIZE = 27;
	private static final int[] NO_SLOTS = new int[0];

	private final NonNullList<ItemStack> _items = NonNullList.withSize(MAX_SIZE, ItemStack.EMPTY);
	private int _playersUsing;

	public boolean hasUpgrade() {
		return getAttributeHelper().hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.SAFE_UPGRADE);
	}

	/**
	 * Gets how many slots hold items.
	 */
	public int getFilledSlots() {
		int filled = 0;
		for (int slot = 0; slot < getSizeInventory(); ++slot) {
			if (!_items.get(slot).isEmpty()) {
				++filled;
			}
		}
		return filled;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		_items.clear();
		ItemStackHelper.loadAllItems(nbt, _items);
		super.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		ItemStackHelper.saveAllItems(nbt, _items);
		return nbt;
	}

	/**
	 * Contents changed: saves and refreshes the fill lights for nearby players.
	 */
	@Override
	public void markDirty() {
		super.markDirty();
		if (world != null && !world.isRemote) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
		}
	}

	////////////
	// IInventory
	////////////

	@Override
	public int getSizeInventory() {
		return hasUpgrade() ? MAX_SIZE : BASE_SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : _items) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot >= 0 && slot < MAX_SIZE ? _items.get(slot) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {
		ItemStack itemStack = ItemStackHelper.getAndSplit(_items, slot, count);
		if (!itemStack.isEmpty()) {
			markDirty();
		}
		return itemStack;
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {
		return ItemStackHelper.getAndRemove(_items, slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack) {
		_items.set(slot, itemStack);
		if (itemStack.getCount() > getInventoryStackLimit()) {
			itemStack.setCount(getInventoryStackLimit());
		}
		markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return world.getTileEntity(pos) == this && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		if (!player.isSpectator() && ++_playersUsing == 1 && getBlockType() instanceof BlockCarpentersSafe) {
			((BlockCarpentersSafe) getBlockType()).setOpen(this, true);
		}
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		if (!player.isSpectator() && _playersUsing > 0 && --_playersUsing == 0 && getBlockType() instanceof BlockCarpentersSafe) {
			((BlockCarpentersSafe) getBlockType()).setOpen(this, false);
		}
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		_items.clear();
	}

	@Override
	public String getName() {
		return "tile.blockCarpentersSafe.name";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentTranslation(getName());
	}

	////////////
	// ISidedInventory
	////////////

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		if (side == BlockCarpentersSafe.getFacing(getCbMetadata())) {
			return NO_SLOTS;
		}
		int[] slots = new int[getSizeInventory()];
		for (int slot = 0; slot < slots.length; ++slot) {
			slots[slot] = slot;
		}
		return slots;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
		int automation = BlockCarpentersSafe.getAutomation(getCbMetadata());
		return side != BlockCarpentersSafe.getFacing(getCbMetadata())
			&& (automation == BlockCarpentersSafe.AUTOMATION_RECEIVE || automation == BlockCarpentersSafe.AUTOMATION_ALL);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, EnumFacing side) {
		int automation = BlockCarpentersSafe.getAutomation(getCbMetadata());
		return side != BlockCarpentersSafe.getFacing(getCbMetadata())
			&& (automation == BlockCarpentersSafe.AUTOMATION_SEND || automation == BlockCarpentersSafe.AUTOMATION_ALL);
	}

}
