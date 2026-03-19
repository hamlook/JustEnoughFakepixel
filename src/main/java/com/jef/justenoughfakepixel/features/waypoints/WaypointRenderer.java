package com.jef.justenoughfakepixel.features.waypoints;

import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;

@RegisterEvents
public class WaypointRenderer {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final float[] FALLBACK_BOX    = {1.00f, 1.00f, 0.00f, 0.85f};
    private static final float[] FALLBACK_TRACER = {1.00f, 1.00f, 0.00f, 1.00f};

    private static float[] resolveColour(String special, float[] fallback) {
        try {
            int argb = ChromaColour.specialToChromaRGB(special);
            return new float[]{
                    ((argb >> 16) & 0xFF) / 255f,
                    ((argb >> 8) & 0xFF) / 255f,
                    (argb & 0xFF) / 255f,
                    ((argb >> 24) & 0xFF) / 255f
            };
        } catch (Exception e) {
            return fallback;
        }
    }

    private float[] boxColour() {
        if (JefConfig.feature == null) return FALLBACK_BOX;
        return resolveColour(JefConfig.feature.waypoints.boxColour, FALLBACK_BOX);
    }

    private float[] tracerColour() {
        if (JefConfig.feature == null) return FALLBACK_TRACER;
        return resolveColour(JefConfig.feature.waypoints.tracerColour, FALLBACK_TRACER);
    }

    private int labelColour() {
        if (JefConfig.feature == null) return 0xFFFFFFFF;
        try {
            return ChromaColour.specialToChromaRGB(JefConfig.feature.waypoints.labelColour);
        } catch (Exception e) {
            return 0xFFFFFFFF;
        }
    }

