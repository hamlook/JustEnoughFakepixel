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

public class InqHealthOverlay extends JefOverlay {

    private static InqHealthOverlay instance;

    public InqHealthOverlay() {
        super(160, LINE_HEIGHT + PADDING * 2);
        instance = this;
    }

    public static InqHealthOverlay getInstance() { return instance; }

    @Override protected int     getBaseWidth()    { return 160; }
    @Override public Position   getPosition()     { return JefConfig.feature.diana.inqHealthPos; }
    @Override public float      getScale()        { return JefConfig.feature.diana.inqScale; }
    @Override public int        getBgColor()      { return ChromaColour.specialToChromaRGB(JefConfig.feature.diana.inqBgColor); }
    @Override public int        getCornerRadius() { return JefConfig.feature.diana.inqCornerRadius; }
    @Override protected boolean extraGuard()      { return DianaStats.getInstance().isTracking(); }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (JefConfig.feature == null || !JefConfig.feature.diana.enabled
                || !JefConfig.feature.diana.showInqHealthOverlay) return;
        render(false);
    }

    @Override
    public List<String> getLines(boolean preview) {
        if (preview)
            return Collections.singletonList("\u00a7dMinos Inquisitor \u00a7c1,200,000\u00a7f/\u00a7a2,000,000HP");

        String raw = DianaMobDetect.getClosestInqName();
        if (raw == null) return new ArrayList<>();
        return Collections.singletonList(raw);
    }
}