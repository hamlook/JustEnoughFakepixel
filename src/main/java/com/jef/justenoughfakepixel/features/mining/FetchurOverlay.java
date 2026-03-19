package com.jef.justenoughfakepixel.features.mining;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.features.scoreboard.CustomScoreboard;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.JefOverlay;
import com.jef.justenoughfakepixel.utils.ScoreboardUtils;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

@RegisterEvents
public class FetchurOverlay extends JefOverlay {

    private static FetchurOverlay instance;

    public FetchurOverlay() {
        super(160, LINE_HEIGHT + PADDING * 2);
        instance = this;
    }

    public static FetchurOverlay getInstance() { return instance; }

    @Override protected int     getBaseWidth()    { return 160; }
    @Override public Position   getPosition()     { return JefConfig.feature.mining.fetchurOverlayPos; }
    @Override public float      getScale()        { return JefConfig.feature.mining.fetchurOverlayScale; }
    @Override public int        getBgColor()      { return ChromaColour.specialToChromaRGB(JefConfig.feature.mining.overlayBgColor); }
    @Override public int        getCornerRadius() { return JefConfig.feature.mining.overlayCornerRadius; }
    @Override protected boolean extraGuard()      { return ScoreboardUtils.isOnSkyblock() && !CustomScoreboard.isActive(); }

    @Override
    protected boolean isEnabled() {
        return JefConfig.feature.mining.showFetchurOverlay;
    }

    @Override
    public List<String> getLines(boolean preview) {
        String item = preview ? "Yellow Stained Glass x20" : FetchurHelper.getTodaysItem();
        return Collections.singletonList(EnumChatFormatting.GOLD + "Fetchur: " + EnumChatFormatting.YELLOW + item);
    }
}