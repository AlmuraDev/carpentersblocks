package com.carpentersblocks.block;

import com.carpentersblocks.util.registry.BlockRegistry;

import net.minecraft.block.BlockDaylightDetector;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCarpentersDaylightSensor extends BlockDaylightDetector
{
	private final boolean inverted;
	private static final PropertyBool INVERTED = PropertyBool.create("inverted");

	public BlockCarpentersDaylightSensor(boolean inverted)
	{
		super(inverted);
		this.inverted = inverted;
		//this.setDefaultState(this.getDefaultState().withProperty(INVERTED, false));
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
			EntityPlayer player)
	{
		return new ItemStack(BlockRegistry.blockCarpentersDaylightSensor);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (playerIn.isAllowEdit())
		{
			if (worldIn.isRemote)
			{
				return true;
			}
			else
			{
				if (!state.getValue(INVERTED).booleanValue())
				{
					worldIn.setBlockState(pos, BlockRegistry.blockCarpentersDaylightSensor.getDefaultState().withProperty(INVERTED, true));
					Blocks.DAYLIGHT_DETECTOR.updatePower(worldIn, pos);
				}
				else
				{
					worldIn.setBlockState(pos, BlockRegistry.blockCarpentersDaylightSensor.getDefaultState().withProperty(INVERTED, false));
					Blocks.DAYLIGHT_DETECTOR_INVERTED.updatePower(worldIn, pos);
				}

				return true;
			}
		}
		else
		{
			return true;//super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
		}
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
}
