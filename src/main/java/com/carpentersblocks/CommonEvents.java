package com.carpentersblocks;

import com.carpentersblocks.block.ICoverableBlock;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.util.block.BlockUtil;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Game events (1.12.2's EventHandler).
 */
public final class CommonEvents {

    private CommonEvents() {}

    /**
     * Left clicks reach the block with the clicked face (Block.attack has no face). Carpenter's tools don't break the
     * block (see their canDestroyBlock), they change it.
     */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide() || event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START || event.getFace() == null) {
            return;
        }
        BlockState blockState = level.getBlockState(event.getPos());
        if (blockState.getBlock() instanceof ICoverableBlock block && level.getBlockEntity(event.getPos()) instanceof CbBlockEntity) {
            block.onBlockClicked(level, event.getPos(), event.getEntity(), event.getFace());
        }
    }

    /**
     * Sneaking players still use the block when holding something that is not placed (glowstone, dyes, overlays, the
     * hammer), so those attributes can be added while sneaking, like 1.12.2.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown() || !(event.getLevel().getBlockEntity(event.getPos()) instanceof CbBlockEntity)) {
            return;
        }
        ItemStack itemStack = event.getItemStack();
        if (!(itemStack.getItem() instanceof BlockItem) || BlockUtil.isOverlay(itemStack)) {
            event.setUseBlock(TriState.TRUE);
        }
    }

}
