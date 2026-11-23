package com.carpentersblocks.blockentity;

import java.util.Map;

import com.carpentersblocks.util.attribute.AbstractAttribute;
import com.carpentersblocks.util.attribute.AbstractAttribute.Key;
import com.carpentersblocks.util.attribute.AttributeHelper;

import net.neoforged.neoforge.model.data.ModelProperty;

/**
 * What a Carpenter's block is drawn from: a copy of its attributes and its block data, taken on the main thread
 * (1.12.2 passed these to the renderer in the extended block state).
 *
 * @param attributes the attributes, unmodifiable
 * @param data the block data ({@link CbBlockEntity#getCbMetadata()} or block specific render data)
 */
public record CbRenderData(Map<Key, AbstractAttribute<?>> attributes, int data) {

    public static final ModelProperty<CbRenderData> PROPERTY = new ModelProperty<>();

    public AttributeHelper getAttributeHelper() {
        return new AttributeHelper(attributes);
    }

}
