package com.carpentersblocks.util.handler;

import javax.annotation.Nullable;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * Dyes, found by the dye tags (1.12.2 used the ore dictionary).
 */
public final class DyeHandler {

    private DyeHandler() {}

    @Nullable
    public static DyeColor getDyeColor(ItemStack itemStack) {
        return itemStack.isEmpty() ? null : DyeColor.getColor(itemStack);
    }

    /**
     * Returns true if ItemStack is a dye.
     */
    public static boolean isDye(ItemStack itemStack, boolean allowWhite) {
        DyeColor color = getDyeColor(itemStack);
        return color != null && (allowWhite || color != DyeColor.WHITE);
    }

    /**
     * Gets a dye's color as 0xRRGGBB (1.12.2 dyed with the firework colors), or white.
     */
    public static int getColor(ItemStack itemStack) {
        DyeColor color = getDyeColor(itemStack);
        return color == null ? 0xFFFFFF : color.getFireworkColor() & 0xFFFFFF;
    }

}
