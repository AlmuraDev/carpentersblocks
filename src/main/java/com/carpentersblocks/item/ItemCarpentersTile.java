package com.carpentersblocks.item;

import java.util.List;

import com.carpentersblocks.entity.EntityCarpentersTile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Places a Carpenter's Tile on the clicked face, taking dye, rotation and design from a tile next to it (unless
 * sneaking), like 1.7.10.
 */
public class ItemCarpentersTile extends Item {

    public ItemCarpentersTile(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        Direction side = context.getClickedFace();
        BlockPos clickedPos = context.getClickedPos();
        BlockPos tilePos = clickedPos.relative(side);
        ItemStack itemStack = context.getItemInHand();
        if ((player != null && !player.mayUseItemAt(tilePos, side, itemStack)) || !level.getBlockState(clickedPos).isFaceSturdy(level, clickedPos, side)) {
            return InteractionResult.FAIL;
        }
        // One tile per face
        AABB box = EntityCarpentersTile.getLocalBox(side).move(tilePos).deflate(0.01D);
        for (EntityCarpentersTile existing : level.getEntitiesOfClass(EntityCarpentersTile.class, box)) {
            if (existing.getDirection() == side) {
                return InteractionResult.FAIL;
            }
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        EntityCarpentersTile tile = new EntityCarpentersTile(level, player, tilePos, side);
        if (player == null || !player.isShiftKeyDown()) {
            copyFromNeighbor(level, tile, tilePos, side);
        }
        level.addFreshEntity(tile);
        level.playSound(null, tilePos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        itemStack.consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }

    /**
     * Takes dye, rotation and design from a tile on the same face next to this one.
     */
    private static void copyFromNeighbor(Level level, EntityCarpentersTile tile, BlockPos tilePos, Direction side) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == side.getAxis()) {
                continue;
            }
            AABB box = EntityCarpentersTile.getLocalBox(side).move(tilePos.relative(direction)).deflate(0.01D);
            List<EntityCarpentersTile> neighbors = level.getEntitiesOfClass(EntityCarpentersTile.class, box);
            for (EntityCarpentersTile neighbor : neighbors) {
                if (neighbor.getDirection() == side) {
                    tile.setDye(neighbor.getDye());
                    tile.setRotation(neighbor.getRotation());
                    tile.setDesign(neighbor.getDesign());
                    return;
                }
            }
        }
    }

}
