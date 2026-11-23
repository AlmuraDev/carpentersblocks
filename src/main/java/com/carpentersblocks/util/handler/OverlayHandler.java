package com.carpentersblocks.util.handler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.CbConfig;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Overlays: grass, snow and the like laid over a cover, set by items listed in the config.
 */
public final class OverlayHandler {

    public enum Overlay {
        NONE(Blocks.AIR),
        GRASS(Blocks.GRASS_BLOCK),
        SNOW(Blocks.SNOW_BLOCK),
        WEB(Blocks.COBWEB),
        VINE(Blocks.VINE),
        HAY(Blocks.HAY_BLOCK),
        MYCELIUM(Blocks.MYCELIUM);

        private final Block block;

        Overlay(Block block) {
            this.block = block;
        }

        /**
         * Gets the block the overlay looks like.
         */
        public Block getBlock() {
            return block;
        }

        public ItemStack getItemStack() {
            return new ItemStack(block);
        }
    }

    private static final Map<Item, Overlay> overlayMap = new HashMap<>();

    private OverlayHandler() {}

    /**
     * Reads the overlay items from the config. Called once items are registered.
     */
    public static void init() {
        overlayMap.clear();
        for (String entry : CbConfig.OVERLAY_ITEMS.get()) {
            int split = entry.indexOf('=');
            Identifier id = Identifier.tryParse(entry.substring(0, split).trim());
            Overlay overlay = parseOverlay(entry.substring(split + 1).trim());
            Item item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (item == null || overlay == null) {
                CarpentersBlocks.LOGGER.warn("Unknown overlay definition '{}'", entry);
                continue;
            }
            overlayMap.putIfAbsent(item, overlay);
        }
    }

    private static Overlay parseOverlay(String name) {
        try {
            Overlay overlay = Overlay.valueOf(name.toUpperCase(Locale.ROOT));
            return overlay == Overlay.NONE ? null : overlay;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isOverlay(ItemStack itemStack) {
        return !itemStack.isEmpty() && overlayMap.containsKey(itemStack.getItem());
    }

    /**
     * Returns overlay from qualified ItemStack.
     */
    public static Overlay getOverlayType(ItemStack itemStack) {
        return itemStack.isEmpty() ? Overlay.NONE : overlayMap.getOrDefault(itemStack.getItem(), Overlay.NONE);
    }

    /**
     * Returns true if overlay covers a majority or all of side.
     */
    public static boolean coversFullSide(Overlay overlay, Direction facing) {
        return switch (overlay) {
            case GRASS, SNOW, HAY, MYCELIUM -> Direction.UP.equals(facing);
            default -> true;
        };
    }

}