    private int distanceLabelColour() {
        if (JefConfig.feature == null) return 0xFF55FFFF;
        try {
            return ChromaColour.specialToChromaRGB(JefConfig.feature.waypoints.distanceLabelColour);
        } catch (Exception e) {
            return 0xFF55FFFF;
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        WaypointState state = WaypointState.getInstance();
        if (!state.enabled || !state.hasGroup()) return;

        if (JefConfig.feature != null) {
            state.advanceRange = JefConfig.feature.waypoints.advanceRange;
            state.advanceDelayMs = (long) JefConfig.feature.waypoints.advanceDelayMs;
        }

        tickAdvance(state);

        double vx = mc.getRenderManager().viewerPosX;
        double vy = mc.getRenderManager().viewerPosY;
        double vz = mc.getRenderManager().viewerPosZ;

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0f);
        GlStateManager.disableCull();

        GL11.glPushMatrix();
        GL11.glTranslated(-vx, -vy, -vz);

        if (state.setupMode) {
            drawSetupBoxes(state, event);
        } else {
            drawNormalBoxes(state, event);
        }

        GL11.glPopMatrix();

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();

        GL11.glPushMatrix();
        GL11.glTranslated(-vx, -vy, -vz);

        if (state.setupMode) {
            drawSetupLabels(state);
        } else {
            drawNormalLabels(state);
        }

        GL11.glPopMatrix();

        GL11.glDepthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GL11.glLineWidth(1.0f);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private void tickAdvance(WaypointState state) {
        if (mc.thePlayer == null) return;
        WaypointPoint next = state.getNext();
        if (next == null) return;

        double dist = next.distanceTo(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

        if (dist <= state.advanceRange) {
            if (state.advanceTimerStart < 0) {
                state.advanceTimerStart = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - state.advanceTimerStart >= state.advanceDelayMs) {
                state.advance();
            }
        } else {
            state.advanceTimerStart = -1L;
        }
    }

    private void drawSetupBoxes(WaypointState state, RenderWorldLastEvent event) {
        WaypointPoint nxt = state.getNext();
        if (nxt == null) return;

        float[] col = boxColour();
        drawEspBox(nxt.x, nxt.y, nxt.z, col[0], col[1], col[2], col[3]);
        drawTracer(event, nxt.x + 0.5, nxt.y + 0.5, nxt.z + 0.5);
    }

    private void drawNormalBoxes(WaypointState state, RenderWorldLastEvent event) {
        WaypointPoint nxt = state.getNext();
        if (nxt == null) return;

        float[] col = boxColour();
        drawEspBox(nxt.x, nxt.y, nxt.z, col[0], col[1], col[2], col[3]);
        drawTracer(event, nxt.x + 0.5, nxt.y + 0.5, nxt.z + 0.5);
    }

    private void drawSetupLabels(WaypointState state) {
        List<WaypointPoint> wps = state.loadedGroup.waypoints;
        int cur = state.currentIndex;
        int nxt = state.getNextIndex();

        for (int i = 0; i < wps.size(); i++) {
            WaypointPoint wp = wps.get(i);
            String col = (i == cur) ? "\u00a7a" : (i == nxt) ? "\u00a7e" : "\u00a77";
            String label = (wp.name != null && !wp.name.isEmpty()) ? wp.name : String.valueOf(i + 1);
            drawLabel(wp.x + 0.5, wp.y + 2.2, wp.z + 0.5, col + label);
        }
    }

    private void drawNormalLabels(WaypointState state) {
        WaypointPoint prev = state.getPrev();
        WaypointPoint cur  = state.getCurrent();
        WaypointPoint nxt  = state.getNext();

        if (prev != null && prev != cur)
            drawLabel(prev.x + 0.5, prev.y + 2.2, prev.z + 0.5, "\u00a77" + safeName(prev, "Prev"));
        if (cur != null)
            drawLabel(cur.x + 0.5, cur.y + 2.2, cur.z + 0.5, "\u00a7a" + safeName(cur, "Current"));
        if (nxt != null && nxt != cur)
            drawLabel(nxt.x + 0.5, nxt.y + 2.2, nxt.z + 0.5, "\u00a7e" + safeName(nxt, "Next"));
    }

    private String safeName(WaypointPoint wp, String fallback) {
        return (wp.name != null && !wp.name.isEmpty()) ? wp.name : fallback;
    }

    private void drawEspBox(double x, double y, double z, float r, float g, float b, float a) {
        final double[][] edges = {
                {0,0,0,1,0,0},{0,0,1,1,0,1},{0,0,0,0,0,1},{1,0,0,1,0,1},
                {0,1,0,1,1,0},{0,1,1,1,1,1},{0,1,0,0,1,1},{1,1,0,1,1,1},
                {0,0,0,0,1,0},{1,0,0,1,1,0},{0,0,1,0,1,1},{1,0,1,1,1,1}
        };

        int ri = (int)(r * 255), gi = (int)(g * 255), bi = (int)(b * 255), ai = (int)(a * 255);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (double[] e : edges) {
            wr.pos(x + e[0], y + e[1], z + e[2]).color(ri, gi, bi, ai).endVertex();
            wr.pos(x + e[3], y + e[4], z + e[5]).color(ri, gi, bi, ai).endVertex();
        }
        tess.draw();
    }

    private void drawTracer(RenderWorldLastEvent event, double tx, double ty, double tz) {
        if (mc.thePlayer == null) return;

        float[] col = tracerColour();
        int ri = (int)(col[0] * 255), gi = (int)(col[1] * 255),
                bi = (int)(col[2] * 255), ai = (int)(col[3] * 255);

        Vec3 eyes = mc.thePlayer.getPositionEyes(event.partialTicks);

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0f);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(eyes.xCoord, eyes.yCoord, eyes.zCoord).color(ri, gi, bi, ai).endVertex();
        wr.pos(tx, ty, tz).color(ri, gi, bi, ai).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GL11.glDepthMask(true);
        GL11.glLineWidth(1.0f);
    }

    private void drawLabel(double wx, double wy, double wz, String text) {
        if (mc.thePlayer == null || mc.fontRendererObj == null) return;

        double dx = wx - mc.thePlayer.posX;
        double dy = wy - mc.thePlayer.posY;
        double dz = wz - mc.thePlayer.posZ;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

        double renderDist = Math.min(dist, 50.0);
        float scale = Math.max(0.025f, (float)(renderDist / 300.0));

        String nameStr = net.minecraft.util.StringUtils.stripControlCodes(text);
        String distStr = (int)Math.round(dist) + "m";
        String separator = " ";

        int nameW = mc.fontRendererObj.getStringWidth(nameStr);
        int sepW  = mc.fontRendererObj.getStringWidth(separator);
        int distW = mc.fontRendererObj.getStringWidth(distStr);
        int totalW = nameW + sepW + distW;

        GL11.glPushMatrix();
        GL11.glTranslated(wx, wy, wz);
        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0f, 1f, 0f);
        GL11.glRotatef(mc.getRenderManager().playerViewX, 1f, 0f, 0f);
        GL11.glScalef(-scale, -scale, scale);

        GlStateManager.color(1f, 1f, 1f, 1f);
        float startX = -totalW / 2f;
        mc.fontRendererObj.drawStringWithShadow(nameStr, startX, 0f, labelColour());
        mc.fontRendererObj.drawStringWithShadow(distStr, startX + nameW + sepW, 0f, distanceLabelColour());

        GL11.glPopMatrix();
    }
}