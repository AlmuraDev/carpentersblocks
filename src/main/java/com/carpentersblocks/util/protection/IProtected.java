package com.carpentersblocks.util.protection;

import net.minecraft.world.entity.player.Player;

public interface IProtected {

    /**
     * Returns object owner as string (UUID, or the player name for objects owned in offline mode in 1.12.2).
     */
    String getOwner();

    /**
     * Sets owner of object.
     */
    void setOwner(Player player);

}
