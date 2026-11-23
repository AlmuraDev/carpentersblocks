package com.carpentersblocks.client.renderer;

import com.carpentersblocks.CarpentersBlocks;
import com.carpentersblocks.entity.EntityCarpentersTile;
import com.carpentersblocks.util.handler.DesignHandler;
import com.carpentersblocks.util.handler.DesignHandler.DesignType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;

/**
 * Draws a Carpenter's Tile: its design (or blank) texture tinted with its dye, turned by its rotation, on every face
 * of its box (1.12.2: RenderCarpentersTile).
 */
public class CarpentersTileRenderer extends EntityRenderer<EntityCarpentersTile, CarpentersTileRenderer.State> {

    private static final Identifier BLANK = CarpentersBlocks.id("block/tile/blank");

    private final TextureAtlas blockAtlas;

    public CarpentersTileRenderer(EntityRendererProvider.Context context) {
        super(context);
        blockAtlas = context.getAtlas(AtlasIds.BLOCKS);
    }

    public static class State extends EntityRenderState {
        public Direction direction = Direction.UP;
        public int rotation;
        public int color = 0xFFFFFF;
        public String design = "";
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityCarpentersTile entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.direction = entity.getDirection();
        state.rotation = entity.getRotation();
        state.color = entity.getColor();
        state.design = entity.getDesign();
    }

    private TextureAtlasSprite getSprite(String design) {
        if (!design.isEmpty() && DesignHandler.getListForType(DesignType.TILE).contains(design)) {
            TextureAtlasSprite sprite = blockAtlas.getSprite(DesignHandler.getTexture(DesignType.TILE, design));
            if (sprite != blockAtlas.missingSprite()) {
                return sprite;
            }
        }
        return blockAtlas.getSprite(BLANK);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        TextureAtlasSprite sprite = getSprite(state.design);
        int color = 0xFF000000 | state.color;
        int light = state.lightCoords;
        int rotation = state.rotation;
        AABB box = EntityCarpentersTile.getLocalBox(state.direction);
        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(sprite.atlasLocation()), (pose, buffer) -> {
            // Bottom, top, north, south, west, east; four corners each, counter clockwise from outside like 1.12.2
            face(pose, buffer, sprite, rotation, color, light, 0, -1, 0, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1);
            face(pose, buffer, sprite, rotation, color, light, 0, 1, 0, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
            face(pose, buffer, sprite, rotation, color, light, 0, 0, -1, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0);
            face(pose, buffer, sprite, rotation, color, light, 0, 0, 1, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1);
            face(pose, buffer, sprite, rotation, color, light, -1, 0, 0, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1);
            face(pose, buffer, sprite, rotation, color, light, 1, 0, 0, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0);
        });
        super.submit(state, poseStack, collector, camera);
    }

    /**
     * Draws one face with the whole texture, turned by the tile rotation.
     */
    private static void face(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite, int rotation, int color, int light,
                             int nx, int ny, int nz, float... corners) {
        float[][] uv = {
            { sprite.getU0(), sprite.getV0() }, { sprite.getU0(), sprite.getV1() },
            { sprite.getU1(), sprite.getV1() }, { sprite.getU1(), sprite.getV0() } };
        for (int corner = 0; corner < 4; ++corner) {
            float[] texture = uv[(corner + rotation) & 3];
            buffer.addVertex(pose, corners[corner * 3], corners[corner * 3 + 1], corners[corner * 3 + 2])
                .setColor(color)
                .setUv(texture[0], texture[1])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
        }
    }

}
