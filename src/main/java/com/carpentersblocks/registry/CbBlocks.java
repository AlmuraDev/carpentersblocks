package com.carpentersblocks.registry;

import java.util.function.Function;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.BlockCarpentersBlock;
import com.carpentersblocks.block.BlockCoverable;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The blocks, with the 1.12.2 names. Blocks are added here as they are ported.
 */
public final class CbBlocks {

    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(CarpentersBlocks.MOD_ID);

    public static final String REGISTRY_NAME_BLOCK = "carpenters_block";

    // Always enabled, like 1.12.2
    public static final DeferredBlock<BlockCarpentersBlock> CARPENTERS_BLOCK = register(REGISTRY_NAME_BLOCK, BlockCarpentersBlock::new, 0.2F);

    private CbBlocks() {}

    /**
     * Registers a wooden Carpenter's block and its item.
     *
     * @param hardness the 1.12.2 hardness
     */
    private static <B extends BlockCoverable> DeferredBlock<B> register(String name, Function<BlockBehaviour.Properties, B> factory, float hardness) {
        DeferredBlock<B> block = REGISTER.registerBlock(name, factory, () -> woodProperties(hardness));
        CbItems.REGISTER.registerSimpleBlockItem(name, block);
        return block;
    }

    private static BlockBehaviour.Properties woodProperties(float hardness) {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .instrument(NoteBlockInstrument.BASS)
            .strength(hardness)
            .sound(SoundType.WOOD)
            .ignitedByLava()
            .dynamicShape();
    }

}
