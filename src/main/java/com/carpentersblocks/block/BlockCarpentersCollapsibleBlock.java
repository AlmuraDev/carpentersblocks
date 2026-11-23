package com.carpentersblocks.block;

import static com.carpentersblocks.util.block.CollapsibleUtil.QUAD_XZNN;
import static com.carpentersblocks.util.block.CollapsibleUtil.QUAD_XZNP;
import static com.carpentersblocks.util.block.CollapsibleUtil.QUAD_XZPN;
import static com.carpentersblocks.util.block.CollapsibleUtil.QUAD_XZPP;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.carpentersblocks.CbConfig;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.util.block.CollapsibleUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Carpenter's Collapsible Block: a block whose four corners are raised and lowered with the hammer, to make uneven
 * ground and roofs. Placed on a floor it grows up; hung from a ceiling it hangs down. It needs no support.
 * <p>
 * Data bits 0-2: facing (the clicked face). Bits 3-22: the depth (0-16) of each corner, see {@link CollapsibleUtil}.
 */
public class BlockCarpentersCollapsibleBlock extends BlockCoverable {

    /** Render data of the item: a flat plate. */
    public static final int INVENTORY = -1;

    // Clicked face picked in getStateForPlacement, applied in setPlacedBy once the block entity exists
    private static final ThreadLocal<Direction> _threadLocalFacing = new ThreadLocal<>();

    private final Map<Integer, VoxelShape> _outlineShapes = new ConcurrentHashMap<>();
    private final Map<Integer, VoxelShape> _collisionShapes = new ConcurrentHashMap<>();

    public BlockCarpentersCollapsibleBlock(Properties properties) {
        super(properties);
    }

    private static CollapsibleUtil util(CbBlockEntity blockEntity) {
        return new CollapsibleUtil(blockEntity.getCbMetadata());
    }

