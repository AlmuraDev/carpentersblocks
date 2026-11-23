package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.blockentity.SafeBlockEntity;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.protection.PlayerPermissions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.Tags;

/**
 * Carpenter's Safe, ported from 1.7.10: a lockable chest with a sliding door and status lights.
 * <p>
 * Data bits 0-1: facing (toward whoever placed it). Bit 2: open. Bit 3: unlocked. Bits 4-5: automation.
 * A new safe is locked, with automation off. Explosion proof (6000000 resistance, set in CbBlocks).
 */
public class BlockCarpentersSafe extends BlockCoverable implements IShapedBlock {

    public static final int AUTOMATION_DISABLED = 0;
    public static final int AUTOMATION_SEND = 1;
    public static final int AUTOMATION_RECEIVE = 2;
    public static final int AUTOMATION_ALL = 3;

    private static final int FACING_MASK = 0x3;
    private static final int OPEN_BIT = 0x4;
    private static final int UNLOCKED_BIT = 0x8;
    private static final int AUTOMATION_MASK = 0x30;
    private static final int AUTOMATION_SHIFT = 4;

    // Render data (added by SafeBlockEntity.createRenderData)
    private static final int INVENTORY = 1 << 22;

    private static final AABB FULL_BLOCK = new AABB(0, 0, 0, 1, 1, 1);

    public BlockCarpentersSafe(Properties properties) {
        super(properties);
    }

    public static Direction getFacing(int data) {
        return Direction.from2DDataValue(data & FACING_MASK);
    }

    private static int setFacing(int data, Direction facing) {
        return (data & ~FACING_MASK) | facing.get2DDataValue();
    }

    private static boolean isOpen(int data) {
        return (data & OPEN_BIT) != 0;
    }

    private static boolean isUnlocked(int data) {
        return (data & UNLOCKED_BIT) != 0;
    }

    public static int getAutomation(int data) {
        return (data & AUTOMATION_MASK) >> AUTOMATION_SHIFT;
    }

    private static int setAutomation(int data, int automation) {
        return (data & ~AUTOMATION_MASK) | (automation << AUTOMATION_SHIFT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
        return new SafeBlockEntity(pos, blockState);
    }

    @Nullable
    private SafeBlockEntity getSafe(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SafeBlockEntity safe ? safe : null;
    }

    /**
     * Gets the upgrade (1 gold, 2 diamond, 3 emerald), or 0 for none (1.12.2 used the ore dictionary).
     */
    public static int getUpgradeIndex(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        } else if (itemStack.is(Tags.Items.INGOTS_GOLD)) {
            return 1;
        } else if (itemStack.is(Tags.Items.GEMS_DIAMOND)) {
            return 2;
        } else if (itemStack.is(Tags.Items.GEMS_EMERALD)) {
            return 3;
        }
        return 0;
    }

