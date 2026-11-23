package com.carpentersblocks.item;

import com.carpentersblocks.block.data.DoorData;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.block.BlockCarpentersDoor;
import com.carpentersblocks.registry.CbBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Places a Carpenter's Door (both halves) on top of the clicked block, like 1.7.10.
 */
public class ItemCarpentersDoor extends Item {

    public ItemCarpentersDoor(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();
        if (context.getClickedFace() != Direction.UP || CbBlocks.CARPENTERS_DOOR == null || player == null) {
            return InteractionResult.FAIL;
        }
        BlockCarpentersDoor door = CbBlocks.CARPENTERS_DOOR.get();
        BlockPos bottomPos = clickedPos.above();
        BlockPos topPos = bottomPos.above();
        ItemStack itemStack = context.getItemInHand();
        if (topPos.getY() > level.getMaxY()
                || !player.mayUseItemAt(bottomPos, Direction.UP, itemStack) || !player.mayUseItemAt(topPos, Direction.UP, itemStack)
                || !level.getBlockState(bottomPos).isAir() || !level.getBlockState(topPos).isAir()
                || !level.getBlockState(clickedPos).isFaceSturdy(level, clickedPos, Direction.UP)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Direction facing = player.getDirection();
        int data = DoorData.setFacing(0, facing);
        // Hinge on the right next to a left hinged door facing the same way: a double door
        int leftData = getDoorData(level, bottomPos.relative(facing.getCounterClockWise()));
        boolean hingeRight = leftData >= 0 && !DoorData.isTop(leftData) && DoorData.getFacing(leftData) == facing && !DoorData.isHingeRight(leftData);
        data = DoorData.setHingeRight(data, hingeRight);
        // Take type and redstone behavior from an adjacent door
        for (BlockPos adjacentPos : new BlockPos[] { bottomPos.west(), bottomPos.east(), bottomPos.north(), bottomPos.south() }) {
            int adjacentData = getDoorData(level, adjacentPos);
            if (adjacentData >= 0) {
                data = DoorData.setRigid(DoorData.setType(data, DoorData.getType(adjacentData)), DoorData.isRigid(adjacentData));
                break;
            }
        }

        // Place both halves before either checks for the other
        BlockState doorState = door.defaultBlockState();
        level.setBlock(bottomPos, door.withWater(doorState, level, bottomPos), Block.UPDATE_CLIENTS);
        level.setBlock(topPos, door.withWater(doorState, level, topPos), Block.UPDATE_CLIENTS);
        setPiece(level, bottomPos, player, itemStack, DoorData.setTop(data, false));
        setPiece(level, topPos, player, itemStack, DoorData.setTop(data, true));
        level.updateNeighborsAt(bottomPos, door);
        level.updateNeighborsAt(topPos, door);

        SoundType soundType = doorState.getSoundType(level, bottomPos, player);
        level.playSound(null, bottomPos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        itemStack.consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void setPiece(Level level, BlockPos pos, Player player, ItemStack itemStack, int data) {
        BlockState blockState = level.getBlockState(pos);
        blockState.getBlock().setPlacedBy(level, pos, blockState, player, itemStack);
        if (level.getBlockEntity(pos) instanceof CbBlockEntity blockEntity) {
            blockEntity.setCbMetadata(data);
        }
    }

    /**
     * Gets door data at a position, or -1 if there is no Carpenter's Door.
     */
    private static int getDoorData(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).is(CbBlocks.CARPENTERS_DOOR.get())) {
            return -1;
        }
        return level.getBlockEntity(pos) instanceof CbBlockEntity blockEntity ? blockEntity.getCbMetadata() : -1;
    }

}
