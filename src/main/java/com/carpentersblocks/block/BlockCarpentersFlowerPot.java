package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.api.ICarpentersHammer;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.util.attribute.AttributeHelper;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.block.FlowerPotUtil;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.DesignHandler.DesignType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Carpenter's Flower Pot, ported from 1.7.10: holds soil and a plant, bone meal tints the plant.
 * <p>
 * Soil, plant and fertilizer are block attributes. Render data bit 16: has a plant; bit 17: the plant is tall; bits
 * 18-25: the design number. The client draws the soil and plant (the "flower_pot" shape).
 */
public class BlockCarpentersFlowerPot extends BlockCoverable implements IShapedBlock {

    public static final int HAS_PLANT = 1 << 16;
    public static final int TALL_PLANT = 1 << 17;
    private static final int DESIGN_SHIFT = 18; // 8 bits: design number, 0 for none
    private static final int DESIGN_MASK = 0xFF;

    private static final AABB POT_BOUNDS = new AABB(0.3125, 0, 0.3125, 0.6875, 0.375, 0.6875);
    private static final AABB SOIL_BOX = new AABB(0.375, 0.0625, 0.375, 0.625, 0.25, 0.625);

    public BlockCarpentersFlowerPot(Properties properties) {
        super(properties);
    }

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        List<AABB> boxes = new ArrayList<>();
        boxes.add(new AABB(0.375, 0, 0.375, 0.625, 0.0625, 0.625));
        boxes.add(new AABB(0.375, 0, 0.3125, 0.625, 0.375, 0.375));
        boxes.add(new AABB(0.375, 0, 0.625, 0.625, 0.375, 0.6875));
        boxes.add(new AABB(0.3125, 0, 0.3125, 0.375, 0.375, 0.6875));
        boxes.add(new AABB(0.625, 0, 0.3125, 0.6875, 0.375, 0.6875));
        return boxes;
    }

    @Override
    public List<AABB> getCollisionBoxes(int renderData) {
        return Collections.singletonList((renderData & TALL_PLANT) != 0 ? POT_BOUNDS.setMaxY(0.99) : POT_BOUNDS);
    }

    @Override
    public List<AABB> getSelectionBoxes(int renderData) {
        double maxY = (renderData & TALL_PLANT) != 0 ? 0.99 : (renderData & HAS_PLANT) != 0 ? 0.75 : 0.375;
        return Collections.singletonList(POT_BOUNDS.setMaxY(maxY));
    }

    public static AABB getSoilBox() {
        return SOIL_BOX;
    }

    @Override
    public int getRenderData(BlockGetter level, BlockPos pos, int cbMetadata) {
        int data = cbMetadata;
        AttributeHelper helper = getAttributeHelperAt(level, pos);
        ItemStack plant = helper.getItemStack(EnumAttributeLocation.HOST, EnumAttributeType.PLANT);
        if (!plant.isEmpty()) {
            data |= HAS_PLANT;
            BlockState plantState = FlowerPotUtil.getPlantState(plant);
            if (plantState != null && FlowerPotUtil.isTall(plantState)) {
                data |= TALL_PLANT;
            }
        }
        data |= DesignHandler.getDesignNumber(DesignType.FLOWERPOT, helper.getString(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC)) << DESIGN_SHIFT;
        return data;
    }

    ////////////
    // Designs
    ////////////

    @Nullable
    private static String getDesign(CbBlockEntity blockEntity) {
        return blockEntity.getAttributeHelper().getString(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC);
    }

    /**
     * Sets the pot design, pushing out the cover (a designed pot takes no cover), like 1.7.10.
     */
    private static boolean setDesign(CbBlockEntity blockEntity, @Nullable String design) {
        String current = getDesign(blockEntity);
        if (design == null ? current == null : design.equals(current)) {
            return false;
        }
        blockEntity.removeAttribute(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC);
        if (design != null) {
            blockEntity.addAttribute(EnumAttributeLocation.HOST, EnumAttributeType.DESIGN_BASIC, design);
            blockEntity.dropAttribute(EnumAttributeLocation.HOST, EnumAttributeType.COVER);
        }
        blockEntity.update(true);
        return true;
    }

    /**
     * Hammer left click: previous design.
     */
    @Override
    public boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        return setDesign(blockEntity, DesignHandler.getPrev(DesignType.FLOWERPOT, getDesign(blockEntity)));
    }

    /**
     * Hammer right click: next design, or none when sneaking.
     */
    @Override
    public boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        return setDesign(blockEntity, player.isShiftKeyDown() ? null : DesignHandler.getNext(DesignType.FLOWERPOT, getDesign(blockEntity)));
    }

    /**
     * A pot with a design takes no cover.
     */
    @Override
    public boolean canSupportCover(CbBlockEntity blockEntity, EnumAttributeLocation location) {
        return EnumAttributeLocation.HOST.equals(location) && getDesign(blockEntity) == null;
    }

    @Override
    public List<ShapePart> getRenderParts(int renderData) {
        String design = DesignHandler.getDesign(DesignType.FLOWERPOT, (renderData >> DESIGN_SHIFT) & DESIGN_MASK);
        List<ShapePart> parts = new ArrayList<>();
        for (AABB box : getShapeBoxes(renderData)) {
            parts.add(design == null ? ShapePart.covered(box) : ShapePart.textured(box, DesignHandler.getTexture(DesignType.FLOWERPOT, design)));
        }
        return parts;
    }

    ////////////
    // Placement
    ////////////

    private static boolean hasSupport(LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) || Block.canSupportCenter(level, below, Direction.UP);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return hasSupport(context.getLevel(), context.getClickedPos()) ? super.getStateForPlacement(context) : null;
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(blockState, level, pos, block, orientation, movedByPiston);
        if (!level.isClientSide() && level.getBlockState(pos).is(this) && !hasSupport(level, pos)) {
            breakBlock(level, pos);
        }
    }

    ////////////
    // Planting
    ////////////

    private static boolean isSoilArea(Direction face, @Nullable Vec3 hit) {
        return face == Direction.UP && hit != null && hit.x > 0.375D && hit.x < 0.625D && hit.z > 0.375D && hit.z < 0.625D;
    }

    @Override
    public boolean wouldPreActivate(CbBlockEntity blockEntity, ItemStack itemStack, Player player) {
        AttributeHelper helper = blockEntity.getAttributeHelper();
        boolean hasSoil = helper.hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.SOIL);
        boolean hasPlant = helper.hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.PLANT);
        if (hasPlant && itemStack.is(Items.BONE_MEAL)) {
            return !helper.hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.FERTILIZER);
        }
        if (hasSoil) {
            return !hasPlant && FlowerPotUtil.isPlant(itemStack) && FlowerPotUtil.getPlantState(itemStack) != null;
        }
        return FlowerPotUtil.isSoil(itemStack) && isSoilArea(getClickFace(), getClickHit());
    }

    /**
     * Adds soil, then a plant; bone meal tints the plant.
     */
    @Override
    public void preOnBlockActivated(CbBlockEntity blockEntity, Player player, net.minecraft.world.phys.BlockHitResult hitResult, ActionResult actionResult) {
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.isEmpty() || !wouldPreActivate(blockEntity, itemStack, player)) {
            return;
        }
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        AttributeHelper helper = blockEntity.getAttributeHelper();
        if (helper.hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.PLANT)) {
            // Bone meal
            blockEntity.addAttribute(EnumAttributeLocation.HOST, EnumAttributeType.FERTILIZER, itemStack);
            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 15);
        } else if (helper.hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.SOIL)) {
            blockEntity.addAttribute(EnumAttributeLocation.HOST, EnumAttributeType.PLANT, itemStack);
            level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        } else {
            blockEntity.addAttribute(EnumAttributeLocation.HOST, EnumAttributeType.SOIL, itemStack);
            level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        }
        actionResult.setAltered().decInventory().setNoSound();
    }

    /**
     * Sneak left click with a hammer takes out the fertilizer or plant (clicked high), or the soil.
     */
    @Override
    public void preOnBlockClicked(CbBlockEntity blockEntity, Player player, ActionResult actionResult) {
        if (!player.isShiftKeyDown() || !(player.getMainHandItem().getItem() instanceof ICarpentersHammer)) {
            return;
        }
        Vec3 hit = getClickHit();
        boolean high = hit == null || hit.y > 0.375D;
        EnumAttributeType[] order = high ? new EnumAttributeType[] { EnumAttributeType.FERTILIZER, EnumAttributeType.PLANT } : new EnumAttributeType[] { EnumAttributeType.SOIL };
        for (EnumAttributeType type : order) {
            if (blockEntity.dropAttribute(EnumAttributeLocation.HOST, type)) {
                actionResult.setAltered();
                return;
            }
        }
    }

    /**
     * Cacti still hurt.
     */
    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        ItemStack plant = blockEntity != null ? blockEntity.getAttributeHelper().getItemStack(EnumAttributeLocation.HOST, EnumAttributeType.PLANT) : ItemStack.EMPTY;
        BlockState plantState = plant.isEmpty() ? null : FlowerPotUtil.getPlantState(plant);
        if (plantState != null && plantState.getBlock() instanceof CactusBlock) {
            entity.hurtServer(serverLevel, level.damageSources().cactus(), 1.0F);
        }
    }

}