    ////////////
    // Shape
    ////////////

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return Collections.singletonList(FULL_BLOCK);
    }

    @Override
    public int getInventoryRenderData(int itemDamage) {
        return INVENTORY | UNLOCKED_BIT;
    }

    @Override
    public List<ShapePart> getRenderParts(int renderData) {
        Direction facing = (renderData & INVENTORY) != 0 ? Direction.WEST : getFacing(renderData);
        boolean open = isOpen(renderData);
        boolean unlocked = isUnlocked(renderData);
        int litCells = (renderData >> SafeBlockEntity.LIT_CELLS_SHIFT) & 0xf;
        int upgrade = (renderData >> SafeBlockEntity.UPGRADE_SHIFT) & 0x3;
        Identifier panel = upgrade == 1 ? ShapeTextures.GOLD_BLOCK : upgrade == 2 ? ShapeTextures.DIAMOND_BLOCK : upgrade == 3 ? ShapeTextures.EMERALD_BLOCK : ShapeTextures.IRON_BLOCK;

        // Authored with the front to the south
        List<ShapePart> parts = new ArrayList<>();
        AABB[] shell = {
            new AABB(0, 0, 0, 0.0625, 1, 1),
            new AABB(0.9375, 0, 0, 1, 1, 1),
            new AABB(0.0625, 0, 0.0625, 0.3125, 0.0625, 1),
            new AABB(0.375, 0, 0.0625, 0.9375, 0.0625, 1),
            new AABB(0.0625, 0.9375, 0.0625, 0.3125, 1, 1),
            new AABB(0.375, 0.9375, 0.0625, 0.9375, 1, 1),
            new AABB(0.0625, 0, 0, 0.3125, 1, 0.0625),
            new AABB(0.375, 0, 0, 0.9375, 1, 0.0625),
            new AABB(0.3125, 0, 0, 0.375, 1, 1),
            new AABB(0.375, 0.625, 0.0625, 0.9375, 0.6875, 0.875),
            new AABB(0.375, 0.3125, 0.0625, 0.9375, 0.375, 0.875),
            // Sliding door
            new AABB(0.375, 0.0625, 0.875, open ? 0.5625 : 0.9375, 0.9375, 0.9375) };
        for (AABB box : shell) {
            parts.add(ShapePart.covered(SidedUtil.rotate(box, facing)));
        }
        AABB[] panelBoxes = {
            new AABB(0.125, 0.875, 0.9375, 0.25, 0.9375, 1),
            new AABB(0.0625, 0.0625, 0.9375, 0.125, 0.9375, 1),
            new AABB(0.25, 0.0625, 0.9375, 0.3125, 0.9375, 1),
            new AABB(0.125, 0.6875, 0.9375, 0.25, 0.75, 1),
            new AABB(0.125, 0.0625, 0.9375, 0.25, 0.125, 1),
            new AABB(0.0625, 0.0625, 0.875, 0.3125, 0.9375, 0.9375),
            // Handle
            open ? new AABB(0.4375, 0.375, 0.9375, 0.5, 0.625, 1) : new AABB(0.8125, 0.375, 0.9375, 0.875, 0.625, 1) };
        for (AABB box : panelBoxes) {
            parts.add(ShapePart.textured(SidedUtil.rotate(box, facing), panel));
        }
        // Lock lights and the fill gauge
        parts.add(light(new AABB(0.125, 0.8125, 0.9375, 0.25, 0.875, 1), facing, unlocked, 0x00FF00, 0x367E36));
        parts.add(light(new AABB(0.125, 0.75, 0.9375, 0.25, 0.8125, 1), facing, !unlocked, 0xFF0000, 0x7E3636));
        for (int cell = 0; cell < 9; ++cell) {
            double y0 = 0.125 + 0.0625 * cell;
            parts.add(light(new AABB(0.125, y0, 0.9375, 0.25, y0 + 0.0625, 1), facing, cell < litCells, 0x0000FF, 0x383884));
        }
        return parts;
    }

    private static ShapePart light(AABB box, Direction facing, boolean on, int onColor, int offColor) {
        AABB rotated = SidedUtil.rotate(box, facing);
        return (on ? ShapePart.glowing(rotated, ShapeTextures.SAFE_LIGHT) : ShapePart.textured(rotated, ShapeTextures.SAFE_LIGHT)).withColor(on ? onColor : offColor);
    }

    ////////////
    // Use
    ////////////

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (!level.isClientSide() && blockEntity != null && placer != null) {
            blockEntity.setCbMetadata(setFacing(blockEntity.getCbMetadata(), placer.getDirection().getOpposite()));
        }
    }

    /**
     * Locked safes open only for their owner (and operators).
     */
    private boolean canOpen(CbBlockEntity blockEntity, Player player) {
        return isUnlocked(blockEntity.getCbMetadata()) || PlayerPermissions.hasElevatedPermission(blockEntity, player, true);
    }

    @Override
    public boolean wouldActivateBlock(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult) {
        return true;
    }

    @Override
    public void postOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {
        actionResult.setAltered().setNoSound();
        if (!(blockEntity instanceof SafeBlockEntity safe)) {
            return;
        }
        if (!canOpen(safe, player)) {
            sendMessage(player, "message.carpentersblocks.block_lock");
            return;
        }
        // Sneak with gold, a diamond or an emerald to double the space
        ItemStack held = player.getMainHandItem();
        if (player.isShiftKeyDown() && !safe.hasUpgrade() && getUpgradeIndex(held) > 0) {
            safe.addAttribute(EnumAttributeLocation.HOST, EnumAttributeType.SAFE_UPGRADE, held);
            actionResult.decInventory();
            return;
        }
        player.openMenu(safe);
    }

    /**
     * Slides the door and plays the door sound.
     */
    public void setOpen(SafeBlockEntity safe, boolean open) {
        Level level = safe.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        int data = safe.getCbMetadata();
        safe.setCbMetadata(open ? data | OPEN_BIT : data & ~OPEN_BIT);
        level.playSound(null, safe.getBlockPos(), open ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    /**
     * Hammer left click: turns the front toward the player.
     */
    @Override
    public boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        return blockEntity.setCbMetadata(setFacing(blockEntity.getCbMetadata(), player.getDirection().getOpposite()));
    }

    /**
     * Hammer right click: next automation mode, or toggles the lock when sneaking.
     */
    @Override
    public boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        if (!PlayerPermissions.hasElevatedPermission(blockEntity, player, true)) {
            return false;
        }
        int data = blockEntity.getCbMetadata();
        if (player.isShiftKeyDown()) {
            if (isUnlocked(data)) {
                data = setAutomation(data & ~UNLOCKED_BIT, AUTOMATION_DISABLED);
                sendMessage(player, "message.carpentersblocks.safe_lock");
            } else {
                data = setAutomation(data | UNLOCKED_BIT, AUTOMATION_ALL);
                sendMessage(player, "message.carpentersblocks.safe_unlock");
            }
        } else {
            int automation = (getAutomation(data) + 1) % 4;
            data = setAutomation(data, automation);
            String[] messages = { "message.carpentersblocks.automation_disabled", "message.carpentersblocks.automation_extract",
                "message.carpentersblocks.automation_insert", "message.carpentersblocks.automation_all" };
            sendMessage(player, messages[automation]);
        }
        return blockEntity.setCbMetadata(data);
    }

    ////////////
    // Protection
    ////////////

    /**
     * Can't be broken while open, or by players who don't own it.
     */
    @Override
    protected float getDestroyProgress(BlockState blockState, Player player, BlockGetter level, BlockPos pos) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity != null && (isOpen(blockEntity.getCbMetadata()) || !PlayerPermissions.hasElevatedPermission(blockEntity, player, true))) {
            return 0.0F;
        }
        return super.getDestroyProgress(blockState, player, level, pos);
    }

}
