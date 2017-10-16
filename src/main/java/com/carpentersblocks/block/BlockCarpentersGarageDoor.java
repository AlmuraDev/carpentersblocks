package com.carpentersblocks.block;

import com.carpentersblocks.Reference;
import com.carpentersblocks.block.state.Property;
import com.carpentersblocks.block.types.BlockCoverable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCarpentersGarageDoor extends BlockCoverable
{
	private static final PropertyDirection FACING = BlockHorizontal.FACING;
	private static final PropertyBool OPEN = PropertyBool.create("open");

	protected static final AxisAlignedBB EAST_CLOSED_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);
	protected static final AxisAlignedBB WEST_CLOSED_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB SOUTH_CLOSED_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
	protected static final AxisAlignedBB NORTH_CLOSED_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB TOP_AABB = new AxisAlignedBB(0.0D, 0.8125D, 0.0D, 1.0D, 1.0D, 1.0D);

	public BlockCarpentersGarageDoor(Material material)
	{
		super(material);
		this.setDefaultState(this.getDefaultState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, true));
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
	{
			super.breakBlock(worldIn, pos, state);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		if(!state.getValue(OPEN).booleanValue())
		{
			switch((EnumFacing)state.getValue(FACING))
			{
				case EAST:
					return EAST_CLOSED_AABB;
				case WEST:
					return WEST_CLOSED_AABB;
				case SOUTH:
					return SOUTH_CLOSED_AABB;
				case NORTH:
					return NORTH_CLOSED_AABB;
			}
		}
		return TOP_AABB;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		if(!state.getValue(OPEN).booleanValue())
		{
			switch((EnumFacing)state.getValue(FACING))
			{
				case EAST:
					return EAST_CLOSED_AABB;
				case WEST:
					return WEST_CLOSED_AABB;
				case SOUTH:
					return SOUTH_CLOSED_AABB;
				case NORTH:
					return NORTH_CLOSED_AABB;
			}
		}
		return TOP_AABB;
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		if(!state.getValue(OPEN).booleanValue())
		{
			switch((EnumFacing)state.getValue(FACING))
			{
				case EAST:
					return 4;
				case WEST:
					return 3;
				case SOUTH:
					return 2;
				case NORTH:
					return 1;
			}
		}
		return 0;
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		switch(meta)
		{
			case 1:
				return this.getDefaultState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, false);
			case 2:
				return this.getDefaultState().withProperty(FACING, EnumFacing.SOUTH).withProperty(OPEN, false);
			case 3:
				return this.getDefaultState().withProperty(FACING, EnumFacing.WEST).withProperty(OPEN, false);
			case 4:
				return this.getDefaultState().withProperty(FACING, EnumFacing.EAST).withProperty(OPEN, false);
			case 0:
			default:
				return this.getDefaultState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, true);
		}
	}

	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] {FACING, OPEN});
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
	{
		if(worldIn.getBlockState(pos.up()).isFullBlock())
			return true;
		return false;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer)
	{
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing()).withProperty(OPEN, true);
	}

	//@Override
	// Not
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn)
	{
		if(!worldIn.getBlockState(pos.up()).isFullBlock() && !worldIn.getBlockState(pos.up()).getBlock().equals(this))
		{
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer entityPlayer,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		Boolean open = blockState.getValue(OPEN).booleanValue();
		facing = blockState.getValue(FACING);
		EnumFacing left = facing.rotateY().getOpposite();
		int rowSize = 0;
		BlockPos topleft = blockPos;
		switch(left)
		{
			case NORTH:
				while(world.getBlockState(topleft.north()).getBlock().equals(this)
						&& world.getBlockState(topleft.north()).getValue(FACING).equals(facing))
				{
					topleft = topleft.north();
				}
				break;
			case EAST:
				while(world.getBlockState(topleft.east()).getBlock().equals(this)
						&& world.getBlockState(topleft.east()).getValue(FACING).equals(facing))
				{
					topleft = topleft.east();
				}
				break;
			case SOUTH:
				while(world.getBlockState(topleft.south()).getBlock().equals(this)
						&& world.getBlockState(topleft.south()).getValue(FACING).equals(facing))
				{
					topleft = topleft.south();
				}
				break;
			case WEST:
				while(world.getBlockState(topleft.west()).getBlock().equals(this)
						&& world.getBlockState(topleft.west()).getValue(FACING).equals(facing))
				{
					topleft = topleft.west();
				}
				break;
		}

		while(world.getBlockState(topleft.up()).getBlock().equals(this)
				&& world.getBlockState(topleft.up()).getValue(FACING).equals(facing))
		{
			topleft = topleft.up();
		}

		BlockPos temp = topleft;
		switch(left.getOpposite())
		{
			case NORTH:
				while(world.getBlockState(temp.north()).getBlock().equals(this)
						&& world.getBlockState(temp.north()).getValue(FACING).equals(facing))
				{
					rowSize++;
					temp = temp.north();
				}
				break;
			case EAST:
				while(world.getBlockState(temp.east()).getBlock().equals(this)
						&& world.getBlockState(temp.east()).getValue(FACING).equals(facing))
				{
					rowSize++;
					temp = temp.east();
				}
				break;
			case SOUTH:
				while(world.getBlockState(temp.south()).getBlock().equals(this)
						&& world.getBlockState(temp.south()).getValue(FACING).equals(facing))
				{
					rowSize++;
					temp = temp.south();
				}
				break;
			case WEST:
				while(world.getBlockState(temp.west()).getBlock().equals(this)
						&& world.getBlockState(temp.west()).getValue(FACING).equals(facing))
				{
					rowSize++;
					temp = temp.west();
				}
				break;
		}
		blockPos = topleft;

		if(open)
		{
			Boolean nextRow = true;
			int total = 0;

			for(int i=0; i<=rowSize; i++)
			{
				if(world.getBlockState(blockPos).getBlock().equals(this))
				{
					world.playSound(entityPlayer, blockPos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1f, 1f);
				}
				world.setBlockState(blockPos, this.getDefaultState().withProperty(FACING, facing).withProperty(OPEN, false));
				total++;

				if(!world.getBlockState(blockPos.down()).getBlock().equals(Blocks.AIR))
					nextRow = false;



				if(i==rowSize)
				{
					if(!nextRow || blockPos.getY()-1 < 0 || (total+rowSize+1) > Reference.multiBlockSizeLimit)
						break;
					blockPos = new BlockPos(topleft.getX(), blockPos.getY()-1, topleft.getZ());
					i=-1;
				}
				else
				{
					switch(left.getOpposite())
					{
						case NORTH:
							blockPos = blockPos.north();
							break;
						case EAST:
							blockPos = blockPos.east();
							break;
						case SOUTH:
							blockPos = blockPos.south();
							break;
						case WEST:
							blockPos = blockPos.west();
							break;
					}
				}
			}
		}
		else
		{
			for(int i=0; i<=rowSize;i++)
			{
				world.setBlockState(blockPos, this.getDefaultState().withProperty(OPEN, true).withProperty(FACING, facing));
				world.playSound(entityPlayer, blockPos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1f, 1f);

				while(world.getBlockState(blockPos.down()).getBlock().equals(this) && blockPos.getY() > 0)
					blockPos = blockPos.down();

				int minY = blockPos.getY();

				while(blockPos.getY() >= 0 && world.getBlockState(blockPos.up()).getBlock().equals(this))
				{

					world.setBlockToAir(blockPos);
					blockPos = blockPos.up();
				}

				world.setBlockState(blockPos, this.getDefaultState().withProperty(FACING, facing).withProperty(OPEN, true));
				blockPos = new BlockPos(blockPos.getX(), minY, blockPos.getZ());

				switch(left.getOpposite())
				{
					case NORTH:
						blockPos = blockPos.north();
						break;
					case EAST:
						blockPos = blockPos.east();
						break;
					case SOUTH:
						blockPos = blockPos.south();
						break;
					case WEST:
						blockPos = blockPos.west();
						break;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isFullBlock(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}
}
