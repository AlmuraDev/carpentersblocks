package com.carpentersblocks.block;

import java.util.List;

import javax.annotation.Nullable;

import com.carpentersblocks.block.data.SlopeData;
import com.carpentersblocks.tileentity.CbTileEntity;
import com.carpentersblocks.util.RotationUtil.Rotation;
import com.carpentersblocks.util.block.SlopeShape;
import com.carpentersblocks.util.handler.EventHandler;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCarpentersSlope extends BlockCoverable {
	
    // Slopes offered as items (creative tab and scroll wheel), like 1.7.10; other shapes come from the hammer
    public final static String slopeType[] = { "wedge", "obliqueInterior", "obliqueExterior", "prism", "prismWedge" };

    private final static SlopeData.Type[] ITEM_SLOPE_TYPES = {
    	SlopeData.Type.WEDGE,
    	SlopeData.Type.OBLIQUE_INTERIOR,
    	SlopeData.Type.OBLIQUE_EXTERIOR,
    	SlopeData.Type.PRISM,
    	SlopeData.Type.PRISM_WEDGE };
    
    public BlockCarpentersSlope(Material material) {
        super(material);
    }

    @Override
    public void getSubBlocks(CreativeTabs creativeTab, NonNullList<ItemStack> items) {
    	for (int damage = 0; damage < slopeType.length; ++damage) {
    		items.add(new ItemStack(this, 1, damage));
    	}
    }

    @Override
    /**
     * Alters block direction.
     */
    protected boolean onHammerLeftClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
    	EnumFacing facing = EventHandler.getEventFace();
    	return facing != null && rotateBlock(cbTileEntity.getWorld(), cbTileEntity.getPos(), facing);
    }

    @Override
    /**
     * Alters block type.
     */
    protected boolean onHammerRightClick(CbTileEntity cbTileEntity, EntityPlayer entityPlayer) {
    	new SlopeData().setNextType(cbTileEntity);
        return true;        
    }

/*    @Override
    *//**
     * Damages hammer with a chance to not damage.
     *//*
    protected void damageItemWithChance(World world, EntityPlayer entityPlayer) {
        if (world.rand.nextFloat() <= ItemRegistry.itemHammerDamageChanceFromSlopes) {
            super.damageItemWithChance(world, entityPlayer);
        }
    }*/

    /**
     * Side covers only go on the slope's full, flat faces, like 1.7.10.
     */
    @Override
    protected boolean canSupportCover(CbTileEntity cbTileEntity, com.carpentersblocks.util.attribute.EnumAttributeLocation location) {
    	if (com.carpentersblocks.util.attribute.EnumAttributeLocation.HOST.equals(location)) {
    		return true;
    	}
    	int cbMetadata = cbTileEntity.getCbMetadata();
    	EnumFacing face = EnumFacing.byIndex(location.ordinal());
    	return SlopeShape.isFaceFull(SlopeData.getType(cbMetadata), Rotation.get(cbMetadata), face);
    }

    /**
     * Gets the stepped collision boxes for the slope, relative to the block position.
     */
    private List<AxisAlignedBB> getSlopeBoxes(CbTileEntity cbTileEntity) {
    	int cbMetadata = cbTileEntity.getCbMetadata();
    	return SlopeShape.getBoxes(SlopeData.getType(cbMetadata), Rotation.get(cbMetadata));
    }

    /**
     * Adds the stepped slope boxes, so entities can walk up slopes like stairs.
     */
    @Override
    public void addCollisionBoxToList(IBlockState blockState, World world, BlockPos blockPos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
    	CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
    	if (cbTileEntity == null) {
    		super.addCollisionBoxToList(blockState, world, blockPos, entityBox, collidingBoxes, entity, isActualState);
    		return;
    	}
    	for (AxisAlignedBB box : getSlopeBoxes(cbTileEntity)) {
    		addCollisionBoxToList(blockPos, entityBox, collidingBoxes, box);
    	}
    }

    /**
     * Only hits the solid part of the slope, but reports the face of the full block,
     * since a sloped face is shared by two block faces.
     */
    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos blockPos, Vec3d start, Vec3d end) {
    	CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
    	if (cbTileEntity != null) {
    		boolean hit = false;
    		for (AxisAlignedBB box : getSlopeBoxes(cbTileEntity)) {
    			if (rayTrace(blockPos, start, end, box) != null) {
    				hit = true;
    				break;
    			}
    		}
    		if (!hit) {
    			return null;
    		}
    	}
    	return rayTrace(blockPos, start, end, FULL_BLOCK_AABB);
    }

