package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.blockentity.CbBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Carpenter's Hatch, ported from 1.7.10 (data/Hatch, BlockHandlerCarpentersHatch).
 * <p>
 * Data bits 0-2: type. Bit 3: hangs high. Bit 4: open. Bits 5-6: side of the supporting block
 * (0 south, 1 north, 2 east, 3 west). Bit 7: rigid (redstone only).
 */
public class BlockCarpentersHatch extends BlockCoverable implements IShapedBlock {

    public static final int TYPE_HIDDEN = 0;
    public static final int TYPE_WINDOW = 1;
    public static final int TYPE_SCREEN = 2;
    public static final int TYPE_FRENCH_WINDOW = 3;
    public static final int TYPE_PANEL = 4;
    private static final int NUM_TYPES = 5;

    private static final int TYPE_MASK = 0x7;
    private static final int HIGH_BIT = 0x8;
    private static final int OPEN_BIT = 0x10;
    private static final int DIR_MASK = 0x60;
    private static final int DIR_SHIFT = 5;
    private static final int RIGID_BIT = 0x80;

    // Data picked in getStateForPlacement, applied in setPlacedBy once the block entity exists
    private static final ThreadLocal<Integer> _threadLocalPlacement = new ThreadLocal<>();

    public BlockCarpentersHatch(Properties properties) {
        super(properties);
    }

    private static int getType(int data) {
        int type = data & TYPE_MASK;
        return type < NUM_TYPES ? type : TYPE_HIDDEN;
    }

    private static boolean isHigh(int data) {
        return (data & HIGH_BIT) != 0;
    }

    private static boolean isOpen(int data) {
        return (data & OPEN_BIT) != 0;
    }

    private static int setOpen(int data, boolean open) {
        return open ? data | OPEN_BIT : data & ~OPEN_BIT;
    }

    private static int getDir(int data) {
        return (data & DIR_MASK) >> DIR_SHIFT;
    }

    private static int setDir(int data, int dir) {
        return (data & ~DIR_MASK) | (dir << DIR_SHIFT);
    }

    private static boolean isRigid(int data) {
        return (data & RIGID_BIT) != 0;
    }

