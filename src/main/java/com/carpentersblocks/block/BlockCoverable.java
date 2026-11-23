package com.carpentersblocks.block;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.carpentersblocks.CbConfig;
import com.carpentersblocks.api.ICarpentersChisel;
import com.carpentersblocks.api.ICarpentersHammer;
import com.carpentersblocks.blockentity.CbBlockEntity;
import com.carpentersblocks.util.attribute.AttributeHelper;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;
import com.carpentersblocks.util.block.BlockUtil;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.DesignHandler.DesignType;
import com.carpentersblocks.util.protection.PlayerPermissions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import com.carpentersblocks.blockentity.CbRenderData;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

/**
 * A block that takes covers, side covers, dyes, overlays, chisel designs and glowstone (1.12.2's BlockCoverable).
 * <p>
 * Attributes are added by right clicking with them. The Carpenter's Hammer removes them (sneak left click) and turns
 * blocks; the Carpenter's Chisel cycles chisel designs on covers. Left clicks reach {@link #onBlockClicked} through
 * {@link com.carpentersblocks.CommonEvents}, which knows the clicked face.
 */
public abstract class BlockCoverable extends Block implements EntityBlock {

    /**
     * Stores actions taken on a block in order to properly play sounds,
     * decrement player inventory, and to determine if a block was altered.
     */
    protected static class ActionResult {

        public ItemStack itemStack = ItemStack.EMPTY;
        public boolean playSound = true;
        public boolean altered = false;
        public boolean decInv = false;

        public ActionResult setSoundSource(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public ActionResult setNoSound() {
            playSound = false;
            return this;
        }

        public ActionResult setAltered() {
            altered = true;
            return this;
        }

        public ActionResult decInventory() {
            decInv = true;
            return this;
        }

    }

    public BlockCoverable(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
        return new CbBlockEntity(pos, blockState);
    }

    /**
     * Gets the data the block is drawn and shaped from. Blocks whose shape depends on their neighbors (fences) add
     * that here.
     * <p>
     * Also called while chunks are meshed, off the main thread: read neighbors only through the level, and their
     * block data through {@link #getCbMetadataAt}.
     *
     * @param cbMetadata the block's own data
     */
    public int getRenderData(BlockGetter level, BlockPos pos, int cbMetadata) {
        return cbMetadata;
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
     * Gets the position of the block whose cover, dye and overlays this block is drawn with. Blocks drawn with
     * another piece's look (an open garage door) override this. Also called while chunks are meshed.
     *
     * @param cbMetadata the block's own data
     */
    public BlockPos getRenderAttributeSource(BlockGetter level, BlockPos pos, int cbMetadata) {
        return pos;
    }

    /**
     * Gets the render data at a position (main thread: from the block entity).
     */
    protected int getRenderDataAt(BlockGetter level, BlockPos pos) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        return getRenderData(level, pos, blockEntity != null ? blockEntity.getCbMetadata() : 0);
    }

    ////////////
    // Shapes of IShapedBlock blocks
    ////////////

    // Shapes by render data, made from the block's boxes; render data covers everything a shape depends on
    private final Map<Integer, VoxelShape> _outlineShapes = new ConcurrentHashMap<>();
    private final Map<Integer, VoxelShape> _collisionShapes = new ConcurrentHashMap<>();

