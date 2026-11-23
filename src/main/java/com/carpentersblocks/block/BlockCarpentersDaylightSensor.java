package com.carpentersblocks.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.carpentersblocks.block.data.SidedUtil;
import com.carpentersblocks.blockentity.CbBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Carpenter's Daylight Sensor, ported from 1.7.10.
 * <p>
 * Data bits 0-3: light level. Bit 4: inverted polarity. Bits 5-6: sensitivity. Bits 7-9: attach side.
 */
public class BlockCarpentersDaylightSensor extends BlockSidedShaped {

    public static final int SENSITIVITY_SLEEP = 0;
    public static final int SENSITIVITY_MONSTERS = 1;
    public static final int SENSITIVITY_DYNAMIC = 2;

    private static final int LIGHT_MASK = 0xf;
    private static final int POLARITY_BIT = 0x10;
    private static final int SENSITIVITY_MASK = 0x60;
    private static final int SENSITIVITY_SHIFT = 5;
    private static final int SIDE_MASK = 0x380;
    private static final int SIDE_SHIFT = 7;

    private static final int UPDATE_TICKS = 20;

    public BlockCarpentersDaylightSensor(Properties properties) {
        super(properties);
    }

    @Override
    public Direction getAttachSide(int data) {
        int side = (data & SIDE_MASK) >> SIDE_SHIFT;
        return side >= 1 && side <= 5 ? Direction.from3DDataValue(side) : Direction.UP;
    }

    @Override
    public int setAttachSide(int data, Direction side) {
        return (data & ~SIDE_MASK) | (side.get3DDataValue() << SIDE_SHIFT);
    }

    @Override
    protected boolean canAttachToSide(Direction side) {
        return side != Direction.DOWN;
    }

    private static int getLight(int data) {
        return data & LIGHT_MASK;
    }

    private static boolean isInverted(int data) {
        return (data & POLARITY_BIT) != 0;
    }

    private static int getSensitivity(int data) {
        int sensitivity = (data & SENSITIVITY_MASK) >> SENSITIVITY_SHIFT;
        return sensitivity <= SENSITIVITY_DYNAMIC ? sensitivity : SENSITIVITY_SLEEP;
    }

