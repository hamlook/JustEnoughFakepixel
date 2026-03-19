package com.jef.justenoughfakepixel.features.diana;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.JefOverlay;

import java.util.ArrayList;
import java.util.List;

@RegisterEvents
public class DianaEventOverlay extends JefOverlay {

    private static DianaEventOverlay instance;

    public DianaEventOverlay() {
        super(180, LINE_HEIGHT * 7 + PADDING * 2);
        instance = this;
    }

    public static DianaEventOverlay getInstance() { return instance; }

    @Override protected int     getBaseWidth()    { return 180; }
    @Override public Position   getPosition()     { return JefConfig.feature.diana.eventOverlayPos; }
    @Override public float      getScale()        { return JefConfig.feature.diana.eventScale; }
    @Override public int        getBgColor()      { return ChromaColour.specialToChromaRGB(JefConfig.feature.diana.eventBgColor); }
    @Override public int        getCornerRadius() { return JefConfig.feature.diana.eventCornerRadius; }

    @Override
    protected boolean isEnabled() {
        return JefConfig.feature.diana.enabled && JefConfig.feature.diana.showEventOverlay;
    }

    @Override
    public List<String> getLines(boolean preview) {
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
                DianaStats.formatTime(d.activeTimeMs), DianaStats.formatTime(stats.getSessionTimeMs())));
        lines.add(String.format("\u00a7eBurrows: \u00a7f%d  \u00a77(\u00a7a%.1f\u00a77/hr)", d.totalBorrows, bph));

        lines.add(String.format("\u00a7dInquisitor \u00a7d%s \u00a7f(%d)%s",
                stats.formatMobPct(d.totalInqs), d.totalInqs, d.getLootsharedSuffix()));
        lines.add(String.format("\u00a76Minotaur \u00a7d%s \u00a7f(%d)",       stats.formatMobPct(d.totalMinotaurs),      d.totalMinotaurs));
        lines.add(String.format("\u00a75Minos Champion \u00a7d%s \u00a7f(%d)", stats.formatMobPct(d.totalChamps),         d.totalChamps));
        lines.add(String.format("\u00a7fGaia Construct \u00a7d%s \u00a7f(%d)", stats.formatMobPct(d.totalGaiaConstructs), d.totalGaiaConstructs));
        lines.add(String.format("\u00a7aMinos Hunter \u00a7d%s \u00a7f(%d)",   stats.formatMobPct(d.totalMinosHunters),   d.totalMinosHunters));
        lines.add(String.format("\u00a7eSiamese Lynx \u00a7d%s \u00a7f(%d)",   stats.formatMobPct(d.totalSiameseLynxes),  d.totalSiameseLynxes));

        return lines;
    }
}