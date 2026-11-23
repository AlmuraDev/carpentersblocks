package com.carpentersblocks.api;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * An item that works as the Carpenter's Hammer: it removes attributes and turns blocks.
 */
public interface ICarpentersHammer {

    void onHammerUse(Level level, Player player, InteractionHand hand);

    boolean canUseHammer(Level level, Player player, InteractionHand hand);

}
