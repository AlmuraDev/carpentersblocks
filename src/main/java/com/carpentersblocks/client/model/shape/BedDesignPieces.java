package com.carpentersblocks.client.model.shape;

import com.carpentersblocks.client.model.Quad;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * The 8 pieces of a bed design texture (1.12.2's DesignHandler.getBedIcons, which cut them into separate textures in a
 * generated resource pack; here each quad shows its piece of the whole texture).
 * <p>
 * A design is a grid three pieces wide (16x16 pieces in a 48x64 texture for the 16x template). Pieces are read row by
 * row, skipping the four corners of the 3x4 grid; the side pieces are turned.
 */
final class BedDesignPieces {

    // Grid cell of each piece, counted row by row, and its clockwise quarter turns
    private static final int[] CELLS = { 1, 3, 4, 5, 6, 7, 8, 10 };
    private static final int[] TURNS = { 0, 3, 0, 1, 3, 0, 1, 0 };
    private static final int COLUMNS = 3;

    private BedDesignPieces() {}

    /**
     * Makes a quad, textured with a whole bed design, show one of its pieces.
     */
    static void apply(Quad quad, int piece) {
        if (piece < 0 || piece >= CELLS.length) {
            return;
        }
        TextureAtlasSprite sprite = quad.getSprite();
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        int rows = Math.max(1, Math.round(COLUMNS * (float) height / width));
        int column = CELLS[piece] % COLUMNS;
        int row = CELLS[piece] / COLUMNS;
        quad.setSpriteRegion(
            (float) column / COLUMNS, (float) row / rows,
            (float) (column + 1) / COLUMNS, (float) (row + 1) / rows,
            TURNS[piece]);
    }

}
