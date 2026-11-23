package com.carpentersblocks.renderer;

import java.util.function.Predicate;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.block.IShapedBlock;
import com.carpentersblocks.renderer.model.ModelBlock;
import com.carpentersblocks.renderer.model.ModelCollapsibleBlock;
import com.carpentersblocks.renderer.model.ModelPressurePlate;
import com.carpentersblocks.renderer.model.ModelShapedBlock;
import com.carpentersblocks.renderer.model.ModelSlope;
import com.carpentersblocks.util.registry.BlockRegistry;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ModelLoader implements ICustomModelLoader {
	
	@Override
	public boolean accepts(ResourceLocation resourceLocation) {
    	IModel model = null;
    	try { model = loadModel(resourceLocation); } catch (Exception ex) {}
    	return CarpentersBlocks.MOD_ID.equalsIgnoreCase(resourceLocation.getNamespace())
    			&& model != null;
	}

	@Override
	public IModel loadModel(ResourceLocation resourceLocation) throws Exception {
    	if (resourceLocation.getPath().equals(BlockRegistry.REGISTRY_NAME_BLOCK)) {
    		return new ModelBlock();
    	} else if (resourceLocation.getPath().endsWith(BlockRegistry.REGISTRY_NAME_SLOPE)) {
    		return new ModelSlope(((ModelResourceLocation)resourceLocation).getVariant());
    	} else if (resourceLocation.getPath().equals(BlockRegistry.REGISTRY_NAME_COLLAPSIBLE_BLOCK)) {
    		return new ModelCollapsibleBlock();
    	} else if (resourceLocation.getPath().equals(BlockRegistry.REGISTRY_NAME_PRESSURE_PLATE)) {
    		return new ModelPressurePlate();
    	}
    	// Blocks drawn from their boxes (fences, doors, torches...)
    	Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath()));
    	if (block instanceof IShapedBlock) {
    		return new ModelShapedBlock(block);
    	}
    	return null;
	}
	
	/**
	 * Won't build without this override
	 */
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) { }

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) { }
    
}