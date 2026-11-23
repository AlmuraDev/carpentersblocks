package com.carpentersblocks.entity;

import javax.annotation.Nullable;

import com.carpentersblocks.api.ICarpentersHammer;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.DesignHandler.DesignType;
import com.carpentersblocks.util.handler.DyeHandler;
import com.carpentersblocks.util.protection.IProtected;
import com.carpentersblocks.util.protection.PlayerPermissions;
import com.carpentersblocks.util.protection.ProtectedObject;
import com.carpentersblocks.util.registry.ItemRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * Carpenter's Tile, ported from 1.7.10: a 1/16 thick decorative panel on any face of a block.
 * <p>
 * The entity sits at the origin of the block space in front of the face it was placed on.
 */
public class EntityCarpentersTile extends Entity implements IProtected {

	private static final DataParameter<Integer> DIRECTION = EntityDataManager.createKey(EntityCarpentersTile.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> ROTATION = EntityDataManager.createKey(EntityCarpentersTile.class, DataSerializers.VARINT);
	private static final DataParameter<String> DYE = EntityDataManager.createKey(EntityCarpentersTile.class, DataSerializers.STRING);
	private static final DataParameter<String> DESIGN = EntityDataManager.createKey(EntityCarpentersTile.class, DataSerializers.STRING);
	private static final DataParameter<String> OWNER = EntityDataManager.createKey(EntityCarpentersTile.class, DataSerializers.STRING);

	private static final String DEFAULT_DYE = "dyeWhite";
	private static final double THICKNESS = 0.0625D;

	public EntityCarpentersTile(World world) {
		super(world);
		setSize(1.0F, 1.0F);
	}

	public EntityCarpentersTile(World world, EntityPlayer player, BlockPos blockPos, EnumFacing direction) {
		this(world);
		setDirection(direction);
		setPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		if (player != null) {
			setOwner(new ProtectedObject(player));
		}
	}

	@Override
	protected void entityInit() {
		dataManager.register(DIRECTION, EnumFacing.UP.getIndex());
		dataManager.register(ROTATION, 0);
		dataManager.register(DYE, DEFAULT_DYE);
		dataManager.register(DESIGN, "");
		dataManager.register(OWNER, "");
	}

	////////////
	// Properties
	////////////

	/**
	 * Gets the face of the supporting block the tile lies on.
	 */
	public EnumFacing getDirection() {
		return EnumFacing.byIndex(dataManager.get(DIRECTION));
	}

	public void setDirection(EnumFacing direction) {
		dataManager.set(DIRECTION, direction.getIndex());
		setPosition(posX, posY, posZ);
	}

	public int getRotation() {
		return dataManager.get(ROTATION);
	}

	public void setRotation(int rotation) {
		dataManager.set(ROTATION, rotation & 3);
	}

	public String getDye() {
		return dataManager.get(DYE);
	}

	public void setDye(String dye) {
		dataManager.set(DYE, dye);
	}

	public String getDesign() {
		return dataManager.get(DESIGN);
	}

	public void setDesign(String design) {
		dataManager.set(DESIGN, design);
	}

	@Override
	public void setOwner(ProtectedObject obj) {
		dataManager.set(OWNER, obj.toString());
	}

	@Override
	public String getOwner() {
		return dataManager.get(OWNER);
	}

	/**
	 * Gets the tile's box in block coordinates (0 to 1).
	 */
	public static AxisAlignedBB getLocalBox(EnumFacing direction) {
		switch (direction) {
			case DOWN:
				return new AxisAlignedBB(0, 1 - THICKNESS, 0, 1, 1, 1);
			case NORTH:
				return new AxisAlignedBB(0, 0, 1 - THICKNESS, 1, 1, 1);
			case SOUTH:
				return new AxisAlignedBB(0, 0, 0, 1, 1, THICKNESS);
			case WEST:
				return new AxisAlignedBB(1 - THICKNESS, 0, 0, 1, 1, 1);
			case EAST:
				return new AxisAlignedBB(0, 0, 0, THICKNESS, 1, 1);
			default: // UP
				return new AxisAlignedBB(0, 0, 0, 1, THICKNESS, 1);
		}
	}

	public BlockPos getBlockPos() {
		return new BlockPos(Math.round(posX), Math.round(posY), Math.round(posZ));
	}

	/**
	 * Keeps the entity on its block origin; the box follows the direction.
	 */
	@Override
	public void setPosition(double x, double y, double z) {
		posX = x;
		posY = y;
		posZ = z;
		if (dataManager != null) {
			setEntityBoundingBox(getLocalBox(getDirection()).offset(x, y, z));
		}
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (DIRECTION.equals(key)) {
			setPosition(posX, posY, posZ);
		}
	}

	/**
	 * Whether the supporting face is still solid.
	 */
	public boolean onValidSurface() {
		EnumFacing direction = getDirection();
		BlockPos support = getBlockPos().offset(direction.getOpposite());
		return world.isSideSolid(support, direction, false);
	}

	////////////
	// Update
	////////////

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		if (!world.isRemote && ticksExisted++ % 21 == 0 && !onValidSurface()) {
			dropTile();
		}
	}

