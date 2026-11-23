package com.carpentersblocks.item;

import com.carpentersblocks.api.ICarpentersChisel;
import com.carpentersblocks.block.ICoverableBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemCarpentersChisel extends Item implements ICarpentersChisel {

    public ItemCarpentersChisel(Properties properties) {
        super(properties);
    }

    @Override
    public void onChiselUse(Level level, Player player, InteractionHand hand) {
        player.getItemInHand(hand).hurtAndBreak(1, player, hand);
    }

    @Override
    public boolean canUseChisel(Level level, Player player, InteractionHand hand) {
        return true;
    }

    /**
     * Left clicks with the chisel change designs instead of breaking Carpenter's blocks.
     */
    @Override
    public boolean canDestroyBlock(ItemStack itemStack, BlockState blockState, Level level, BlockPos pos, LivingEntity user) {
        return !(blockState.getBlock() instanceof ICoverableBlock);
    }

}
