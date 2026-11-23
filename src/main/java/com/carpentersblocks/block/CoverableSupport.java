package com.carpentersblocks.block;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.carpentersblocks.api.ICarpentersChisel;
import com.carpentersblocks.api.ICarpentersHammer;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.blockentity.CbRenderData;
import com.carpentersblocks.util.attribute.AttributeHelper;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Helpers shared by every Carpenter's block ({@link ICoverableBlock}), whatever block class it extends.
 */
public final class CoverableSupport {

    // The click being handled, for hammer clicks that depend on the face or corner hit (slopes, collapsible blocks)
    static final ThreadLocal<BlockHitResult> CLICK_HIT = new ThreadLocal<>();

    // Shapes of IShapedBlock blocks by block and render data, made from their boxes
    private static final Map<Block, Map<Integer, VoxelShape>> OUTLINE_SHAPES = new ConcurrentHashMap<>();
    private static final Map<Block, Map<Integer, VoxelShape>> COLLISION_SHAPES = new ConcurrentHashMap<>();

    private CoverableSupport() {}

    /**
     * Gets the face of the click being handled, or null.
     */
    @Nullable
    public static Direction getClickFace() {
        BlockHitResult hit = CLICK_HIT.get();
        return hit != null ? hit.getDirection() : null;
    }

    /**
     * Gets where the click being handled hit, relative to the block (0 to 1), or null.
     */
    @Nullable
    public static Vec3 getClickHit() {
        BlockHitResult hit = CLICK_HIT.get();
        return hit != null ? hit.getLocation().subtract(Vec3.atLowerCornerOf(hit.getBlockPos())) : null;
    }

    /**
     * Returns the block entity if it is a Carpenter's block entity, whatever the block.
     */
    @Nullable
    public static CbBlockEntity getSimpleBlockEntity(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CbBlockEntity blockEntity ? blockEntity : null;
    }

    /**
     * Gets the block data of the Carpenter's block at a position, or 0: from the snapshot taken for chunk meshing if
     * there is one, else from the block entity.
     */
    public static int getCbMetadataAt(BlockGetter level, BlockPos pos) {
        CbRenderData data = level.getModelData(pos).get(CbRenderData.PROPERTY);
        if (data != null) {
            return data.cbMetadata();
        }
        CbBlockEntity blockEntity = getSimpleBlockEntity(level, pos);
        return blockEntity != null ? blockEntity.getCbMetadata() : 0;
    }

    /**
     * Gets the attributes of the Carpenter's block at a position: from the snapshot taken for chunk meshing if there
     * is one, else from the block entity.
     */
    public static AttributeHelper getAttributeHelperAt(BlockGetter level, BlockPos pos) {
        CbRenderData data = level.getModelData(pos).get(CbRenderData.PROPERTY);
        if (data != null) {
            return data.getAttributeHelper();
        }
        CbBlockEntity blockEntity = getSimpleBlockEntity(level, pos);
        return blockEntity != null ? blockEntity.getAttributeHelper() : new AttributeHelper(Map.of());
    }

    /**
     * Gets the cover at a location, or null.
     */
    @Nullable
    public static BlockState getCover(CbBlockEntity blockEntity, EnumAttributeLocation location) {
        return blockEntity.getAttributeHelper().getBlockState(location, EnumAttributeType.COVER);
    }

    /**
     * Breaks the block as if mined, dropping it and its attributes (1.12.2's destroyBlock).
     */
    public static void breakBlock(Level level, BlockPos pos) {
        level.destroyBlock(pos, true);
    }

    /**
     * Sends a message from the language files (1.12.2's ChatHandler).
     */
    public static void sendMessage(Player player, String key) {
        player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.GOLD));
    }

    public static boolean isCarpentersTool(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item instanceof ICarpentersHammer || item instanceof ICarpentersChisel;
    }

    static VoxelShape getOutlineShape(Block block, int renderData, Function<Integer, List<AABB>> boxes) {
        return OUTLINE_SHAPES.computeIfAbsent(block, key -> new ConcurrentHashMap<>()).computeIfAbsent(renderData, data -> toShape(boxes.apply(data)));
    }

    static VoxelShape getCollisionShape(Block block, int renderData, Function<Integer, List<AABB>> boxes) {
        return COLLISION_SHAPES.computeIfAbsent(block, key -> new ConcurrentHashMap<>()).computeIfAbsent(renderData, data -> toShape(boxes.apply(data)));
    }

    private static VoxelShape toShape(List<AABB> boxes) {
        VoxelShape shape = Shapes.empty();
        for (AABB box : boxes) {
            shape = Shapes.or(shape, Shapes.create(box));
        }
        return shape.optimize();
    }

}
