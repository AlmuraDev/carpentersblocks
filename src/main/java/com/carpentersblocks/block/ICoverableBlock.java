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

import com.carpentersblocks.util.handler.OverlayHandler;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DryVegetationBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

/**
 * A block that takes covers, side covers, dyes, overlays, chisel designs and glowstone (1.12.2's BlockCoverable).
 * <p>
 * Attributes are added by right clicking with them. The Carpenter's Hammer removes them (sneak left click) and turns
 * blocks; the Carpenter's Chisel cycles chisel designs on covers. Left clicks reach {@link #onBlockClicked} through
 * {@link com.carpentersblocks.CommonEvents}, which knows the clicked face.
 * <p>
 * The behavior lives here so blocks that must extend a vanilla class (the bed is an AbstractBedBlock, which is the only
 * kind of block players sleep in) share it: {@link BlockCoverable} and those blocks forward the vanilla methods to the
 * <code>coverable...</code> methods. Blocks override the hooks.
 */
public interface ICoverableBlock extends EntityBlock {

    /**
     * Stores actions taken on a block in order to properly play sounds, decrement player inventory, and to determine
     * if a block was altered.
     */
    class ActionResult {

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

    default Block coverableBlock() {
        return (Block) this;
    }

    ////////////
    // Hooks
    ////////////

    /**
     * Gets the data the block is drawn and shaped from. Blocks whose shape depends on their neighbors (fences) add
     * that here.
     * <p>
     * Also called while chunks are meshed, off the main thread: read neighbors only through the level, and their
     * block data through {@link CoverableSupport#getCbMetadataAt}.
     *
     * @param cbMetadata the block's own data
     */
    default int getRenderData(BlockGetter level, BlockPos pos, int cbMetadata) {
        return cbMetadata;
    }

    /**
     * Gets the position of the block whose cover, dye and overlays this block is drawn with. Blocks drawn with
     * another piece's look (an open garage door) override this. Also called while chunks are meshed.
     */
    default BlockPos getRenderAttributeSource(BlockGetter level, BlockPos pos, int cbMetadata) {
        return pos;
    }

    /**
     * Gets the render data at a position (main thread: from the block entity).
     */
    default int getRenderDataAt(BlockGetter level, BlockPos pos) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        return getRenderData(level, pos, blockEntity != null ? blockEntity.getCbMetadata() : 0);
    }

    /**
     * Gets the data an item of this block is drawn with.
     */
    default int getInventoryRenderData(ItemStack itemStack) {
        return this instanceof IShapedBlock shapedBlock ? shapedBlock.getInventoryRenderData(0) : 0;
    }

    /**
     * Gets the block's light: by default the light of its attributes (glowstone, glowing covers).
     */
    default int getLightValue(CbBlockEntity blockEntity, int attributeLight) {
        return attributeLight;
    }

    /**
     * Returns the block entity if it is a Carpenter's block entity of this block.
     */
    @Nullable
    default CbBlockEntity getBlockEntity(BlockGetter level, BlockPos pos) {
        CbBlockEntity blockEntity = CoverableSupport.getSimpleBlockEntity(level, pos);
        return blockEntity != null && blockEntity.getBlockState().is(coverableBlock()) ? blockEntity : null;
    }

    /**
     * Returns adjacent Carpenter's blocks, used for copying designs.
     */
    default CbBlockEntity[] getAdjacentBlockEntities(Level level, BlockPos pos) {
        CbBlockEntity[] blockEntities = new CbBlockEntity[Direction.values().length];
        for (Direction direction : Direction.values()) {
            blockEntities[direction.ordinal()] = CoverableSupport.getSimpleBlockEntity(level, pos.relative(direction));
        }
        return blockEntities;
    }

    /**
     * Returns whether player is allowed to activate this block.
     */
    default boolean canPlayerActivate(CbBlockEntity blockEntity, Player player) {
        return true;
    }

    /**
     * Whether {@link #preOnBlockActivated} would change the block with this item (relighting a torch), for the client
     * side of right clicks with an item.
     */
    default boolean wouldPreActivate(CbBlockEntity blockEntity, ItemStack itemStack, Player player) {
        return false;
    }

    /**
     * Whether {@link #postOnBlockActivated} changes the block (pressing a button, opening a gate), for the client side
     * of clicks that add nothing.
     */
    default boolean wouldActivateBlock(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult) {
        return false;
    }

