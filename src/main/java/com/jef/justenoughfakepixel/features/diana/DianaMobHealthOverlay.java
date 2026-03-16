package com.jef.justenoughfakepixel.features.diana;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.utils.JefOverlay;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DianaMobHealthOverlay extends JefOverlay {

    private static DianaMobHealthOverlay instance;

    public DianaMobHealthOverlay() {
        super(180, LINE_HEIGHT + PADDING * 2);
        instance = this;
    }

    public static DianaMobHealthOverlay getInstance() { return instance; }

    @Override protected int     getBaseWidth()    { return 180; }
    @Override public Position   getPosition()     { return JefConfig.feature.diana.dianaMobHealthPos; }
    @Override public float      getScale()        { return JefConfig.feature.diana.mobScale; }
    @Override public int        getBgColor()      { return ChromaColour.specialToChromaRGB(JefConfig.feature.diana.mobBgColor); }
    @Override public int        getCornerRadius() { return JefConfig.feature.diana.mobCornerRadius; }
    @Override protected boolean extraGuard()      { return true; }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (JefConfig.feature == null || !JefConfig.feature.diana.enabled
                || !JefConfig.feature.diana.showDianaMobHealthOverlay) return;
        render(false);
    }

    @Override
    public List<String> getLines(boolean preview) {
        if (preview)
            return Collections.singletonList("\u00a72[Lv260] \u2724\u273f Gaia Construct 839.6k\u00a7f/\u00a7a1.5M\u00a7c\u2764");

        String raw = DianaMobDetect.getClosestNonInqMobName();
        if (raw == null) return new ArrayList<>();
        return Collections.singletonList(raw);
    }
}