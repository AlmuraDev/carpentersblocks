package com.carpentersblocks.util.attribute;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * An item attribute (cover, dye, overlay, illuminator...), dropped when removed.
 * <p>
 * A cover also keeps the block state it is drawn with (the axis of a log, for instance). Without one, the item's block
 * is drawn in its default state.
 */
public class AttributeItemStack extends AbstractAttribute<ItemStack> {

    private static final String TAG_ITEM  = "cbItem";
    private static final String TAG_STATE = "cbState";

    @Nullable
    private BlockState _blockState;

    public AttributeItemStack() {
        super(null, null, ItemStack.EMPTY);
    }

    public AttributeItemStack(EnumAttributeLocation location, EnumAttributeType type, ItemStack itemStack) {
        this(location, type, itemStack, null);
    }

    public AttributeItemStack(EnumAttributeLocation location, EnumAttributeType type, ItemStack itemStack, @Nullable BlockState blockState) {
        super(location, type, itemStack);
        _blockState = blockState;
    }

    /**
     * Gets the block state the cover is drawn with, or null to use the item's block.
     */
    @Nullable
    public BlockState getBlockState() {
        return _blockState;
    }

    /**
     * Whether the item could not be loaded (for example its mod was removed).
     */
    public boolean isMissing() {
        return getModel() == null || getModel().isEmpty();
    }

    @Override
    protected void writeModel(ValueOutput output) {
        if (!isMissing()) {
            output.store(TAG_ITEM, ItemStack.CODEC, getModel());
        }
        if (_blockState != null) {
            output.store(TAG_STATE, BlockState.CODEC, _blockState);
        }
    }

    @Override
    protected void readModel(ValueInput input) {
        // Items that no longer exist read as empty, and the block entity skips them
        setModel(input.read(TAG_ITEM, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        _blockState = input.read(TAG_STATE, BlockState.CODEC).orElse(null);
    }

    @Override
    public String getModelIdentifier() {
        return IDENT_ATTR_ITEMSTACK;
    }

    @Override
    public AttributeItemStack copy() {
        return new AttributeItemStack(_location, _type, _model.copy(), _blockState);
    }

}
