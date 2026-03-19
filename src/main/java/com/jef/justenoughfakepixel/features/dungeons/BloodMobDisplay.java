package com.jef.justenoughfakepixel.features.dungeons;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.ScoreboardUtils;
import com.jef.justenoughfakepixel.events.RenderEntityModelEvent;
import com.jef.justenoughfakepixel.utils.EntityHighlightUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@RegisterEvents
public class BloodMobDisplay {

    // Matches armor stand name only if it contains a blood room mob name
    private static final Pattern MOB_NAME = Pattern.compile(
            ".*(?:Putrid|Reaper|Vader|Frost|Cannibal|Revoker|Tear|Mr\\.? Dead|Skull|Walker|Psycho|Ooze|Freak|Flamer|Mute|Leech|Parasite).*"
    );
    private static final Pattern DYING1 = Pattern.compile(
            "^§.\\[§.Lv\\d+§.\\] §.+ (?:§.)+0§f/.+§c❤$"
    );
    private static final Pattern DYING2 = Pattern.compile(
            "^.+ (?:§.)+0§c❤$"
    );

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Set<EntityLivingBase> bloodMobs = new HashSet<>();
    private long lastScan = 0;

    // scan

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (JefConfig.feature == null || JefConfig.feature.dungeons.bloodMobHighlight == 2) return;
        if (!ScoreboardUtils.isInDungeon()) { bloodMobs.clear(); return; }
        long now = System.currentTimeMillis();
        if (now - lastScan < 100) return;
        lastScan = now;

        bloodMobs.clear();
        if (mc.theWorld == null) return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityArmorStand)) continue;
            String name = entity.getName();
            if (name == null) continue;
            if (!MOB_NAME.matcher(name).matches()) continue;

            EntityLivingBase mob = mc.theWorld.getEntitiesWithinAABB(
                    EntityLivingBase.class,
                    entity.getEntityBoundingBox().expand(0.5, 3.0, 0.5),
                    e -> e != null && !(e instanceof EntityArmorStand) && e != mc.thePlayer
            ).stream().findFirst().orElse(null);

            if (mob != null && !isDying(mob))
                bloodMobs.add(mob);
        }
    }

    // outline mode

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderEntityModel(RenderEntityModelEvent event) {
        if (JefConfig.feature == null || JefConfig.feature.dungeons.bloodMobHighlight != 1) return;
        EntityLivingBase entity = event.getEntity();
        if (!bloodMobs.contains(entity) || isDying(entity) || entity.isInvisible()) return;
        EntityHighlightUtils.renderEntityOutline(event, new Color(255, 0, 0, 200));
    }

    //box mode

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (JefConfig.feature == null || JefConfig.feature.dungeons.bloodMobHighlight != 0) return;
        if (bloodMobs.isEmpty() || mc.thePlayer == null) return;

        Color c = new Color(255, 0, 0, 200);
        float r = c.getRed() / 255f, g = c.getGreen() / 255f,
                b = c.getBlue() / 255f, a = c.getAlpha() / 255f;

        double vx = mc.getRenderManager().viewerPosX;
        double vy = mc.getRenderManager().viewerPosY;
        double vz = mc.getRenderManager().viewerPosZ;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0f);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glPushMatrix();
        GL11.glTranslated(-vx, -vy, -vz);

        for (EntityLivingBase mob : bloodMobs) {
            if (mob.isDead || mob.getHealth() <= 0) continue;
            drawBox(mob.getEntityBoundingBox().expand(0.1, 0.1, 0.1), r, g, b, a);
        }

        GL11.glPopMatrix();
        GL11.glPopAttrib();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    // helpers

    private boolean isDying(EntityLivingBase entity) {
        if (entity == null || entity.isDead || entity.getHealth() <= 0.1f) return true;
        IChatComponent name = entity.getDisplayName();
        if (name == null) return false;
        String text = name.getUnformattedText();
        return DYING1.matcher(text).matches() || DYING2.matcher(text).matches();
    }

    private void drawBox(AxisAlignedBB bb, float r, float g, float b, float a) {
        double[][] edges = {
                {bb.minX,bb.minY,bb.minZ, bb.maxX,bb.minY,bb.minZ},
                {bb.minX,bb.minY,bb.maxZ, bb.maxX,bb.minY,bb.maxZ},
                {bb.minX,bb.minY,bb.minZ, bb.minX,bb.minY,bb.maxZ},
                {bb.maxX,bb.minY,bb.minZ, bb.maxX,bb.minY,bb.maxZ},
                {bb.minX,bb.maxY,bb.minZ, bb.maxX,bb.maxY,bb.minZ},
                {bb.minX,bb.maxY,bb.maxZ, bb.maxX,bb.maxY,bb.maxZ},
                {bb.minX,bb.maxY,bb.minZ, bb.minX,bb.maxY,bb.maxZ},
                {bb.maxX,bb.maxY,bb.minZ, bb.maxX,bb.maxY,bb.maxZ},
                {bb.minX,bb.minY,bb.minZ, bb.minX,bb.maxY,bb.minZ},
                {bb.maxX,bb.minY,bb.minZ, bb.maxX,bb.maxY,bb.minZ},
                {bb.minX,bb.minY,bb.maxZ, bb.minX,bb.maxY,bb.maxZ},
                {bb.maxX,bb.minY,bb.maxZ, bb.maxX,bb.maxY,bb.maxZ},
        };
        int ri = (int)(r*255), gi = (int)(g*255), bi = (int)(b*255), ai = (int)(a*255);
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (double[] e : edges) {
            wr.pos(e[0],e[1],e[2]).color(ri,gi,bi,ai).endVertex();
            wr.pos(e[3],e[4],e[5]).color(ri,gi,bi,ai).endVertex();
        }
        tess.draw();
    }

    private Color parseColor(String raw) {
        try {
            String[] p = raw.split(":");
            return new Color(Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                    Integer.parseInt(p[3]), Math.max(Integer.parseInt(p[0]), 153));
        } catch (Exception e) {
            return new Color(255, 0, 0, 200);
        }
    }
}