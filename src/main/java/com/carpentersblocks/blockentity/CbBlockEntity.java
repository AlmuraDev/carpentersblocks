package com.carpentersblocks.blockentity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.CbConfig;
import com.carpentersblocks.block.ICoverableBlock;
import com.carpentersblocks.registry.CbBlockEntities;
import com.carpentersblocks.util.attribute.AbstractAttribute;
import com.carpentersblocks.util.attribute.AbstractAttribute.Key;
import com.carpentersblocks.util.attribute.AttributeHelper;
import com.carpentersblocks.util.attribute.AttributeItemStack;
import com.carpentersblocks.util.attribute.AttributeString;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.protection.IProtected;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import net.neoforged.neoforge.model.data.ModelData;

/**
 * The block entity of every Carpenter's block (1.12.2's CbTileEntity): its attributes (covers, dyes, overlays,
 * designs...), its block data and its owner.
 * <p>
 * Attributes are saved like 1.12.2 (cbAttrList, cbMetadata, cbOwner), with items in the 26.3 item format.
 */
public class CbBlockEntity extends BlockEntity implements IProtected {

    public static final String TAG_ATTR_LIST = "cbAttrList";
    public static final String TAG_METADATA  = "cbMetadata";
    public static final String TAG_OWNER     = "cbOwner";

    // Map holding all block attributes, in the order they were added
    private final Map<Key, AbstractAttribute<?>> _cbAttrMap = new LinkedHashMap<>();

    private final AttributeHelper _cbAttrHelper = new AttributeHelper(_cbAttrMap);

    // Holds specific block information like facing, states, etc
    private int _cbMetadata;

    // Owner of block entity
    private String _cbOwner = "";

    public CbBlockEntity(BlockPos pos, BlockState blockState) {
        this(CbBlockEntities.COVERABLE.get(), pos, blockState);
    }

    protected CbBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        // Client chunk packets can pass a shared mutable position
        super(type, pos.immutable(), blockState);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        _cbAttrMap.clear();
        for (ValueInput attrInput : input.childrenListOrEmpty(TAG_ATTR_LIST)) {
            String identifier = attrInput.getStringOr(AbstractAttribute.TAG_ATTR_IDENT, "");
            AbstractAttribute<?> attribute = null;
            if (identifier.equalsIgnoreCase(AbstractAttribute.IDENT_ATTR_ITEMSTACK)) {
                attribute = new AttributeItemStack();
            } else if (identifier.equalsIgnoreCase(AbstractAttribute.IDENT_ATTR_STRING)) {
                attribute = new AttributeString();
            }
            if (attribute == null || !attribute.read(attrInput)) {
                continue;
            }
            // Skip items that no longer exist, they would draw and drop as air
            if (attribute instanceof AttributeItemStack itemAttribute && itemAttribute.isMissing()) {
                CarpentersBlocks.LOGGER.warn("Dropped a missing {} attribute from the Carpenter's block at {}. Was a mod removed?",
                    attribute.getType(), worldPosition);
                continue;
            }
            _cbAttrMap.put(attribute.getKey(), attribute);
        }
        _cbMetadata = input.getIntOr(TAG_METADATA, 0);
        _cbOwner = input.getStringOr(TAG_OWNER, "");
        // Chunks load block entities before they have a level (see setLevel); update packets arrive with one
        if (level != null && level.isClientSide()) {
            onClientDataChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.ValueOutputList list = output.childrenList(TAG_ATTR_LIST);
        for (AbstractAttribute<?> attribute : _cbAttrMap.values()) {
            attribute.write(list.addChild());
        }
        output.putInt(TAG_METADATA, _cbMetadata);
        output.putString(TAG_OWNER, _cbOwner);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateLight();
    }

