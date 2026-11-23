package com.carpentersblocks.util.attribute;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A block attribute (a cover, dye, overlay, design...) at a location of a Carpenter's block.
 *
 * @param <K> the attribute model
 */
public abstract class AbstractAttribute<K> {

    public static final String TAG_ATTR_LOCATION = "cbAttrLocation";
    public static final String TAG_ATTR_TYPE     = "cbAttrType";
    public static final String TAG_ATTR_IDENT    = "cbAttrModel";
    public static final String IDENT_ATTR_ITEMSTACK = "ItemStack";
    public static final String IDENT_ATTR_STRING    = "String";
    protected EnumAttributeType _type;
    protected EnumAttributeLocation _location;
    protected K _model;

    public AbstractAttribute(EnumAttributeLocation location, EnumAttributeType type, K model) {
        _location = location;
        _type = type;
        _model = model;
    }

    public void setModel(K model) {
        _model = model;
    }

    public K getModel() {
        return _model;
    }

    public EnumAttributeType getType() {
        return _type;
    }

    public EnumAttributeLocation getLocation() {
        return _location;
    }

    public void write(ValueOutput output) {
        output.putInt(TAG_ATTR_LOCATION, _location.ordinal());
        output.putInt(TAG_ATTR_TYPE, _type.ordinal());
        output.putString(TAG_ATTR_IDENT, getModelIdentifier());
        writeModel(output);
    }

    /**
     * Reads the attribute.
     *
     * @return <code>false</code> if the location or type is unknown
     */
    public boolean read(ValueInput input) {
        _location = EnumAttributeLocation.valueOf(input.getIntOr(TAG_ATTR_LOCATION, -1));
        _type = EnumAttributeType.valueOf(input.getIntOr(TAG_ATTR_TYPE, -1));
        if (_location == null || _type == null) {
            return false;
        }
        readModel(input);
        return true;
    }

    public abstract String getModelIdentifier();

    protected abstract void writeModel(ValueOutput output);

    protected abstract void readModel(ValueInput input);

    public static Key generateKey(EnumAttributeLocation location, EnumAttributeType type) {
        return new Key(location, type);
    }

    public Key getKey() {
        return new Key(_location, _type);
    }

    /**
     * An attribute's location and type: a block has at most one attribute of each.
     */
    public record Key(EnumAttributeLocation location, EnumAttributeType type) {

        public EnumAttributeLocation getLocation() {
            return location;
        }

        public EnumAttributeType getType() {
            return type;
        }

    }

    public boolean isDroppable() {
        return _model instanceof ItemStack;
    }

    public abstract AbstractAttribute<K> copy();

}