    /**
     * Gets the direction from the hatch to its supporting block.
     */
    private static Direction getSupportSide(int dir) {
        return switch (dir) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.NORTH;
            case 2 -> Direction.EAST;
            default -> Direction.WEST;
        };
    }

    ////////////
    // Shape
    ////////////

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return Collections.singletonList(getBounds(renderData));
    }

    private static AABB getBounds(int data) {
        double t = getType(data) == TYPE_HIDDEN ? 0.125 : 0.1875;
        if (!isOpen(data)) {
            return isHigh(data) ? new AABB(0, 1 - t, 0, 1, 1, 1) : new AABB(0, 0, 0, 1, t, 1);
        }
        return SidedUtil.rotate(new AABB(0, 0, 1 - t, 1, 1, 1), getSupportSide(getDir(data)));
    }

    @Override
    public int getInventoryRenderData(int itemDamage) {
        return -1;
    }

    @Override
    public List<ShapePart> getRenderParts(int renderData) {
        List<ShapePart> parts = new ArrayList<>();
        if (renderData == -1) {
            parts.add(ShapePart.covered(new AABB(0, 0.4375, 0, 1, 0.5625, 1)));
            parts.add(ShapePart.textured(new AABB(0.0625, 0.5625, 0.375, 0.125, 0.625, 0.4375), ShapeTextures.IRON_BLOCK));
            parts.add(ShapePart.textured(new AABB(0.0625, 0.5625, 0.5625, 0.125, 0.625, 0.625), ShapeTextures.IRON_BLOCK));
            parts.add(ShapePart.textured(new AABB(0.0625, 0.625, 0.375, 0.125, 0.6875, 0.625), ShapeTextures.IRON_BLOCK));
            return parts;
        }
        int type = getType(renderData);
        boolean open = isOpen(renderData);
        boolean high = isHigh(renderData);
        Direction baseDir = getSupportSide(getDir(renderData));
        Direction side = open ? baseDir : high ? Direction.UP : Direction.DOWN;

        // Panel, authored at z 0.8125-1
        List<AABB> covered = new ArrayList<>();
        Identifier pane = null;
        if (type == TYPE_HIDDEN) {
            covered.add(new AABB(0, 0, 0.875, 1, 1, 1));
        } else {
            covered.add(new AABB(0, 0, 0.8125, 0.1875, 1, 1));
            covered.add(new AABB(0.1875, 0.8125, 0.8125, 0.8125, 1, 1));
            covered.add(new AABB(0.1875, 0, 0.8125, 0.8125, 0.1875, 1));
            covered.add(new AABB(0.8125, 0, 0.8125, 1, 1, 1));
            switch (type) {
                case TYPE_WINDOW -> pane = ShapeTextures.HATCH_GLASS;
                case TYPE_SCREEN -> pane = ShapeTextures.HATCH_SCREEN;
                case TYPE_FRENCH_WINDOW -> {
                    covered.add(new AABB(0.1875, 0.4375, 0.875, 0.8125, 0.5625, 0.9375));
                    covered.add(new AABB(0.4375, 0.1875, 0.875, 0.5625, 0.4375, 0.9375));
                    covered.add(new AABB(0.4375, 0.5625, 0.875, 0.5625, 0.8125, 0.9375));
                    pane = ShapeTextures.HATCH_FRENCH_GLASS;
                }
                default -> { // PANEL
                    covered.add(new AABB(0.1875, 0.1875, 0.875, 0.8125, 0.8125, 0.9375));
                    covered.add(new AABB(0.3125, 0.3125, 0.8125, 0.6875, 0.6875, 1));
                }
            }
        }
        for (AABB box : covered) {
            parts.add(ShapePart.covered(SidedUtil.rotate(box, side)));
        }
        if (pane != null) {
            parts.add(ShapePart.translucent(SidedUtil.rotate(new AABB(0.1875, 0.1875, 0.90625, 0.8125, 0.8125, 0.921875), side), pane));
        }

        // Handle on the face that is inside this block space: the outer face when open, the inner face when closed
        double zo = type == TYPE_HIDDEN ? 0.125 : 0.1875;
        double yMin = high ? 0.0625 : 0.875;
        double yMax = yMin + 0.0625;
        AABB[] handle = {
            new AABB(0.375, yMin, 0.9375 - zo, 0.4375, yMax, 1 - zo),
            new AABB(0.5625, yMin, 0.9375 - zo, 0.625, yMax, 1 - zo),
            new AABB(0.375, yMin, 0.875 - zo, 0.625, yMax, 0.9375 - zo) };
        if (open && type != TYPE_HIDDEN) {
            for (AABB box : handle) {
                parts.add(ShapePart.textured(SidedUtil.rotate(box, baseDir), ShapeTextures.IRON_BLOCK));
            }
        } else if (!open) {
            // Closed hatches are seen from both sides (the room and through the opening), so the handle is mirrored
            // onto the far face too. Open ones rest their far face on the support.
            Direction turn = high ? baseDir : baseDir.getOpposite();
            double mirror = 2 - zo; // twice the panel's middle depth
            for (AABB box : handle) {
                AABB far = new AABB(box.minX, box.minY, mirror - box.maxZ, box.maxX, box.maxY, mirror - box.minZ);
                parts.add(ShapePart.textured(SidedUtil.rotate(SidedUtil.rotate(box, side), turn), ShapeTextures.IRON_BLOCK));
                parts.add(ShapePart.textured(SidedUtil.rotate(SidedUtil.rotate(far, side), turn), ShapeTextures.IRON_BLOCK));
            }
        }
        return parts;
    }

    ////////////
    // Placement and support
    ////////////

    /**
     * Hatches hang off the side of a block: solid faces, slabs, stairs, Carpenter's Blocks and glowstone.
     */
    private boolean isValidSupport(BlockGetter level, BlockPos hatchPos, int dir) {
        Direction toSupport = getSupportSide(dir);
        BlockPos supportPos = hatchPos.relative(toSupport);
        BlockState supportState = level.getBlockState(supportPos);
        Block block = supportState.getBlock();
        return block == Blocks.GLOWSTONE || block instanceof SlabBlock || block instanceof StairBlock
            || block instanceof BlockCarpentersStairs || block instanceof BlockCarpentersBlock
            || supportState.isFaceSturdy(level, supportPos, toSupport.getOpposite());
    }

    private static int dirForClickedSide(Direction side) {
        return side.get3DDataValue() - 2;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (!facing.getAxis().isHorizontal() || !isValidSupport(context.getLevel(), context.getClickedPos(), dirForClickedSide(facing))) {
            return null;
        }
        int data = setDir(0, dirForClickedSide(facing));
        if (context.getClickLocation().y - context.getClickedPos().getY() > 0.5D) {
            data |= HIGH_BIT;
        }
        _threadLocalPlacement.set(data);
        return super.getStateForPlacement(context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        Integer data = _threadLocalPlacement.get();
        _threadLocalPlacement.remove();
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (!level.isClientSide() && blockEntity != null && data != null) {
            blockEntity.setCbMetadata(data);
        }
    }

    /**
     * Turns to the next side with a valid support.
     *
     * @return whether a supported side was found
     */
    private boolean findNextSupport(CbBlockEntity blockEntity) {
        int data = blockEntity.getCbMetadata();
        int dir = getDir(data);
        for (int count = 0; count < 4; ++count) {
            dir = (dir + 1) % 4;
            if (isValidSupport(blockEntity.getLevel(), blockEntity.getBlockPos(), dir)) {
                blockEntity.setCbMetadata(setDir(data, dir));
                return true;
            }
        }
        return false;
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(blockState, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide() || !level.getBlockState(pos).is(this)) {
            return;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return;
        }
        if (!isValidSupport(level, pos, getDir(blockEntity.getCbMetadata())) && !findNextSupport(blockEntity)) {
            breakBlock(level, pos);
            return;
        }
        // Redstone works on rigid hatches too
        boolean powered = level.hasNeighborSignal(pos);
        int data = blockEntity.getCbMetadata();
        if (block.defaultBlockState().isSignalSource() && powered != isOpen(data)) {
            setHatchOpen(blockEntity, powered);
        }
    }

    private void setHatchOpen(CbBlockEntity blockEntity, boolean open) {
        blockEntity.setCbMetadata(setOpen(blockEntity.getCbMetadata(), open));
        Level level = blockEntity.getLevel();
        level.playSound(null, blockEntity.getBlockPos(), open ? SoundEvents.WOODEN_TRAPDOOR_OPEN : SoundEvents.WOODEN_TRAPDOOR_CLOSE,
            SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    @Override
    protected boolean wouldActivateBlock(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult) {
        return !isRigid(blockEntity.getCbMetadata());
    }

    @Override
    protected void postOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {
        int data = blockEntity.getCbMetadata();
        if (!isRigid(data)) {
            setHatchOpen(blockEntity, !isOpen(data));
            actionResult.setAltered().setNoSound();
        }
    }

    /**
     * Hidden hatches hanging high act as ladders when open.
     */
    @Override
    public boolean isLadder(BlockState blockState, LevelReader level, BlockPos pos, LivingEntity entity) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return false;
        }
        int data = blockEntity.getCbMetadata();
        return getType(data) == TYPE_HIDDEN && isHigh(data) && isOpen(data);
    }

    /**
     * Hammer left click: turns to the next supported side.
     */
    @Override
    protected boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        return findNextSupport(blockEntity);
    }

    /**
     * Hammer right click: next type, or toggles redstone only when sneaking.
     */
    @Override
    protected boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        int data = blockEntity.getCbMetadata();
        if (player.isShiftKeyDown()) {
            data ^= RIGID_BIT;
            sendMessage(player, isRigid(data) ? "message.carpentersblocks.activation_iron" : "message.carpentersblocks.activation_wood");
        } else {
            data = (data & ~TYPE_MASK) | ((getType(data) + 1) % NUM_TYPES);
        }
        return blockEntity.setCbMetadata(data);
    }

}