    /**
     * Called before click event alters block features such as cover, overlay, or dye color.
     */
    default void preOnBlockClicked(CbBlockEntity blockEntity, Player player, ActionResult actionResult) {}

    /**
     * Called before activation event alters block features such as cover, overlay, or dye color.
     */
    default void preOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {}

    /**
     * Called for clicks that add no attribute (pressing a button, opening a door).
     */
    default void postOnBlockActivated(CbBlockEntity blockEntity, Player player, BlockHitResult hitResult, ActionResult actionResult) {}

    /**
     * Handles hammer left click interactions.
     *
     * @return <code>true</code> if the block changed
     */
    default boolean onHammerLeftClick(CbBlockEntity blockEntity, Player player) {
        return false;
    }

    /**
     * Handles hammer right click interactions.
     *
     * @return <code>true</code> if the block changed
     */
    default boolean onHammerRightClick(CbBlockEntity blockEntity, Player player) {
        return false;
    }

    /**
     * Gets whether block location supports a cover: one cover for the whole block, side covers only where a block
     * allows them.
     */
    default boolean canSupportCover(CbBlockEntity blockEntity, EnumAttributeLocation location) {
        return EnumAttributeLocation.HOST.equals(location) || supportsSideCovers();
    }

    /**
     * Whether the block takes side covers.
     */
    default boolean supportsSideCovers() {
        return false;
    }

    /**
     * Allows a block entity called during block activation to be changed before altering attributes. Offered for the
     * garage door, when open, to swap the top piece with the bottom piece.
     */
    default CbBlockEntity getBlockEntityForBlockActivation(CbBlockEntity blockEntity) {
        return blockEntity;
    }

    /**
     * Damages the Carpenter's tool used.
     */
    default void damageItemWithChance(Level level, Player player, InteractionHand hand) {
        Item item = player.getItemInHand(hand).getItem();
        if (item instanceof ICarpentersHammer hammer) {
            hammer.onHammerUse(level, player, hand);
        } else if (item instanceof ICarpentersChisel chisel) {
            chisel.onChiselUse(level, player, hand);
        }
    }

