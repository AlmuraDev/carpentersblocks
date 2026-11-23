package com.carpentersblocks.block;

import java.util.Collections;
import java.util.List;

import com.carpentersblocks.block.data.RedstoneData;
import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.blockentity.CbBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Carpenter's Button, ported from 1.7.10: pressed for one second, any face, invertible polarity.
 */
public class BlockCarpentersButton extends BlockSidedShaped {

    private static final int PRESS_TICKS = 20;

    public BlockCarpentersButton(Properties properties) {
        super(properties);
    }

    @Override
    public Direction getAttachSide(int data) {
        return RedstoneData.getSide(data);
    }

    @Override
    public int setAttachSide(int data, Direction side) {
        return RedstoneData.setSide(data, side);
    }

    @Override
    protected int getPowerOutput(int data) {
        return RedstoneData.getPowerOutput(data);
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    private static AABB getBox(int data) {
        double depth = RedstoneData.isOn(data) ? 0.0625 : 0.125;
        return SidedUtil.rotate(new AABB(0.3125, 0.375, 0, 0.6875, 0.625, depth), RedstoneData.getSide(data));
    }

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        if (renderData == -1) {
            return Collections.singletonList(new AABB(0.3125, 0.375, 0.375, 0.6875, 0.625, 0.625));
        }
        return Collections.singletonList(getBox(renderData));
    }

    @Override
    public List<AABB> getCollisionBoxes(int renderData) {
        return Collections.emptyList();
    }

    @Override
    public int getInventoryRenderData(int itemDamage) {
        return -1;
    }

    @Override
    public boolean wouldActivateBlock(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult) {
        return !RedstoneData.isOn(blockEntity.getCbMetadata());
    }

    /**
     * Presses the button.
     */
    @Override
    public void postOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {
        int data = blockEntity.getCbMetadata();
        if (RedstoneData.isOn(data)) {
            return;
        }
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        data = RedstoneData.setOn(data, true);
        blockEntity.setCbMetadata(data);
        playClick(level, pos, true);
        notifyPowerChange(level, pos, data);
        level.scheduleTick(pos, this, PRESS_TICKS);
        actionResult.setAltered().setNoSound();
    }

    /**
     * Releases the button.
     */
    @Override
    protected void tick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null || !RedstoneData.isOn(blockEntity.getCbMetadata())) {
            return;
        }
        int data = RedstoneData.setOn(blockEntity.getCbMetadata(), false);
        blockEntity.setCbMetadata(data);
        playClick(level, pos, false);
        notifyPowerChange(level, pos, data);
    }

    private static void playClick(Level level, BlockPos pos, boolean on) {
        level.playSound(null, pos, on ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.3F, on ? 0.6F : 0.5F);
    }

    /**
     * Hammer left click: inverts the redstone output.
     */
    @Override
    public boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        int data = RedstoneData.togglePolarity(blockEntity.getCbMetadata());
        blockEntity.setCbMetadata(data);
        notifyPowerChange(blockEntity.getLevel(), blockEntity.getBlockPos(), data);
        sendMessage(player, RedstoneData.isInverted(data) ? "message.carpentersblocks.polarity_neg" : "message.carpentersblocks.polarity_pos");
        return true;
    }

}
