package com.carpentersblocks.registry;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.CbConfig;
import com.carpentersblocks.block.BlockCarpentersBarrier;
import com.carpentersblocks.block.BlockCarpentersBlock;
import com.carpentersblocks.block.BlockCarpentersButton;
import com.carpentersblocks.block.BlockCarpentersFlowerPot;
import com.carpentersblocks.block.BlockCarpentersPressurePlate;
import com.carpentersblocks.block.BlockCarpentersCollapsibleBlock;
import com.carpentersblocks.block.BlockCarpentersSlope;
import com.carpentersblocks.item.ItemBlockCarpentersSlope;
import net.minecraft.world.item.Item;
import com.carpentersblocks.block.BlockCarpentersDaylightSensor;
import com.carpentersblocks.block.BlockCarpentersSafe;
import com.carpentersblocks.block.BlockCarpentersStairs;
import com.carpentersblocks.block.BlockCarpentersDoor;
import com.carpentersblocks.block.BlockCarpentersGarageDoor;
import com.carpentersblocks.block.BlockCarpentersHatch;
import com.carpentersblocks.block.BlockCarpentersGate;
import com.carpentersblocks.block.BlockCarpentersLadder;
import com.carpentersblocks.block.BlockCarpentersLever;
import com.carpentersblocks.block.BlockCarpentersTorch;
import com.carpentersblocks.block.BlockCoverable;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The blocks, with the 1.12.2 names and hardness. Blocks are added here as they are ported; one turned off in the
 * config is not registered, and its field is null.
 */
public final class CbBlocks {

    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(CarpentersBlocks.MOD_ID);

    public static final String REGISTRY_NAME_BLOCK = "carpenters_block";

    // Always enabled, like 1.12.2
    public static final DeferredBlock<BlockCarpentersBlock> CARPENTERS_BLOCK = register(REGISTRY_NAME_BLOCK, BlockCarpentersBlock::new, 0.2F, UnaryOperator.identity());

    public static final DeferredBlock<BlockCarpentersBarrier> CARPENTERS_BARRIER = CbConfig.ENABLE_BARRIER.get()
        ? register("carpenters_barrier", BlockCarpentersBarrier::new, 0.2F, CbBlocks::shaped) : null;
    public static final DeferredBlock<BlockCarpentersGate> CARPENTERS_GATE = CbConfig.ENABLE_GATE.get()
        ? register("carpenters_gate", BlockCarpentersGate::new, 0.2F, CbBlocks::shaped) : null;
    public static final DeferredBlock<BlockCarpentersLadder> CARPENTERS_LADDER = CbConfig.ENABLE_LADDER.get()
        ? register("carpenters_ladder", BlockCarpentersLadder::new, 0.2F, properties -> shaped(properties).sound(SoundType.LADDER)) : null;
    // 1.12.2 made these of the circuits material: pistons break them
    public static final DeferredBlock<BlockCarpentersButton> CARPENTERS_BUTTON = CbConfig.ENABLE_BUTTON.get()
        ? register("carpenters_button", BlockCarpentersButton::new, 0.2F, CbBlocks::attached) : null;
    public static final DeferredBlock<BlockCarpentersLever> CARPENTERS_LEVER = CbConfig.ENABLE_LEVER.get()
        ? register("carpenters_lever", BlockCarpentersLever::new, 0.2F, CbBlocks::attached) : null;
    public static final DeferredBlock<BlockCarpentersTorch> CARPENTERS_TORCH = CbConfig.ENABLE_TORCH.get()
        ? register("carpenters_torch", BlockCarpentersTorch::new, 0.2F, CbBlocks::attached) : null;