    private static VoxelShape toShape(List<AABB> boxes) {
        VoxelShape shape = Shapes.empty();
        for (AABB box : boxes) {
            shape = Shapes.or(shape, Shapes.create(box));
        }
        return shape.optimize();
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!(this instanceof IShapedBlock shapedBlock)) {
            return super.getShape(blockState, level, pos, context);
        }
        return _outlineShapes.computeIfAbsent(getRenderDataAt(level, pos), data -> toShape(shapedBlock.getSelectionBoxes(data)));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!(this instanceof IShapedBlock shapedBlock)) {
            return super.getCollisionShape(blockState, level, pos, context);
        }
        return _collisionShapes.computeIfAbsent(getRenderDataAt(level, pos), data -> toShape(shapedBlock.getCollisionBoxes(data)));
    }

    /**
     * Breaks the block as if mined, dropping it and its attributes (1.12.2's destroyBlock).
     */
    protected static void breakBlock(Level level, BlockPos pos) {
        level.destroyBlock(pos, true);
    }

    /**
     * Sends a message from the language files (1.12.2's ChatHandler).
     */
    protected static void sendMessage(Player player, String key) {
        player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.GOLD));
    }

    /**
     * Gets the block's light: by default the light of its attributes (glowstone, glowing covers).
     */
    public int getLightValue(CbBlockEntity blockEntity, int attributeLight) {
        return attributeLight;
    }

    /**
     * Returns the block entity if it is a Carpenter's block entity, whatever the block.
     */
    @Nullable
    protected static CbBlockEntity getSimpleBlockEntity(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CbBlockEntity blockEntity ? blockEntity : null;
    }

    /**
     * Returns the block entity if it is a Carpenter's block entity of this block.
     */
    @Nullable
    protected CbBlockEntity getBlockEntity(BlockGetter level, BlockPos pos) {
        CbBlockEntity blockEntity = getSimpleBlockEntity(level, pos);
        return blockEntity != null && blockEntity.getBlockState().is(this) ? blockEntity : null;
    }

    /**
     * Returns adjacent Carpenter's blocks, used for copying designs.
     */
    protected CbBlockEntity[] getAdjacentBlockEntities(Level level, BlockPos pos) {
        CbBlockEntity[] blockEntities = new CbBlockEntity[Direction.values().length];
        for (Direction direction : Direction.values()) {
            blockEntities[direction.ordinal()] = getSimpleBlockEntity(level, pos.relative(direction));
        }
        return blockEntities;
    }

    /**
     * Returns whether player is allowed to activate this block.
     */
    protected boolean canPlayerActivate(CbBlockEntity blockEntity, Player player) {
        return true;
    }

    private static boolean isHammer(ItemStack itemStack, Level level, Player player, InteractionHand hand) {
        return itemStack.getItem() instanceof ICarpentersHammer hammer && hammer.canUseHammer(level, player, hand);
    }

    private static boolean isChisel(ItemStack itemStack, Level level, Player player, InteractionHand hand) {
        return itemStack.getItem() instanceof ICarpentersChisel chisel && chisel.canUseChisel(level, player, hand);
    }

    /**
     * Called on the server when the block is left clicked (see {@link com.carpentersblocks.CommonEvents}).
     *
     * @param face the clicked face
     */
    public void onBlockClicked(Level level, BlockPos pos, Player player, Direction face) {
        if (level.isClientSide()) {
            return;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null || !PlayerPermissions.hasElevatedPermission(blockEntity, player, false)) {
            return;
        }

        // Left click is always the main hand
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.isEmpty()) {
            return;
        }

        ActionResult actionResult = new ActionResult();
        EnumAttributeLocation location = BlockUtil.getAttributeLocationForFacing(blockEntity, face);
        if (isHammer(itemStack, level, player, InteractionHand.MAIN_HAND)) {
            preOnBlockClicked(blockEntity, player, actionResult);
            if (!actionResult.altered) {
                if (player.isShiftKeyDown()) {
                    blockEntity.removeLastAddedDroppableAttribute(location);
                } else {
                    onHammerLeftClick(blockEntity, player);
                }
                actionResult.setAltered();
            }
        } else if (isChisel(itemStack, level, player, InteractionHand.MAIN_HAND)) {
            AttributeHelper helper = blockEntity.getAttributeHelper();
            if (player.isShiftKeyDown() && helper.hasAttribute(location, EnumAttributeType.DESIGN_CHISEL)) {
                if (blockEntity.removeAttribute(location, EnumAttributeType.DESIGN_CHISEL)) {
                    actionResult.setAltered();
                }
            } else if (helper.hasAttribute(location, EnumAttributeType.COVER)) {
                if (onChiselClick(blockEntity, location, InteractionHand.MAIN_HAND, true)) {
                    actionResult.setAltered();
                }
            }
        }

        if (actionResult.altered) {
            blockEntity.update(true);
        }
    }

    /**
     * Whether a right click with an item would change the block, without changing it. Decides on the client whether
     * the click is used here or the item is used (placed) instead.
     */
    protected boolean wouldActivate(CbBlockEntity blockEntity, ItemStack itemStack, Level level, Player player, InteractionHand hand, Direction facing) {
        if (isHammer(itemStack, level, player, hand)) {
            return true;
        }
        EnumAttributeLocation location = BlockUtil.getAttributeLocationForFacing(blockEntity, facing);
        AttributeHelper helper = blockEntity.getAttributeHelper();
        if (CbConfig.ENABLE_CHISEL.get() && isChisel(itemStack, level, player, hand)) {
            return helper.hasAttribute(location, EnumAttributeType.COVER);
        }
        if (CbConfig.ENABLE_COVERS.get() && BlockUtil.isCover(itemStack)) {
            return getCoverLocation(blockEntity, location, facing) != null;
        }
        if (player.isShiftKeyDown()) {
            if (CbConfig.ENABLE_ILLUMINATION.get() && BlockUtil.isIlluminator(itemStack)) {
                return !helper.hasAttribute(location, EnumAttributeType.ILLUMINATOR);
            } else if (CbConfig.ENABLE_OVERLAYS.get() && BlockUtil.isOverlay(itemStack)) {
                return !helper.hasAttribute(location, EnumAttributeType.OVERLAY);
            } else if (CbConfig.ENABLE_DYE_COLORS.get() && BlockUtil.isDye(itemStack, false)) {
                return !helper.hasAttribute(location, EnumAttributeType.DYE);
            }
        }
        return false;
    }

    /**
     * Gets where a cover placed on a face goes: the whole block, or the side once the block has a cover, or null if
     * neither can take it.
     */
    @Nullable
    private EnumAttributeLocation getCoverLocation(CbBlockEntity blockEntity, EnumAttributeLocation location, Direction facing) {
        AttributeHelper helper = blockEntity.getAttributeHelper();
        if (EnumAttributeLocation.HOST.equals(location)
                && (!canSupportCover(blockEntity, location) || helper.hasAttribute(EnumAttributeLocation.HOST, EnumAttributeType.COVER))) {
            location = EnumAttributeLocation.fromDirection(facing);
        }
        boolean sideCoverAllowed = EnumAttributeLocation.HOST.equals(location) || CbConfig.ENABLE_SIDE_COVERS.get();
        if (sideCoverAllowed && canSupportCover(blockEntity, location) && !helper.hasAttribute(location, EnumAttributeType.COVER)) {
            return location;
        }
        return null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null || itemStack.isEmpty() || !canPlayerActivate(blockEntity, player)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        Direction facing = hitResult.getDirection();
        if (level.isClientSide()) {
            // The server decides; this only keeps the item from being placed when the click changes the block
            return wouldActivate(blockEntity, itemStack, level, player, hand, facing) || wouldPreActivate(blockEntity, itemStack, player)
                ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        EnumAttributeLocation location = BlockUtil.getAttributeLocationForFacing(blockEntity, facing);

        // Allow block to change block entity if needed before altering attributes
        blockEntity = getBlockEntityForBlockActivation(blockEntity);
        ActionResult actionResult = new ActionResult();
        preOnBlockActivated(blockEntity, player, hitResult, actionResult);

        // If no prior event occurred, try regular activation
        if (!actionResult.altered && PlayerPermissions.hasElevatedPermission(blockEntity, player, false)) {
            AttributeHelper helper = blockEntity.getAttributeHelper();
            if (isHammer(itemStack, level, player, hand)) {
                if (onHammerRightClick(blockEntity, player)) {
                    actionResult.setAltered();
                }
            } else if (CbConfig.ENABLE_CHISEL.get() && isChisel(itemStack, level, player, hand)) {
                if (helper.hasAttribute(location, EnumAttributeType.COVER) && onChiselClick(blockEntity, location, hand, false)) {
                    actionResult.setAltered();
                }
            } else if (CbConfig.ENABLE_COVERS.get() && BlockUtil.isCover(itemStack)) {
                EnumAttributeLocation coverLocation = getCoverLocation(blockEntity, location, facing);
                if (coverLocation != null) {
                    blockEntity.addAttribute(coverLocation, EnumAttributeType.COVER, itemStack, getCoverState(itemStack, player, hitResult));
                    actionResult.setAltered().decInventory().setSoundSource(itemStack);
                }
            } else if (player.isShiftKeyDown()) {
                if (CbConfig.ENABLE_ILLUMINATION.get() && BlockUtil.isIlluminator(itemStack)) {
                    if (blockEntity.addAttribute(location, EnumAttributeType.ILLUMINATOR, itemStack)) {
                        actionResult.setAltered().decInventory().setSoundSource(itemStack);
                    }
                } else if (CbConfig.ENABLE_OVERLAYS.get() && BlockUtil.isOverlay(itemStack)) {
                    if (blockEntity.addAttribute(location, EnumAttributeType.OVERLAY, itemStack)) {
                        actionResult.setAltered().decInventory().setSoundSource(itemStack);
                    }
                } else if (CbConfig.ENABLE_DYE_COLORS.get() && BlockUtil.isDye(itemStack, false)) {
                    if (blockEntity.addAttribute(location, EnumAttributeType.DYE, itemStack)) {
                        actionResult.setAltered().decInventory().setSoundSource(itemStack);
                    }
                }
            }
        }

        // Clicks that change nothing go on to useWithoutItem (pressing a button, opening a gate)
        if (actionResult.altered) {
            damageItemWithChance(level, player, hand);
        }

        if (actionResult.altered) {
            blockEntity.update(true);
            if (actionResult.playSound) {
                playAttributeSound(level, pos, actionResult.itemStack);
            }
        }

        if (actionResult.decInv) {
            itemStack.consume(1, player);
        }

        return actionResult.altered ? InteractionResult.SUCCESS_SERVER : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    /**
     * Whether {@link #preOnBlockActivated} would change the block with this item (relighting a torch), for the client
     * side of {@link #useItemOn}.
     */
    protected boolean wouldPreActivate(CbBlockEntity blockEntity, ItemStack itemStack, Player player) {
        return false;
    }

    /**
     * Whether {@link #postOnBlockActivated} changes the block (pressing a button, opening a gate), for the client side
     * of {@link #useWithoutItem}.
     */
    protected boolean wouldActivateBlock(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult) {
        return false;
    }

    /**
     * Clicks that add no attribute (with an empty hand, or an item that does nothing here) use the block.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null || !canPlayerActivate(blockEntity, player)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return wouldActivateBlock(blockEntity, player, hitResult) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        ActionResult actionResult = new ActionResult();
        postOnBlockActivated(blockEntity, player, hitResult, actionResult);
        if (!actionResult.altered) {
            return InteractionResult.PASS;
        }
        blockEntity.update(true);
        if (actionResult.decInv) {
            player.getMainHandItem().consume(1, player);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    /**
     * Gets the block state a cover is drawn with: the way the block would be placed by the player, for blocks that
     * turn (logs, quartz pillars).
     */
    @Nullable
    protected BlockState getCoverState(ItemStack itemStack, Player player, BlockHitResult hitResult) {
        if (!(itemStack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        BlockState defaultState = block.defaultBlockState();
        if (!defaultState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS)) {
            return null;
        }
        return defaultState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS, hitResult.getDirection().getAxis());
    }

    /**
     * Plays the place sound of a block attribute.
     */
    private static void playAttributeSound(Level level, BlockPos pos, ItemStack itemStack) {
        BlockState blockState = BlockUtil.getBlockState(itemStack);
        if (blockState != null) {
            SoundType soundType = blockState.getSoundType();
            level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
        }
    }

    /**
     * Cycles through chisel patterns.
     *
     * @param blockEntity the Carpenter's block entity
     * @param location the location of the cover being chiseled
     * @param hand the player hand holding tool
     * @param leftClick whether this is a left click (next design) or right click (previous design)
     */
    public boolean onChiselClick(CbBlockEntity blockEntity, EnumAttributeLocation location, InteractionHand hand, boolean leftClick) {
        if (!CbConfig.ENABLE_CHISEL_DESIGNS.get()) {
            return false;
        }
        String design = blockEntity.getAttributeHelper().getString(location, EnumAttributeType.DESIGN_CHISEL);
        String designAdj = "";

        // Attempt to set first design to adjacent
        if (design == null || design.isEmpty()) {
            for (CbBlockEntity adjacent : getAdjacentBlockEntities(blockEntity.getLevel(), blockEntity.getBlockPos())) {
                String adjacentDesign = adjacent == null ? null : adjacent.getAttributeHelper().getString(location, EnumAttributeType.DESIGN_CHISEL);
                if (adjacentDesign != null && !adjacentDesign.isEmpty()) {
                    design = adjacentDesign;
                    designAdj = design;
                    break;
                }
            }
        }

        // Set next design
        if (designAdj.isEmpty() && InteractionHand.MAIN_HAND.equals(hand)) {
            design = leftClick ? DesignHandler.getNext(DesignType.CHISEL, design) : DesignHandler.getPrev(DesignType.CHISEL, design);
        }

        if (design != null && !design.isEmpty()) {
            blockEntity.removeAttribute(location, EnumAttributeType.DESIGN_CHISEL);
            blockEntity.addAttribute(location, EnumAttributeType.DESIGN_CHISEL, design);
        }

        return true;
    }

    /**
     * Drops side covers that are obstructed by a solid neighbor, or that the block no longer supports.
     */
    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(blockState, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return;
        }
        for (Direction facing : Direction.values()) {
            EnumAttributeLocation location = EnumAttributeLocation.fromDirection(facing);
            if (!blockEntity.getAttributeHelper().hasAttribute(location, EnumAttributeType.COVER)) {
                continue;
            }
            BlockPos neighborPos = pos.relative(facing);
            if (!canSupportCover(blockEntity, location)
                    || level.getBlockState(neighborPos).isFaceSturdy(level, neighborPos, facing.getOpposite())
                    && blockState.isFaceSturdy(level, pos, facing)) {
                blockEntity.dropAttributes(location);
            }
        }
    }

    ////////////
    // Covers act as the block
    ////////////

    /**
     * Gets the cover at a location, or null.
     */
    @Nullable
    protected static BlockState getCover(CbBlockEntity blockEntity, EnumAttributeLocation location) {
        return blockEntity.getAttributeHelper().getBlockState(location, EnumAttributeType.COVER);
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return false;
    }

    /**
     * Gets weak redstone power from covers (a redstone block cover powers like a redstone block).
     */
    @Override
    protected int getSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return 0;
        }
        int power = 0;
        for (EnumAttributeLocation location : EnumAttributeLocation.values()) {
            BlockState cover = getCover(blockEntity, location);
            if (cover == null) {
                continue;
            }
            if (EnumAttributeLocation.HOST.equals(location)) {
                power = Math.max(power, cover.getSignal(level, pos, direction));
            } else {
                for (Direction coverDirection : Direction.values()) {
                    power = Math.max(power, cover.getSignal(level, pos, coverDirection));
                }
            }
        }
        return power;
    }

    /**
     * Gets strong redstone power from the cover and the side cover facing the asking block.
     */
    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return 0;
        }
        Set<BlockState> covers = new HashSet<>();
        BlockState sideCover = getCover(blockEntity, EnumAttributeLocation.fromDirection(direction.getOpposite()));
        if (sideCover != null) {
            covers.add(sideCover);
        }
        BlockState hostCover = getCover(blockEntity, EnumAttributeLocation.HOST);
        if (hostCover != null) {
            covers.add(hostCover);
        }
        int power = 0;
        for (BlockState cover : covers) {
            power = Math.max(power, cover.getDirectSignal(level, pos, direction));
        }
        return power;
    }

    /**
     * Light from covers and glowstone, stored by the block entity where the light engine can read it.
     */
    @Override
    public boolean hasDynamicLightEmission(BlockState blockState) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState blockState, BlockGetter level, BlockPos pos) {
        AuxiliaryLightManager lightManager = level.getAuxLightManager(pos);
        return lightManager != null ? lightManager.getLightAt(pos) : 0;
    }

    /**
     * Breaks like the cover; Carpenter's tools don't break the block at all.
     */
    @Override
    protected float getDestroyProgress(BlockState blockState, Player player, BlockGetter level, BlockPos pos) {
        if (isCarpentersTool(player.getMainHandItem())) {
            return 0.0F;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        BlockState cover = blockEntity != null ? getCover(blockEntity, EnumAttributeLocation.HOST) : null;
        if (cover != null && !(cover.getBlock() instanceof BlockCoverable)) {
            return cover.getDestroyProgress(player, level, pos);
        }
        return super.getDestroyProgress(blockState, player, level, pos);
    }

    public static boolean isCarpentersTool(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item instanceof ICarpentersHammer || item instanceof ICarpentersChisel;
    }

    /**
     * Sounds like the cover.
     */
    @Override
    public SoundType getSoundType(BlockState blockState, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        BlockState cover = blockEntity != null ? getCover(blockEntity, EnumAttributeLocation.HOST) : null;
        return cover != null ? cover.getSoundType() : super.getSoundType(blockState, level, pos, entity);
    }

    /**
     * Connected textures treat this block as the side cover facing the neighbor asking, else the cover, else the
     * block itself (1.12.2 implemented Chisel's IFacade).
     */
    @Override
    public BlockState getAppearance(BlockState blockState, BlockAndLightGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return blockState;
        }
        BlockState sideCover = getCover(blockEntity, EnumAttributeLocation.fromDirection(side));
        if (sideCover != null) {
            return sideCover;
        }
        BlockState cover = getCover(blockEntity, EnumAttributeLocation.HOST);
        return cover != null ? cover : blockState;
    }

    /**
     * Burns like wood (1.12.2: encouragement 5, flammability 20).
     */
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
        if (!level.isClientSide() && placer instanceof Player player) {
            CbBlockEntity blockEntity = getBlockEntity(level, pos);
            if (blockEntity != null) {
                blockEntity.setOwner(player);
            }
        }
    }

    /**
     * Damages the Carpenter's tool used.
     */
    protected void damageItemWithChance(Level level, Player player, InteractionHand hand) {
        Item item = player.getItemInHand(hand).getItem();
        if (item instanceof ICarpentersHammer hammer) {
            hammer.onHammerUse(level, player, hand);
        } else if (item instanceof ICarpentersChisel chisel) {
            chisel.onChiselUse(level, player, hand);
        }
    }

    /**
     * Called before click event alters block features such as cover, overlay, or dye color.
     */
    protected void preOnBlockClicked(CbBlockEntity blockEntity, Player player, ActionResult actionResult) {}

    /**
     * Called before activation event alters block features such as cover, overlay, or dye color.
     */
    protected void preOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {}

    /**
     * Called after attribute changes processed as long as no changes occurred.
     */
    protected void postOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {}

    /**
     * Handles hammer left click interactions. Override this for block-specific functionality.
     *
     * @return <code>true</code> if the block changed
     */
    protected boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        return false;
    }

    /**
     * Handles hammer right click interactions. Override this for block-specific functionality.
     *
     * @return <code>true</code> if the block changed
     */
    protected boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        return false;
    }

    /**
     * Gets whether block location supports a cover: one cover for the whole block, side covers only where a block
     * allows them.
     */
    protected boolean canSupportCover(CbBlockEntity blockEntity, EnumAttributeLocation location) {
        return EnumAttributeLocation.HOST.equals(location) || supportsSideCovers();
    }

    /**
     * Whether the block takes side covers.
     */
    public boolean supportsSideCovers() {
        return false;
    }

    /**
     * Allows a block entity called during block activation to be changed before altering attributes like cover, dye,
     * overlay, etc. Offered for the garage door, when open, to swap the top piece with the bottom piece.
     */
    protected CbBlockEntity getBlockEntityForBlockActivation(CbBlockEntity blockEntity) {
        return blockEntity;
    }

}
