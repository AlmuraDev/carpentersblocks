package com.carpentersblocks.block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.carpentersblocks.CbConfig;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Carpenter's Garage Door, ported from 1.7.10 (data/GarageDoor, BlockCarpentersGarageDoor).
 * <p>
 * One item fills a column from the placed block down to the floor. When open, only the top (host) piece shows,
 * rolled up to half height, with the bottom piece's cover.
 * <p>
 * Data bits 0-3: type. Bits 4-6: facing. Bit 7: open. Bit 8: rigid (redstone only). Bit 9: host (top piece).
 */
public class BlockCarpentersGarageDoor extends BlockCoverable implements IShapedBlock {

    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_GLASS_TOP = 1;
    public static final int TYPE_GLASS = 2;
    public static final int TYPE_SIDING = 3;
    public static final int TYPE_HIDDEN = 4;
    private static final int NUM_TYPES = 5;

    private static final int TYPE_MASK = 0xf;
    private static final int FACING_MASK = 0x70;
    private static final int FACING_SHIFT = 4;
    private static final int OPEN_BIT = 0x80;
    private static final int RIGID_BIT = 0x100;
    private static final int HOST_BIT = 0x200;

    /** Render data: no garage door below (the bottom piece). */
    private static final int BOTTOM = 1 << 16;
    private static final int INVENTORY = 1 << 17;

    private static final int MAX_PIECES = 500;

    public BlockCarpentersGarageDoor(Properties properties) {
        super(properties);
    }

    private static int getType(int data) {
        int type = data & TYPE_MASK;
        return type < NUM_TYPES ? type : TYPE_DEFAULT;
    }

    private static Direction getFacing(int data) {
        int facing = (data & FACING_MASK) >> FACING_SHIFT;
        return facing >= 2 && facing <= 5 ? Direction.from3DDataValue(facing) : Direction.SOUTH;
    }

    private static int setFacing(int data, Direction facing) {
        return (data & ~FACING_MASK) | (facing.get3DDataValue() << FACING_SHIFT);
    }

    private static boolean isOpen(int data) {
        return (data & OPEN_BIT) != 0;
    }

    private static boolean isRigid(int data) {
        return (data & RIGID_BIT) != 0;
    }

    private static boolean isHost(int data) {
        return (data & HOST_BIT) != 0;
    }

    /**
     * Open pieces below the host are rolled up into it.
     */
    private static boolean isVisible(int data) {
        return !isOpen(data) || isHost(data);
    }

