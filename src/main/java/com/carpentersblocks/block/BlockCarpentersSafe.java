package com.carpentersblocks.block;

import com.carpentersblocks.block.state.Property;
import com.carpentersblocks.block.types.BlockCoverable;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.handler.ChatHandler;
import com.carpentersblocks.util.handler.EventHandler;
import com.carpentersblocks.util.registry.BlockRegistry;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockCarpentersSafe extends BlockCoverable
{
	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	public static final PropertyBool LOCKED = PropertyBool.create("locked");

	public BlockCarpentersSafe(Material material)
	{
		super(material);
	}


	//Block Meta/Rotation
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new ExtendedBlockState(this, new IProperty[] {LOCKED, FACING}, Property.unlistedProperties.toArray(new IUnlistedProperty[Property.unlistedProperties.size()]));
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		int i;

		switch ((EnumFacing)state.getValue(FACING))
		{
			case NORTH:
				i = 0;
				break;
			case EAST:
				i = 1;
				break;
			case SOUTH:
				i = 2;
				break;
			case WEST:
			default:
				i = 3;
				break;
		}

		if ((state.getValue(LOCKED)).booleanValue())
		{
			i |= 8;
		}
		return i;
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		EnumFacing enumfacing;

		switch (meta & 7)
		{
			case 0:
				enumfacing = EnumFacing.NORTH;
				break;
			case 1:
				enumfacing = EnumFacing.EAST;
				break;
			case 2:
				enumfacing = EnumFacing.SOUTH;
				break;
			case 3:
			default:
				enumfacing = EnumFacing.WEST;
				break;
		}

		return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(LOCKED, Boolean.valueOf((meta & 8) > 0));
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer)
	{
		facing = placer.getHorizontalFacing();
		return this.getDefaultState().withProperty(LOCKED, true).withProperty(FACING, facing);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState blockState, EntityPlayer entityPlayer,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack itemStack = entityPlayer.getHeldItem(EventHandler.eventHand);
		if(itemStack == null && blockState.getValue(LOCKED).booleanValue() && !world.isRemote && hand.equals(EnumHand.MAIN_HAND))
		{
			ChatHandler.sendMessageToPlayer("message.block_lock.name", entityPlayer);
		}
		return super.onBlockActivated(world, blockPos, blockState, entityPlayer, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer)
	{
		World world = entityPlayer.world;
		BlockPos pos = cbTileEntity.getPos();
		Boolean locked = world.getBlockState(pos).getValue(LOCKED);
		world.setBlockState(pos, this.getDefaultState().withProperty(FACING, world.getBlockState(pos).getValue(FACING)).withProperty(LOCKED, !locked));
		locked = !locked;
		if(!world.isRemote)
			ChatHandler.sendMessageToPlayer(locked == true ? "message.safe_lock.name":"message.safe_unlock.name", entityPlayer);
		return true;
	}
} 