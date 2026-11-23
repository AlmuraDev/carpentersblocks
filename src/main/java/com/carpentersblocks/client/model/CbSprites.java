package com.carpentersblocks.client.model;

import com.carpentersblocks.CarpentersBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

/**
 * The block atlas textures the models draw with (1.12.2's SpriteRegistry). 26.3 puts every texture under
 * textures/block in the block atlas, so these are looked up rather than registered.
 */
public final class CbSprites {

    public static final Identifier UNCOVERED_SOLID         = CarpentersBlocks.id("block/general/uncovered_solid");
    public static final Identifier UNCOVERED_FULL          = CarpentersBlocks.id("block/general/uncovered_full_frame");
    public static final Identifier UNCOVERED_QUARTERED     = CarpentersBlocks.id("block/general/uncovered_quartered_frame");
    public static final Identifier UNCOVERED_OBLIQUE_POS   = CarpentersBlocks.id("block/slope/uncovered_oblique_pos");
    public static final Identifier UNCOVERED_OBLIQUE_NEG   = CarpentersBlocks.id("block/slope/uncovered_oblique_neg");
    public static final Identifier OVERLAY_FAST_GRASS_SIDE = CarpentersBlocks.id("block/overlay/overlay_fast_grass_side");
    public static final Identifier OVERLAY_HAY_SIDE        = CarpentersBlocks.id("block/overlay/overlay_hay_side");
    public static final Identifier OVERLAY_SNOW_SIDE       = CarpentersBlocks.id("block/overlay/overlay_snow_side");
    public static final Identifier OVERLAY_MYCELIUM_SIDE   = CarpentersBlocks.id("block/overlay/overlay_mycelium_side");
    public static final Identifier GRASS_TOP               = Identifier.withDefaultNamespace("block/grass_block_top");
    public static final Identifier GRASS_SIDE_OVERLAY      = Identifier.withDefaultNamespace("block/grass_block_side_overlay");

    private CbSprites() {}

    public static TextureAtlas blockAtlas() {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
    }

    /**
     * Gets a block atlas texture, or the missing texture.
     */
    public static TextureAtlasSprite get(Identifier id) {
        return blockAtlas().getSprite(id);
    }

    public static boolean isMissing(TextureAtlasSprite sprite) {
        return sprite == blockAtlas().missingSprite();
    }

}
