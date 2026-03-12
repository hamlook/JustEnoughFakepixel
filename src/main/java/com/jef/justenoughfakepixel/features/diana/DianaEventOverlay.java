package com.jef.justenoughfakepixel.features.diana;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.utils.OverlayUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;


public class DianaEventOverlay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final int LINE_HEIGHT = 10;
    private static final int PADDING     = 3;
    private static final int BASE_WIDTH  = 180;

    private static int lastW = BASE_WIDTH;
    private static int lastH = LINE_HEIGHT * 7 + PADDING * 2;

    public static int getOverlayWidth()  { return lastW; }
    public static int getOverlayHeight() { return lastH; }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (JefConfig.feature == null || !JefConfig.feature.diana.enabled
                || !JefConfig.feature.diana.showEventOverlay) return;
        renderOverlay(false);
    }

    public static void renderOverlay(boolean preview) {
        if (JefConfig.feature == null) return;
        if (!preview && OverlayUtils.shouldHide()) return;
        List<String> lines = buildLines(preview);
        if (lines.isEmpty()) return;

        float scale = JefConfig.feature.diana.overlayScale;
        int w = BASE_WIDTH;
        for (String line : lines)
            w = Math.max(w, mc.fontRendererObj.getStringWidth(line) + PADDING * 2);
        int h = lines.size() * LINE_HEIGHT + PADDING * 2;
        lastW = w;
        lastH = h;

        ScaledResolution sr  = new ScaledResolution(mc);
        Position         pos = JefConfig.feature.diana.eventOverlayPos;
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

    static List<String> buildLines(boolean preview) {
        List<String> lines = new ArrayList<>();

        if (preview) {
            lines.add("\u00a7e\u00a7lDiana Event");
            lines.add("\u00a79Total Mobs: \u00a7f165");
            lines.add("\u00a71Playtime: \u00a7f2h 30m  \u00a71Session: \u00a7f45m");
            lines.add("\u00a7eBurrows: \u00a7f42  \u00a77(\u00a7a120.0\u00a77/hr)");
            lines.add("\u00a7dInquisitor \u00a7d4.20% \u00a7f(7) \u00a77[\u00a7bLS \u00a7f3\u00a77]");
            lines.add("\u00a76Minotaur \u00a7d12.30% \u00a7f(45)");
            lines.add("\u00a75Minos Champion \u00a7d8.10% \u00a7f(30)");
            lines.add("\u00a7fGaia Construct \u00a7d5.00% \u00a7f(8)");
            lines.add("\u00a7aMinos Hunter \u00a7d20.00% \u00a7f(33)");
            lines.add("\u00a7eSiamese Lynx \u00a7d10.00% \u00a7f(17)");
            return lines;
        }

        DianaStats stats = DianaStats.getInstance();
        if (!stats.isTracking()) return lines;

        DianaData d   = stats.getData();
        double    bph = stats.getBph();

        lines.add("\u00a7e\u00a7lDiana Event");

        lines.add(String.format("\u00a79Total Mobs: \u00a7f%d", d.totalMobs));

        lines.add(String.format("\u00a71Playtime: \u00a7f%s  \u00a71Session: \u00a7f%s",
                DianaStats.formatTime(d.activeTimeMs),
                DianaStats.formatTime(stats.getSessionTimeMs())));

        lines.add(String.format("\u00a7eBurrows: \u00a7f%d  \u00a77(\u00a7a%.1f\u00a77/hr)",
                d.totalBorrows, bph));

        String inqPct = d.totalMobs > 0
                ? String.format("%.2f%%", stats.getMobPercent(d.totalInqs))
                : "-.--%%";
        String lsSuffix = d.totalInqsLootshared > 0
                ? String.format("  \u00a77[\u00a7bLS \u00a7f%d\u00a77]", d.totalInqsLootshared)
                : "";
        lines.add(String.format("\u00a7dInquisitor \u00a7d%s \u00a7f(%d)%s",
                inqPct, d.totalInqs, lsSuffix));

        String minoPct = d.totalMobs > 0
                ? String.format("%.2f%%", stats.getMobPercent(d.totalMinotaurs))
                : "-.--%%";
        lines.add(String.format("\u00a76Minotaur \u00a7d%s \u00a7f(%d)",
                minoPct, d.totalMinotaurs));

        String champPct = d.totalMobs > 0
                ? String.format("%.2f%%", stats.getMobPercent(d.totalChamps))
                : "-.--%%";
        lines.add(String.format("\u00a75Minos Champion \u00a7d%s \u00a7f(%d)",
                champPct, d.totalChamps));

        String gaiaPct = d.totalMobs > 0
                ? String.format("%.2f%%", stats.getMobPercent(d.totalGaiaConstructs))
                : "-.--%%";
        lines.add(String.format("\u00a7fGaia Construct \u00a7d%s \u00a7f(%d)",
                gaiaPct, d.totalGaiaConstructs));

        String hunterPct = d.totalMobs > 0
                ? String.format("%.2f%%", stats.getMobPercent(d.totalMinosHunters))
                : "-.--%%";
        lines.add(String.format("\u00a7aMinos Hunter \u00a7d%s \u00a7f(%d)",
                hunterPct, d.totalMinosHunters));

        String lynxPct = d.totalMobs > 0
                ? String.format("%.2f%%", stats.getMobPercent(d.totalSiameseLynxes))
                : "-.--%%";
        lines.add(String.format("\u00a7eSiamese Lynx \u00a7d%s \u00a7f(%d)",
                lynxPct, d.totalSiameseLynxes));

        return lines;
    }
}