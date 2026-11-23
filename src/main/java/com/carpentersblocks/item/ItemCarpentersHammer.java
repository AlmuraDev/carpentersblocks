package com.carpentersblocks.item;

import com.carpentersblocks.api.ICarpentersHammer;
import com.carpentersblocks.block.ICoverableBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemCarpentersHammer extends Item implements ICarpentersHammer {

    public ItemCarpentersHammer(Properties properties) {
        super(properties);
    }

    @Override
    public void onHammerUse(Level level, Player player, InteractionHand hand) {
        player.getItemInHand(hand).hurtAndBreak(1, player, hand);
    }

    @Override
    public boolean canUseHammer(Level level, Player player, InteractionHand hand) {
        return true;
    }

    /**
     * Left clicks with the hammer change Carpenter's blocks instead of breaking them.
     */
    @Override
    public boolean canDestroyBlock(ItemStack itemStack, BlockState blockState, Level level, BlockPos pos, LivingEntity user) {
        return !(blockState.getBlock() instanceof ICoverableBlock);
    }

}
