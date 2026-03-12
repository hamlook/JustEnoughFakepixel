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


public class DianaLootOverlay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final int LINE_HEIGHT = 10;
    private static final int PADDING     = 3;
    private static final int BASE_WIDTH  = 180;

    private static int lastW = BASE_WIDTH;
    private static int lastH = LINE_HEIGHT * 9 + PADDING * 2;

    public static int getOverlayWidth()  { return lastW; }
    public static int getOverlayHeight() { return lastH; }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (JefConfig.feature == null || !JefConfig.feature.diana.enabled
                || !JefConfig.feature.diana.showLootOverlay) return;
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
        Position         pos = JefConfig.feature.diana.lootOverlayPos;
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
            lines.add("\u00a76\u00a7lDiana Loot");
            lines.add("\u00a77Inqs since Chimera: \u00a7f4  \u00a77[\u00a7bLS \u00a7f3\u00a77]");
            lines.add("\u00a7dChimeras: \u00a7f1");
            lines.add("\u00a71Feathers: \u00a7f5");
            lines.add("\u00a72Shelmets: \u00a7f2  \u00a75Remedies: \u00a7f1  \u00a75Plushies: \u00a7f0");
            lines.add("\u00a76Daedalus Sticks: \u00a7f2  \u00a77(since last: \u00a7f12\u00a77)");
            lines.add("\u00a75Minos Relics: \u00a7f1  \u00a77(since last: \u00a7f30\u00a77)");
            lines.add("\u00a75Souvenirs: \u00a7f2  \u00a76Crowns: \u00a7f1");
            lines.add("\u00a76Coins: \u00a7f1.2M");
            return lines;
        }

        DianaStats stats = DianaStats.getInstance();
        if (!stats.isTracking()) return lines;

        DianaData d = stats.getData();

        lines.add("\u00a76\u00a7lDiana Loot");

        String lsSuffix = d.totalInqsLootshared > 0
                ? String.format("  \u00a77[\u00a7bLS \u00a7f%d\u00a77]", d.totalInqsLootshared)
                : "";
        lines.add(String.format("\u00a77Inqs since Chimera: \u00a7f%d%s",
                d.inqsSinceChimera, lsSuffix));

        lines.add(String.format("\u00a7dChimeras: \u00a7f%d", d.totalChimeras));

        lines.add(String.format("\u00a71Feathers: \u00a7f%d", d.griffinFeathers));

        lines.add(String.format("\u00a72Shelmets: \u00a7f%d  \u00a75Remedies: \u00a7f%d  \u00a75Plushies: \u00a7f%d",
                d.dwarfTurtleShelmets, d.antiqueRemedies, d.crochetTigerPlushies));

        lines.add(String.format("\u00a76Daedalus Sticks: \u00a7f%d  \u00a77(since last: \u00a7f%d\u00a77)",
                d.totalSticks, d.minotaursSinceStick));

        lines.add(String.format("\u00a75Minos Relics: \u00a7f%d  \u00a77(since last: \u00a7f%d\u00a77)",
                d.totalRelics, d.champsSinceRelic));

        lines.add(String.format("\u00a75Souvenirs: \u00a7f%d  \u00a76Crowns: \u00a7f%d",
                d.souvenirs, d.crownsOfGreed));
        lines.add(String.format("\u00a76Coins: \u00a7f%s", DianaStats.fmtCoins(d.totalCoins)));

        return lines;
    }
}