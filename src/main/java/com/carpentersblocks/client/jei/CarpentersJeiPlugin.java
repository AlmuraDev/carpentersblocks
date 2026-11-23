package com.carpentersblocks.client.jei;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.registry.CbDataComponents;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * JEI: the five slope items are one item told apart by their slope type component. Only loaded when JEI is installed.
 */
@JeiPlugin
public class CarpentersJeiPlugin implements IModPlugin {

    private static final Identifier UID = CarpentersBlocks.id("jei");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        Item slope = BuiltInRegistries.ITEM.getValue(CarpentersBlocks.id("carpenters_slope"));
        if (slope != Items.AIR) {
            registration.registerFromDataComponentTypes(slope, CbDataComponents.SLOPE_TYPE.get());
        }
    }

}
