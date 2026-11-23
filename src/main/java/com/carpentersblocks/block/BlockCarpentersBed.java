package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.registry.CbItems;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.DesignHandler.DesignType;
import com.carpentersblocks.util.handler.DyeHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.util.TriState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Carpenter's Bed, ported from 1.7.10: two blocks, placed with {@link com.carpentersblocks.item.ItemCarpentersBed}.
 * Dye the foot to color the blanket; the hammer gives it a design.
 * <p>
 * Players only sleep in vanilla bed blocks in 26.3, so this is an AbstractBedBlock (the facing, part and occupied
 * properties are its blockstate, and vanilla does the sleeping, waking, spawning and removing the other half) with the
 * Carpenter's behavior added through {@link ICoverableBlock}. The block data keeps the cover and design attributes.
 */
public class BlockCarpentersBed extends AbstractBedBlock implements ICoverableBlock, IShapedBlock {

    // Render data, worked out from the blockstate (the 1.12.2 block data layout)
    private static final int DIR_MASK = 0x3;
    private static final int OCCUPIED_BIT = 0x10;
    private static final int HEAD_BIT = 0x8000;
    private static final int PARALLEL_NEG = 1 << 16;
    private static final int PARALLEL_POS = 1 << 17;
    private static final int BLANKET_COLOR_SHIFT = 18; // 5 bits: dye color + 1, 0 for white
    private static final int INVENTORY = 1 << 23;
    private static final int DESIGN_SHIFT = 24; // 8 bits: design number, 0 for none

    // Bed design pieces (0-7) of the blanket faces in authored order (down, up, north, south, west, east), from 1.7.10
    private static final int[] DESIGN_HEAD_FACES = { 2, 2, 2, 7, 1, 3 };
    private static final int[] DESIGN_FOOT_FACES = { 5, 5, 2, 7, 4, 6 };

    private static final AABB BOUNDS = new AABB(0, 0, 0, 1, 0.625, 1);

    public BlockCarpentersBed(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
        return new CbBlockEntity(pos, blockState);
    }

    /**
     * Gets the direction from the head to the foot, from render data.
     */
    private static Direction getDirection(int data) {
        return Direction.from2DDataValue(data & DIR_MASK);
    }

    private static boolean isHead(int data) {
        return (data & HEAD_BIT) != 0;
    }

    private static boolean isOccupied(int data) {
        return (data & OCCUPIED_BIT) != 0;
    }

    /**
     * Gets the other half of a bed.
     */
    private static BlockPos getOtherHalfPos(BlockState blockState, BlockPos pos) {
        return pos.relative(getConnectedDirection(blockState));
    }

