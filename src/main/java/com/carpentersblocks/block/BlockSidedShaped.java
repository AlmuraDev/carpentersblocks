package com.carpentersblocks.block;

import javax.annotation.Nullable;

import com.carpentersblocks.blockentity.CbBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;

/**
 * A shaped block attached to one face of the block next to it (1.7.10 BlockSided): buttons, levers, torches,
 * daylight sensors.
 * <p>
 * The attach side is the face that was clicked, pointing away from the supporting block. Redstone output is weak on
 * every side and strong into the supporting block.
 */
public abstract class BlockSidedShaped extends BlockCoverable implements IShapedBlock {

    // Clicked face picked in getStateForPlacement, applied in setPlacedBy once the block entity exists
    private static final ThreadLocal<Direction> _threadLocalAttachSide = new ThreadLocal<>();

    public BlockSidedShaped(Properties properties) {
        super(properties);
    }

    /**
     * Gets the side the block is attached to, from its data.
     */
    public abstract Direction getAttachSide(int data);

    /**
     * Sets the side the block is attached to.
     */
    public abstract int setAttachSide(int data, Direction side);

    /**
     * Whether the block can attach to a side.
     */
    protected boolean canAttachToSide(Direction side) {
        return true;
    }

    /**
     * Gets the redstone output, 0 to 15.
     */
    protected int getPowerOutput(int data) {
        return 0;
    }

    /**
     * Sets up the data of a newly placed block.
     *
     * @param data the data, with the attach side set
     * @param placer the placer
     * @return the data
     */
    protected int onSidedBlockPlaced(int data, @Nullable LivingEntity placer) {
        return data;
    }

    protected boolean isSupported(BlockGetter level, BlockPos pos, Direction side) {
        BlockPos supportPos = pos.relative(side.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, side);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        if (!canAttachToSide(side) || !isSupported(context.getLevel(), context.getClickedPos(), side)) {
            return null;
        }
        _threadLocalAttachSide.set(side);
        return super.getStateForPlacement(context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        Direction side = _threadLocalAttachSide.get();
        _threadLocalAttachSide.remove();
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (level.isClientSide() || blockEntity == null) {
            return;
        }
        if (side == null || !canAttachToSide(side)) {
            side = Direction.UP;
        }
        int data = onSidedBlockPlaced(setAttachSide(blockEntity.getCbMetadata(), side), placer);
        blockEntity.setCbMetadata(data);
        notifyPowerChange(level, pos, data);
    }

    /**
     * Breaks the block when its support is removed.
     */
    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(blockState, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide() || !level.getBlockState(pos).is(this)) {
            return;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity != null && !isSupported(level, pos, getAttachSide(blockEntity.getCbMetadata()))) {
            breakBlock(level, pos);
        }
    }

    /**
     * Updates the block and the supporting block after the redstone output changed.
     */
    protected void notifyPowerChange(Level level, BlockPos pos, int data) {
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.relative(getAttachSide(data).getOpposite()), this);
    }

    /**
     * Turns off the power when removed. The block data is gone by now, so every neighbor is updated, as any of them
     * may be the support.
     */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(blockState, level, pos, movedByPiston);
        if (isSignalSource(blockState)) {
            level.updateNeighborsAt(pos, this);
            for (Direction direction : Direction.values()) {
                level.updateNeighborsAt(pos.relative(direction), this);
            }
        }
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        return blockEntity != null ? getPowerOutput(blockEntity.getCbMetadata()) : 0;
    }

    /**
     * Strong power goes into the supporting block, like vanilla buttons.
     */
    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return 0;
        }
        int data = blockEntity.getCbMetadata();
        return direction == getAttachSide(data) ? getPowerOutput(data) : 0;
    }

}
