package com.carpentersblocks.registry;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.CbConfig;
import com.carpentersblocks.item.ItemCarpentersBed;
import com.carpentersblocks.item.ItemCarpentersChisel;
import com.carpentersblocks.item.ItemCarpentersDoor;
import com.carpentersblocks.item.ItemCarpentersHammer;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The items, with the 1.12.2 names. Items are added here as they are ported.
 */
public final class CbItems {

    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(CarpentersBlocks.MOD_ID);

    public static final DeferredItem<ItemCarpentersHammer> CARPENTERS_HAMMER = CbConfig.ENABLE_HAMMER.get()
        ? REGISTER.registerItem("carpenters_hammer", ItemCarpentersHammer::new, CbItems::toolProperties) : null;
    public static final DeferredItem<ItemCarpentersChisel> CARPENTERS_CHISEL = CbConfig.ENABLE_CHISEL.get()
        ? REGISTER.registerItem("carpenters_chisel", ItemCarpentersChisel::new, CbItems::toolProperties) : null;

    public static final DeferredItem<ItemCarpentersDoor> CARPENTERS_DOOR = CbConfig.ENABLE_DOOR.get()
        ? REGISTER.registerItem("carpenters_door_item", ItemCarpentersDoor::new, Item.Properties::new) : null;

    public static final DeferredItem<ItemCarpentersBed> CARPENTERS_BED = CbConfig.ENABLE_BED.get()
        ? REGISTER.registerItem("carpenters_bed_item", ItemCarpentersBed::new, Item.Properties::new) : null;

    private CbItems() {}

    private static Item.Properties toolProperties() {
        Item.Properties properties = new Item.Properties().stacksTo(1);
        if (CbConfig.TOOLS_DAMAGEABLE.get()) {
            properties.durability(CbConfig.TOOL_USES.get());
        }
        return properties;
    }

}
