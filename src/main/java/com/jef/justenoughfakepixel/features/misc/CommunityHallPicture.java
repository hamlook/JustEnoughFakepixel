package com.jef.justenoughfakepixel.features.misc;

import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.data.SkyblockData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@RegisterEvents
public class CommunityHallPicture {

    private static final double WORLD_X = 6;
    private static final double WORLD_Y = 72;
    private static final double WORLD_Z = -92.97;

    // Single-string format: "justenoughfakepixel:logo.png"
    // maps to assets/justenoughfakepixel/textures/logo.png
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("justenoughfakepixel:logo.png");

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (SkyblockData.getCurrentLocation() != SkyblockData.Location.HUB) return;
        if (mc.thePlayer == null || mc.getRenderManager() == null) return;

        double vx = mc.getRenderManager().viewerPosX;
        double vy = mc.getRenderManager().viewerPosY;
        double vz = mc.getRenderManager().viewerPosZ;

        double rx = (WORLD_X + 1) - vx;
        double ry = WORLD_Y - vy;
        double rz = (WORLD_Z + 1) - vz;

        GlStateManager.pushMatrix();
        GlStateManager.translate(rx, ry, rz);
        GlStateManager.rotate(180f, 0f, 1f, 0f);
        GlStateManager.translate(1f, 1f, 0f);
        GlStateManager.rotate(180f, 0f, 0f, 1f);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableCull();

        mc.getTextureManager().bindTexture(TEXTURE);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(0.0, 1.0, 0.01).tex(0.0, 1.0).endVertex();
        wr.pos(1.0, 1.0, 0.01).tex(1.0, 1.0).endVertex();
        wr.pos(1.0, 0.0, 0.01).tex(1.0, 0.0).endVertex();
        wr.pos(0.0, 0.0, 0.01).tex(0.0, 0.0).endVertex();
        tess.draw();

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}