package com.carpentersblocks.block;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.block.data.BarrierData;
import com.carpentersblocks.block.data.GateData;
import com.carpentersblocks.blockentity.CbBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Carpenter's Gate, ported from 1.7.10: a fence gate in every barrier style, opening away from the player.
 */
public class BlockCarpentersGate extends BlockCoverable implements IShapedBlock {

    public BlockCarpentersGate(Properties properties) {
        super(properties);
    }

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return GateData.getBoxes(renderData);
    }

    @Override
    public List<AABB> getCollisionBoxes(int renderData) {
        return GateData.getCollisionBoxes(renderData);
    }

    @Override
    public List<AABB> getSelectionBoxes(int renderData) {
        return Collections.singletonList(GateData.getSelectionBox(renderData));
    }

    @Override
    public int getInventoryRenderData(int itemDamage) {
        return GateData.INVENTORY;
    }

    /**
     * Stacked gates merge into one tall gate.
     */
    @Override
    public int getRenderData(BlockGetter level, BlockPos pos, int cbMetadata) {
        int data = cbMetadata;
        if (level.getBlockState(pos.below()).is(this)) {
            data |= GateData.GATE_BELOW;
        }
        if (level.getBlockState(pos.above()).is(this)) {
            data |= GateData.GATE_ABOVE;
        }
        return data;
    }

    /**
     * Gates stand on something solid.
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos below = context.getClickedPos().below();
        if (context.getLevel().getBlockState(below).getCollisionShape(context.getLevel(), below).isEmpty()) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    /**
     * Faces across the player's view, and takes the type of an adjacent gate or barrier.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (level.isClientSide() || blockEntity == null) {
            return;
        }
        int data = GateData.setOnZ(0, placer != null && placer.getDirection().getAxis() == Direction.Axis.X);
        for (Direction facing : Direction.values()) {
            BlockPos adjacentPos = pos.relative(facing);
            Block block = level.getBlockState(adjacentPos).getBlock();
            if (block == this || block instanceof BlockCarpentersBarrier) {
                CbBlockEntity adjacent = getSimpleBlockEntity(level, adjacentPos);
                if (adjacent != null) {
                    data = GateData.setType(data, BarrierData.getType(adjacent.getCbMetadata()));
                }
            }
        }
        blockEntity.setCbMetadata(data);
    }

    @Override
    protected boolean wouldActivateBlock(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult) {
        return true;
    }

    /**
     * Opens away from the player, or closes.
     */
    @Override
    protected void postOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {
        int data = blockEntity.getCbMetadata();
        if (GateData.isOpen(data)) {
            data = GateData.setOpen(data, false);
        } else {
            Direction look = player.getDirection();
            boolean negative = GateData.isOnZ(data) ? look != Direction.EAST : look != Direction.SOUTH;
            data = GateData.setOpen(GateData.setOpensNegative(data, negative), true);
        }
        setGate(blockEntity, data, true);
        actionResult.setAltered().setNoSound();
    }

    /**
     * Sets the gate, and the gate stacked on it (below first, else above) if it faces the same way.
     */
    private void setGate(CbBlockEntity blockEntity, int data, boolean playSound) {
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        blockEntity.setCbMetadata(data);
        if (playSound) {
            boolean open = GateData.isOpen(data);
            level.playSound(null, pos, open ? SoundEvents.FENCE_GATE_OPEN : SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }
        BlockPos neighborPos = level.getBlockState(pos.below()).is(this) ? pos.below() : pos.above();
        CbBlockEntity neighbor = getBlockEntity(level, neighborPos);
        if (neighbor != null && GateData.isOnZ(neighbor.getCbMetadata()) == GateData.isOnZ(data)) {
            int neighborData = GateData.setOpen(GateData.setOpensNegative(neighbor.getCbMetadata(), GateData.opensNegative(data)), GateData.isOpen(data));
            neighbor.setCbMetadata(neighborData);
        }
    }

    /**
     * Redstone opens and closes the gate.
     */
    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(blockState, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered || block.defaultBlockState().isSignalSource()) {
            int data = blockEntity.getCbMetadata();
            if (powered != GateData.isOpen(data)) {
                setGate(blockEntity, GateData.setOpen(data, powered), true);
            }
        }
    }

    /**
     * Hammer right click: next style, or the next vanilla variant when sneaking.
     */
    @Override
    protected boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        int data = blockEntity.getCbMetadata();
        int type = GateData.getType(data);
        if (player.isShiftKeyDown()) {
            if (type > BarrierData.TYPE_VANILLA_X3) {
                return true;
            }
            type = (type + 1) % (BarrierData.TYPE_VANILLA_X3 + 1);
        } else {
            type = type <= BarrierData.TYPE_VANILLA_X3 ? BarrierData.TYPE_PICKET : (type + 1) % (BarrierData.TYPE_WALL + 1);
        }
        return blockEntity.setCbMetadata(GateData.setType(data, type));
    }

}
