package com.carpentersblocks.proxy;

import com.carpentersblocks.CarpentersBlocksCachedResources;
import com.carpentersblocks.entity.EntityCarpentersTile;
import com.carpentersblocks.renderer.CoverColorHandler;
import com.carpentersblocks.renderer.ModelLoader;
import com.carpentersblocks.renderer.entity.RenderCarpentersTile;
import com.carpentersblocks.util.ModLogger;
import com.carpentersblocks.util.registry.ConfigRegistry;
import com.carpentersblocks.util.registry.SpriteRegistry;

import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
    	super.preInit(event);
    	MinecraftForge.EVENT_BUS.register(new SpriteRegistry());
        ModelLoaderRegistry.registerLoader(new ModelLoader());
        RenderingRegistry.registerEntityRenderingHandler(EntityCarpentersTile.class, RenderCarpentersTile::new);
    }
    
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        CarpentersBlocksCachedResources.INSTANCE.init();
        //BlockRegistry.registerRenderers();
            // Biome tints of covers and potted plants, applied by the renderer
        CoverColorHandler coverColorHandler = new CoverColorHandler();
        for (net.minecraft.block.Block block : net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS) {
            if (block instanceof com.carpentersblocks.block.BlockCoverable) {
                net.minecraft.client.Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(coverColorHandler, block);
            }
        }

        //ShadersHandler.init();

        // Register entity renderers
        //RenderingRegistry.registerEntityRenderingHandler(EntityCarpentersTile.class, new RenderCarpentersTile());
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
    	if (FMLClientHandler.instance().hasOptifine()) {
    		ModLogger.info("Optifine detected. Disabling custom vertexformat.");
    		ConfigRegistry.enableOptifineCompatibility = true;
    	}
    }
	
}
