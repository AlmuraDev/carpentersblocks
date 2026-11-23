package com.carpentersblocks.client.model;

import java.util.ArrayList;
import java.util.List;

import com.carpentersblocks.blockentity.CbRenderData;
import com.carpentersblocks.util.attribute.EnumAttributeLocation;
import com.carpentersblocks.util.attribute.EnumAttributeType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Biome and block tints of covers (grass, leaves, vines...) and the grass overlay, applied by the chunk renderer
 * (1.12.2's CoverColorHandler).
 * <p>
 * A Carpenter's block has a list of tint sources, and its quads pick one by tint index: one per cover location and
 * cover tint index, plus the grass overlay. Each looks up the cover in the block's model data, so it reads the chunk
 * snapshot being meshed rather than the live world.
 */
public final class CoverTints {

    /** Grass color, for the grass overlay. */
    public static final int TINT_GRASS = 0;

    /** Cover tints: base + location * COVER_TINTS + the cover quad's own tint index. */
    private static final int TINT_COVER = 1;

    /** Tint indices kept per cover; vanilla blocks use at most two. */
    private static final int COVER_TINTS = 4;

    private CoverTints() {}

    /**
     * Gets the tint index for a cover quad, or -1 if the cover's tint index is out of range.
     */
    public static int getCoverTint(EnumAttributeLocation location, int coverTintIndex) {
        if (coverTintIndex < 0 || coverTintIndex >= COVER_TINTS) {
            return -1;
        }
        return TINT_COVER + location.ordinal() * COVER_TINTS + coverTintIndex;
    }

    /**
     * Gets the tint sources of a Carpenter's block, in tint index order.
     */
    public static List<BlockTintSource> createTintSources() {
        List<BlockTintSource> sources = new ArrayList<>();
        sources.add(new GrassTint());
        for (EnumAttributeLocation location : EnumAttributeLocation.values()) {
            for (int tint = 0; tint < COVER_TINTS; ++tint) {
                sources.add(new CoverTint(location, tint));
            }
        }
        return sources;
    }

    private static int colorOf(BlockState blockState, int tintIndex, BlockAndTintGetter level, BlockPos pos) {
        BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(blockState, tintIndex);
        return source == null ? -1 : source.colorInWorld(blockState, level, pos);
    }

    private static final class GrassTint implements BlockTintSource {

        @Override
        public int color(BlockState blockState) {
            return -1;
        }

        @Override
        public int colorInWorld(BlockState blockState, BlockAndTintGetter level, BlockPos pos) {
            return colorOf(Blocks.GRASS_BLOCK.defaultBlockState(), 0, level, pos);
        }

    }

    private record CoverTint(EnumAttributeLocation location, int coverTintIndex) implements BlockTintSource {

        @Override
        public int color(BlockState blockState) {
            return -1;
        }

        @Override
        public int colorInWorld(BlockState blockState, BlockAndTintGetter level, BlockPos pos) {
            CbRenderData data = level.getModelData(pos).get(CbRenderData.PROPERTY);
            BlockState cover = data == null ? null : data.getAttributeHelper().getBlockState(location, EnumAttributeType.COVER);
            return cover == null ? -1 : colorOf(cover, coverTintIndex, level, pos);
        }

    }

}
