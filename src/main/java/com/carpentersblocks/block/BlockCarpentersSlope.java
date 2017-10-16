package com.carpentersblocks.block;


import java.util.List;

import com.carpentersblocks.block.types.BlockCoverable;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;

public class BlockCarpentersSlope extends BlockCoverable 
{ 
    public final static String slopeType[] = { "wedge", "obliqueInterior", "obliqueExterior", "prism", "prismWedge" };
    public final static String baseUnlocalName = "tile.blockCarpentersSlope.";

    public static final PropertyEnum<BlockCarpentersSlope.EnumType> VARIANT = PropertyEnum.create("variant", BlockCarpentersSlope.EnumType.class);
    
    
    public BlockCarpentersSlope(Material material)
    {
        super(material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockCarpentersSlope.EnumType.WEDGE));
    }

    @Override
    public String getUnlocalizedName() 
    {
    	return   baseUnlocalName+"wedge";
    }
    
    @Override
    public int getMetaFromState(IBlockState state) 
    {
    	return state.getValue(VARIANT).META;
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) 
    {
    	return this.getDefaultState().withProperty(VARIANT, BlockCarpentersSlope.EnumType.byMetadata(meta));
    }
    
    @Override
    protected BlockStateContainer createBlockState() 
    {
    	return new BlockStateContainer(this, new IProperty[] {VARIANT});
    }
    
    public static enum EnumType implements IStringSerializable
    {
    	WEDGE(0, "wedge"),
    	INTERIOR(1, "oblique_interior", "obliqueInterior"),
    	EXTERIOR(2, "oblique_exterior", "obliqueExterior"),
    	PRISM(3, "prism"),
    	PRISM_WEDGE(4, "prism_wedge", "prismWedge");
    	
    	private static final BlockCarpentersSlope.EnumType[] META_LOOKUP = new BlockCarpentersSlope.EnumType[values().length];
    	private final int META;
    	private final String NAME, ID;
    	
    	private EnumType(int meta, String id)
    	{
    		META = meta;
    		NAME = id;
    		ID = id;
    	}
    	
    	private EnumType(int meta, String name, String id)
    	{
    		META = meta;
    		NAME = name;
    		ID = id;
    	}
 
		public String getName() 
		{
			return NAME;
		}
		
		public String getUnlocalName()
		{
			return ID;
		}
		
		public int getMeta()
		{
			return META;
		}
		
		public static BlockCarpentersSlope.EnumType byMetadata(int meta)
        {
            if (meta < 0 || meta >= META_LOOKUP.length)
            {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

		 static
	        {
	            for (BlockCarpentersSlope.EnumType blockcarpentersslope$enumtype : values())
	            {
	                META_LOOKUP[blockcarpentersslope$enumtype.getMeta()] = blockcarpentersslope$enumtype;
	            }
	        }
    	
    }
}