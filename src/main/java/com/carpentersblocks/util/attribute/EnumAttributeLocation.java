package com.carpentersblocks.util.attribute;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;

/**
 * Where an attribute sits: on one side of the block (a side cover and what is on it), or on the whole block ({@link #HOST}).
 * <p>
 * The ordinals are saved and match {@link Direction#ordinal()} for the six sides.
 */
public enum EnumAttributeLocation {

    DOWN,
    UP,
    NORTH,
    SOUTH,
    WEST,
    EAST,
    HOST;

    private static final EnumAttributeLocation[] VALUES = values();

    /**
     * Gets a location from its saved number, or null if it is out of range.
     */
    @Nullable
    public static EnumAttributeLocation valueOf(int value) {
        return value >= 0 && value < VALUES.length ? VALUES[value] : null;
    }

    public static EnumAttributeLocation fromDirection(Direction direction) {
        return VALUES[direction.ordinal()];
    }

    /**
     * Gets the side of a side location, or null for {@link #HOST}.
     */
    @Nullable
    public Direction getDirection() {
        return this == HOST ? null : Direction.values()[ordinal()];
    }

}