    /**
     * Redraws the block and relights it after new data arrived from the server.
     */
    private void onClientDataChanged() {
        requestModelDataUpdate();
        updateLight();
        BlockState blockState = getBlockState();
        level.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_ALL_IMMEDIATE);
    }

    @Override
    public ModelData getModelData() {
        return ModelData.of(CbRenderData.PROPERTY, createRenderData());
    }

    /**
     * Gets what the block is drawn from. Main thread only.
     */
    public CbRenderData createRenderData() {
        return new CbRenderData(_cbAttrHelper.copyMap(), _cbMetadata);
    }

    /**
     * Drops the item attributes when the block is removed (not when it is replaced by the same block).
     */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState blockState) {
        super.preRemoveSideEffects(pos, blockState);
        if (level != null && !level.isClientSide()) {
            for (ItemStack itemStack : getAllDroppableAttributes()) {
                Block.popResource(level, pos, itemStack);
            }
        }
    }

    /**
     * Copies owner from another Carpenter's block.
     */
    public void copyOwner(CbBlockEntity blockEntity) {
        _cbOwner = blockEntity.getOwner();
        setChanged();
    }

    @Override
    public void setOwner(Player player) {
        _cbOwner = player.getUUID().toString();
        setChanged();
    }

    @Override
    public String getOwner() {
        return _cbOwner;
    }

    public boolean addAttribute(EnumAttributeLocation location, EnumAttributeType type, Object model) {
        return addAttribute(location, type, model, null);
    }

    /**
     * Adds an attribute, unless the location already has one of the type.
     *
     * @param model an ItemStack (one item of it is kept) or a String
     * @param blockState the block state a cover is drawn with, or null for the item's block
     * @return <code>true</code> if added
     */
    public boolean addAttribute(EnumAttributeLocation location, EnumAttributeType type, Object model, @Nullable BlockState blockState) {
        if (_cbAttrHelper.hasAttribute(location, type) || model == null) {
            return false;
        }
        if (model instanceof ItemStack itemStack) {
            _cbAttrMap.put(AbstractAttribute.generateKey(location, type), new AttributeItemStack(location, type, itemStack.copyWithCount(1), blockState));
        } else if (model instanceof String string) {
            _cbAttrMap.put(AbstractAttribute.generateKey(location, type), new AttributeString(location, type, string));
        } else {
            return false;
        }
        return true;
    }

    /**
     * Removes an attribute without dropping it.
     */
    public boolean removeAttribute(EnumAttributeLocation location, EnumAttributeType type) {
        return _cbAttrMap.remove(AbstractAttribute.generateKey(location, type)) != null;
    }

    /**
     * Removes an item attribute (a cover pushed out by a design, for instance) and drops it.
     */
    public boolean dropAttribute(EnumAttributeLocation location, EnumAttributeType type) {
        AbstractAttribute<?> attribute = _cbAttrHelper.getAttribute(location, type);
        return attribute instanceof AttributeItemStack itemAttribute && dropAttribute(itemAttribute);
    }

    private boolean dropAttribute(AttributeItemStack attribute) {
        if (_cbAttrMap.remove(attribute.getKey()) == null) {
            return false;
        }
        if (level != null && !level.isClientSide()) {
            Block.popResource(level, worldPosition, attribute.getModel().copy());
        }
        update(true);
        return true;
    }

    /**
     * Drops the item attribute added last at a location.
     */
    public boolean removeLastAddedDroppableAttribute(EnumAttributeLocation location) {
        return _cbAttrHelper.getLastAddedDroppableAttribute(location) instanceof AttributeItemStack attribute && dropAttribute(attribute);
    }

    /**
     * Drops all attributes at a location: items drop, the rest are removed.
     */
    public void dropAttributes(EnumAttributeLocation location) {
        List<ItemStack> drops = new ArrayList<>();
        Iterator<AbstractAttribute<?>> it = _cbAttrMap.values().iterator();
        while (it.hasNext()) {
            AbstractAttribute<?> attribute = it.next();
            if (attribute.getLocation().equals(location)) {
                if (attribute instanceof AttributeItemStack itemAttribute) {
                    drops.add(itemAttribute.getModel().copy());
                }
                it.remove();
            }
        }
        if (level != null && !level.isClientSide()) {
            for (ItemStack itemStack : drops) {
                Block.popResource(level, worldPosition, itemStack);
            }
        }
        update(true);
    }

    /**
     * Gets block-specific data.
     */
    public int getCbMetadata() {
        return _cbMetadata;
    }

    /**
     * Sets block-specific data (server only).
     *
     * @return <code>true</code> if data was updated, or <code>false</code> if no change
     */
    public boolean setCbMetadata(int cbMetadata) {
        if (level != null && !level.isClientSide()) {
            if (cbMetadata == _cbMetadata) {
                return false;
            }
            _cbMetadata = cbMetadata;
            update(true);
        }
        return true;
    }

    /**
     * Gets copies of all item attributes.
     */
    public List<ItemStack> getAllDroppableAttributes() {
        List<ItemStack> list = new ArrayList<>();
        for (AbstractAttribute<?> attribute : _cbAttrMap.values()) {
            if (attribute instanceof AttributeItemStack itemAttribute) {
                list.add(itemAttribute.getModel().copy());
            }
        }
        return list;
    }

    public AttributeHelper getAttributeHelper() {
        return _cbAttrHelper;
    }

    ///////////////////////////////////////////////////////
    // Light                                             //
    ///////////////////////////////////////////////////////

    /**
     * Returns the block light value from block attributes.
     *
     * @return a value from 0 to 15
     */
    protected int getDynamicLightValue() {
        if (CbConfig.ENABLE_ILLUMINATION.get() && _cbAttrHelper.hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.ILLUMINATOR)) {
            return 15;
        }
        // The block adds its own light (torches)
        int value = 0;
        for (BlockState blockState : _cbAttrHelper.getAttributeBlockStates()) {
            value = Math.max(value, blockState.getLightEmission());
        }
        return getBlockState().getBlock() instanceof ICoverableBlock block ? block.getLightValue(this, value) : value;
    }

    /**
     * Stores the light value where the light engine reads it (see {@link BlockCoverable#getLightEmission}) and
     * relights the block if it changed.
     */
    private void updateLight() {
        if (level == null) {
            return;
        }
        AuxiliaryLightManager lightManager = level.getAuxLightManager(worldPosition);
        if (lightManager == null) {
            return;
        }
        int value = getDynamicLightValue();
        if (lightManager.getLightAt(worldPosition) != value) {
            lightManager.setLightAt(worldPosition, value);
            level.getLightEngine().checkBlock(worldPosition);
        }
    }

    /**
     * Sends the block to clients, relights it and updates redstone neighbors.
     */
    public void update(boolean markDirty) {
        Level level = getLevel();
        if (level == null) {
            return;
        }
        BlockState blockState = getBlockState();
        updateLight();
        if (markDirty) {
            setChanged();
        }
        level.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_ALL);
        if (blockState.isSignalSource()) {
            level.updateNeighborsAt(worldPosition, blockState.getBlock());
        }
    }

}
