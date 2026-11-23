package com.carpentersblocks.block;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.carpentersblocks.CbConfig;
import com.carpentersblocks.block.data.SlopeData;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.item.ItemBlockCarpentersSlope;
import com.carpentersblocks.util.RotationUtil.Rotation;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.block.SlopeShape;

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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Carpenter's Slope: wedges, oblique slopes and prisms, turned to face the way they are placed. Entities walk up the
 * stepped collision like stairs; the outline is the whole block, like 1.12.2.
 * <p>
 * Data: the rotation (bits 0-5) and the slope type (bits 6-10).
 */
public class BlockCarpentersSlope extends BlockCoverable {

    // Clicked face and hit picked in getStateForPlacement, applied in setPlacedBy once the block entity exists
    private static final ThreadLocal<Direction> _threadLocalFacing = new ThreadLocal<>();
    private static final ThreadLocal<Vec3> _threadLocalHit = new ThreadLocal<>();

    private final Map<Integer, VoxelShape> _collisionShapes = new ConcurrentHashMap<>();

    public BlockCarpentersSlope(Properties properties) {
        super(properties);
    }

    /**
     * Alters block direction: turns about the axis of the clicked face.
     */
    @Override
    protected boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        Direction face = getClickFace();
        if (face == null) {
            return false;
        }
        SlopeData.rotate(blockEntity, face.getAxis());
        return true;
    }

    /**
     * Alters block type.
     */
    @Override
    protected boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        SlopeData.setNextType(blockEntity);
        return true;
    }

    /**
     * Damages hammer with a chance to not damage.
     */
    @Override
    protected void damageItemWithChance(Level level, Player player, InteractionHand hand) {
        if (level.getRandom().nextFloat() <= CbConfig.HAMMER_DAMAGE_CHANCE_SLOPES.get()) {
            super.damageItemWithChance(level, player, hand);
        }
    }

    @Override
    public boolean supportsSideCovers() {
        return true;
    }

    /**
     * Side covers only go on the slope's full, flat faces, like 1.7.10.
     */
    @Override
    protected boolean canSupportCover(CbBlockEntity blockEntity, EnumAttributeLocation location) {
        if (EnumAttributeLocation.HOST.equals(location)) {
            return true;
        }
        int cbMetadata = blockEntity.getCbMetadata();
        return SlopeShape.isFaceFull(SlopeData.getType(cbMetadata), Rotation.get(cbMetadata), location.getDirection());
    }

    /**
     * The stepped boxes, so entities can walk up slopes like stairs.
     */
    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return Shapes.block();
        }
        int cbMetadata = blockEntity.getCbMetadata();
        return _collisionShapes.computeIfAbsent(cbMetadata & 0x7ff, data -> {
            VoxelShape shape = Shapes.empty();
            for (AABB box : SlopeShape.getBoxes(SlopeData.getType(data), Rotation.get(data))) {
                shape = Shapes.or(shape, Shapes.create(box));
            }
            return shape.optimize();
        });
    }

    @Override
    public int getInventoryRenderData(ItemStack itemStack) {
        // Turned a quarter, like 1.12.2's inventory slopes
        return SlopeData.withType(Rotation.X0_Y90_Z0.asInt(), ItemBlockCarpentersSlope.getSlopeType(itemStack));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        _threadLocalFacing.set(context.getClickedFace());
        _threadLocalHit.set(context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos())));
        return super.getStateForPlacement(context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, blockState, placer, itemStack);
        Direction facing = _threadLocalFacing.get();
        Vec3 hit = _threadLocalHit.get();
        _threadLocalFacing.remove();
        _threadLocalHit.remove();
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (level.isClientSide() || blockEntity == null) {
            return;
        }
        // Placed without getStateForPlacement (machines, fake players): treat as placed on top of a block
        if (facing == null || hit == null) {
            facing = Direction.UP;
            hit = new Vec3(0.5D, 1.0D, 0.5D);
        }
        Direction look = placer != null ? placer.getDirection() : Direction.NORTH;
        Rotation rotation = getPlacementRotation(facing, (float) hit.x, (float) hit.y, (float) hit.z, look);
        blockEntity.setCbMetadata(SlopeData.withType(rotation.asInt(), ItemBlockCarpentersSlope.getSlopeType(itemStack)));
    }

    /**
     * Gets the slope rotation for where it was placed (1.12.2's onBlockPlacedBy).
     *
     * @param facing the clicked face
     * @param look the direction the placer looks
     */
    private static Rotation getPlacementRotation(Direction facing, float x, float y, float z, Direction look) {
        switch (facing) {
            case DOWN:
                if (x >= 0.2f && x <= 0.8f && z >= 0.2f && z <= 0.8f) {
                    return switch (look) {
                        case SOUTH -> Rotation.X180_Y0_Z0;
                        case WEST -> Rotation.X180_Y90_Z0;
                        case EAST -> Rotation.X180_Y270_Z0;
                        default -> Rotation.X0_Y0_Z180;
                    };
                } else if (1.0f - x > z && x <= 0.5f && x < z) {
                    return Rotation.X180_Y90_Z0;
                } else if (z > x && z >= 0.5f) {
                    return Rotation.X180_Y0_Z0;
                } else if (x > 1.0f - z) {
                    return Rotation.X180_Y270_Z0;
                }
                return Rotation.X0_Y0_Z180;
            case UP:
                if (x >= 0.2f && x <= 0.8f && z >= 0.2f && z <= 0.8f) {
                    return switch (look) {
                        case SOUTH -> Rotation.X0_Y180_Z0;
                        case WEST -> Rotation.X0_Y270_Z0;
                        case EAST -> Rotation.X0_Y90_Z0;
                        default -> Rotation.X0_Y0_Z0;
                    };
                } else if (x < z && x <= 0.5f && 1.0f - x > z) {
                    return Rotation.X0_Y270_Z0;
                } else if (1.0f - z > x && z <= 0.5f) {
                    return Rotation.X0_Y0_Z0;
                } else if (x > z) {
                    return Rotation.X0_Y90_Z0;
                }
                return Rotation.X0_Y180_Z0;
            case NORTH:
                if (x >= 0.2f && x <= 0.8f) {
                    return y >= 0.5f ? Rotation.X90_Y0_Z180 : Rotation.X90_Y0_Z0;
                } else if (x > y && x >= 0.5f && x > 1.0f - y) {
                    return Rotation.X90_Y0_Z270;
                } else if (y > 1.0f - x && y >= 0.5f) {
                    return Rotation.X90_Y0_Z180;
                } else if (x < y) {
                    return Rotation.X90_Y0_Z90;
                }
                return Rotation.X90_Y0_Z0;
            case SOUTH:
                if (x >= 0.2f && x <= 0.8f) {
                    return y >= 0.5f ? Rotation.X270_Y0_Z0 : Rotation.X90_Y180_Z0;
                } else if (1.0f - x > y && x <= 0.5f && x < y) {
                    return Rotation.X90_Y180_Z90;
                } else if (y > x && y >= 0.5f) {
                    return Rotation.X270_Y0_Z0;
                } else if (x > 1.0f - y) {
                    return Rotation.X270_Y0_Z90;
                }
                return Rotation.X90_Y180_Z0;
            case WEST:
                if (z >= 0.2f && z <= 0.8f) {
                    return y >= 0.5f ? Rotation.X270_Y90_Z0 : Rotation.X90_Y270_Z0;
                } else if (1.0f - z > y && z <= 0.5f && z < y) {
                    return Rotation.X0_Y0_Z270;
                } else if (y > z && y >= 0.5f) {
                    return Rotation.X270_Y90_Z0;
                } else if (z > 1.0f - y) {
                    return Rotation.X180_Y0_Z90;
                }
                return Rotation.X90_Y270_Z0;
            default: // EAST
                if (z >= 0.2f && z <= 0.8f) {
                    return y >= 0.5f ? Rotation.X0_Y270_Z90 : Rotation.X90_Y90_Z0;
                } else if (z > y && z >= 0.5f && z > 1.0f - y) {
                    return Rotation.X0_Y180_Z90;
                } else if (y > 1.0f - z && y >= 0.5f) {
                    return Rotation.X0_Y270_Z90;
                } else if (z < y) {
                    return Rotation.X0_Y0_Z90;
                }
                return Rotation.X90_Y90_Z0;
        }
    }

}
