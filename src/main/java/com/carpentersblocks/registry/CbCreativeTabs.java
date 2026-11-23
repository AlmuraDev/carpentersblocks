package com.carpentersblocks.registry;

import com.carpentersblocks.CarpentersBlocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CbCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CarpentersBlocks.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTER.register(CarpentersBlocks.MOD_ID, () -> CreativeModeTab.builder()
        .icon(() -> new ItemStack(CbBlocks.CARPENTERS_BLOCK.get()))
        .title(Component.translatable("itemGroup." + CarpentersBlocks.MOD_ID))
        .displayItems((parameters, output) -> CbItems.REGISTER.getEntries().forEach(item -> output.accept(item.get())))
        .build());

    private CbCreativeTabs() {}

}
