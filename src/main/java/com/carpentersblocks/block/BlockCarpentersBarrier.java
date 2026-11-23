package com.carpentersblocks.block;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.block.data.BarrierData;
import com.carpentersblocks.blockentity.CbBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Carpenter's Barrier: fences and walls in several styles, connecting to each other, gates and solid faces.
 */
public class BlockCarpentersBarrier extends BlockCoverable implements IShapedBlock {

    /** How far up and down a post on a stacked barrier shows through the column. */
    private static final int MAX_POST_SCAN = 16;

    /** What torches and the like stand on: the post, whether drawn or not (1.12.2: torches always fit on top). */
    private static final VoxelShape SUPPORT_SHAPE = Shapes.box(0.375, 0, 0.375, 0.625, 1, 0.625);

    public BlockCarpentersBarrier(Properties properties) {
        super(properties);
    }

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return BarrierData.getBoxes(renderData);
    }

    @Override
    public List<AABB> getCollisionBoxes(int renderData) {
        return BarrierData.getCollisionBoxes(renderData);
    }

    @Override
    public List<AABB> getSelectionBoxes(int renderData) {
        return Collections.singletonList(BarrierData.getSelectionBox(renderData));
    }

    @Override
    public int getInventoryRenderData(int itemDamage) {
        return BarrierData.INVENTORY;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter level, BlockPos pos) {
        return SUPPORT_SHAPE;
    }

    /**
     * Adds the connections, stacked barriers and post visibility the shape depends on.
     */
    @Override
    public int getRenderData(BlockGetter level, BlockPos pos, int cbMetadata) {
        int data = getConnectionData(level, pos, cbMetadata);
        BlockPos above = pos.above();
        if (canConnectTo(level, above, Direction.DOWN, false)) {
            data |= BarrierData.CONNECT_ABOVE;
        }
        if (level.getBlockState(pos.below()).is(this)) {
            data |= BarrierData.BARRIER_BELOW;
        }
        if (level.getBlockState(above).is(this)) {
            data |= BarrierData.BARRIER_ABOVE;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (canConnectTo(level, above.relative(side), side.getOpposite(), false)) {
                data |= BarrierData.connectUp(side);
            }
        }
        if (isPostShown(level, pos, data)) {
            data |= BarrierData.SHOW_POST;
        }
        return data;
    }

    /**
     * Gets the block data with the horizontal connections added.
     */
    private int getConnectionData(BlockGetter level, BlockPos pos, int data) {
        // A forced post only connects to other barriers
        boolean barriersOnly = BarrierData.hasPostBit(data);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (canConnectTo(level, pos.relative(side), side.getOpposite(), barriersOnly)) {
                data |= BarrierData.connect(side);
            }
        }
        return data;
    }

    /**
     * Barriers connect to other barriers, gates and solid faces.
     *
     * @param pos the neighbor position
     * @param face the neighbor face pointing back at the barrier
     */
    private boolean canConnectTo(BlockGetter level, BlockPos pos, Direction face, boolean barriersOnly) {
        BlockState blockState = level.getBlockState(pos);
        if (blockState.is(this) || blockState.getBlock() instanceof BlockCarpentersGate) {
            return true;
        }
        return !barriersOnly && blockState.isFaceSturdy(level, pos, face);
    }

    /**
     * A post shows when this barrier needs one, or any barrier stacked directly above or below does.
     */
    private boolean isPostShown(BlockGetter level, BlockPos pos, int renderData) {
        if (needsPost(level, pos, renderData)) {
            return true;
        }
        for (Direction vertical : new Direction[] { Direction.UP, Direction.DOWN }) {
            BlockPos scanPos = pos.relative(vertical);
            for (int i = 0; i < MAX_POST_SCAN && level.getBlockState(scanPos).is(this); ++i) {
                if (needsPost(level, scanPos, getConnectionData(level, scanPos, getCbMetadataAt(level, scanPos)))) {
                    return true;
                }
                scanPos = scanPos.relative(vertical);
            }
        }
        return false;
    }

    private boolean needsPost(BlockGetter level, BlockPos pos, int connectionData) {
        // A torch or lantern on top, or a gate alongside, needs a post
        BlockState above = level.getBlockState(pos.above());
        boolean postNeeded = above.getBlock() instanceof TorchBlock || above.getBlock() instanceof LanternBlock
            || above.getBlock() instanceof BlockCarpentersTorch;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            postNeeded |= level.getBlockState(pos.relative(side)).getBlock() instanceof BlockCarpentersGate;
        }
        return BarrierData.hasPost(connectionData, postNeeded);
    }

    /**
     * Takes the type of an adjacent barrier.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (level.isClientSide() || blockEntity == null) {
            return;
        }
        int type = BarrierData.TYPE_VANILLA;
        for (Direction facing : Direction.values()) {
            CbBlockEntity adjacent = getBlockEntity(level, pos.relative(facing));
            if (adjacent != null) {
                type = BarrierData.getType(adjacent.getCbMetadata());
            }
        }
        blockEntity.setCbMetadata(BarrierData.setType(blockEntity.getCbMetadata(), type));
    }

    /**
     * Hammer left click: toggles the post.
     */
    @Override
    protected boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        return blockEntity.setCbMetadata(BarrierData.togglePost(blockEntity.getCbMetadata()));
    }

    /**
     * Hammer right click: next type, or next vanilla variant when sneaking.
     */
    @Override
    protected boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        int data = blockEntity.getCbMetadata();
        int next = player.isShiftKeyDown() ? BarrierData.getNextVanillaType(data) : BarrierData.getNextType(data);
        return blockEntity.setCbMetadata(next);
    }

}
