package com.jef.justenoughfakepixel.features.diana;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;


public class DianaOverlay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final int LINE_HEIGHT = 10;
    private static final int PADDING     = 3;
    private static final int BASE_WIDTH  = 160;

    // Dimensions (read by GuiPositionEditor)

    private static int lastW = BASE_WIDTH;
    private static int lastH = LINE_HEIGHT * 10 + PADDING * 2;

    public static int getOverlayWidth()  { return lastW; }
    public static int getOverlayHeight() { return lastH; }

    // Render

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (JefConfig.feature == null || !JefConfig.feature.diana.enabled
                || !JefConfig.feature.diana.showOverlay) return;
        renderOverlay(false);
    }

    public static void renderOverlay(boolean preview) {
        if (JefConfig.feature == null) return;

        List<String> lines = buildLines(preview);
        if (lines.isEmpty()) return;

        float scale = JefConfig.feature.diana.overlayScale;

        // Measure widest line
        int w = BASE_WIDTH;
        for (String line : lines)
            w = Math.max(w, mc.fontRendererObj.getStringWidth(line) + PADDING * 2);
        int h = lines.size() * LINE_HEIGHT + PADDING * 2;
        lastW = w;
        lastH = h;

        ScaledResolution sr  = new ScaledResolution(mc);
        Position         pos = JefConfig.feature.diana.overlayPos;
        int x = pos.getAbsX(sr, (int)(w * scale));
        int y = pos.getAbsY(sr, (int)(h * scale));
        if (pos.isCenterX()) x -= (int)(w * scale / 2);
        if (pos.isCenterY()) y -= (int)(h * scale / 2);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(scale, scale, 1f);

        if (JefConfig.feature.diana.overlayBackground)
            Gui.drawRect(-PADDING, -PADDING, w, h - PADDING, 0x88000000);

        int dy = 0;
        for (String line : lines) {
            mc.fontRendererObj.drawStringWithShadow(line, 0, dy, 0xFFFFFF);
            dy += LINE_HEIGHT;
        }

        GL11.glPopMatrix();
    }

    // build lines

    static List<String> buildLines(boolean preview) {
        List<String> lines = new ArrayList<>();

        if (preview) {
            lines.add("\u00a76\u00a7lDiana Tracker");
            lines.add("\u00a7eBurrows: \u00a7f42  \u00a77(\u00a7a120.0\u00a77/hr)");
            lines.add("\u00a7dMobs since Inq: \u00a7f7");
            lines.add("\u00a7dInqs since Chimera: \u00a7f2  \u00a77(\u00a7d1.00%\u00a77/mob)");
            lines.add("\u00a76Minotaurs since Stick: \u00a7f15");
            lines.add("\u00a75Champs since Relic: \u00a7f30");
            lines.add("\u00a71Griffin Feathers: \u00a7f3");
            lines.add("\u00a76Souvenirs: \u00a7f1  \u00a76Crowns: \u00a7f0");
            lines.add("\u00a76Coins: \u00a7f1.50M");
            lines.add("\u00a7aShelmets: \u00a7f2  \u00a7aRemedies: \u00a7f1  \u00a7aPlushies: \u00a7f0");
            return lines;
        }

        DianaStats stats = DianaStats.getInstance();
        if (!stats.isTracking()) return lines;   // empty → nothing drawn

        DianaData d      = stats.getData();
        double bph       = stats.getBph();
        double inqChance = stats.getInqChance();   // -1 means no inq yet

        lines.add("\u00a76\u00a7lDiana Tracker");

        lines.add(String.format("\u00a7eBurrows: \u00a7f%d  \u00a77(\u00a7a%.1f\u00a77/hr)",
                d.totalBurrows, bph));

        lines.add(String.format("\u00a7dMobs since Inq: \u00a7f%d",
                d.mobsSinceInq));

        if (inqChance >= 0) {
            lines.add(String.format("\u00a7dInqs since Chimera: \u00a7f%d  \u00a77(\u00a7d%.2f%%\u00a77/mob)",
                    d.inqsSinceChimera, inqChance));
        } else {
            lines.add(String.format("\u00a7dInqs since Chimera: \u00a7f%d",
                    d.inqsSinceChimera));
        }

        lines.add(String.format("\u00a76Minotaurs since Stick: \u00a7f%d",
                d.minotaursSinceStick));

        lines.add(String.format("\u00a75Champs since Relic: \u00a7f%d",
                d.champsSinceRelic));

        lines.add(String.format("\u00a71Griffin Feathers: \u00a7f%d",
                d.griffinFeathers));

        lines.add(String.format("\u00a76Souvenirs: \u00a7f%d  \u00a76Crowns: \u00a7f%d",
                d.souvenirs, d.crownsOfGreed));
        lines.add(String.format("\u00a76Coins: \u00a7f%s",
                formatCoins(d.totalCoins)));

        lines.add(String.format("\u00a7aShelmets: \u00a7f%d  \u00a7aRemedies: \u00a7f%d  \u00a7aPlushies: \u00a7f%d",
                d.dwarfTurtleShelmets, d.antiqueRemedies, d.crochetTigerPlushies));

        return lines;
    }


    private static String formatCoins(long coins) {
        if (coins >= 1_000_000) return String.format("%.2fM", coins / 1_000_000.0);
        if (coins >= 1_000)     return String.format("%.1fk", coins / 1_000.0);
        return String.valueOf(coins);
    }
}