	@Override
	public void move(net.minecraft.entity.MoverType type, double x, double y, double z) {
		// Pushed by pistons or explosions: falls off
		if (!world.isRemote && !isDead && (x != 0 || y != 0 || z != 0)) {
			dropTile();
		}
	}

	@Override
	public void addVelocity(double x, double y, double z) {
		if (!world.isRemote && !isDead && (x != 0 || y != 0 || z != 0)) {
			dropTile();
		}
	}

	private void dropTile() {
		if (ItemRegistry.itemCarpentersTile != null) {
			entityDropItem(new ItemStack(ItemRegistry.itemCarpentersTile), 0.0F);
		}
		setDead();
	}

	////////////
	// Collision
	////////////

	@Override
	public boolean canBeCollidedWith() {
		return !isDead;
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox() {
		return getEntityBoundingBox();
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public float getCollisionBorderSize() {
		return 0.0F;
	}

	////////////
	// Interaction
	////////////

	private void playSound(net.minecraft.util.SoundEvent sound) {
		world.playSound(null, posX + 0.5D, posY + 0.5D, posZ + 0.5D, sound, SoundCategory.BLOCKS, 1.0F, 0.8F);
	}

	/**
	 * Right click: hammer changes the design (sneaking rotates it), sneaking with a dye colors it.
	 */
	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		if (world.isRemote) {
			return true;
		}
		ItemStack itemStack = player.getHeldItem(hand);
		if (itemStack.isEmpty() || !PlayerPermissions.hasElevatedPermission(this, player, false)) {
			return false;
		}
		if (itemStack.getItem() instanceof ICarpentersHammer) {
			if (player.isSneaking()) {
				setRotation(getRotation() + 1);
			} else {
				setDesign(DesignHandler.getPrev(DesignType.TILE, getDesign()));
			}
			playSound(SoundEvents.BLOCK_STONE_PLACE);
			return true;
		}
		if (player.isSneaking() && DyeHandler.isDye(itemStack, true)) {
			String dye = DyeHandler.getOreDictName(itemStack);
			if (dye != null && !dye.equals(getDye())) {
				setDye(dye);
				playSound(SoundEvents.BLOCK_SAND_PLACE);
			}
			return true;
		}
		return false;
	}

	/**
	 * Left click: hammer changes the design, sneaking takes the tile off. Creative players remove it.
	 */
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (world.isRemote || isDead || !(source.getTrueSource() instanceof EntityPlayer)) {
			return false;
		}
		EntityPlayer player = (EntityPlayer) source.getTrueSource();
		if (!PlayerPermissions.hasElevatedPermission(this, player, false)) {
			return false;
		}
		ItemStack itemStack = player.getHeldItemMainhand();
		if (itemStack.getItem() instanceof ICarpentersHammer) {
			if (player.isSneaking()) {
				if (player.capabilities.isCreativeMode) {
					setDead();
				} else {
					dropTile();
				}
			} else {
				setDesign(DesignHandler.getNext(DesignType.TILE, getDesign()));
			}
			playSound(SoundEvents.BLOCK_STONE_HIT);
			return true;
		}
		if (player.capabilities.isCreativeMode) {
			setDead();
			playSound(SoundEvents.BLOCK_STONE_BREAK);
			return true;
		}
		return false;
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		return ItemRegistry.itemCarpentersTile != null ? new ItemStack(ItemRegistry.itemCarpentersTile) : ItemStack.EMPTY;
	}

	////////////
	// Saving
	////////////

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		dataManager.set(DIRECTION, nbt.getInteger("dir"));
		setRotation(nbt.getInteger("rot"));
		setDye(nbt.hasKey("dye") ? nbt.getString("dye") : DEFAULT_DYE);
		setDesign(nbt.getString("tile"));
		dataManager.set(OWNER, nbt.getString("owner"));
		setPosition(posX, posY, posZ);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("dir", getDirection().getIndex());
		nbt.setInteger("rot", getRotation());
		nbt.setString("dye", getDye());
		nbt.setString("tile", getDesign());
		nbt.setString("owner", getOwner());
	}

	@Override
	public boolean shouldSetPosAfterLoading() {
		return false;
	}

}