    /**
     * Gets the block state a cover is drawn with: the way the block would be placed by the player, for blocks that
     * turn (logs, quartz pillars).
     */
    @Nullable
    default BlockState getCoverState(ItemStack itemStack, Player player, BlockHitResult hitResult) {
        if (!(itemStack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        BlockState defaultState = blockItem.getBlock().defaultBlockState();
        if (!defaultState.hasProperty(BlockStateProperties.AXIS)) {
            return null;
        }
        return defaultState.setValue(BlockStateProperties.AXIS, hitResult.getDirection().getAxis());
    }

    /**
     * Cycles through chisel patterns.
     *
     * @param location the location of the cover being chiseled
     * @param leftClick whether this is a left click (next design) or right click (previous design)
     */
    default boolean onChiselClick(CbBlockEntity blockEntity, EnumAttributeLocation location, InteractionHand hand, boolean leftClick) {
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

    ////////////
    // Left clicks
    ////////////

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
    default void onBlockClicked(Level level, BlockPos pos, Player player, Direction face) {
        // The left click event has no hit position: trace it, for blocks that care where they were hit
        Vec3 location = player.pick(player.blockInteractionRange(), 1.0F, false) instanceof BlockHitResult trace && trace.getBlockPos().equals(pos)
            ? trace.getLocation() : Vec3.atCenterOf(pos);
        CoverableSupport.CLICK_HIT.set(new BlockHitResult(location, face, pos, false));
        try {
            handleBlockClicked(level, pos, player, face);
        } finally {
            CoverableSupport.CLICK_HIT.remove();
        }
    }

    private void handleBlockClicked(Level level, BlockPos pos, Player player, Direction face) {
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

    ////////////
    // Right clicks
    ////////////

    /**
     * Whether a right click with an item would change the block, without changing it. Decides on the client whether
     * the click is used here or the item is used (placed) instead.
     */
    default boolean wouldActivate(CbBlockEntity blockEntity, ItemStack itemStack, Level level, Player player, InteractionHand hand, Direction facing) {
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

    /**
     * Block.useItemOn: adds attributes and uses the Carpenter's tools.
     */
    default InteractionResult coverableUseItemOn(ItemStack itemStack, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        CoverableSupport.CLICK_HIT.set(hitResult);
        try {
            return handleUseItemOn(itemStack, level, pos, player, hand, hitResult);
        } finally {
            CoverableSupport.CLICK_HIT.remove();
        }
    }

    private InteractionResult handleUseItemOn(ItemStack itemStack, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
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
     * Block.useWithoutItem: clicks that add no attribute (with an empty hand, or an item that does nothing here) use
     * the block.
     */
    default InteractionResult coverableUseWithoutItem(Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        CoverableSupport.CLICK_HIT.set(hitResult);
        try {
            return handleUseWithoutItem(level, pos, player, hitResult);
        } finally {
            CoverableSupport.CLICK_HIT.remove();
        }
    }

    private InteractionResult handleUseWithoutItem(Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
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

    ////////////
    // Neighbors, shapes, redstone, light...
    ////////////

    /**
     * Block.neighborChanged: drops side covers that are obstructed by a solid neighbor, or that the block no longer
     * supports.
     */
    default void coverableNeighborChanged(BlockState blockState, Level level, BlockPos pos) {
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

    /**
     * Block.getShape of an IShapedBlock, or null for other blocks.
     */
    @Nullable
    default VoxelShape coverableGetShape(BlockGetter level, BlockPos pos) {
        return this instanceof IShapedBlock shapedBlock
            ? CoverableSupport.getOutlineShape(coverableBlock(), getRenderDataAt(level, pos), shapedBlock::getSelectionBoxes) : null;
    }

    /**
     * Block.getCollisionShape of an IShapedBlock, or null for other blocks.
     */
    @Nullable
    default VoxelShape coverableGetCollisionShape(BlockGetter level, BlockPos pos) {
        return this instanceof IShapedBlock shapedBlock
            ? CoverableSupport.getCollisionShape(coverableBlock(), getRenderDataAt(level, pos), shapedBlock::getCollisionBoxes) : null;
    }

    /**
     * Block.getSignal: weak redstone power from covers (a redstone block cover powers like a redstone block).
     */
    default int coverableGetSignal(BlockGetter level, BlockPos pos, Direction direction) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return 0;
        }
        int power = 0;
        for (EnumAttributeLocation location : EnumAttributeLocation.values()) {
            BlockState cover = CoverableSupport.getCover(blockEntity, location);
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
     * Block.getDirectSignal: strong redstone power from the cover and the side cover facing the asking block.
     */
    default int coverableGetDirectSignal(BlockGetter level, BlockPos pos, Direction direction) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return 0;
        }
        Set<BlockState> covers = new HashSet<>();
        BlockState sideCover = CoverableSupport.getCover(blockEntity, EnumAttributeLocation.fromDirection(direction.getOpposite()));
        if (sideCover != null) {
            covers.add(sideCover);
        }
        BlockState hostCover = CoverableSupport.getCover(blockEntity, EnumAttributeLocation.HOST);
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
     * Light from covers and glowstone, stored by the block entity where the light engine can read it (forward
     * hasDynamicLightEmission as true).
     */
    default int coverableGetLightEmission(BlockGetter level, BlockPos pos) {
        AuxiliaryLightManager lightManager = level.getAuxLightManager(pos);
        return lightManager != null ? lightManager.getLightAt(pos) : 0;
    }

    /**
     * Block.getDestroyProgress: breaks like the cover; Carpenter's tools don't break the block at all.
     *
     * @return the progress, or NaN to use the block's own
     */
    default float coverableGetDestroyProgress(Player player, BlockGetter level, BlockPos pos) {
        if (CoverableSupport.isCarpentersTool(player.getMainHandItem())) {
            return 0.0F;
        }
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        BlockState cover = blockEntity != null ? CoverableSupport.getCover(blockEntity, EnumAttributeLocation.HOST) : null;
        if (cover != null && !(cover.getBlock() instanceof ICoverableBlock)) {
            return cover.getDestroyProgress(player, level, pos);
        }
        return Float.NaN;
    }

    /**
     * Block.getSoundType: sounds like the cover.
     *
     * @return the sound, or null to use the block's own
     */
    @Nullable
    default SoundType coverableGetSoundType(LevelReader level, BlockPos pos) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        BlockState cover = blockEntity != null ? CoverableSupport.getCover(blockEntity, EnumAttributeLocation.HOST) : null;
        return cover != null ? cover.getSoundType() : null;
    }

    /**
     * Block.getAppearance: connected textures treat this block as the side cover facing the neighbor asking, else the
     * cover, else the block itself (1.12.2 implemented Chisel's IFacade).
     */
    default BlockState coverableGetAppearance(BlockState blockState, BlockAndLightGetter level, BlockPos pos, Direction side) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) {
            return blockState;
        }
        BlockState sideCover = CoverableSupport.getCover(blockEntity, EnumAttributeLocation.fromDirection(side));
        if (sideCover != null) {
            return sideCover;
        }
        BlockState cover = CoverableSupport.getCover(blockEntity, EnumAttributeLocation.HOST);
        return cover != null ? cover : blockState;
    }

    /**
     * Block.setPlacedBy: the placer owns the block.
     */
    default void coverableSetPlacedBy(Level level, BlockPos pos, @Nullable LivingEntity placer) {
        if (!level.isClientSide() && placer instanceof Player player) {
            CbBlockEntity blockEntity = getBlockEntity(level, pos);
            if (blockEntity != null) {
                blockEntity.setOwner(player);
            }
        }
    }


    /**
     * Block.canSustainPlant: a solid face covered with a soil (or with a grass or mycelium overlay) holds the plants
     * that soil holds (1.12.2: desert, nether, plains and beach plants). Plants are matched to the 26.3 support tags.
     */
    default TriState coverableCanSustainPlant(BlockState blockState, BlockGetter level, BlockPos pos, Direction facing, BlockState plant) {
        CbBlockEntity blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null || !blockState.isFaceSturdy(level, pos, facing)) {
            return TriState.DEFAULT;
        }
        AttributeHelper helper = blockEntity.getAttributeHelper();
        EnumAttributeLocation location = BlockUtil.getAttributeLocationForFacing(blockEntity, facing);
        Set<BlockState> soils = new HashSet<>();
        BlockState cover = helper.getBlockState(location, EnumAttributeType.COVER);
        if (cover != null) {
            soils.add(cover);
        }
        if (helper.hasAttribute(location, EnumAttributeType.OVERLAY)) {
            OverlayHandler.Overlay overlay = OverlayHandler.getOverlayType(helper.getItemStack(location, EnumAttributeType.OVERLAY));
            if (overlay != null && overlay != OverlayHandler.Overlay.NONE) {
                soils.add(overlay.getBlock().defaultBlockState());
            }
        }
        for (BlockState soil : soils) {
            if (soil.getBlock() instanceof ICoverableBlock) {
                continue;
            }
            TriState decision = soil.canSustainPlant(level, pos, facing, plant);
            if (!decision.isDefault()) {
                if (decision.isTrue()) {
                    return TriState.TRUE;
                }
                continue;
            }
            if (supportsPlant(soil, level, pos, plant)) {
                return TriState.TRUE;
            }
        }
        return TriState.DEFAULT;
    }

    /**
     * Whether a soil holds a plant, by the support tag the plant checks.
     */
    private static boolean supportsPlant(BlockState soil, BlockGetter level, BlockPos pos, BlockState plant) {
        Block block = plant.getBlock();
        if (block instanceof SugarCaneBlock) {
            if (!soil.is(BlockTags.SUPPORTS_SUGAR_CANE)) {
                return false;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos side = pos.relative(direction);
                if (level.getFluidState(side).is(FluidTags.SUPPORTS_SUGAR_CANE_ADJACENTLY)
                        || level.getBlockState(side).is(BlockTags.SUPPORTS_SUGAR_CANE_ADJACENTLY)) {
                    return true;
                }
            }
            return false;
        }
        if (block instanceof CactusBlock) {
            return soil.is(BlockTags.SUPPORTS_CACTUS);
        }
        if (block instanceof NetherWartBlock) {
            return soil.is(BlockTags.SUPPORTS_NETHER_WART);
        }
        if (block instanceof BambooStalkBlock || block instanceof BambooSaplingBlock) {
            return soil.is(BlockTags.SUPPORTS_BAMBOO);
        }
        if (block instanceof CropBlock || block instanceof StemBlock) {
            return soil.is(BlockTags.SUPPORTS_CROPS);
        }
        if (block instanceof DryVegetationBlock) {
            return soil.is(BlockTags.SUPPORTS_DRY_VEGETATION);
        }
        if (block instanceof VegetationBlock) {
            return soil.is(BlockTags.SUPPORTS_VEGETATION);
        }
        return false;
    }

}
