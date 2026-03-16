package com.jef.justenoughfakepixel.features.diana;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.utils.JefOverlay;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class DianaLootOverlay extends JefOverlay {

    private static DianaLootOverlay instance;

    public DianaLootOverlay() {
        super(180, LINE_HEIGHT * 9 + PADDING * 2);
        instance = this;
    }

    public static DianaLootOverlay getInstance() { return instance; }

    @Override protected int     getBaseWidth()    { return 180; }
    @Override public Position   getPosition()     { return JefConfig.feature.diana.lootOverlayPos; }
    @Override public float      getScale()        { return JefConfig.feature.diana.lootScale; }
    @Override public int        getBgColor()      { return ChromaColour.specialToChromaRGB(JefConfig.feature.diana.lootBgColor); }
    @Override public int        getCornerRadius() { return JefConfig.feature.diana.lootCornerRadius; }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (JefConfig.feature == null || !JefConfig.feature.diana.enabled
                || !JefConfig.feature.diana.showLootOverlay) return;
        render(false);
    }

    @Override
    public List<String> getLines(boolean preview) {
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
                ? String.format("  \u00a77[\u00a7bLS \u00a7f%d\u00a77]", d.totalInqsLootshared) : "";
        lines.add(String.format("\u00a77Inqs since Chimera: \u00a7f%d%s", d.inqsSinceChimera, lsSuffix));
        lines.add(String.format("\u00a7dChimeras: \u00a7f%d", d.totalChimeras));
        lines.add(String.format("\u00a71Feathers: \u00a7f%d", d.griffinFeathers));
        lines.add(String.format("\u00a72Shelmets: \u00a7f%d  \u00a75Remedies: \u00a7f%d  \u00a75Plushies: \u00a7f%d",
                d.dwarfTurtleShelmets, d.antiqueRemedies, d.crochetTigerPlushies));
        lines.add(String.format("\u00a76Daedalus Sticks: \u00a7f%d  \u00a77(since last: \u00a7f%d\u00a77)",
                d.totalSticks, d.minotaursSinceStick));
        lines.add(String.format("\u00a75Minos Relics: \u00a7f%d  \u00a77(since last: \u00a7f%d\u00a77)",
                d.totalRelics, d.champsSinceRelic));
        lines.add(String.format("\u00a75Souvenirs: \u00a7f%d  \u00a76Crowns: \u00a7f%d", d.souvenirs, d.crownsOfGreed));
        lines.add(String.format("\u00a76Coins: \u00a7f%s", DianaStats.fmtCoins(d.totalCoins)));

        return lines;
    }
}