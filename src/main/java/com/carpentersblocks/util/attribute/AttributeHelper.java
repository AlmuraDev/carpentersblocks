package com.carpentersblocks.util.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.carpentersblocks.util.attribute.AbstractAttribute.Key;
import com.carpentersblocks.util.block.BlockUtil;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Reads a block's attributes.
 */
public class AttributeHelper {

    private final Map<Key, AbstractAttribute<?>> _map;

    public AttributeHelper(Map<Key, AbstractAttribute<?>> map) {
        _map = map;
    }

    public boolean hasAttribute(Key key) {
        return _map.containsKey(key);
    }

    public boolean hasAttribute(EnumAttributeLocation location, EnumAttributeType type) {
        return hasAttribute(AbstractAttribute.generateKey(location, type));
    }

    @Nullable
    public AbstractAttribute<?> getAttribute(Key key) {
        return _map.get(key);
    }

    @Nullable
    public AbstractAttribute<?> getAttribute(EnumAttributeLocation location, EnumAttributeType type) {
        return getAttribute(AbstractAttribute.generateKey(location, type));
    }

    /**
     * Gets an item attribute's stack, or an empty stack.
     */
    public ItemStack getItemStack(EnumAttributeLocation location, EnumAttributeType type) {
        return getAttribute(location, type) instanceof AttributeItemStack attribute ? attribute.getModel() : ItemStack.EMPTY;
    }

    /**
     * Gets a string attribute, or null.
     */
    @Nullable
    public String getString(EnumAttributeLocation location, EnumAttributeType type) {
        return getAttribute(location, type) instanceof AttributeString attribute ? attribute.getModel() : null;
    }

    /**
     * Gets the block state an item attribute (a cover) is drawn and acts as, or null.
     */
    @Nullable
    public BlockState getBlockState(EnumAttributeLocation location, EnumAttributeType type) {
        return getAttribute(location, type) instanceof AttributeItemStack attribute ? BlockUtil.getBlockState(attribute) : null;
    }

    @Nullable
    public AbstractAttribute<?> getLastAddedDroppableAttribute(EnumAttributeLocation location) {
        AbstractAttribute<?> attribute = null;
        for (Map.Entry<Key, AbstractAttribute<?>> entry : _map.entrySet()) {
            if (location.equals(entry.getKey().getLocation()) && entry.getValue().isDroppable()) {
                attribute = entry.getValue();
            }
        }
        return attribute;
    }

    /**
     * Copies the attributes, for the renderer (which reads them off the main thread).
     */
    public Map<Key, AbstractAttribute<?>> copyMap() {
        Map<Key, AbstractAttribute<?>> map = new LinkedHashMap<>();
        for (Map.Entry<Key, AbstractAttribute<?>> entry : _map.entrySet()) {
            map.put(entry.getKey(), entry.getValue().copy());
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Gets the block states of all item attributes that are blocks.
     */
    public List<BlockState> getAttributeBlockStates() {
        List<BlockState> list = new ArrayList<>();
        for (AbstractAttribute<?> attribute : _map.values()) {
            if (attribute instanceof AttributeItemStack itemAttribute) {
                BlockState blockState = BlockUtil.getBlockState(itemAttribute);
                if (blockState != null) {
                    list.add(blockState);
                }
            }
        }
        return list;
    }

}
