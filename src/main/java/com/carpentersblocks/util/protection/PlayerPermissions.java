package com.carpentersblocks.util.protection;

import java.util.UUID;

import com.carpentersblocks.CbConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Who may change a protected object. Only meaningful on the server: blocks are only changed there.
 */
public final class PlayerPermissions {

    private PlayerPermissions() {}

    /**
     * Whether the player is a server operator.
     */
    public static boolean isOp(Player player) {
        return player instanceof ServerPlayer serverPlayer && serverPlayer.level().getServer().getPlayerList().isOp(serverPlayer.nameAndId());
    }

    /**
     * Whether player is an operator, owner or is in a singleplayer world.
     *
     * @param object the block or entity
     * @param player the player
     * @param enforceOwnership whether ownership is required, bypassing configuration settings
     * @return <code>true</code> if player has elevated permission
     */
    public static boolean hasElevatedPermission(IProtected object, Player player, boolean enforceOwnership) {
        MinecraftServer server = player.level().getServer();
        if (server == null || server.isSingleplayer()) {
            return true;
        } else if (isOp(player)) {
            return true;
        } else if (!enforceOwnership && !CbConfig.ENABLE_OWNERSHIP.get()) {
            return true;
        } else {
            return isOwner(object, player);
        }
    }

    /**
     * Whether the player owns the object. Objects owned in offline mode in 1.12.2 are owned by name.
     */
    private static boolean isOwner(IProtected object, Player player) {
        String owner = object.getOwner();
        if (owner.isEmpty()) {
            return true;
        }
        try {
            return UUID.fromString(owner).equals(player.getUUID());
        } catch (IllegalArgumentException e) {
            return owner.equals(player.getGameProfile().name());
        }
    }

}
