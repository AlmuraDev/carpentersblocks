package com.carpentersblocks.entity;

import java.util.Locale;

import javax.annotation.Nullable;

import com.carpentersblocks.api.ICarpentersHammer;
import com.carpentersblocks.registry.CbEntities;
import com.carpentersblocks.registry.CbItems;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.DesignHandler.DesignType;
import com.carpentersblocks.util.handler.DyeHandler;
import com.carpentersblocks.util.protection.IProtected;
import com.carpentersblocks.util.protection.PlayerPermissions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Carpenter's Tile, ported from 1.7.10: a 1/16 thick decorative panel on any face of a block, with a design, a dye
 * color and a rotation.
 * <p>
 * The entity sits at the origin of the block space in front of the face it was placed on. It is saved like 1.12.2
 * (dir, rot, dye, tile, owner), with the dye as a dye color name (1.12.2 saved its ore dictionary name, read here too).
 */
public class EntityCarpentersTile extends Entity implements IProtected {

    private static final EntityDataAccessor<Integer> DIRECTION = SynchedEntityData.defineId(EntityCarpentersTile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ROTATION = SynchedEntityData.defineId(EntityCarpentersTile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DYE = SynchedEntityData.defineId(EntityCarpentersTile.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DESIGN = SynchedEntityData.defineId(EntityCarpentersTile.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> OWNER = SynchedEntityData.defineId(EntityCarpentersTile.class, EntityDataSerializers.STRING);

    private static final String DEFAULT_DYE = DyeColor.WHITE.getSerializedName();
    private static final double THICKNESS = 0.0625D;

    public EntityCarpentersTile(EntityType<? extends EntityCarpentersTile> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public EntityCarpentersTile(Level level, @Nullable Player player, BlockPos pos, Direction direction) {
        this(CbEntities.CARPENTERS_TILE.get(), level);
        setDirection(direction);
        setPos(pos.getX(), pos.getY(), pos.getZ());
        if (player != null) {
            setOwner(player);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DIRECTION, Direction.UP.get3DDataValue());
        builder.define(ROTATION, 0);
        builder.define(DYE, DEFAULT_DYE);
        builder.define(DESIGN, "");
        builder.define(OWNER, "");
    }

    ////////////
    // Properties
    ////////////

    /**
     * Gets the face of the supporting block the tile lies on.
     */
    @Override
    public Direction getDirection() {
        return Direction.from3DDataValue(entityData.get(DIRECTION));
    }

    public void setDirection(Direction direction) {
        entityData.set(DIRECTION, direction.get3DDataValue());
        setPos(getX(), getY(), getZ());
    }

    public int getRotation() {
        return entityData.get(ROTATION);
    }

    public void setRotation(int rotation) {
        entityData.set(ROTATION, rotation & 3);
    }

    /**
     * Gets the dye color name.
     */
    public String getDye() {
        return entityData.get(DYE);
    }

    public void setDye(String dye) {
        entityData.set(DYE, dye);
    }

    /**
     * Gets the tile color (0xRRGGBB, the dye's firework color like 1.12.2).
     */
    public int getColor() {
        DyeColor color = DyeColor.byName(getDye(), DyeColor.WHITE);
        return color.getFireworkColor() & 0xFFFFFF;
    }

    public String getDesign() {
        return entityData.get(DESIGN);
    }

    public void setDesign(String design) {
        entityData.set(DESIGN, design == null ? "" : design);
    }

    @Override
    public void setOwner(Player player) {
        entityData.set(OWNER, player.getUUID().toString());
    }

    @Override
    public String getOwner() {
        return entityData.get(OWNER);
    }

    /**
     * Gets the tile's box in block coordinates (0 to 1).
     */
    public static AABB getLocalBox(Direction direction) {
        return switch (direction) {
            case DOWN -> new AABB(0, 1 - THICKNESS, 0, 1, 1, 1);
            case NORTH -> new AABB(0, 0, 1 - THICKNESS, 1, 1, 1);
            case SOUTH -> new AABB(0, 0, 0, 1, 1, THICKNESS);
            case WEST -> new AABB(1 - THICKNESS, 0, 0, 1, 1, 1);
            case EAST -> new AABB(0, 0, 0, THICKNESS, 1, 1);
            default -> new AABB(0, 0, 0, 1, THICKNESS, 1); // UP
        };
    }

    /**
     * The box follows the direction, from the block origin the entity sits on.
     */
    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        return getLocalBox(getDirection()).move(position);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DIRECTION.equals(accessor)) {
            setPos(getX(), getY(), getZ());
        }
    }

    /**
     * Whether the supporting face is still solid.
     */
    public boolean onValidSurface() {
        Direction direction = getDirection();
        BlockPos support = blockPosition().relative(direction.getOpposite());
        return level().getBlockState(support).isFaceSturdy(level(), support, direction);
    }

    ////////////
    // Update
    ////////////

    @Override
    public void tick() {
        // Fixed in place: no movement
        if (level() instanceof ServerLevel serverLevel && tickCount % 21 == 0 && !onValidSurface()) {
            dropTile(serverLevel);
        }
    }

    /**
     * Pushed by pistons or explosions: falls off.
     */
    @Override
    public void move(MoverType moverType, Vec3 delta) {
        if (level() instanceof ServerLevel serverLevel && !isRemoved() && delta.lengthSqr() > 0) {
            dropTile(serverLevel);
        }
    }

    @Override
    public void push(double x, double y, double z) {
        if (level() instanceof ServerLevel serverLevel && !isRemoved() && (x != 0 || y != 0 || z != 0)) {
            dropTile(serverLevel);
        }
    }

    private void dropTile(ServerLevel level) {
        if (CbItems.CARPENTERS_TILE != null) {
            spawnAtLocation(level, new ItemStack(CbItems.CARPENTERS_TILE.get()));
        }
        discard();
    }

    ////////////
    // Collision
    ////////////

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) {
        return !isRemoved();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    ////////////
    // Interaction
    ////////////

    private void playTileSound(SoundEvent sound) {
        level().playSound(null, getX() + 0.5D, getY() + 0.5D, getZ() + 0.5D, sound, SoundSource.BLOCKS, 1.0F, 0.8F);
    }

    /**
     * Right click: hammer changes the design (sneaking rotates it), sneaking with a dye colors it.
     */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        ItemStack itemStack = player.getItemInHand(hand);
        boolean hammer = itemStack.getItem() instanceof ICarpentersHammer;
        boolean dye = player.isShiftKeyDown() && DyeHandler.isDye(itemStack, true);
        if (!hammer && !dye) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!PlayerPermissions.hasElevatedPermission(this, player, false)) {
            return InteractionResult.PASS;
        }
        if (hammer) {
            if (player.isShiftKeyDown()) {
                setRotation(getRotation() + 1);
            } else {
                setDesign(DesignHandler.getPrev(DesignType.TILE, getDesign()));
            }
            playTileSound(SoundEvents.STONE_PLACE);
        } else {
            String color = DyeHandler.getDyeColor(itemStack).getSerializedName();
            if (!color.equals(getDye())) {
                setDye(color);
                playTileSound(SoundEvents.SAND_PLACE);
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    /**
     * Left click: hammer changes the design, sneaking takes the tile off. Creative players remove it.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (isRemoved() || !(source.getEntity() instanceof Player player) || !PlayerPermissions.hasElevatedPermission(this, player, false)) {
            return false;
        }
        if (player.getMainHandItem().getItem() instanceof ICarpentersHammer) {
            if (player.isShiftKeyDown()) {
                if (player.isCreative()) {
                    discard();
                } else {
                    dropTile(level);
                }
            } else {
                setDesign(DesignHandler.getNext(DesignType.TILE, getDesign()));
            }
            playTileSound(SoundEvents.STONE_HIT);
            return true;
        }
        if (player.isCreative()) {
            discard();
            playTileSound(SoundEvents.STONE_BREAK);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return CbItems.CARPENTERS_TILE != null ? new ItemStack(CbItems.CARPENTERS_TILE.get()) : ItemStack.EMPTY;
    }

    ////////////
    // Saving
    ////////////

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        entityData.set(DIRECTION, input.getIntOr("dir", Direction.UP.get3DDataValue()));
        setRotation(input.getIntOr("rot", 0));
        setDye(readDye(input.getStringOr("dye", DEFAULT_DYE)));
        setDesign(input.getStringOr("tile", ""));
        entityData.set(OWNER, input.getStringOr("owner", ""));
        setPos(getX(), getY(), getZ());
    }

    /**
     * Reads a dye color name, or a 1.12.2 ore dictionary name (dyeLightBlue).
     */
    private static String readDye(String dye) {
        if (dye.startsWith("dye") && dye.length() > 3) {
            String name = dye.substring(3).replaceAll("(?<!^)(?=[A-Z])", "_").toLowerCase(Locale.ROOT);
            return DyeColor.byName(name, DyeColor.WHITE).getSerializedName();
        }
        return DyeColor.byName(dye, DyeColor.WHITE).getSerializedName();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("dir", getDirection().get3DDataValue());
        output.putInt("rot", getRotation());
        output.putString("dye", getDye());
        output.putString("tile", getDesign());
        output.putString("owner", getOwner());
    }

}
