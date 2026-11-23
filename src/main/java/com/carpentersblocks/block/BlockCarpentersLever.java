package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.block.data.RedstoneData;
import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.blockentity.CbBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Carpenter's Lever, ported from 1.7.10: toggles, any face, invertible polarity.
 */
public class BlockCarpentersLever extends BlockSidedShaped {

    private static final double HANDLE_TILT = 2.0D * Math.PI / 9.0D;

    public BlockCarpentersLever(Properties properties) {
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

    /**
     * Floor and ceiling levers point across the player's view.
     */
    @Override
    protected int onSidedBlockPlaced(int data, @Nullable LivingEntity placer) {
        Direction side = RedstoneData.getSide(data);
        boolean axisZ = side.getAxis().isVertical()
            ? placer != null && placer.getDirection().getAxis() == Direction.Axis.Z
            : side.getAxis() == Direction.Axis.Z;
        return RedstoneData.setAxisZ(data, axisZ);
    }

    private static AABB getBase(int data) {
        Direction side = RedstoneData.getSide(data);
        if (side.getAxis().isVertical()) {
            double y0 = side == Direction.UP ? 0 : 0.8125;
            double y1 = y0 + 0.1875;
            return RedstoneData.isAxisZ(data)
                ? new AABB(0.3125, y0, 0.2, 0.6875, y1, 0.8)
                : new AABB(0.2, y0, 0.3125, 0.8, y1, 0.6875);
        }
        return SidedUtil.rotate(new AABB(0.3125, 0.2, 0, 0.6875, 0.8, 0.1875), side);
    }

    /**
     * Gets the tilted handle (1.7.10 BlockHandlerCarpentersLever).
     */
    private static Vec3[] getHandle(int data) {
        Direction side = RedstoneData.getSide(data);
        boolean on = RedstoneData.isOn(data);
        Vec3[] corners = SidedUtil.centeredCuboid(0.0625, 0, 0.625);
        corners = SidedUtil.transform(corners, vec -> new Vec3(vec.x, vec.y, vec.z + (on ? -0.0625 : 0.0625)).xRot((float) (on ? HANDLE_TILT : -HANDLE_TILT)));
        if (side.getAxis().isVertical()) {
            boolean axisX = !RedstoneData.isAxisZ(data);
            double yOffset = side == Direction.UP ? 0.125 : 0.875;
            return SidedUtil.transform(corners, vec -> {
                if (side == Direction.DOWN) {
                    vec = new Vec3(-vec.x, -vec.y, vec.z);
                }
                if (axisX) {
                    vec = vec.yRot((float) (Math.PI / 2));
                }
                return vec.add(0.5, yOffset, 0.5);
            });
        }
        float yaw = side == Direction.NORTH ? 0 : side == Direction.SOUTH ? (float) Math.PI : side == Direction.WEST ? (float) (Math.PI / 2) : (float) (-Math.PI / 2);
        return SidedUtil.transform(corners, vec -> vec.add(0, -0.375, 0).xRot((float) (Math.PI / 2)).yRot(yaw).add(0.5, 0.5, 0.5));
    }

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return Collections.singletonList(getBase(renderData));
    }

    @Override
    public List<ShapePart> getRenderParts(int renderData) {
        List<ShapePart> parts = new ArrayList<>(2);
        parts.add(ShapePart.covered(getBase(renderData)));
        parts.add(ShapePart.cuboid(getHandle(renderData), ShapeTextures.LEVER, false));
        return parts;
    }

    @Override
    public List<AABB> getCollisionBoxes(int renderData) {
        return Collections.emptyList();
    }

    @Override
    public int getInventoryRenderData(int itemDamage) {
        return RedstoneData.setSide(0, Direction.UP);
    }

    @Override
    protected boolean wouldActivateBlock(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult) {
        return true;
    }

    /**
     * Toggles the lever.
     */
    @Override
    protected void postOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        int data = blockEntity.getCbMetadata();
        boolean on = !RedstoneData.isOn(data);
        data = RedstoneData.setOn(data, on);
        blockEntity.setCbMetadata(data);
        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, on ? 0.6F : 0.5F);
        notifyPowerChange(level, pos, data);
        actionResult.setAltered().setNoSound();
    }

    /**
     * Hammer left click: inverts the redstone output.
     */
    @Override
    protected boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        int data = RedstoneData.togglePolarity(blockEntity.getCbMetadata());
        blockEntity.setCbMetadata(data);
        notifyPowerChange(blockEntity.getLevel(), blockEntity.getBlockPos(), data);
        sendMessage(player, RedstoneData.isInverted(data) ? "message.carpentersblocks.polarity_neg" : "message.carpentersblocks.polarity_pos");
        return true;
    }

}