    public static final DeferredBlock<BlockCarpentersHatch> CARPENTERS_HATCH = CbConfig.ENABLE_HATCH.get()
        ? register("carpenters_hatch", BlockCarpentersHatch::new, 0.2F, CbBlocks::shaped) : null;
    public static final DeferredBlock<BlockCarpentersGarageDoor> CARPENTERS_GARAGE_DOOR = CbConfig.ENABLE_GARAGE_DOOR.get()
        ? register("carpenters_garage_door", BlockCarpentersGarageDoor::new, 0.2F, CbBlocks::shaped) : null;
    public static final DeferredBlock<BlockCarpentersStairs> CARPENTERS_STAIRS = CbConfig.ENABLE_STAIRS.get()
        ? register("carpenters_stairs", BlockCarpentersStairs::new, 0.2F, CbBlocks::shaped) : null;
    public static final DeferredBlock<BlockCarpentersDaylightSensor> CARPENTERS_DAYLIGHT_SENSOR = CbConfig.ENABLE_DAYLIGHT_SENSOR.get()
        ? register("carpenters_daylight_sensor", BlockCarpentersDaylightSensor::new, 0.2F, CbBlocks::attached) : null;
    // Explosion proof, like 1.12.2
    public static final DeferredBlock<BlockCarpentersSafe> CARPENTERS_SAFE = CbConfig.ENABLE_SAFE.get()
        ? register("carpenters_safe", BlockCarpentersSafe::new, 2.5F, properties -> shaped(properties).explosionResistance(6000000.0F)) : null;
    public static final DeferredBlock<BlockCarpentersCollapsibleBlock> CARPENTERS_COLLAPSIBLE_BLOCK = CbConfig.ENABLE_COLLAPSIBLE_BLOCK.get()
        ? register("carpenters_collapsible_block", BlockCarpentersCollapsibleBlock::new, 0.2F, CbBlocks::shaped) : null;
    // One item for the five slopes, which it keeps in a data component
    public static final DeferredBlock<BlockCarpentersSlope> CARPENTERS_SLOPE = CbConfig.ENABLE_SLOPE.get() ? registerSlope() : null;
    public static final DeferredBlock<BlockCarpentersPressurePlate> CARPENTERS_PRESSURE_PLATE = CbConfig.ENABLE_PRESSURE_PLATE.get()
        ? register("carpenters_pressure_plate", BlockCarpentersPressurePlate::new, 0.2F, CbBlocks::attached) : null;
    public static final DeferredBlock<BlockCarpentersFlowerPot> CARPENTERS_FLOWER_POT = CbConfig.ENABLE_FLOWER_POT.get()
        ? register("carpenters_flower_pot", BlockCarpentersFlowerPot::new, 0.5F, CbBlocks::attached) : null;
    // Placed with the door item (CbItems.CARPENTERS_DOOR), so it has no block item
    public static final DeferredBlock<BlockCarpentersDoor> CARPENTERS_DOOR = CbConfig.ENABLE_DOOR.get()
        ? REGISTER.registerBlock("carpenters_door", BlockCarpentersDoor::new, () -> shaped(woodProperties(0.2F))) : null;

    private CbBlocks() {}

    /**
     * Registers a wooden Carpenter's block and its item.
     *
     * @param hardness the 1.12.2 hardness
     */
    private static <B extends BlockCoverable> DeferredBlock<B> register(String name, Function<BlockBehaviour.Properties, B> factory, float hardness,
            UnaryOperator<BlockBehaviour.Properties> extra) {
        DeferredBlock<B> block = REGISTER.registerBlock(name, factory, () -> extra.apply(woodProperties(hardness)));
        CbItems.REGISTER.registerSimpleBlockItem(name, block);
        return block;
    }

    private static DeferredBlock<BlockCarpentersSlope> registerSlope() {
        DeferredBlock<BlockCarpentersSlope> block = REGISTER.registerBlock("carpenters_slope", BlockCarpentersSlope::new, () -> shaped(woodProperties(0.2F)));
        CbItems.REGISTER.registerItem("carpenters_slope", properties -> new ItemBlockCarpentersSlope(block.get(), properties),
            () -> new Item.Properties().useBlockDescriptionPrefix());
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

    /**
     * Blocks that are not full cubes: neighbors are drawn behind them.
     */
    private static BlockBehaviour.Properties shaped(BlockBehaviour.Properties properties) {
        return properties.noOcclusion();
    }

    private static BlockBehaviour.Properties attached(BlockBehaviour.Properties properties) {
        return properties.noOcclusion().pushReaction(PushReaction.POPPED);
    }

}
