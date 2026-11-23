package com.carpentersblocks.block;

import javax.annotation.Nullable;

import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.util.attribute.AttributeHelper;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import com.carpentersblocks.CbConfig;
import net.minecraft.util.TriState;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The base of most Carpenter's blocks: a plain block with the Carpenter's behavior ({@link ICoverableBlock}). It
 * forwards the vanilla methods to the interface, and keeps the shared helpers at hand for its subclasses.
 */
public abstract class BlockCoverable extends Block implements ICoverableBlock {

    public BlockCoverable(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
        return new CbBlockEntity(pos, blockState);
    }

    ////////////
    // Helpers for subclasses (see CoverableSupport)
    ////////////

    @Nullable
    protected static Direction getClickFace() {
        return CoverableSupport.getClickFace();
    }

    @Nullable
    protected static Vec3 getClickHit() {
        return CoverableSupport.getClickHit();
    }

    public static int getCbMetadataAt(BlockGetter level, BlockPos pos) {
        return CoverableSupport.getCbMetadataAt(level, pos);
    }

    public static AttributeHelper getAttributeHelperAt(BlockGetter level, BlockPos pos) {
        return CoverableSupport.getAttributeHelperAt(level, pos);
    }

    @Nullable
    protected static CbBlockEntity getSimpleBlockEntity(BlockGetter level, BlockPos pos) {
        return CoverableSupport.getSimpleBlockEntity(level, pos);
    }

    @Nullable
    protected static BlockState getCover(CbBlockEntity blockEntity, EnumAttributeLocation location) {
        return CoverableSupport.getCover(blockEntity, location);
    }

    protected static void breakBlock(Level level, BlockPos pos) {
        CoverableSupport.breakBlock(level, pos);
    }

    protected static void sendMessage(Player player, String key) {
        CoverableSupport.sendMessage(player, key);
    }

    public static boolean isCarpentersTool(ItemStack itemStack) {
        return CoverableSupport.isCarpentersTool(itemStack);
    }

    ////////////
    // Vanilla methods, forwarded to ICoverableBlock
    ////////////

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = coverableGetShape(level, pos);
        return shape != null ? shape : super.getShape(blockState, level, pos, context);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = coverableGetCollisionShape(level, pos);
        return shape != null ? shape : super.getCollisionShape(blockState, level, pos, context);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return coverableUseItemOn(itemStack, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return coverableUseWithoutItem(level, pos, player, hitResult);
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(blockState, level, pos, block, orientation, movedByPiston);
        coverableNeighborChanged(blockState, level, pos);
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return false;
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return coverableGetSignal(level, pos, direction);
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return coverableGetDirectSignal(level, pos, direction);
    }

    @Override
    public boolean hasDynamicLightEmission(BlockState blockState) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState blockState, BlockGetter level, BlockPos pos) {
        return coverableGetLightEmission(level, pos);
    }

    @Override
    protected float getDestroyProgress(BlockState blockState, Player player, BlockGetter level, BlockPos pos) {
        float progress = coverableGetDestroyProgress(player, level, pos);
        return Float.isNaN(progress) ? super.getDestroyProgress(blockState, player, level, pos) : progress;
    }

    @Override
    public SoundType getSoundType(BlockState blockState, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        SoundType soundType = coverableGetSoundType(level, pos);
        return soundType != null ? soundType : super.getSoundType(blockState, level, pos, entity);
    }

    @Override
    public BlockState getAppearance(BlockState blockState, BlockAndLightGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        return coverableGetAppearance(blockState, level, pos, side);
    }

    /**
     * Burns like wood (1.12.2: encouragement 5, flammability 20).
     */
    @Override
    public int getFlammability(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return 20;
    }

    @Override
    public int getFireSpreadSpeed(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        coverableSetPlacedBy(level, pos, placer);
    }


    @Override
    public TriState canSustainPlant(BlockState blockState, BlockGetter level, BlockPos soilPos, Direction facing, BlockState plant) {
        TriState decision = coverableCanSustainPlant(blockState, level, soilPos, facing, plant);
        return decision.isDefault() ? super.canSustainPlant(blockState, level, soilPos, facing, plant) : decision;
    }


    /**
     * With routable fluids, a fluid beside the block doesn't draw its side into the block space, where the block
     * draws the fluid's surface itself.
     */
    @Override
    public boolean shouldHideAdjacentFluidFace(BlockState blockState, Direction selfFace, FluidState adjacentFluid) {
        if (CbConfig.ENABLE_ROUTABLE_FLUIDS.get() && selfFace.getAxis().isHorizontal()) {
            return true;
        }
        return super.shouldHideAdjacentFluidFace(blockState, selfFace, adjacentFluid);
    }

}