    /**
     * Decrease corner depth.
     */
    @Override
    public boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        int quad = CollapsibleUtil.getHitQuad(getClickHit());
        CollapsibleUtil.setQuadDepth(blockEntity, quad, util(blockEntity).getQuadDepth(quad) - 1);
        smoothAdjacentCollapsibles(blockEntity, quad);
        return true;
    }

    /**
     * Increase corner depth.
     */
    @Override
    public boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        int quad = CollapsibleUtil.getHitQuad(getClickHit());
        CollapsibleUtil.setQuadDepth(blockEntity, quad, util(blockEntity).getQuadDepth(quad) + 1);
        smoothAdjacentCollapsibles(blockEntity, quad);
        return true;
    }

    /**
     * Damages hammer with a chance to not damage.
     */
    @Override
    public void damageItemWithChance(Level level, Player player, InteractionHand hand) {
        if (level.getRandom().nextFloat() <= CbConfig.HAMMER_DAMAGE_CHANCE_COLLAPSIBLE.get()) {
            super.damageItemWithChance(level, player, hand);
        }
    }

    private static boolean isSameOrientation(CbBlockEntity blockEntity1, CbBlockEntity blockEntity2) {
        return util(blockEntity1).getFacing().equals(util(blockEntity2).getFacing());
    }

    /**
     * Whether smoothing may change the adjacent block: same orientation and same owner, so players can't reshape
     * blocks they don't own.
     */
    private static boolean canSmoothInto(CbBlockEntity blockEntity, @Nullable CbBlockEntity adjacent) {
        return adjacent != null && isSameOrientation(blockEntity, adjacent) && blockEntity.getOwner().equals(adjacent.getOwner());
    }

    /**
     * Smooths the transition into adjacent collapsible blocks from a corner.
     */
    private void smoothAdjacentCollapsibles(CbBlockEntity blockEntity, int quad) {
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        int depth = util(blockEntity).getQuadDepth(quad);
        int dx = quad == QUAD_XZNN || quad == QUAD_XZNP ? -1 : 1;
        int dz = quad == QUAD_XZNN || quad == QUAD_XZPN ? -1 : 1;
        // The corner is shared with the block along x, the block along z and the diagonal one
        CbBlockEntity alongX = getBlockEntity(level, pos.offset(dx, 0, 0));
        CbBlockEntity alongZ = getBlockEntity(level, pos.offset(0, 0, dz));
        CbBlockEntity diagonal = getBlockEntity(level, pos.offset(dx, 0, dz));
        if (canSmoothInto(blockEntity, alongX)) {
            CollapsibleUtil.setQuadDepth(alongX, quadAt(-dx, dz), depth);
        }
        if (canSmoothInto(blockEntity, alongZ)) {
            CollapsibleUtil.setQuadDepth(alongZ, quadAt(dx, -dz), depth);
        }
        if (canSmoothInto(blockEntity, diagonal)) {
            CollapsibleUtil.setQuadDepth(diagonal, quadAt(-dx, -dz), depth);
        }
    }

    private static int quadAt(int dx, int dz) {
        return dx < 0 ? (dz < 0 ? QUAD_XZNN : QUAD_XZNP) : (dz < 0 ? QUAD_XZPN : QUAD_XZPP);
    }

    ////////////
    // Shape
    ////////////

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return Shapes.block();
        }
        return _outlineShapes.computeIfAbsent(blockEntity.getCbMetadata(), data -> {
            CollapsibleUtil util = new CollapsibleUtil(data);
            // A fully collapsed block keeps a sliver, so it can still be clicked (1.12.2 hit flat boxes)
            double maxDepth = Math.max(util.getBoundsMaxDepth(), 1.0D / 16.0D);
            return util.isPositive() ? Shapes.box(0, 0, 0, 1, maxDepth, 1) : Shapes.box(0, 1 - maxDepth, 0, 1, 1, 1);
        });
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return Shapes.block();
        }
        return _collisionShapes.computeIfAbsent(blockEntity.getCbMetadata(), data -> {
            CollapsibleUtil util = new CollapsibleUtil(data);
            VoxelShape shape = Shapes.empty();
            for (int quad = 0; quad < 4; ++quad) {
                shape = Shapes.or(shape, Shapes.create(util.genQuadBoundingBox(quad)));
            }
            return shape.optimize();
        });
    }

    @Override
    public int getInventoryRenderData(ItemStack itemStack) {
        return INVENTORY;
    }

    ////////////
    // Placement
    ////////////

    /**
     * Placed on a floor or ceiling, or against a side with a floor or ceiling next to it.
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (facing.getAxis().isHorizontal()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            boolean supported = false;
            for (Direction direction : new Direction[] { Direction.UP, Direction.DOWN }) {
                BlockPos supportPos = pos.relative(direction.getOpposite());
                supported |= level.getBlockState(supportPos).isFaceSturdy(level, supportPos, direction);
            }
            if (!supported) {
                return null;
            }
        }
        _threadLocalFacing.set(facing);
        return super.getStateForPlacement(context);
    }

    /**
     * Faces the clicked side and, unless the player sneaks, slopes from the neighboring blocks.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        Direction facing = _threadLocalFacing.get();
        _threadLocalFacing.remove();
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (level.isClientSide() || blockEntity == null) {
            return;
        }
        new CollapsibleUtil(blockEntity.getCbMetadata()).setFacing(blockEntity, facing != null ? facing : Direction.UP);
        if (placer == null || !placer.isShiftKeyDown()) {
            slopeFromNeighbors(level, pos, blockEntity);
        }
    }

    /**
     * Creates a linear slope from neighbor blocks and collapsible corners (1.12.2's onBlockPlacedBy).
     */
    private void slopeFromNeighbors(Level level, BlockPos pos, CbBlockEntity blockEntity) {
        // Minimum and maximum height of corners
        final int MIN_HEIGHT = 0;
        final int MAX_HEIGHT = 16;

        // Find slopes in landscape
        int xn = scanX(level, pos, -1, MAX_HEIGHT);
        int xp = scanX(level, pos, 1, MAX_HEIGHT);
        int zn = scanZ(level, pos, -1, MAX_HEIGHT);
        int zp = scanZ(level, pos, 1, MAX_HEIGHT);

        CbBlockEntity blockEntityXN = getBlockEntity(level, pos.offset(-1, 0, 0));
        CbBlockEntity blockEntityXP = getBlockEntity(level, pos.offset(1, 0, 0));
        CbBlockEntity blockEntityZN = getBlockEntity(level, pos.offset(0, 0, -1));
        CbBlockEntity blockEntityZP = getBlockEntity(level, pos.offset(0, 0, 1));

        int hxn1, hxn2;
        if (blockEntityXN != null && isSameOrientation(blockEntity, blockEntityXN)) {
            hxn1 = util(blockEntityXN).getQuadDepth(QUAD_XZPN);
            hxn2 = util(blockEntityXN).getQuadDepth(QUAD_XZPP);
        } else {
            hxn1 = hxn2 = getBlockHeight(level, pos.offset(-1, 0, 0));
        }
        int hxp1, hxp2;
        if (blockEntityXP != null && isSameOrientation(blockEntity, blockEntityXP)) {
            hxp1 = util(blockEntityXP).getQuadDepth(QUAD_XZNN);
            hxp2 = util(blockEntityXP).getQuadDepth(QUAD_XZNP);
        } else {
            hxp1 = hxp2 = getBlockHeight(level, pos.offset(1, 0, 0));
        }
        int hzn1, hzn2;
        if (blockEntityZN != null && isSameOrientation(blockEntity, blockEntityZN)) {
            hzn1 = util(blockEntityZN).getQuadDepth(QUAD_XZNP);
            hzn2 = util(blockEntityZN).getQuadDepth(QUAD_XZPP);
        } else {
            hzn1 = hzn2 = getBlockHeight(level, pos.offset(0, 0, -1));
        }
        int hzp1, hzp2;
        if (blockEntityZP != null && isSameOrientation(blockEntity, blockEntityZP)) {
            hzp1 = util(blockEntityZP).getQuadDepth(QUAD_XZNN);
            hzp2 = util(blockEntityZP).getQuadDepth(QUAD_XZPN);
        } else {
            hzp1 = hzp2 = getBlockHeight(level, pos.offset(0, 0, 1));
        }

        // Lerp between heights, create smooth slope
        int heightXZNN = MIN_HEIGHT, heightXZPN = MIN_HEIGHT, heightXZPP = MIN_HEIGHT, heightXZNP = MIN_HEIGHT;
        int xdist = 1;
        double dx1 = (double) (hxp1 - hxn1) / (xp - xn - 1);
        double dx2 = (double) (hxp2 - hxn2) / (xp - xn - 1);
        heightXZNN = Math.max(heightXZNN, (int) (hxn1 + dx1 * (xdist - 1)));
        heightXZNP = Math.max(heightXZNP, (int) (hxn2 + dx2 * (xdist - 1)));
        heightXZPN = Math.max(heightXZPN, (int) (hxn1 + dx1 * xdist));
        heightXZPP = Math.max(heightXZPP, (int) (hxn2 + dx2 * xdist));

        int zdist = 1;
        double dz1 = (double) (hzp1 - hzn1) / (zp - zn - 1);
        double dz2 = (double) (hzp2 - hzn2) / (zp - zn - 1);
        heightXZNN = Math.max(heightXZNN, (int) (hzn1 + dz1 * (zdist - 1)));
        heightXZNP = Math.max(heightXZNP, (int) (hzn1 + dz1 * zdist));
        heightXZPN = Math.max(heightXZPN, (int) (hzn2 + dz2 * (zdist - 1)));
        heightXZPP = Math.max(heightXZPP, (int) (hzn2 + dz2 * zdist));

        CollapsibleUtil.setQuadDepth(blockEntity, QUAD_XZNN, heightXZNN);
        CollapsibleUtil.setQuadDepth(blockEntity, QUAD_XZNP, heightXZNP);
        CollapsibleUtil.setQuadDepth(blockEntity, QUAD_XZPP, heightXZPP);
        CollapsibleUtil.setQuadDepth(blockEntity, QUAD_XZPN, heightXZPN);

        for (int quad = 0; quad < 4; ++quad) {
            smoothAdjacentCollapsibles(blockEntity, quad);
        }
    }

    /**
     * Returns true if a slope should end at the position.
     */
    private boolean isSlopeBoundary(Level level, BlockPos pos) {
        if (getBlockEntity(level, pos) != null) {
            return true;
        }
        return level.getBlockState(pos).isSolid() || level.getBlockState(pos.below()).isSolid();
    }

    /**
     * Scans the X axis for slopes.
     */
    private int scanX(Level level, BlockPos pos, int dir, int maxDist) {
        for (int nx = pos.getX() + dir; nx != pos.getX() + maxDist * dir; nx += dir) {
            if (isSlopeBoundary(level, new BlockPos(nx, pos.getY(), pos.getZ()))) {
                return nx;
            }
        }
        return pos.getX() + dir;
    }

    /**
     * Scans the Z axis for slopes.
     */
    private int scanZ(Level level, BlockPos pos, int dir, int maxDist) {
        for (int nz = pos.getZ() + dir; nz != pos.getZ() + maxDist * dir; nz += dir) {
            if (isSlopeBoundary(level, new BlockPos(pos.getX(), pos.getY(), nz))) {
                return nz;
            }
        }
        return pos.getZ() + dir;
    }

    /**
     * Returns a block's height in sixteenths.
     */
    private static int getBlockHeight(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        if (!blockState.isSolid()) {
            return 0;
        }
        VoxelShape shape = blockState.getShape(level, pos);
        return shape.isEmpty() ? 0 : (int) (shape.max(Direction.Axis.Y) * 16.0D);
    }

}
