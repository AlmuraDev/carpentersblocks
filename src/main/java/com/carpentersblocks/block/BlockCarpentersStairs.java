package com.carpentersblocks.block;

import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.CbConfig;
import com.carpentersblocks.block.data.StairsData;
import com.carpentersblocks.blockentity.CbBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Carpenter's Stairs: stairs in many shapes and directions, joining with adjacent stairs, with side covers.
 */
public class BlockCarpentersStairs extends BlockCoverable implements IShapedBlock {

    // Stairs ID picked in getStateForPlacement, applied in setPlacedBy once the block entity exists
    private static final ThreadLocal<Integer> _threadLocalPlacementId = new ThreadLocal<>();

    public BlockCarpentersStairs(Properties properties) {
        super(properties);
    }

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return StairsData.getBoxes(StairsData.getId(renderData));
    }

    @Override
    public int getInventoryRenderData(int itemDamage) {
        return StairsData.ID_POS_N;
    }

    @Override
    public boolean supportsSideCovers() {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Vec3 hit = context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos()));
        _threadLocalPlacementId.set(StairsData.getPlacementId(context.getClickedFace(), (float) hit.x, (float) hit.y, (float) hit.z, context.getHorizontalDirection()));
        return super.getStateForPlacement(context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        Integer placementId = _threadLocalPlacementId.get();
        _threadLocalPlacementId.remove();
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (level.isClientSide() || blockEntity == null) {
            return;
        }
        int id = placementId != null ? placementId : StairsData.ID_POS_N;
        // Sneaking skips connecting to adjacent stairs
        if (placer == null || !placer.isShiftKeyDown()) {
            StairsData.Neighbors neighbors = getNeighbors(level, pos);
            id = StairsData.transformStairs(id, neighbors);
            StairsData.Neighbors adjusted = StairsData.transformAdjacentStairs(id, neighbors);
            setStairsId(level, pos.west(), adjusted.xn);
            setStairsId(level, pos.east(), adjusted.xp);
            setStairsId(level, pos.north(), adjusted.zn);
            setStairsId(level, pos.south(), adjusted.zp);
        }
        blockEntity.setCbMetadata(id);
    }

    private StairsData.Neighbors getNeighbors(Level level, BlockPos pos) {
        StairsData.Neighbors neighbors = new StairsData.Neighbors();
        neighbors.xn = getStairsId(level, pos.west());
        neighbors.xp = getStairsId(level, pos.east());
        neighbors.yn = getStairsId(level, pos.below());
        neighbors.yp = getStairsId(level, pos.above());
        neighbors.zn = getStairsId(level, pos.north());
        neighbors.zp = getStairsId(level, pos.south());
        return neighbors;
    }

    /**
     * Gets the stairs ID at a position, or -1 if there are no Carpenter's Stairs.
     */
    private int getStairsId(Level level, BlockPos pos) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        return blockEntity != null ? StairsData.getId(blockEntity.getCbMetadata()) : -1;
    }

    private void setStairsId(Level level, BlockPos pos, int id) {
        CbBlockEntity blockEntity = id >= 0 ? getBlockEntity(level, pos) : null;
        if (blockEntity != null) {
            blockEntity.setCbMetadata(id);
        }
    }

    /**
     * Hammer left click: rotates the stairs.
     */
    @Override
    public boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        int id = StairsData.getId(blockEntity.getCbMetadata());
        return blockEntity.setCbMetadata(StairsData.getNextDirection(id));
    }

    /**
     * Hammer right click: changes the stairs type.
     */
    @Override
    public boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        int id = StairsData.getId(blockEntity.getCbMetadata());
        return blockEntity.setCbMetadata(StairsData.getNextType(id));
    }

    /**
     * Damages the hammer with a chance to not damage.
     */
    @Override
    public void damageItemWithChance(Level level, Player player, InteractionHand hand) {
        if (level.getRandom().nextFloat() <= CbConfig.HAMMER_DAMAGE_CHANCE_STAIRS.get()) {
            super.damageItemWithChance(level, player, hand);
        }
    }

}
