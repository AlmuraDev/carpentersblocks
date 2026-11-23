package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.block.data.DoorData;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.registry.CbItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Carpenter's Door, ported from 1.7.10 (BlockHinged, BlockCarpentersDoor): two blocks high, placed with
 * {@link com.carpentersblocks.item.ItemCarpentersDoor}, pairs into double doors. Its loot table drops the door item.
 */
public class BlockCarpentersDoor extends BlockCoverable implements IShapedBlock {

    public BlockCarpentersDoor(Properties properties) {
        super(properties);
    }

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return Collections.singletonList(DoorData.getBounds(renderData));
    }

    @Override
    public List<ShapePart> getRenderParts(int renderData) {
        return DoorData.getParts(renderData);
    }

    ////////////
    // Door pieces
    ////////////

    @Nullable
    private CbBlockEntity getDoor(Level level, BlockPos pos) {
        return getBlockEntity(level, pos);
    }

    /**
     * Gets the door paired with this one into a double door: on the side away from the hinge, same piece and facing,
     * opposite hinge.
     */
    @Nullable
    private CbBlockEntity getPairedDoor(Level level, BlockPos pos, int data) {
        Direction facing = DoorData.getFacing(data);
        boolean right = DoorData.isHingeRight(data);
        CbBlockEntity pair = getDoor(level, pos.relative(right ? facing.getCounterClockWise() : facing.getClockWise()));
        if (pair != null) {
            int pairData = pair.getCbMetadata();
            if (DoorData.isTop(pairData) == DoorData.isTop(data) && DoorData.getFacing(pairData) == facing && DoorData.isHingeRight(pairData) != right) {
                return pair;
            }
        }
        return null;
    }

    /**
     * Gets this door piece, its other half, and both halves of a paired door.
     */
    private List<CbBlockEntity> getPieces(CbBlockEntity blockEntity) {
        List<CbBlockEntity> pieces = new ArrayList<>();
        Level level = blockEntity.getLevel();
        addHalves(blockEntity, pieces);
        CbBlockEntity pair = getPairedDoor(level, blockEntity.getBlockPos(), blockEntity.getCbMetadata());
        if (pair != null) {
            addHalves(pair, pieces);
        }
        return pieces;
    }

    private void addHalves(CbBlockEntity blockEntity, List<CbBlockEntity> pieces) {
        pieces.add(blockEntity);
        CbBlockEntity other = getOtherHalf(blockEntity);
        if (other != null) {
            pieces.add(other);
        }
    }

    @Nullable
    private CbBlockEntity getOtherHalf(CbBlockEntity blockEntity) {
        boolean top = DoorData.isTop(blockEntity.getCbMetadata());
        return getDoor(blockEntity.getLevel(), top ? blockEntity.getBlockPos().below() : blockEntity.getBlockPos().above());
    }

    /**
     * Copies the shared door properties to the other half.
     */
    private void updateOtherHalf(CbBlockEntity blockEntity) {
        CbBlockEntity other = getOtherHalf(blockEntity);
        if (other != null) {
            other.setCbMetadata(DoorData.setTop(blockEntity.getCbMetadata(), DoorData.isTop(other.getCbMetadata())));
        }
    }

    private void setOpen(CbBlockEntity clicked, boolean open) {
        for (CbBlockEntity piece : getPieces(clicked)) {
            piece.setCbMetadata(DoorData.setOpen(piece.getCbMetadata(), open));
        }
        boolean rigid = DoorData.isRigid(clicked.getCbMetadata());
        Level level = clicked.getLevel();
        level.playSound(null, clicked.getBlockPos(),
            rigid ? (open ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE) : (open ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE),
            SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    ////////////
    // Interaction
    ////////////

    @Override
    protected boolean wouldActivateBlock(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult) {
        return !DoorData.isRigid(blockEntity.getCbMetadata());
    }

    @Override
    protected void postOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {
        int data = blockEntity.getCbMetadata();
        if (!DoorData.isRigid(data)) {
            setOpen(blockEntity, !DoorData.isOpen(data));
            actionResult.setAltered().setNoSound();
        }
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(blockState, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide() || !level.getBlockState(pos).is(this)) {
            return;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return;
        }
        int data = blockEntity.getCbMetadata();
        // Pieces without their other half go away (only the broken half drops the door); the bottom drops the door
        // without a floor
        if (DoorData.isTop(data)) {
            if (getDoor(level, pos.below()) == null) {
                level.removeBlock(pos, false);
                return;
            }
        } else {
            if (getDoor(level, pos.above()) == null) {
                level.removeBlock(pos, false);
                return;
            }
            BlockPos floor = pos.below();
            if (!level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)) {
                level.removeBlock(pos.above(), false);
                breakBlock(level, pos);
                return;
            }
        }
        // Redstone opens and closes wooden and rigid doors alike
        if (block.defaultBlockState().isSignalSource()) {
            boolean powered = false;
            for (CbBlockEntity piece : getPieces(blockEntity)) {
                powered |= level.hasNeighborSignal(piece.getBlockPos());
            }
            if (powered != DoorData.isOpen(data)) {
                setOpen(blockEntity, powered);
            }
        }
    }

    /**
     * Hammer left click: moves the hinge to the other side.
     */
    @Override
    protected boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        int data = blockEntity.getCbMetadata();
        blockEntity.setCbMetadata(DoorData.setHingeRight(data, !DoorData.isHingeRight(data)));
        updateOtherHalf(blockEntity);
        return true;
    }

    /**
     * Hammer right click: next type, or toggles redstone only when sneaking.
     */
    @Override
    protected boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        int data = blockEntity.getCbMetadata();
        if (player.isShiftKeyDown()) {
            boolean rigid = !DoorData.isRigid(data);
            for (CbBlockEntity piece : getPieces(blockEntity)) {
                piece.setCbMetadata(DoorData.setRigid(piece.getCbMetadata(), rigid));
            }
            sendMessage(player, rigid ? "message.carpentersblocks.activation_iron" : "message.carpentersblocks.activation_wood");
            return true;
        }
        blockEntity.setCbMetadata(DoorData.setType(data, (DoorData.getType(data) + 1) % DoorData.NUM_TYPES));
        updateOtherHalf(blockEntity);
        return true;
    }

    /**
     * The door block has no item of its own: pick the door item.
     */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState blockState, boolean includeData, Player player) {
        return CbItems.CARPENTERS_DOOR != null ? new ItemStack(CbItems.CARPENTERS_DOOR.get()) : ItemStack.EMPTY;
    }

}
