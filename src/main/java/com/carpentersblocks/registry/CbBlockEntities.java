package com.carpentersblocks.registry;

import java.util.Set;
import java.util.stream.Collectors;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.BlockCarpentersSafe;
import com.carpentersblocks.block.BlockCoverable;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.blockentity.SafeBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CbBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CarpentersBlocks.MOD_ID);

    /**
     * The block entity of every Carpenter's block (1.12.2: TileEntityCarpentersBlock). Blocks register first, so they
     * are all known here.
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CbBlockEntity>> COVERABLE = REGISTER.register("coverable",
        () -> new BlockEntityType<>(CbBlockEntity::new, coverableBlocks()));

    /**
     * The Carpenter's Safe, with its inventory (1.12.2: TileEntityCarpentersSafe).
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SafeBlockEntity>> SAFE = REGISTER.register("safe",
        () -> new BlockEntityType<>(SafeBlockEntity::new, safeBlocks()));

    private CbBlockEntities() {}

    private static Set<Block> coverableBlocks() {
        return CbBlocks.REGISTER.getEntries().stream()
            .map(DeferredHolder::get)
            .filter(block -> block instanceof BlockCoverable && !(block instanceof BlockCarpentersSafe))
            .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<Block> safeBlocks() {
        return CbBlocks.REGISTER.getEntries().stream()
            .map(DeferredHolder::get)
            .filter(block -> block instanceof BlockCarpentersSafe)
            .collect(Collectors.toUnmodifiableSet());
    }

}
