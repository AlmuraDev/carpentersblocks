package com.carpentersblocks.block;

import com.carpentersblocks.block.types.BlockCoverable;
import com.carpentersblocks.tileentity.CbTileEntity;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCarpentersBlock extends BlockCoverable
{
    private static final PropertyBool HALF = PropertyBool.create("half");
    private static final PropertyDirection FACING = PropertyDirection.create("facing");

    public BlockCarpentersBlock(Material material)
    {
        super(material);
        setSoundType(SoundType.WOOD);
        this.setDefaultState(this.getDefaultState().withProperty(HALF, false).withProperty(FACING, EnumFacing.UP));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        if(!state.getValue(HALF).booleanValue())
            return 0;
        else
        {
            switch(state.getValue(FACING).getIndex()) //DUNSWE
            {
                case 1:
                    return 1;
                case 0:
                    return 2;
                case 2:
                    return 3;
                case 5:
                    return 4;
                case 3:
                    return 5;
                case 4:
                    return 6;

                default:
                    return 0;
            }
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        switch(meta)
        {
            case 0:
                return this.getDefaultState().withProperty(HALF, false).withProperty(FACING, EnumFacing.UP);
            case 1:
                return this.getDefaultState().withProperty(HALF, true).withProperty(FACING, EnumFacing.UP);
            case 2:
                return this.getDefaultState().withProperty(HALF, true).withProperty(FACING, EnumFacing.DOWN);
            case 3:
                return this.getDefaultState().withProperty(HALF, true).withProperty(FACING, EnumFacing.NORTH);
            case 4:
                return this.getDefaultState().withProperty(HALF, true).withProperty(FACING, EnumFacing.EAST);
            case 5:
                return this.getDefaultState().withProperty(HALF, true).withProperty(FACING, EnumFacing.SOUTH);
            case 6:
                return this.getDefaultState().withProperty(HALF, true).withProperty(FACING, EnumFacing.WEST);

            default:
                return this.getDefaultState().withProperty(HALF, false).withProperty(FACING, EnumFacing.UP);
        }
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {HALF, FACING});
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState blockState)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState blockState)
    {
        return false;
    }

    @Override
    protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer)
    {
        //slabify
        return true;
    }
}