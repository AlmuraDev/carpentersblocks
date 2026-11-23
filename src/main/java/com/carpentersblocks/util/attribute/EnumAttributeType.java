package com.carpentersblocks.util.attribute;

import javax.annotation.Nullable;

/**
 * What an attribute is. The ordinals are saved.
 */
public enum EnumAttributeType {

    COVER,
    DYE,
    OVERLAY,
    DESIGN_BASIC,
    DESIGN_CHISEL,
    ILLUMINATOR,
    PLANT,
    SOIL,
    FERTILIZER,
    SAFE_UPGRADE,
    SNOW_PILE;

    private static final EnumAttributeType[] VALUES = values();

    /**
     * Gets a type from its saved number, or null if it is out of range.
     */
    @Nullable
    public static EnumAttributeType valueOf(int value) {
        return value >= 0 && value < VALUES.length ? VALUES[value] : null;
    }

}
