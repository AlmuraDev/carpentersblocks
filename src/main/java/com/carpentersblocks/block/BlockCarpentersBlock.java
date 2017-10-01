package com.carpentersblocks.block;

import com.carpentersblocks.block.state.Property;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCarpentersBlock extends BlockCoverable
{
    private static final PropertyBool HALF = PropertyBool.create("half");
    private static final PropertyDirection FACING = PropertyDirection.create("facing");

    private static final AxisAlignedBB TOP_SLAB = new AxisAlignedBB(0, .5f, 0, 1, 1, 1);
	private static final AxisAlignedBB BOT_SLAB = new AxisAlignedBB(0, 0, 0, 1, .5f, 1);
	private static final AxisAlignedBB NORTH_SLAB = new AxisAlignedBB(0, 0, .5f, 1, 1, 1);
	private static final AxisAlignedBB EAST_SLAB = new AxisAlignedBB(0, 0, 0, .5f, 1, 1); //gg
	private static final AxisAlignedBB SOUTH_SLAB = new AxisAlignedBB(0, 0, 0, 1, 1, .5f); //gg
	private static final AxisAlignedBB WEST_SLAB = new AxisAlignedBB(.5f, 0, 0, 1, 1, 1);

    public BlockCarpentersBlock(Material material)
    {
        super(material);
        setSoundType(SoundType.WOOD);
        this.setDefaultState(this.getDefaultState().withProperty(HALF, false).withProperty(FACING, EnumFacing.UP));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        switch(state.getValue(FACING))
        {
            case NORTH:
                return 3;
            case EAST:
                return 4;
            case SOUTH:
                return 5;
            case WEST:
                return 6;
            case UP:
                if(!state.getValue(HALF).booleanValue())
                    return 0;
                return 1;
            case DOWN:
            default:
                return 2;
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
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        EnumFacing facing = blockState.getValue(FACING);
        switch(facing)
        {
            case NORTH:
                return NORTH_SLAB;
            case EAST:
                return EAST_SLAB;
            case SOUTH:
                return SOUTH_SLAB;
            case WEST:
                return WEST_SLAB;
            case DOWN:
                return BOT_SLAB;
            case UP:
            default:
                if(blockState.getValue(HALF).booleanValue())
                    return TOP_SLAB;
                return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        EnumFacing facing = state.getValue(FACING);
        switch(facing)
        {
            case NORTH:
                return NORTH_SLAB;
            case EAST:
                return EAST_SLAB;
            case SOUTH:
                return SOUTH_SLAB;
            case WEST:
                return WEST_SLAB;
            case DOWN:
                return BOT_SLAB;
            case UP:
            default:
                if(state.getValue(HALF).booleanValue())
                    return TOP_SLAB;
                return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        }
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] {HALF, FACING}, Property.unlistedProperties.toArray(new IUnlistedProperty[Property.unlistedProperties.size()]));
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
        World world = entityPlayer.world;
        BlockPos pos = cbTileEntity.getPos();
        if(world.getBlockState(pos).getValue(HALF).booleanValue())
            return false;
        world.setBlockState(pos, this.getDefaultState().withProperty(FACING, entityPlayer.getHorizontalFacing().getOpposite()).withProperty(HALF, true));
        return true;
    }

    @Override
    protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer)
    {
        World world = entityPlayer.world;
        BlockPos pos = cbTileEntity.getPos();
        if(!world.getBlockState(pos).getValue(HALF).booleanValue())
            return false;
        world.setBlockState(pos, this.getDefaultState().withProperty(FACING, EnumFacing.UP).withProperty(HALF, false));
        return true;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        // TODO Auto-generated method stub
        return false;
    }
}