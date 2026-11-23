package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carpentersblocks.CbConfig;
import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.blockentity.CbBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Carpenter's Torch, ported from 1.7.10: vanilla style torch or lantern, put out by rain.
 * <p>
 * Data bits 0-2: attach side. Bits 3-4: state (lit, smoldering, unlit). Bits 5-8: type (torch, lantern).
 */
public class BlockCarpentersTorch extends BlockSidedShaped {

    public static final int STATE_LIT = 0;
    public static final int STATE_SMOLDERING = 1;
    public static final int STATE_UNLIT = 2;

    public static final int TYPE_VANILLA = 0;
    public static final int TYPE_LANTERN = 1;
    private static final int NUM_TYPES = 2;

    private static final int SIDE_MASK = 0x7;
    private static final int STATE_MASK = 0x18;
    private static final int STATE_SHIFT = 3;
    private static final int TYPE_MASK = 0x1e0;
    private static final int TYPE_SHIFT = 5;

    private static final int LIGHT_LIT = 14;
    private static final int LIGHT_SMOLDERING = 9;

    public BlockCarpentersTorch(Properties properties) {
        super(properties);
    }

    public static int getState(int data) {
        int state = (data & STATE_MASK) >> STATE_SHIFT;
        return state <= STATE_UNLIT ? state : STATE_LIT;
    }

    public static int setState(int data, int state) {
        return (data & ~STATE_MASK) | (state << STATE_SHIFT);
    }

    public static int getType(int data) {
        int type = (data & TYPE_MASK) >> TYPE_SHIFT;
        return type < NUM_TYPES ? type : TYPE_VANILLA;
    }

    public static int setType(int data, int type) {
        return (data & ~TYPE_MASK) | (type << TYPE_SHIFT);
    }

    @Override
    public Direction getAttachSide(int data) {
        int side = data & SIDE_MASK;
        return side >= 1 && side <= 5 ? Direction.from3DDataValue(side) : Direction.UP;
    }

    @Override
    public int setAttachSide(int data, Direction side) {
        return (data & ~SIDE_MASK) | side.get3DDataValue();
    }

    /**
     * Torches stand on the floor or hang on walls, never from ceilings.
     */
    @Override
    protected boolean canAttachToSide(Direction side) {
        return side != Direction.DOWN;
    }

