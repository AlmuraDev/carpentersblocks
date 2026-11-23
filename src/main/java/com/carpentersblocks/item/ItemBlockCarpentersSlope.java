package com.carpentersblocks.item;

import java.util.function.Consumer;

import com.carpentersblocks.block.data.SlopeData;
import com.carpentersblocks.registry.CbDataComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Slope item: places one of five slopes, like 1.7.10 (1.12.2: one subtype per slope). Sneak and scroll to switch
 * (see {@link com.carpentersblocks.network.SlopeSelectPayload}).
 */
public class ItemBlockCarpentersSlope extends BlockItem {

    /** The slopes offered as items, with their language key suffixes; other shapes come from the hammer. */
    public static final String[] SLOPE_NAMES = { "wedge", "oblique_interior", "oblique_exterior", "prism", "prism_wedge" };
    private static final SlopeData.Type[] SLOPE_TYPES = {
        SlopeData.Type.WEDGE,
        SlopeData.Type.OBLIQUE_INTERIOR,
        SlopeData.Type.OBLIQUE_EXTERIOR,
        SlopeData.Type.PRISM,
        SlopeData.Type.PRISM_WEDGE };

    public ItemBlockCarpentersSlope(Block block, Properties properties) {
        super(block, properties);
    }

    /**
     * Gets which slope a stack places, 0 to 4.
     */
    public static int getSlopeIndex(ItemStack itemStack) {
        return Mth.clamp(itemStack.getOrDefault(CbDataComponents.SLOPE_TYPE.get(), 0), 0, SLOPE_TYPES.length - 1);
    }

    public static SlopeData.Type getSlopeType(ItemStack itemStack) {
        return SLOPE_TYPES[getSlopeIndex(itemStack)];
    }

    /**
     * Turns a stack to the next or previous slope.
     */
    public static void cycle(ItemStack itemStack, boolean forward) {
        int index = getSlopeIndex(itemStack) + (forward ? 1 : -1);
        index = index >= SLOPE_TYPES.length ? 0 : index < 0 ? SLOPE_TYPES.length - 1 : index;
        itemStack.set(CbDataComponents.SLOPE_TYPE.get(), index);
    }

    /**
     * Adds one stack of each slope (the creative tab).
     */
    public void fillVariants(Consumer<ItemStack> output) {
        for (int index = 0; index < SLOPE_TYPES.length; ++index) {
            ItemStack itemStack = new ItemStack(this);
            itemStack.set(CbDataComponents.SLOPE_TYPE.get(), index);
            output.accept(itemStack);
        }
    }

    @Override
    public Component getName(ItemStack itemStack) {
        return Component.translatable(getDescriptionId() + "." + SLOPE_NAMES[getSlopeIndex(itemStack)]);
    }

}