    ////////////
    // Shape
    ////////////

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        if ((renderData & INVENTORY) != 0) {
            return getInventoryBoxes();
        }
        if (!isVisible(renderData)) {
            return Collections.emptyList();
        }
        double yMin = isHost(renderData) && isOpen(renderData) ? 0.5 : 0;
        AABB box = getType(renderData) == TYPE_HIDDEN ? new AABB(0, yMin, 0, 1, 1, 0.125) : new AABB(0, yMin, 0.125, 1, 1, 0.25);
        return Collections.singletonList(SidedUtil.rotate(box, getFacing(renderData)));
    }

    private static List<AABB> getInventoryBoxes() {
        List<AABB> boxes = new ArrayList<>();
        boxes.add(new AABB(0.4375, 0.75, 0, 0.5625, 1, 1));
        boxes.add(new AABB(0.4375, 0.625, 0, 0.5625, 0.75, 0.3125));
        boxes.add(new AABB(0.4375, 0.625, 0.6875, 0.5625, 0.75, 1));
        boxes.add(new AABB(0.4375, 0, 0, 0.5625, 0.625, 1));
        return boxes;
    }

    @Override
    public int getInventoryRenderData(int itemDamage) {
        return INVENTORY;
    }

    @Override
    public int getRenderData(BlockGetter level, BlockPos pos, int cbMetadata) {
        int data = cbMetadata;
        if (!level.getBlockState(pos.below()).is(this)) {
            data |= BOTTOM;
        }
        return data;
    }

    /**
     * An open door is drawn rolled up with the bottom piece's cover, like 1.7.10.
     */
    @Override
    public BlockPos getRenderAttributeSource(BlockGetter level, BlockPos pos, int cbMetadata) {
        if (!isOpen(cbMetadata)) {
            return pos;
        }
        BlockPos bottom = pos;
        BlockPos scanPos = pos.below();
        for (int count = 0; count < MAX_PIECES && level.getBlockState(scanPos).is(this); ++count) {
            bottom = scanPos;
            scanPos = scanPos.below();
        }
        return bottom;
    }

    @Override
    public List<ShapePart> getRenderParts(int renderData) {
        List<ShapePart> parts = new ArrayList<>();
        if ((renderData & INVENTORY) != 0) {
            for (AABB box : getInventoryBoxes()) {
                parts.add(ShapePart.covered(box));
            }
            return parts;
        }
        if (!isVisible(renderData)) {
            return parts;
        }
        boolean open = isOpen(renderData);
        boolean host = isHost(renderData);
        boolean bottom = (renderData & BOTTOM) != 0;
        Direction facing = getFacing(renderData);
        List<AABB> covered = new ArrayList<>();
        List<AABB> iron = new ArrayList<>();
        AABB pane = null;
        Identifier paneTexture = null;
        switch (getType(renderData)) {
            case TYPE_HIDDEN -> covered.add(new AABB(0, open ? 0.5 : 0, 0, 1, 1, 0.125));
            case TYPE_SIDING -> {
                if (open) {
                    iron.add(new AABB(0, 0.5, 0.1875, 1, 1, 0.25));
                    covered.add(new AABB(0, 0.5, 0.125, 1, 0.9375, 0.1875));
                } else {
                    iron.add(new AABB(0, 0, 0.1875, 1, 1, 0.25));
                    covered.add(new AABB(0, 0, 0.125, 1, 0.4375, 0.1875));
                    covered.add(new AABB(0, 0.5, 0.125, 1, 0.9375, 0.1875));
                }
            }
            case TYPE_DEFAULT -> {
                if (open) {
                    covered.add(new AABB(0, 0.5, 0.125, 1, 1, 0.25));
                } else {
                    covered.add(new AABB(0, 0, 0.125, 0.3125, 1, 0.25));
                    covered.add(new AABB(0.3125, 0.75, 0.125, 0.6875, 1, 0.25));
                    covered.add(new AABB(0.3125, 0, 0.125, 0.6875, 0.625, 0.25));
                    covered.add(new AABB(0.6875, 0, 0.125, 1, 1, 0.25));
                }
            }
            default -> { // GLASS_TOP, GLASS
                if (open) {
                    addOpenPanels(covered);
                } else {
                    boolean glassTop = getType(renderData) == TYPE_GLASS_TOP ? host : bottom;
                    if (getType(renderData) == TYPE_GLASS && !bottom) {
                        covered.add(new AABB(0, 0, 0.125, 0.125, 1, 0.25));
                        covered.add(new AABB(0.125, 0.875, 0.125, 0.875, 1, 0.25));
                        covered.add(new AABB(0.125, 0, 0.125, 0.875, 0.125, 0.25));
                        covered.add(new AABB(0.875, 0, 0.125, 1, 1, 0.25));
                        pane = new AABB(0.125, 0.125, 0.1875, 0.875, 0.875, 0.203125);
                        paneTexture = ShapeTextures.GARAGE_GLASS;
                    } else {
                        addPanels(covered, glassTop);
                        if (glassTop) {
                            pane = new AABB(0.125, 0.5625, 0.1875, 0.875, 0.875, 0.203125);
                            paneTexture = ShapeTextures.GARAGE_GLASS_TOP;
                        }
                    }
                }
            }
        }
        for (AABB box : covered) {
            parts.add(ShapePart.covered(SidedUtil.rotate(box, facing)));
        }
        for (AABB box : iron) {
            parts.add(ShapePart.textured(SidedUtil.rotate(box, facing), ShapeTextures.IRON_BLOCK));
        }
        if (pane != null) {
            parts.add(ShapePart.translucent(SidedUtil.rotate(pane, facing), paneTexture));
        }
        return parts;
    }

    private static void addPanels(List<AABB> boxes, boolean glassTop) {
        boxes.add(new AABB(0, 0, 0.125, 0.125, 1, 0.25));
        boxes.add(new AABB(0.125, 0.875, 0.125, 0.875, 1, 0.25));
        boxes.add(new AABB(0.125, 0.4375, 0.125, 0.875, 0.5625, 0.25));
        boxes.add(new AABB(0.125, 0, 0.125, 0.875, 0.125, 0.25));
        boxes.add(new AABB(0.875, 0, 0.125, 1, 1, 0.25));
        boxes.add(glassTop ? new AABB(0.125, 0.125, 0.1875, 0.875, 0.4375, 0.25) : new AABB(0, 0, 0.1875, 1, 1, 0.25));
    }

    private static void addOpenPanels(List<AABB> boxes) {
        boxes.add(new AABB(0, 0.5, 0.1875, 1, 1, 0.25));
        boxes.add(new AABB(0, 0.5, 0.125, 0.125, 1, 0.1875));
        boxes.add(new AABB(0.125, 0.9375, 0.125, 0.875, 1, 0.1875));
        boxes.add(new AABB(0.125, 0.5, 0.125, 0.875, 0.625, 0.1875));
        boxes.add(new AABB(0.875, 0.5, 0.125, 1, 1, 0.1875));
    }

    ////////////
    // Linked pieces
    ////////////

    @Nullable
    private CbBlockEntity getPiece(Level level, BlockPos pos) {
        return getBlockEntity(level, pos);
    }

    /**
     * Gets every piece of the door: connected up, down, and across, facing the same way.
     */
    private List<CbBlockEntity> getLinkedPieces(CbBlockEntity start) {
        Level level = start.getLevel();
        Direction facing = getFacing(start.getCbMetadata());
        Direction[] directions = { Direction.UP, Direction.DOWN, facing.getClockWise(), facing.getCounterClockWise() };
        List<CbBlockEntity> pieces = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start.getBlockPos());
        visited.add(start.getBlockPos());
        int limit = CbConfig.MULTIBLOCK_SIZE_LIMIT.get();
        while (!queue.isEmpty() && pieces.size() < limit) {
            BlockPos pos = queue.poll();
            CbBlockEntity piece = getPiece(level, pos);
            if (piece == null || getFacing(piece.getCbMetadata()) != facing) {
                continue;
            }
            pieces.add(piece);
            for (Direction direction : directions) {
                BlockPos next = pos.relative(direction);
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return pieces;
    }

    private void setOpen(CbBlockEntity clicked, boolean open) {
        for (CbBlockEntity piece : getLinkedPieces(clicked)) {
            int data = piece.getCbMetadata();
            piece.setCbMetadata(open ? data | OPEN_BIT : data & ~OPEN_BIT);
        }
        Level level = clicked.getLevel();
        level.playSound(null, clicked.getBlockPos(), open ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE,
            SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    ////////////
    // Placement
    ////////////

    private boolean hasSupportAbove(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        return aboveState.isFaceSturdy(level, above, Direction.DOWN) || aboveState.is(this);
    }

    private boolean canPlaceAt(Level level, BlockPos pos) {
        return level.getBlockState(pos).canBeReplaced() && hasSupportAbove(level, pos);
    }

    /**
     * Garage doors hang from something solid above.
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return hasSupportAbove(context.getLevel(), context.getClickedPos()) ? super.getStateForPlacement(context) : null;
    }

    /**
     * The placed piece becomes the host; matches a door alongside and fills down to the floor.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (level.isClientSide() || blockEntity == null) {
            return;
        }
        Direction facing = placer != null ? placer.getDirection() : Direction.SOUTH;
        int data = setFacing(HOST_BIT, facing);
        for (Direction side : new Direction[] { facing.getClockWise(), facing.getCounterClockWise() }) {
            CbBlockEntity reference = getPiece(level, pos.relative(side));
            if (reference != null && getFacing(reference.getCbMetadata()) == facing) {
                data = reference.getCbMetadata() | HOST_BIT;
                break;
            }
        }
        blockEntity.setCbMetadata(data);
        fillDown(blockEntity);
    }

    /**
     * Adds pieces below until something is in the way.
     */
    private void fillDown(CbBlockEntity host) {
        Level level = host.getLevel();
        int pieceData = host.getCbMetadata() & ~HOST_BIT;
        BlockPos pos = host.getBlockPos().below();
        for (int count = 0; count < MAX_PIECES && pos.getY() > level.getMinY() && canPlaceAt(level, pos); ++count) {
            level.setBlock(pos, defaultBlockState(), Block.UPDATE_ALL);
            CbBlockEntity piece = getBlockEntity(level, pos);
            if (piece != null) {
                piece.copyOwner(host);
                piece.setCbMetadata(pieceData);
            }
            pos = pos.below();
        }
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
        int data = blockEntity.getCbMetadata();
        // The host needs something to hang from
        if (isHost(data) && !hasSupportAbove(level, pos)) {
            destroyColumn(level, pos, true);
            return;
        }
        // Fill a gap opened up below
        if (CbConfig.ENABLE_GARAGE_DOOR_VOID_FILL.get() && block != this && level.getBlockState(pos.below()).isAir()) {
            CbBlockEntity host = findHost(blockEntity);
            if (host != null) {
                fillDown(host);
            }
        }
        // Redstone
        if (block.defaultBlockState().isSignalSource()) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered && !isOpen(data)) {
                setOpen(blockEntity, true);
            } else if (!powered && isOpen(data)) {
                boolean anyPowered = false;
                for (CbBlockEntity piece : getLinkedPieces(blockEntity)) {
                    anyPowered |= level.hasNeighborSignal(piece.getBlockPos());
                }
                if (!anyPowered) {
                    setOpen(blockEntity, false);
                }
            }
        }
    }

    @Nullable
    private CbBlockEntity findHost(CbBlockEntity piece) {
        Level level = piece.getLevel();
        BlockPos pos = piece.getBlockPos();
        for (int count = 0; count < MAX_PIECES; ++count) {
            CbBlockEntity current = getPiece(level, pos);
            if (current == null) {
                return null;
            }
            if (isHost(current.getCbMetadata())) {
                return current;
            }
            pos = pos.above();
        }
        return null;
    }

    ////////////
    // Interaction
    ////////////

    @Override
    public boolean wouldActivateBlock(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult) {
        return !isRigid(blockEntity.getCbMetadata());
    }

    @Override
    public void postOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {
        int data = blockEntity.getCbMetadata();
        if (!isRigid(data)) {
            setOpen(blockEntity, !isOpen(data));
            actionResult.setAltered().setNoSound();
        }
    }

    private void setTypeAll(CbBlockEntity blockEntity, int type) {
        for (CbBlockEntity piece : getLinkedPieces(blockEntity)) {
            piece.setCbMetadata((piece.getCbMetadata() & ~TYPE_MASK) | type);
        }
    }

    /**
     * Hammer left click: next type.
     */
    @Override
    public boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        setTypeAll(blockEntity, (getType(blockEntity.getCbMetadata()) + 1) % NUM_TYPES);
        return true;
    }

    /**
     * Hammer right click: previous type, or toggles redstone only when sneaking.
     */
    @Override
    public boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        if (player.isShiftKeyDown()) {
            boolean rigid = !isRigid(blockEntity.getCbMetadata());
            for (CbBlockEntity piece : getLinkedPieces(blockEntity)) {
                int data = piece.getCbMetadata();
                piece.setCbMetadata(rigid ? data | RIGID_BIT : data & ~RIGID_BIT);
            }
            sendMessage(player, rigid ? "message.carpentersblocks.activation_iron" : "message.carpentersblocks.activation_wood");
            return true;
        }
        setTypeAll(blockEntity, (getType(blockEntity.getCbMetadata()) + NUM_TYPES - 1) % NUM_TYPES);
        return true;
    }

    ////////////
    // Breaking
    ////////////

    /**
     * Breaking any piece takes down its column. One item drops (the loot table drops it for the host only, so a
     * player breaking another piece gets it here), and every piece drops its attributes.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState blockState, Player player) {
        if (!level.isClientSide() && !player.isCreative() && !isCarpentersTool(player.getMainHandItem())) {
            CbBlockEntity blockEntity = getBlockEntity(level, pos);
            if (blockEntity != null && !isHost(blockEntity.getCbMetadata())) {
                popResource(level, pos, new ItemStack(this));
            }
        }
        if (!level.isClientSide()) {
            destroyColumn(level, pos, false);
        }
        return super.playerWillDestroy(level, pos, blockState, player);
    }

    /**
     * Only the host drops the door item (explosions break every piece).
     */
    @Override
    protected List<ItemStack> getDrops(BlockState blockState, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof CbBlockEntity piece && !isHost(piece.getCbMetadata())) {
            return Collections.emptyList();
        }
        return super.getDrops(blockState, params);
    }

    /**
     * Removes the rest of a column, keeping the piece at pos (the caller breaks it).
     *
     * @param dropItem whether the door item drops (a host that lost its support)
     */
    private void destroyColumn(Level level, BlockPos pos, boolean dropItem) {
        List<BlockPos> column = new ArrayList<>();
        BlockPos scanPos = pos.above();
        while (level.getBlockState(scanPos).is(this) && column.size() < MAX_PIECES) {
            column.add(scanPos);
            scanPos = scanPos.above();
        }
        scanPos = pos.below();
        while (level.getBlockState(scanPos).is(this) && column.size() < MAX_PIECES) {
            column.add(scanPos);
            scanPos = scanPos.below();
        }
        if (dropItem) {
            popResource(level, pos, new ItemStack(this));
            column.add(pos);
        }
        // Each piece drops its attributes as it is removed (CbBlockEntity.preRemoveSideEffects)
        for (BlockPos columnPos : column) {
            level.setBlock(columnPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

}