    ////////////
    // Shape
    ////////////

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return Collections.singletonList(BOUNDS);
    }

    @Override
    public int getInventoryRenderData(int itemDamage) {
        return INVENTORY | HEAD_BIT;
    }

    @Override
    public int getInventoryRenderData(ItemStack itemStack) {
        return getInventoryRenderData(0);
    }

    @Override
    public int getRenderData(BlockGetter level, BlockPos pos, int cbMetadata) {
        BlockState blockState = level.getBlockState(pos);
        if (!blockState.is(this)) {
            return INVENTORY | HEAD_BIT;
        }
        Direction direction = blockState.getValue(FACING).getOpposite();
        boolean head = blockState.getValue(PART) == BedPart.HEAD;
        int data = direction.get2DDataValue() | (head ? HEAD_BIT : 0) | (blockState.getValue(OCCUPIED) ? OCCUPIED_BIT : 0);
        if (isParallelBed(level, pos.relative(direction.getClockWise()), blockState)) {
            data |= PARALLEL_NEG;
        }
        if (isParallelBed(level, pos.relative(direction.getCounterClockWise()), blockState)) {
            data |= PARALLEL_POS;
        }
        // The foot's dye colors the blanket
        BlockPos footPos = head ? getOtherHalfPos(blockState, pos) : pos;
        DyeColor color = DyeHandler.getDyeColor(CoverableSupport.getAttributeHelperAt(level, footPos).getItemStack(EnumAttributeLocation.HOST, EnumAttributeType.DYE));
        if (color != null) {
            data |= (color.getId() + 1) << BLANKET_COLOR_SHIFT;
        }
        String design = CoverableSupport.getAttributeHelperAt(level, pos).getString(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC);
        data |= DesignHandler.getDesignNumber(DesignType.BED, design) << DESIGN_SHIFT;
        return data;
    }

    private boolean isParallelBed(BlockGetter level, BlockPos pos, BlockState blockState) {
        BlockState other = level.getBlockState(pos);
        return other.is(this) && other.getValue(PART) == blockState.getValue(PART) && other.getValue(FACING) == blockState.getValue(FACING);
    }

    /**
     * Gets where a face of a part authored with the foot to the south ends up once rotated like the part.
     */
    private static Direction rotateFace(Direction authoredFace, Direction direction) {
        Vec3 point = SidedUtil.rotate(new Vec3(0.5 + 0.5 * authoredFace.getStepX(), 0.5 + 0.5 * authoredFace.getStepY(), 0.5 + 0.5 * authoredFace.getStepZ()), direction);
        return Direction.getApproximateNearest(point.x - 0.5, point.y - 0.5, point.z - 0.5);
    }

    /**
     * Gets the design pieces of the blanket faces, by world facing.
     */
    private static Map<Direction, Integer> getDesignBlanketFaces(boolean head, Direction direction) {
        int[] faces = head ? DESIGN_HEAD_FACES : DESIGN_FOOT_FACES;
        Map<Direction, Integer> pieces = new EnumMap<>(Direction.class);
        for (Direction authoredFace : Direction.values()) {
            pieces.put(rotateFace(authoredFace, direction), faces[authoredFace.ordinal()]);
        }
        return pieces;
    }

    @Override
    public List<ShapePart> getRenderParts(int renderData) {
        boolean head = isHead(renderData);
        boolean inventory = (renderData & INVENTORY) != 0;
        boolean parallelNeg = (renderData & PARALLEL_NEG) != 0;
        boolean parallelPos = (renderData & PARALLEL_POS) != 0;
        Direction direction = inventory ? Direction.SOUTH : getDirection(renderData);
        int colorIdx = (renderData >> BLANKET_COLOR_SHIFT) & 0x1f;
        Identifier blanket = colorIdx == 0 ? ShapeTextures.WOOL_WHITE
            : Identifier.withDefaultNamespace("block/" + DyeColor.byId(colorIdx - 1).getSerializedName() + "_wool");
        double yTop = isOccupied(renderData) ? 0.875 : 0.625;
        String design = inventory ? null : DesignHandler.getDesign(DesignType.BED, renderData >>> DESIGN_SHIFT);
        Identifier designTexture = design != null ? DesignHandler.getBedTexture(design) : null;

        // Authored for a bed whose foot is to the south
        List<ShapePart> parts = new ArrayList<>();
        List<AABB> frame = new ArrayList<>();
        List<AABB> blanketBoxes = new ArrayList<>();
        double mattressX0 = parallelNeg ? 0 : 0.0625;
        double mattressX1 = parallelPos ? 1 : 0.9375;
        AABB mattress;
        if (head) {
            frame.add(new AABB(0.125, 0.1875, 0, 0.875, 0.875, 0.125));
            frame.add(new AABB(0, 0.1875, 0.125, 1, 0.3125, 1));
            frame.add(new AABB(0, parallelNeg ? 0.1875 : 0, 0, 0.125, parallelNeg ? 0.875 : 1, 0.125));
            frame.add(new AABB(0.875, parallelPos ? 0.1875 : 0, 0, 1, parallelPos ? 0.875 : 1, 0.125));
            blanketBoxes.add(new AABB(0.0625, yTop - 0.0625, 0.5, 0.9375, yTop, 1));
            blanketBoxes.add(new AABB(0, yTop - 0.3125, 0.5, 0.0625, yTop, 1));
            blanketBoxes.add(new AABB(0.9375, yTop - 0.3125, 0.5, 1, yTop, 1));
            mattress = new AABB(mattressX0, 0.3125, 0.125, mattressX1, 0.5625, 1);
            AABB pillow = SidedUtil.rotate(new AABB(0.125, 0.5625, 0.1875, 0.875, 0.6875, 0.5625), direction);
            parts.add(designTexture == null ? ShapePart.textured(pillow, ShapeTextures.BED_PILLOW)
                : ShapePart.textured(pillow, designTexture).withDesignPieces(0, null).withUVRotation(direction));
        } else {
            frame.add(new AABB(0, 0.1875, 0, 1, 0.3125, 1));
            if (!parallelNeg) {
                frame.add(new AABB(0, 0, 0.875, 0.125, 0.1875, 1));
            }
            if (!parallelPos) {
                frame.add(new AABB(0.875, 0, 0.875, 1, 0.1875, 1));
            }
            blanketBoxes.add(new AABB(0.0625, yTop - 0.0625, 0, 0.9375, yTop, 0.9375));
            blanketBoxes.add(new AABB(0, yTop - 0.3125, 0, 0.0625, yTop, 1));
            blanketBoxes.add(new AABB(0.9375, yTop - 0.3125, 0, 1, yTop, 1));
            blanketBoxes.add(new AABB(0.0625, yTop - 0.3125, 0.9375, 0.9375, yTop, 1));
            mattress = new AABB(mattressX0, 0.3125, 0, mattressX1, 0.5625, 0.9375);
        }
        for (AABB box : frame) {
            parts.add(ShapePart.covered(SidedUtil.rotate(box, direction)));
        }
        for (AABB box : blanketBoxes) {
            if (designTexture == null) {
                parts.add(ShapePart.textured(SidedUtil.rotate(box, direction), blanket));
            } else {
                // A design replaces the blanket color, its pieces picked per face like 1.7.10
                parts.add(ShapePart.textured(SidedUtil.rotate(box, direction), designTexture)
                    .withDesignPieces(head ? 2 : 5, getDesignBlanketFaces(head, direction))
                    .withUVRotation(direction));
            }
        }
        parts.add(ShapePart.textured(SidedUtil.rotate(mattress, direction), ShapeTextures.WOOL_WHITE));
        return parts;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        return coverableGetShape(level, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        return coverableGetCollisionShape(level, pos);
    }

    ////////////
    // Designs
    ////////////

    @Nullable
    private static String getDesign(CbBlockEntity blockEntity) {
        return blockEntity.getAttributeHelper().getString(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC);
    }

    private static void setDesign(CbBlockEntity blockEntity, @Nullable String design) {
        blockEntity.removeAttribute(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC);
        if (design != null) {
            blockEntity.addAttribute(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC, design);
        }
        blockEntity.update(true);
    }

    /**
     * Sets the blanket design on both halves, like 1.7.10.
     */
    private boolean setBedDesign(CbBlockEntity blockEntity, @Nullable String design) {
        String current = getDesign(blockEntity);
        if (design == null ? current == null : design.equals(current)) {
            return false;
        }
        setDesign(blockEntity, design);
        CbBlockEntity other = getBlockEntity(blockEntity.getLevel(), getOtherHalfPos(blockEntity.getBlockState(), blockEntity.getBlockPos()));
        if (other != null) {
            setDesign(other, design);
        }
        return true;
    }

    /**
     * Hammer left click: previous design.
     */
    @Override
    public boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        return setBedDesign(blockEntity, DesignHandler.getPrev(DesignType.BED, getDesign(blockEntity)));
    }

    /**
     * Hammer right click: next design, or none when sneaking.
     */
    @Override
    public boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        return setBedDesign(blockEntity, player.isShiftKeyDown() ? null : DesignHandler.getNext(DesignType.BED, getDesign(blockEntity)));
    }

    ////////////
    // Sleeping (AbstractBedBlock), placement and drops
    ////////////

    @Override
    protected EnvironmentAttribute<BedRule> getBedEnvironmentAttribute() {
        return EnvironmentAttributes.BED_RULE;
    }

    /**
     * Beds explode where you can't sleep, like vanilla.
     */
    @Override
    protected InteractionResult destroyOnUse(BlockState blockState, Level level, BlockPos pos, Player player) {
        level.removeBlock(pos, false);
        BlockPos otherPos = pos.relative(blockState.getValue(FACING).getOpposite());
        if (level.getBlockState(otherPos).is(this)) {
            level.removeBlock(otherPos, false);
        }
        Vec3 boomPos = Vec3.atCenterOf(pos);
        level.explode(null, level.damageSources().badRespawnPointExplosion(boomPos), null, boomPos, 5.0F, true, Level.ExplosionInteraction.BLOCK);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected void destroyOnLeave(Level level, BlockPos pos) {}

    /**
     * The bed block has no item of its own: pick the bed item.
     */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState blockState, boolean includeData, Player player) {
        return CbItems.CARPENTERS_BED != null ? new ItemStack(CbItems.CARPENTERS_BED.get()) : ItemStack.EMPTY;
    }

    ////////////
    // ICoverableBlock forwarding (see BlockCoverable)
    ////////////

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return coverableUseItemOn(itemStack, level, pos, player, hand, hitResult);
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(blockState, level, pos, block, orientation, movedByPiston);
        coverableNeighborChanged(blockState, level, pos);
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return coverableGetSignal(level, pos, direction);
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return coverableGetDirectSignal(level, pos, direction);
    }

    @Override
    public boolean hasDynamicLightEmission(BlockState blockState) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState blockState, BlockGetter level, BlockPos pos) {
        return coverableGetLightEmission(level, pos);
    }

    @Override
    protected float getDestroyProgress(BlockState blockState, Player player, BlockGetter level, BlockPos pos) {
        float progress = coverableGetDestroyProgress(player, level, pos);
        return Float.isNaN(progress) ? super.getDestroyProgress(blockState, player, level, pos) : progress;
    }

    @Override
    public SoundType getSoundType(BlockState blockState, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        SoundType soundType = coverableGetSoundType(level, pos);
        return soundType != null ? soundType : super.getSoundType(blockState, level, pos, entity);
    }

    @Override
    public BlockState getAppearance(BlockState blockState, BlockAndLightGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        return coverableGetAppearance(blockState, level, pos, side);
    }

    @Override
    public int getFlammability(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return 20;
    }

    @Override
    public int getFireSpreadSpeed(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        coverableSetPlacedBy(level, pos, placer);
    }


    @Override
    public TriState canSustainPlant(BlockState blockState, BlockGetter level, BlockPos soilPos, Direction facing, BlockState plant) {
        TriState decision = coverableCanSustainPlant(blockState, level, soilPos, facing, plant);
        return decision.isDefault() ? super.canSustainPlant(blockState, level, soilPos, facing, plant) : decision;
    }

}
