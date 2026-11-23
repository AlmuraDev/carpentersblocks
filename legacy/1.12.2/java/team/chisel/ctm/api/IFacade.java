package team.chisel.ctm.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Compile-only copy of CTM's facade API (ConnectedTexturesMod, used by Chisel), excluded from the
 * jar; CTM provides the real interface at runtime. Blocks implementing it are treated by connected
 * textures as the block state they return.
 */
public interface IFacade {

    @Nonnull
    IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side);

}
