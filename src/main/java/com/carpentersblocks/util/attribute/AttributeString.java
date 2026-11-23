package com.carpentersblocks.util.attribute;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A named attribute, like a chisel design.
 */
public class AttributeString extends AbstractAttribute<String> {

    private static final String TAG_ATTR_STRING = "cbAttrString";

    public AttributeString() {
        super(null, null, "");
    }

    public AttributeString(EnumAttributeLocation location, EnumAttributeType type, String model) {
        super(location, type, model);
    }

    @Override
    protected void writeModel(ValueOutput output) {
        output.putString(TAG_ATTR_STRING, getModel());
    }

    @Override
    protected void readModel(ValueInput input) {
        setModel(input.getStringOr(TAG_ATTR_STRING, ""));
    }

    @Override
    public String getModelIdentifier() {
        return IDENT_ATTR_STRING;
    }

    @Override
    public AttributeString copy() {
        return new AttributeString(_location, _type, _model);
    }

}
