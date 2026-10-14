package com.carpentersblocks.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class EntityLivingUtil {

    /**
     * Decrements the ItemStack in the player's hand.
     *
     * @param entityPlayer the player
     * @param hand the hand holding the item that was used
     */
    public static void decrementHeldItem(EntityPlayer entityPlayer, EnumHand hand) {
        ItemStack itemStack = entityPlayer.getHeldItem(hand);
        if (!itemStack.isEmpty()) {
            if (!entityPlayer.capabilities.isCreativeMode) {
            	itemStack.shrink(1);
            }
        }
    }

}