/*    @Override
    *//**
     * Checks if the block is a solid face on the given side, used by placement logic.
     *//*
    public boolean isSideSolid(IBlockAccess blockAccess, BlockPos blockPos, EnumFacing side) {
        CbTileEntity cbTileEntity = getTileEntity(blockAccess, blockPos);
        if (cbTileEntity != null) {
            if (isBlockSolid(blockAccess, blockPos)) {
                return Slope.getSlope(cbTileEntity).isFaceFull(side);
            }
        }
        return false;
    }*/

    @Override
    /**
     * Returns whether sides share faces based on sloping property and face shape.
     */
    protected boolean shareFaces(CbTileEntity cbTileEntity_adj, CbTileEntity cbTileEntity_src, EnumFacing side_adj, EnumFacing side_src) {
/*        if (cbTileEntity_adj.getBlockType() == this) {
            Slope slope_src = Slope.getSlope(cbTileEntity_src);
            Slope slope_adj = Slope.getSlope(cbTileEntity_adj);
            if (!slope_adj.hasSide(side_adj)) {
                return false;
            } else if (slope_src.getFaceBias(side_src) == slope_adj.getFaceBias(side_adj)) {
                return true;
            } else {
                return false;
            }
        }
        return super.shareFaces(cbTileEntity_adj, cbTileEntity_src, side_adj, side_src);*/
    	return false;
    }
    
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
    	// IUnlistedProperty values are not retained between this method and onBlockPlacedBy(). Store with thread temporarily.
    	_threadLocalHitCoords.set(new Float[] { hitX, hitY, hitZ });
    	_threadLocalFacing.set(facing);
    	return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
    }
    
    @Override
    public void onBlockPlacedBy(World world, BlockPos blockPos, IBlockState blockState, EntityLivingBase entityLivingBase, ItemStack itemStack) {
    	super.onBlockPlacedBy(world, blockPos, blockState, entityLivingBase, itemStack);
        CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
        if (cbTileEntity != null) {

        	// Set type
        	int damage = itemStack.getItemDamage();
        	SlopeData.setType(cbTileEntity, ITEM_SLOPE_TYPES[damage >= 0 && damage < ITEM_SLOPE_TYPES.length ? damage : 0]);
        	
        	// Set rotation
        	// Placed without getStateForPlacement (machines, fake players): treat as placed on top of a block
        	Float[] hitCoords = _threadLocalHitCoords.get();
        	if (hitCoords == null) {
        		hitCoords = new Float[] { 0.5f, 1.0f, 0.5f };
        	}
        	float x = hitCoords[0];
        	float y = hitCoords[1];
        	float z = hitCoords[2];
        	_threadLocalHitCoords.remove();
        	EnumFacing facing = _threadLocalFacing.get();
        	if (facing == null) {
        		facing = EnumFacing.UP;
        	}
        	_threadLocalFacing.remove();
        	Rotation rotation = Rotation.X0_Y0_Z0;
        	EnumFacing cardinalFacing = entityLivingBase.getHorizontalFacing().getOpposite();
        	switch (facing) {
	        	case DOWN:
	        		if (x >= 0.2f && x <= 0.8f && z >= 0.2f && z <= 0.8f) {
	        			switch (entityLivingBase.getHorizontalFacing()) {
		        			case NORTH:
		        				rotation = Rotation.X0_Y0_Z180;
		        				break;
		        			case SOUTH:
		        				rotation = Rotation.X180_Y0_Z0;
		        				break;
		        			case WEST:
		        				rotation = Rotation.X180_Y90_Z0;
		        				break;
		        			case EAST:
		        				rotation = Rotation.X180_Y270_Z0;
		        				break;
							default:
								break;
	        			}
	        		} else if (1.0f - x > z && x <= 0.5f && x < z) {
	        			rotation = Rotation.X180_Y90_Z0;
        			} else if (z > x && z >= 0.5f) {
        				rotation = Rotation.X180_Y0_Z0;
        			} else if (x > 1.0f - z) {
        				rotation = Rotation.X180_Y270_Z0;
        			} else {
        				rotation = Rotation.X0_Y0_Z180;
        			}
	        		break;
	        	case UP:
	        		if (x >= 0.2f && x <= 0.8f && z >= 0.2f && z <= 0.8f) {
	        			switch (entityLivingBase.getHorizontalFacing()) {
		        			case NORTH:
		        				rotation = Rotation.X0_Y0_Z0;
		        				break;
		        			case SOUTH:
		        				rotation = Rotation.X0_Y180_Z0;
		        				break;
		        			case WEST:
		        				rotation = Rotation.X0_Y270_Z0;
		        				break;
		        			case EAST:
		        				rotation = Rotation.X0_Y90_Z0;
		        				break;
							default:
								break;
	        			}
	        		} else if (x < z && x <= 0.5f && 1.0f - x > z) {
	        			rotation = Rotation.X0_Y270_Z0;
	        		} else if (1.0f - z > x && z <= 0.5f) {
	        			rotation = Rotation.X0_Y0_Z0;
	        		} else if (x > z) {
	        			rotation = Rotation.X0_Y90_Z0;
	        		} else {
	        			rotation = Rotation.X0_Y180_Z0;
	        		}
	        		break;
	        	case NORTH:
	        		if (x >= 0.2f && x <= 0.8f) {
	        			if (y >= 0.5f) {
	        				rotation = Rotation.X90_Y0_Z180;
	        			} else {
	        				rotation = Rotation.X90_Y0_Z0;
	        			}
	        		} else if (x > y && x >= 0.5f && x > 1.0f - y) {
	        			rotation = Rotation.X90_Y0_Z270;
	        		} else if (y > 1.0f - x && y >= 0.5f) {
	        			rotation = Rotation.X90_Y0_Z180;
	        		} else if (x < y) {
	        			rotation = Rotation.X90_Y0_Z90;
	        		} else {
	        			rotation = Rotation.X90_Y0_Z0;
	        		}
	        		break;
	        	case SOUTH:
	        		if (x >= 0.2f && x <= 0.8f) {
	        			if (y >= 0.5f) {
	        				rotation = Rotation.X270_Y0_Z0;
	        			} else {
	        				rotation = Rotation.X90_Y180_Z0;
	        			}
	        		} else if (1.0f - x > y && x <= 0.5f && x < y) {
	        			rotation = Rotation.X90_Y180_Z90;
	        		} else if (y > x && y >= 0.5f) {
	        			rotation = Rotation.X270_Y0_Z0;
	        		} else if (x > 1.0f - y) {
	        			rotation = Rotation.X270_Y0_Z90;
	        		} else {
	        			rotation = Rotation.X90_Y180_Z0;
	        		}
	        		break;
	        	case WEST:
	        		if (z >= 0.2f && z <= 0.8f) {
	        			if (y >= 0.5f) {
	        				rotation = Rotation.X270_Y90_Z0;
	        			} else {
	        				rotation = Rotation.X90_Y270_Z0;
	        			}
	        		} else if (1.0f - z > y && z <= 0.5f && z < y) {
	        			rotation = Rotation.X0_Y0_Z270;
	        		} else if (y > z && y >= 0.5f) {
	        			rotation = Rotation.X270_Y90_Z0;
	        		} else if (z > 1.0f - y) {
	        			rotation = Rotation.X180_Y0_Z90;
	        		} else {
	        			rotation = Rotation.X90_Y270_Z0;
	        		}
	        		break;
	        	case EAST:
	        		if (z >= 0.2f && z <= 0.8f) {
	        			if (y >= 0.5f) {
	        				rotation = Rotation.X0_Y270_Z90;
	        			} else {
	        				rotation = Rotation.X90_Y90_Z0;
	        			}
	        		} else if (z > y && z >= 0.5f && z > 1.0f - y) {
	        			rotation = Rotation.X0_Y180_Z90;
	        		} else if (y > 1.0f - z && y >= 0.5f) {
	        			rotation = Rotation.X0_Y270_Z90;
	        		} else if (z < y) {
	        			rotation = Rotation.X0_Y0_Z90;
	        		} else {
	        			rotation = Rotation.X90_Y90_Z0;
	        		}
	        		break;
        	}
        	SlopeData.setRotation(cbTileEntity, rotation);
        }
    }

    @Override
    public EnumFacing[] getValidRotations(World world, BlockPos blockPos) {
        return EnumFacing.VALUES;
    }

    @Override
    public boolean rotateBlock(World world, BlockPos blockPos, EnumFacing facing) {
        CbTileEntity cbTileEntity = getTileEntity(world, blockPos);
        if (cbTileEntity != null) {
        	SlopeData.rotate(cbTileEntity, facing.getAxis());
        	return true;
        }
        return false;
    }
    
    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
	@Override
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }

	@Override
    public boolean isFullCube(IBlockState blockState) {
        return false;
    }
    
    /**
     * Check if the face of a block should block rendering.
     *
     * Faces which are fully opaque should return true, faces with transparency
     * or faces which do not span the full size of the block should return false.
     *
     * @param state The current block state
     * @param world The current world
     * @param pos Block position in world
     * @param face The side to check
     * @return True if the block is opaque on the specified side.
     */
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

}
