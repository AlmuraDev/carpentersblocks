package com.carpentersblocks.block;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.CbConfig;
import com.carpentersblocks.block.data.LadderData;
import com.carpentersblocks.blockentity.CbBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

/**
 * Carpenter's Ladder: wall or freestanding ladders in three styles. Climbable through the minecraft:climbable tag.
 */
public class BlockCarpentersLadder extends BlockCoverable implements IShapedBlock {

    // Direction picked in getStateForPlacement, applied in setPlacedBy once the block entity exists
    private static final ThreadLocal<Integer> _threadLocalPlacementDir = new ThreadLocal<>();

    public BlockCarpentersLadder(Properties properties) {
        super(properties);
    }

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return LadderData.getBoxes(renderData);
    }

    @Override
    public List<ShapePart> getRenderParts(int renderData) {
        return LadderData.getParts(renderData);
    }

    @Override
    public List<AABB> getCollisionBoxes(int renderData) {
        return Collections.singletonList(LadderData.getBounds(renderData));
    }

    @Override
    public List<AABB> getSelectionBoxes(int renderData) {
        return Collections.singletonList(LadderData.getBounds(renderData));
    }

    /**
     * Adds joins with adjacent ladders facing the same way, so their stiles merge.
     */
    @Override
    public int getRenderData(BlockGetter level, BlockPos pos, int cbMetadata) {
        int data = cbMetadata;
        Direction lowSide = LadderData.getLowSide(data);
        int dir = LadderData.getDirection(data);
        if (isLadderWithDirection(level, pos.relative(lowSide), dir)) {
            data |= LadderData.JOIN_LOW;
        }
        if (isLadderWithDirection(level, pos.relative(lowSide.getOpposite()), dir)) {
            data |= LadderData.JOIN_HIGH;
        }
        return data;
    }

    private boolean isLadderWithDirection(BlockGetter level, BlockPos pos, int dir) {
        return level.getBlockState(pos).is(this) && LadderData.getDirection(getCbMetadataAt(level, pos)) == dir;
    }

    /**
     * Wall ladders need a solid face behind them; freestanding ladders stand on a solid face or stack on other ladders.
     */
    private boolean canPlaceOnSide(Level level, BlockPos pos, Direction facing) {
        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState support = level.getBlockState(supportPos);
        if (support.isFaceSturdy(level, supportPos, facing)) {
            return true;
        }
        return facing.getAxis().isVertical() && support.is(this);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (!canPlaceOnSide(context.getLevel(), context.getClickedPos(), facing)) {
            return null;
        }
        int dir;
        if (facing.getAxis().isHorizontal()) {
            dir = facing.get3DDataValue();
        } else {
            // Freestanding, across the player's view
            dir = context.getHorizontalDirection().getAxis() == Direction.Axis.X ? LadderData.DIR_ON_Z : LadderData.DIR_ON_X;
        }
        _threadLocalPlacementDir.set(dir);
        return super.getStateForPlacement(context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        Integer placementDir = _threadLocalPlacementDir.get();
        _threadLocalPlacementDir.remove();
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (level.isClientSide() || blockEntity == null) {
            return;
        }
        int data = LadderData.setDirection(0, placementDir != null ? placementDir : LadderData.DIR_ON_X);
        // Unless sneaking, continue the ladder above or below (below wins)
        if (placer == null || !placer.isShiftKeyDown()) {
            for (BlockPos adjacentPos : new BlockPos[] { pos.above(), pos.below() }) {
                CbBlockEntity adjacent = getBlockEntity(level, adjacentPos);
                if (adjacent != null) {
                    data = adjacent.getCbMetadata();
                }
            }
        }
        blockEntity.setCbMetadata(data);
    }

    /**
     * Wall ladders break when the block behind them is removed.
     */
    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(blockState, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide() || CbConfig.ENABLE_FREESTANDING_LADDERS.get() || !level.getBlockState(pos).is(this)) {
            return;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null || LadderData.isFreestanding(blockEntity.getCbMetadata())) {
            return;
        }
        Direction facing = LadderData.getFacing(blockEntity.getCbMetadata());
        BlockPos supportPos = pos.relative(facing.getOpposite());
        if (!level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing)) {
            breakBlock(level, pos);
        }
    }

    /**
     * Hammer left click: next ladder type.
     */
    @Override
    public boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        return blockEntity.setCbMetadata(LadderData.getNextType(blockEntity.getCbMetadata(), true));
    }

    /**
     * Hammer right click: previous ladder type.
     */
    @Override
    public boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        return blockEntity.setCbMetadata(LadderData.getNextType(blockEntity.getCbMetadata(), false));
    }

}
