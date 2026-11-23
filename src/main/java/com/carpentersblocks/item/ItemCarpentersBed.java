package com.carpentersblocks.item;

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
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

/**
 * Places a Carpenter's Bed: the foot on the clicked block, the head away from the player, like 1.7.10.
 */
public class ItemCarpentersBed extends Item {

    public ItemCarpentersBed(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (context.getClickedFace() != Direction.UP || CbBlocks.CARPENTERS_BED == null || player == null) {
            return InteractionResult.FAIL;
        }
        Block bed = CbBlocks.CARPENTERS_BED.get();
        Direction look = player.getDirection();
        BlockPos footPos = context.getClickedPos().above();
        BlockPos headPos = footPos.relative(look);
        ItemStack itemStack = context.getItemInHand();
        if (!player.mayUseItemAt(footPos, Direction.UP, itemStack) || !player.mayUseItemAt(headPos, Direction.UP, itemStack)
                || !level.getBlockState(footPos).isAir() || !level.getBlockState(headPos).isAir()
                || !level.getBlockState(footPos.below()).isFaceSturdy(level, footPos.below(), Direction.UP)
                || !level.getBlockState(headPos.below()).isFaceSturdy(level, headPos.below(), Direction.UP)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Vanilla bed facing: from the foot toward the head
        BlockState footState = bed.defaultBlockState().setValue(AbstractBedBlock.FACING, look).setValue(AbstractBedBlock.PART, BedPart.FOOT);
        BlockState headState = footState.setValue(AbstractBedBlock.PART, BedPart.HEAD);
        level.setBlock(footPos, footState, Block.UPDATE_CLIENTS);
        level.setBlock(headPos, headState, Block.UPDATE_CLIENTS);
        bed.setPlacedBy(level, footPos, footState, player, itemStack);
        bed.setPlacedBy(level, headPos, headState, player, itemStack);
        level.updateNeighborsAt(footPos, bed);
        level.updateNeighborsAt(headPos, bed);

        SoundType soundType = footState.getSoundType(level, footPos, player);
        level.playSound(null, footPos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        itemStack.consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }

}