    ////////////
    // Shape
    ////////////

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return Collections.singletonList(getBounds(renderData));
    }

    private AABB getBounds(int data) {
        Direction side = getAttachSide(data);
        if (getType(data) == TYPE_LANTERN) {
            return side == Direction.UP
                ? new AABB(0.1875, 0, 0.1875, 0.8125, 0.9375, 0.8125)
                : SidedUtil.rotate(new AABB(0.1875, 0.25, 0, 0.8125, 0.9375, 0.8125), side);
        }
        return side == Direction.UP
            ? new AABB(0.4, 0, 0.4, 0.6, 0.6, 0.6)
            : SidedUtil.rotate(new AABB(0.35, 0.2, 0, 0.65, 0.8, 0.3), side);
    }

    @Override
    public List<AABB> getCollisionBoxes(int renderData) {
        return getType(renderData) == TYPE_LANTERN ? getShapeBoxes(renderData) : Collections.emptyList();
    }

    @Override
    public int getInventoryRenderData(int itemDamage) {
        return setAttachSide(0, Direction.UP);
    }

    @Override
    public List<ShapePart> getRenderParts(int renderData) {
        return getType(renderData) == TYPE_LANTERN ? getLanternParts(renderData) : getTorchParts(renderData);
    }

    private List<ShapePart> getTorchParts(int data) {
        Direction side = getAttachSide(data);
        int state = getState(data);
        Identifier head = state == STATE_LIT ? ShapeTextures.TORCH_HEAD_LIT : state == STATE_SMOLDERING ? ShapeTextures.TORCH_HEAD_SMOLDERING : ShapeTextures.TORCH_HEAD_UNLIT;
        List<ShapePart> parts = new ArrayList<>(2);
        parts.add(ShapePart.cuboid(placeTorch(SidedUtil.centeredCuboid(0.0625, 0, 0.5), side), null, false));
        parts.add(ShapePart.cuboid(placeTorch(SidedUtil.centeredCuboid(0.0625, 0.5, 0.625), side), head, state == STATE_LIT));
        return parts;
    }

    /**
     * Places the upright torch: standing on the floor, or leaning out from a wall (1.7.10 BlockHandlerCarpentersTorch).
     */
    private static Vec3[] placeTorch(Vec3[] corners, Direction side) {
        if (side == Direction.UP) {
            return SidedUtil.transform(corners, vec -> vec.add(0.5, 0, 0.5));
        }
        float yaw = side == Direction.NORTH ? 0 : side == Direction.SOUTH ? (float) Math.PI : side == Direction.WEST ? (float) (Math.PI / 2) : (float) (-Math.PI / 2);
        float tilt = (float) (-Math.PI * 3.4D / 9.0D);
        return SidedUtil.transform(corners, vec -> vec.add(0, 0, 0.0625).xRot(tilt).add(0, -0.4375, 0)
            .xRot((float) (Math.PI / 2)).yRot(yaw).add(0.5, 0.1875, 0.5));
    }

    private List<ShapePart> getLanternParts(int data) {
        Direction side = getAttachSide(data);
        List<ShapePart> parts = new ArrayList<>();
        if (side == Direction.UP) {
            parts.add(ShapePart.covered(new AABB(0.375, 0, 0.375, 0.625, 0.375, 0.625)));
        } else {
            parts.add(ShapePart.covered(SidedUtil.rotate(new AABB(0.4375, 0.375, 0, 0.5625, 0.75, 0.25), side)));
            parts.add(ShapePart.covered(SidedUtil.rotate(new AABB(0.375, 0.375, 0.25, 0.625, 0.75, 0.375), side)));
            parts.add(ShapePart.covered(new AABB(0.375, 0.25, 0.375, 0.625, 0.375, 0.625)));
        }
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            parts.add(ShapePart.covered(SidedUtil.rotate(new AABB(0.25, 0.25, 0.25, 0.375, 0.75, 0.375), facing)));
            parts.add(ShapePart.covered(SidedUtil.rotate(new AABB(0.25, 0.25, 0.375, 0.375, 0.375, 0.625), facing)));
            if (facing != side) {
                parts.add(ShapePart.translucent(SidedUtil.rotate(new AABB(0.375, 0.375, 0.3125, 0.625, 0.75, 0.328125), facing), ShapeTextures.LANTERN_GLASS));
            }
        }
        parts.add(ShapePart.covered(new AABB(0.1875, 0.75, 0.1875, 0.8125, 0.875, 0.8125)));
        parts.add(ShapePart.covered(new AABB(0.3125, 0.875, 0.3125, 0.6875, 0.9375, 0.6875)));
        parts.add(ShapePart.glowing(new AABB(0.4375, 0.375, 0.4375, 0.5625, 0.5, 0.5625), ShapeTextures.TORCH_HEAD_LIT));
        return parts;
    }

    ////////////
    // Light and weather
    ////////////

    @Override
    public int getLightValue(CbBlockEntity blockEntity, int attributeLight) {
        int data = blockEntity.getCbMetadata();
        // Lanterns always burn
        if (getType(data) == TYPE_LANTERN || getState(data) == STATE_LIT) {
            return LIGHT_LIT;
        }
        return getState(data) == STATE_SMOLDERING ? Math.max(LIGHT_SMOLDERING, attributeLight) : attributeLight;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return CbConfig.ENABLE_TORCH_WEATHER_EFFECTS.get();
    }

    /**
     * Rain puts torches out one step at a time; they come back once it stops.
     */
    @Override
    protected void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!CbConfig.ENABLE_TORCH_WEATHER_EFFECTS.get()) {
            return;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return;
        }
        int data = blockEntity.getCbMetadata();
        if (getType(data) != TYPE_VANILLA) {
            return;
        }
        boolean wet = level.isRainingAt(pos.above()) || level.isRainingAt(pos);
        int newState = switch (getState(data)) {
            case STATE_LIT -> wet ? STATE_SMOLDERING : STATE_LIT;
            case STATE_SMOLDERING -> wet ? STATE_UNLIT : STATE_LIT;
            default -> wet ? STATE_UNLIT : STATE_SMOLDERING;
        };
        setTorchState(blockEntity, newState);
    }

    private void setTorchState(CbBlockEntity blockEntity, int state) {
        int data = blockEntity.getCbMetadata();
        int oldState = getState(data);
        if (state == oldState) {
            return;
        }
        if (state > oldState) {
            Level level = blockEntity.getLevel();
            level.playSound(null, blockEntity.getBlockPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
        }
        blockEntity.setCbMetadata(setState(data, state));
    }

    private boolean isRelightItem(ItemStack itemStack) {
        return itemStack.is(Items.TORCH) || itemStack.is(asItem());
    }

    @Override
    public boolean wouldPreActivate(CbBlockEntity blockEntity, ItemStack itemStack, Player player) {
        return isRelightItem(itemStack) && getState(blockEntity.getCbMetadata()) != STATE_LIT;
    }

    /**
     * Relights the torch when right clicked with a torch.
     */
    @Override
    public void preOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {
        if (isRelightItem(player.getMainHandItem()) && getState(blockEntity.getCbMetadata()) != STATE_LIT) {
            setTorchState(blockEntity, STATE_LIT);
            actionResult.setAltered().setNoSound();
        }
    }

    /**
     * Hammer clicks switch between torch and lantern.
     */
    @Override
    public boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        int data = blockEntity.getCbMetadata();
        return blockEntity.setCbMetadata(setType(data, (getType(data) + 1) % NUM_TYPES));
    }

    @Override
    public boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        return onHammerLeftClick(blockEntity, player);
    }

    /**
     * Smoke and flame from the head (client).
     */
    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource random) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return;
        }
        int data = blockEntity.getCbMetadata();
        int state = getType(data) == TYPE_LANTERN ? STATE_LIT : getState(data);
        if (state == STATE_UNLIT) {
            return;
        }
        Vec3 head = getHeadPosition(data);
        double x = pos.getX() + head.x, y = pos.getY() + head.y, z = pos.getZ() + head.z;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        if (state == STATE_LIT) {
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }
    }

    private Vec3 getHeadPosition(int data) {
        if (getType(data) == TYPE_LANTERN) {
            return new Vec3(0.5, 0.5625, 0.5);
        }
        return switch (getAttachSide(data)) {
            case NORTH -> new Vec3(0.5, 0.92, 0.77);
            case SOUTH -> new Vec3(0.5, 0.92, 0.23);
            case WEST -> new Vec3(0.77, 0.92, 0.5);
            case EAST -> new Vec3(0.23, 0.92, 0.5);
            default -> new Vec3(0.5, 0.7, 0.5);
        };
    }

}
