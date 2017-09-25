package com.carpentersblocks.renderer;

import com.carpentersblocks.Reference;
import com.carpentersblocks.renderer.model.ModelBlock;
import com.carpentersblocks.renderer.model.ModelCollapsible;
import com.carpentersblocks.renderer.model.ModelSafe;
import com.carpentersblocks.renderer.model.ModelSlope;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import static com.carpentersblocks.Reference.*;
public class ModelLoader implements ICustomModelLoader
{ 
    public static final ModelResourceLocation RESOURCE_BLOCK = new ModelResourceLocation( MOD_ID + ":block/carpenters_block");
    public static final ModelResourceLocation RESOURCE_COLLAPSIBLE_BLOCK = new ModelResourceLocation( MOD_ID + ":block/carpenters_collapsible_block");
    public static final ModelResourceLocation RESOURCE_SLOPE = new ModelResourceLocation( MOD_ID + ":block/carpenters_slope"); 
    
    public static final ModelBlock BLOCK_MODEL = new ModelBlock();
    public static final ModelCollapsible COLLAPSIBLE_MODEL = new ModelCollapsible();
    public static final ModelSafe SAFE_MODEL = new ModelSafe();
    public static final IModel SLOPE_MODEL = new ModelSlope();
    
    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
    	String path = modelLocation.getResourcePath();
    	
    	if(modelLocation.getResourceDomain().equals(MOD_ID))
    			if(path.equals(REGISTRY_NAME_BLOCK) ||
    					 path.equals(REGISTRY_NAME_COLLAPSIBLE) ||
    					 path.equals(REGISTRY_NAME_SLOPE))   
    				return true;
		return false;
    }

    @Override
    public IModel loadModel(ResourceLocation resourceLocation) throws Exception
    {	
    	if (isModel(resourceLocation, Reference.REGISTRY_NAME_COLLAPSIBLE)) 
    	{
    		return COLLAPSIBLE_MODEL;
    	}
        else if (isModel(resourceLocation, Reference.REGISTRY_NAME_SAFE))
        	{
        		return SAFE_MODEL;
        	}
    	else if (isModel(resourceLocation, Reference.REGISTRY_NAME_SLOPE))
    	{
    		return SLOPE_MODEL;
    	} 
        return BLOCK_MODEL;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager){}
    
    private boolean isModel(ResourceLocation resourceLocation, String registryName) 
    {
    	return resourceLocation.getResourceDomain().equals(MOD_ID) && registryName.equals(resourceLocation.getResourcePath());
    } 
}