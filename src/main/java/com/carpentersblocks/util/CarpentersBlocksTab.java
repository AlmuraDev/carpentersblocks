package com.carpentersblocks.util;

import com.carpentersblocks.util.registry.ItemRegistry;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CarpentersBlocksTab extends CreativeTabs {

    public CarpentersBlocksTab(String label) {
        super(label);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ItemStack createIcon() {
        return new ItemStack(ItemRegistry.itemCarpentersHammer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getTranslationKey() {
        return "itemGroup.carpentersBlocks.name";
    }

}