    @Override
    protected int getPowerOutput(int data) {
        int light = getLight(data);
        boolean inverted = isInverted(data);
        return switch (getSensitivity(data)) {
            case SENSITIVITY_DYNAMIC -> inverted ? 15 - light : light;
            case SENSITIVITY_MONSTERS -> (light > 7) != inverted ? 15 : 0;
            default -> (light > 11) != inverted ? 15 : 0; // SLEEP
        };
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    ////////////
    // Shape
    ////////////

    @Override
    public List<AABB> getShapeBoxes(int renderData) {
        return Collections.singletonList(SidedUtil.rotate(new AABB(0, 0, 0, 1, 1, 0.25), getAttachSide(renderData)));
    }

    @Override
    public int getInventoryRenderData(int itemDamage) {
        return setAttachSide(0, Direction.UP);
    }

    @Override
    public List<ShapePart> getRenderParts(int renderData) {
        Direction side = getAttachSide(renderData);
        boolean active = getPowerOutput(renderData) > 0;
        List<ShapePart> parts = new ArrayList<>();
        // Frame
        parts.add(ShapePart.covered(SidedUtil.rotate(new AABB(0.0625, 0.0625, 0, 0.9375, 0.9375, 0.0625), side)));
        parts.add(ShapePart.covered(SidedUtil.rotate(new AABB(0, 0, 0, 0.0625, 1, 0.25), side)));
        parts.add(ShapePart.covered(SidedUtil.rotate(new AABB(0.0625, 0, 0, 0.9375, 0.0625, 0.25), side)));
        parts.add(ShapePart.covered(SidedUtil.rotate(new AABB(0.0625, 0.9375, 0, 0.9375, 1, 0.25), side)));
        parts.add(ShapePart.covered(SidedUtil.rotate(new AABB(0.9375, 0, 0, 1, 1, 0.25), side)));
        // Lapis inlay, ringed with redstone that glows while the sensor outputs
        parts.add(ShapePart.textured(SidedUtil.rotate(new AABB(0.125, 0.125, 0.0625, 0.875, 0.875, 0.1875), side), ShapeTextures.LAPIS_BLOCK));
        AABB[] redstone = {
            new AABB(0.0625, 0.0625, 0.0625, 0.125, 0.9375, 0.1875),
            new AABB(0.125, 0.0625, 0.0625, 0.875, 0.125, 0.1875),
            new AABB(0.125, 0.875, 0.0625, 0.875, 0.9375, 0.1875),
            new AABB(0.875, 0.0625, 0.0625, 0.9375, 0.9375, 0.1875) };
        for (AABB box : redstone) {
            AABB rotated = SidedUtil.rotate(box, side);
            parts.add(active ? ShapePart.glowing(rotated, ShapeTextures.REDSTONE_BLOCK) : ShapePart.textured(rotated, ShapeTextures.REDSTONE_BLOCK));
        }
        // Glass top
        parts.add(ShapePart.translucent(SidedUtil.rotate(new AABB(0.0625, 0.0625, 0.234375, 0.9375, 0.9375, 0.25), side), ShapeTextures.DAYLIGHT_SENSOR_GLASS_TOP));
        return parts;
    }

    ////////////
    // Sensing
    ////////////

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(blockState, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, UPDATE_TICKS);
        }
    }

    /**
     * Restarts sensing for sensors that have no scheduled tick (ones converted from 1.12.2 worlds).
     */
    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return true;
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, UPDATE_TICKS);
        }
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, UPDATE_TICKS);
        if (!level.dimensionType().hasSkyLight()) {
            return;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return;
        }
        int data = blockEntity.getCbMetadata();
        int light = Mth.clamp(computeLight(level, pos, data), 0, 15);
        if (light != getLight(data)) {
            data = (data & ~LIGHT_MASK) | light;
            blockEntity.setCbMetadata(data);
            notifyPowerChange(level, pos, data);
        }
    }

    private int computeLight(Level level, BlockPos pos, int data) {
        int skyLight = level.getEffectiveSkyBrightness(pos);
        switch (getSensitivity(data)) {
            case SENSITIVITY_DYNAMIC: {
                float angle = level.environmentAttributes().getValue(EnvironmentAttributes.SUN_ANGLE, pos) * (float) (Math.PI / 180.0);
                if (angle > 1.67F && angle < 4.62F) {
                    return 0;
                }
                return switch (getAttachSide(data)) {
                    case NORTH, SOUTH -> (int) (skyLight * 0.6F);
                    case EAST -> celestialLight(skyLight, angle + (float) (Math.PI / 2));
                    case WEST -> celestialLight(skyLight, angle - (float) (Math.PI / 2));
                    default -> celestialLight(skyLight, angle);
                };
            }
            case SENSITIVITY_MONSTERS:
                return level.isThundering() ? 7 : skyLight;
            default:
                return skyLight;
        }
    }

    /**
     * Light falling on the sensor for a sun angle (vanilla daylight sensor formula).
     */
    private static int celestialLight(int skyLight, float angle) {
        float pi2 = (float) (Math.PI * 2);
        angle += angle < (float) Math.PI ? (0.0F - angle) * 0.2F : (pi2 - angle) * 0.2F;
        return Mth.clamp(Math.round(skyLight * Mth.cos(angle)), 0, 15);
    }

    /**
     * Hammer left click: inverts the redstone output.
     */
    @Override
    protected boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        int data = blockEntity.getCbMetadata() ^ POLARITY_BIT;
        blockEntity.setCbMetadata(data);
        notifyPowerChange(blockEntity.getLevel(), blockEntity.getBlockPos(), data);
        sendMessage(player, isInverted(data) ? "message.carpentersblocks.polarity_neg" : "message.carpentersblocks.polarity_pos");
        return true;
    }

    /**
     * Hammer right click: next sensitivity.
     */
    @Override
    protected boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        int data = blockEntity.getCbMetadata();
        int sensitivity = (getSensitivity(data) + 1) % (SENSITIVITY_DYNAMIC + 1);
        data = (data & ~SENSITIVITY_MASK) | (sensitivity << SENSITIVITY_SHIFT);
        blockEntity.setCbMetadata(data);
        notifyPowerChange(blockEntity.getLevel(), blockEntity.getBlockPos(), data);
        String[] messages = { "message.carpentersblocks.sensitivity_sleep", "message.carpentersblocks.sensitivity_monsters", "message.carpentersblocks.sensitivity_dynamic" };
        sendMessage(player, messages[sensitivity]);
        return true;
    }

}
