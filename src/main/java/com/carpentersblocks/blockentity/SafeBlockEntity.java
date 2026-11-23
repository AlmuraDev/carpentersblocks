package com.carpentersblocks.blockentity;

import javax.annotation.Nullable;

import com.carpentersblocks.block.BlockCarpentersSafe;
import com.carpentersblocks.registry.CbBlockEntities;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Carpenter's Safe inventory, ported from 1.7.10: 27 slots, 54 with an upgrade. Hoppers and pipes reach every slot
 * from any side but the front, as the automation mode allows (NeoForge wraps this as a WorldlyContainer).
 */
public class SafeBlockEntity extends CbBlockEntity implements WorldlyContainer, MenuProvider {

    private static final int MAX_SIZE = 54;
    private static final int BASE_SIZE = 27;
    private static final int[] NO_SLOTS = new int[0];

    // Render data: lit fill cells and upgrade, see BlockCarpentersSafe
    public static final int LIT_CELLS_SHIFT = 16;
    public static final int UPGRADE_SHIFT = 20;

    private final NonNullList<ItemStack> _items = NonNullList.withSize(MAX_SIZE, ItemStack.EMPTY);
    private int _playersUsing;

    public SafeBlockEntity(BlockPos pos, BlockState blockState) {
        super(CbBlockEntities.SAFE.get(), pos, blockState);
    }

    public boolean hasUpgrade() {
        return getAttributeHelper().hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.SAFE_UPGRADE);
    }

    /**
     * Gets how many slots hold items.
     */
    public int getFilledSlots() {
        int filled = 0;
        for (int slot = 0; slot < getContainerSize(); ++slot) {
            if (!_items.get(slot).isEmpty()) {
                ++filled;
            }
        }
        return filled;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        _items.clear();
        ContainerHelper.loadAllItems(input, _items);
        super.loadAdditional(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, _items);
    }

    /**
     * The model draws the fill gauge and the upgrade panel too.
     */
    @Override
    public CbRenderData createRenderData() {
        CbRenderData data = super.createRenderData();
        // 9 lights, each a ninth of the slots
        int litCells = Math.min(9, getFilledSlots() * 9 / getContainerSize());
        int upgrade = BlockCarpentersSafe.getUpgradeIndex(getAttributeHelper().getItemStack(EnumAttributeLocation.HOST, EnumAttributeType.SAFE_UPGRADE));
        return new CbRenderData(data.attributes(), data.cbMetadata() | litCells << LIT_CELLS_SHIFT | upgrade << UPGRADE_SHIFT);
    }

    /**
     * Contents changed: saves and refreshes the fill lights for nearby players.
     */
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState blockState = getBlockState();
            level.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Drops the contents with the attributes.
     */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState blockState) {
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, _items);
        }
        super.preRemoveSideEffects(pos, blockState);
    }

    ////////////
    // Container
    ////////////

    @Override
    public int getContainerSize() {
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
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < MAX_SIZE ? _items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack itemStack = ContainerHelper.removeItem(_items, slot, count);
        if (!itemStack.isEmpty()) {
            setChanged();
        }
        return itemStack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(_items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        _items.set(slot, itemStack);
        itemStack.limitSize(getMaxStackSize(itemStack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this && player.isWithinBlockInteractionRange(worldPosition, 4.0D);
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (!user.getLivingEntity().isSpectator() && ++_playersUsing == 1 && getBlockState().getBlock() instanceof BlockCarpentersSafe safe) {
            safe.setOpen(this, true);
        }
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (!user.getLivingEntity().isSpectator() && _playersUsing > 0 && --_playersUsing == 0 && getBlockState().getBlock() instanceof BlockCarpentersSafe safe) {
            safe.setOpen(this, false);
        }
    }

    @Override
    public void clearContent() {
        _items.clear();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.carpentersblocks.carpenters_safe");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return hasUpgrade() ? ChestMenu.sixRows(containerId, inventory, this) : ChestMenu.threeRows(containerId, inventory, this);
    }

    ////////////
    // WorldlyContainer
    ////////////

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == BlockCarpentersSafe.getFacing(getCbMetadata())) {
            return NO_SLOTS;
        }
        int[] slots = new int[getContainerSize()];
        for (int slot = 0; slot < slots.length; ++slot) {
            slots[slot] = slot;
        }
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack itemStack, @Nullable Direction side) {
        int automation = BlockCarpentersSafe.getAutomation(getCbMetadata());
        return side != BlockCarpentersSafe.getFacing(getCbMetadata())
            && (automation == BlockCarpentersSafe.AUTOMATION_RECEIVE || automation == BlockCarpentersSafe.AUTOMATION_ALL);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction side) {
        int automation = BlockCarpentersSafe.getAutomation(getCbMetadata());
        return side != BlockCarpentersSafe.getFacing(getCbMetadata())
            && (automation == BlockCarpentersSafe.AUTOMATION_SEND || automation == BlockCarpentersSafe.AUTOMATION_ALL);
    }

}
