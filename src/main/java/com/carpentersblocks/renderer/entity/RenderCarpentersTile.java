package com.carpentersblocks.renderer.entity;

import org.lwjgl.opengl.GL11;

import com.carpentersblocks.entity.EntityCarpentersTile;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.DyeHandler;
import com.carpentersblocks.util.registry.SpriteRegistry;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Draws a Carpenter's Tile: its design (or blank) texture tinted with its dye.
 */
@SideOnly(Side.CLIENT)
public class RenderCarpentersTile extends Render<EntityCarpentersTile> {

	public RenderCarpentersTile(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCarpentersTile entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

	private static TextureAtlasSprite getSprite(EntityCarpentersTile entity) {
		int idx = DesignHandler.listTile.indexOf(entity.getDesign());
		if (idx >= 0 && idx < SpriteRegistry.sprite_design_tile.size()) {
			return SpriteRegistry.sprite_design_tile.get(idx);
		}
		return SpriteRegistry.sprite_tile_blank;
	}

	@Override
	public void doRender(EntityCarpentersTile entity, double x, double y, double z, float entityYaw, float partialTicks) {
		bindEntityTexture(entity);
		TextureAtlasSprite sprite = getSprite(entity);
		int color = DyeHandler.getColor(entity.getDye());
		float r = ((color >> 16) & 0xFF) / 255.0F;
		float g = ((color >> 8) & 0xFF) / 255.0F;
		float b = (color & 0xFF) / 255.0F;
		AxisAlignedBB box = EntityCarpentersTile.getLocalBox(entity.getDirection());
		int rotation = entity.getRotation();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		double x0 = box.minX, y0 = box.minY, z0 = box.minZ, x1 = box.maxX, y1 = box.maxY, z1 = box.maxZ;
		// Bottom, top, north, south, west, east; each face as four corners counter clockwise from outside
		face(buffer, sprite, rotation, r, g, b, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1);
		face(buffer, sprite, rotation, r, g, b, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
		face(buffer, sprite, rotation, r, g, b, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0);
		face(buffer, sprite, rotation, r, g, b, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1);
		face(buffer, sprite, rotation, r, g, b, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1);
		face(buffer, sprite, rotation, r, g, b, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0);
		tessellator.draw();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	/**
	 * Draws one face with the whole texture, turned by the tile rotation.
	 */
	private static void face(BufferBuilder buffer, TextureAtlasSprite sprite, int rotation, float r, float g, float b, double... corners) {
		double[][] uv = { { sprite.getMinU(), sprite.getMinV() }, { sprite.getMinU(), sprite.getMaxV() }, { sprite.getMaxU(), sprite.getMaxV() }, { sprite.getMaxU(), sprite.getMinV() } };
		for (int corner = 0; corner < 4; ++corner) {
			double[] texture = uv[(corner + rotation) & 3];
			buffer.pos(corners[corner * 3], corners[corner * 3 + 1], corners[corner * 3 + 2]).tex(texture[0], texture[1]).color(r, g, b, 1.0F).endVertex();
		}
	}

}
