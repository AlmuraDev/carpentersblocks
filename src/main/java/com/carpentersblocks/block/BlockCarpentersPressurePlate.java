package com.carpentersblocks.block;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;

/**
 * Carpenter's Pressure Plate, ported from 1.7.10: a plate that some entities press (players, monsters, animals or all),
 * with invertible output.
 * <p>
 * Data bits 0-2: facing (always up). Bit 3: pressed. Bit 4: inverted polarity. Bits 5-6: trigger.
 * 1.12.2 drew it from a state file (states/carpenters_pressure_plate.json); its two boxes are here.
 */
public class BlockCarpentersPressurePlate extends BlockCoverable implements IShapedBlock {

    public static final int TRIGGER_PLAYER = 0;
    public static final int TRIGGER_MONSTER = 1;
    public static final int TRIGGER_ANIMAL = 2;
    public static final int TRIGGER_ALL = 3;

    private static final int PRESSED_BIT = 0x8;
    private static final int POLARITY_BIT = 0x10;
    private static final int TRIGGER_MASK = 0x60;
    private static final int TRIGGER_SHIFT = 5;

    private static final int TICK_RATE = 20;

    private static final AABB PLATE = new AABB(0.0625, 0, 0.0625, 0.9375, 0.0625, 0.9375);
    private static final AABB PLATE_PRESSED = new AABB(0.0625, 0, 0.0625, 0.9375, 0.03125, 0.9375);

    public BlockCarpentersPressurePlate(Properties properties) {
        super(properties);
    }

    private static boolean isPressed(int data) {
        return (data & PRESSED_BIT) != 0;
    }

    private static boolean isInverted(int data) {
        return (data & POLARITY_BIT) != 0;
    }

    private static int getTrigger(int data) {
        return (data & TRIGGER_MASK) >> TRIGGER_SHIFT;
    }

    private static int getPowerOutput(int data) {
        return isPressed(data) != isInverted(data) ? 15 : 0;
    }

    ////////////
    // Shape
    ////////////

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return Collections.singletonList(isPressed(renderData) ? PLATE_PRESSED : PLATE);
    }

    @Override
    public List<AABB> getCollisionBoxes(int renderData) {
        return Collections.emptyList();
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState blockState) {
        return true;
    }

    ////////////
    // Placement: on the floor, like vanilla pressure plates
    ////////////

    private static boolean hasSupport(LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) || Block.canSupportCenter(level, below, Direction.UP);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return hasSupport(context.getLevel(), context.getClickedPos()) ? super.getStateForPlacement(context) : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (!level.isClientSide() && blockEntity != null) {
            blockEntity.setCbMetadata((blockEntity.getCbMetadata() & ~0x7) | Direction.UP.ordinal());
        }
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(blockState, level, pos, block, orientation, movedByPiston);
        if (!level.isClientSide() && level.getBlockState(pos).is(this) && !hasSupport(level, pos)) {
            breakBlock(level, pos);
        }
    }

    ////////////
    // Pressing
    ////////////

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level.isClientSide()) {
            return;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity != null && canEntityTrigger(blockEntity.getCbMetadata(), entity)) {
            if (!isPressed(blockEntity.getCbMetadata())) {
                setPressed(blockEntity, true);
            }
            level.scheduleTick(pos, this, TICK_RATE);
        }
    }

    /**
     * Releases the plate once nothing that triggers it is on it.
     */
    @Override
    protected void tick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null || !isPressed(blockEntity.getCbMetadata())) {
            return;
        }
        if (hasTriggerInBounds(level, pos, blockEntity.getCbMetadata())) {
            level.scheduleTick(pos, this, TICK_RATE);
        } else {
            setPressed(blockEntity, false);
        }
    }

    private void setPressed(CbBlockEntity blockEntity, boolean pressed) {
        int data = blockEntity.getCbMetadata();
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        // Wool covered plates press quietly, like 1.12.2
        BlockState cover = blockEntity.getAttributeHelper().getBlockState(EnumAttributeLocation.HOST, EnumAttributeType.COVER);
        if (cover == null || !cover.is(BlockTags.WOOL)) {
            level.playSound(null, pos, pressed ? SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON : SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF,
                SoundSource.BLOCKS, 0.3F, pressed ? 0.6F : 0.5F);
        }
        blockEntity.setCbMetadata(pressed ? data | PRESSED_BIT : data & ~PRESSED_BIT);
        notifyPowerChange(level, pos);
    }

    /**
     * Whether something that triggers the plate is in its block space.
     */
    private boolean hasTriggerInBounds(Level level, BlockPos pos, int data) {
        for (Entity entity : level.getEntities((Entity) null, new AABB(pos))) {
            if (canEntityTrigger(data, entity)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canEntityTrigger(int data, Entity entity) {
        return switch (getTrigger(data)) {
            case TRIGGER_ANIMAL -> entity.getType().getCategory() == MobCategory.CREATURE;
            case TRIGGER_MONSTER -> entity.getType().getCategory() == MobCategory.MONSTER;
            case TRIGGER_PLAYER -> entity instanceof Player;
            default -> !entity.isSpectator();
        };
    }

    ////////////
    // Redstone
    ////////////

    private void notifyPowerChange(Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.below(), this);
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        return blockEntity != null ? getPowerOutput(blockEntity.getCbMetadata()) : 0;
    }

    /**
     * Strong power goes into the block below.
     */
    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return direction == Direction.UP ? getSignal(blockState, level, pos, direction) : 0;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(blockState, level, pos, movedByPiston);
        notifyPowerChange(level, pos);
    }

    ////////////
    // Hammer
    ////////////

    /**
     * Hammer left click: inverts the output.
     */
    @Override
    protected boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        int data = blockEntity.getCbMetadata() ^ POLARITY_BIT;
        blockEntity.setCbMetadata(data);
        notifyPowerChange(blockEntity.getLevel(), blockEntity.getBlockPos());
        // 1.12.2 named the new polarity the other way round; this names it as it is
        sendMessage(player, isInverted(data) ? "message.carpentersblocks.polarity_neg" : "message.carpentersblocks.polarity_pos");
        return true;
    }

    /**
     * Hammer right click: next trigger.
     */
    @Override
    protected boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        int data = blockEntity.getCbMetadata();
        int trigger = (getTrigger(data) + 1) % 4;
        data = (data & ~TRIGGER_MASK) | (trigger << TRIGGER_SHIFT);
        blockEntity.setCbMetadata(data);
        notifyPowerChange(blockEntity.getLevel(), blockEntity.getBlockPos());
        String[] messages = { "message.carpentersblocks.trigger_player", "message.carpentersblocks.trigger_monster",
            "message.carpentersblocks.trigger_animal", "message.carpentersblocks.trigger_all" };
        sendMessage(player, messages[trigger]);
        return true;
    }

}
