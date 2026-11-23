package com.carpentersblocks.api;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * An item that works as the Carpenter's Chisel: it cycles chisel designs on covers.
 */
public interface ICarpentersChisel {

    void onChiselUse(Level level, Player player, InteractionHand hand);

    boolean canUseChisel(Level level, Player player, InteractionHand hand);

}
