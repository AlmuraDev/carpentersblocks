package com.carpentersblocks.block;

import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;

import net.minecraft.world.entity.player.Player;

/**
 * The Carpenter's Block: a full block that takes a cover and a side cover on every side.
 */
public class BlockCarpentersBlock extends BlockCoverable {

    public BlockCarpentersBlock(Properties properties) {
        super(properties);
    }

    /**
     * Covers on every side, like 1.7.10.
     */
    @Override
    protected boolean canSupportCover(CbBlockEntity blockEntity, EnumAttributeLocation location) {
        return true;
    }

    @Override
    protected boolean supportsSideCovers() {
        return true;
    }

    /**
     * Alters block direction (not yet: 1.12.2 did nothing either).
     */
    @Override
    protected boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        return true;
    }

    /**
     * Alters block type (not yet: 1.12.2 did nothing either).
     */
    @Override
    protected boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        return true;
    }

}
