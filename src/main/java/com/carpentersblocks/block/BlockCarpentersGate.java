package com.carpentersblocks.block;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks.EnumType;

public class BlockCarpentersGate extends BlockFenceGate
{
	public BlockCarpentersGate(EnumType type)
	{
		super(type);
		this.setDefaultState(this.blockState.getBaseState().withProperty(OPEN, Boolean.valueOf(false)).withProperty(POWERED, Boolean.valueOf(false)).withProperty(IN_WALL, Boolean.valueOf(false)));
	}
}
