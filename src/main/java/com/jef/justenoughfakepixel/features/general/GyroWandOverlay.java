package com.jef.justenoughfakepixel.features.general;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.utils.JefOverlay;
import com.jef.justenoughfakepixel.utils.OverlayUtils;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.List;

public class GyroWandOverlay extends JefOverlay {

    private static final long COOLDOWN_MS = 30_000L;

    private static GyroWandOverlay instance;
    private long lastUsedMs = 0;

    public GyroWandOverlay() {
        super(80, 12);
        instance = this;
    }

    public static GyroWandOverlay getInstance() { return instance; }

    @Override public Position getPosition()     { return JefConfig.feature.general.gyroWandPos; }
    @Override public float    getScale()        { return JefConfig.feature.general.gyroWandScale; }
    @Override public int      getBgColor()      { return ChromaColour.specialToChromaRGB(JefConfig.feature.general.gyroWandBgColor); }
    @Override public int      getCornerRadius() { return JefConfig.feature.general.gyroWandCornerRadius; }

    @Override
    public List<String> getLines(boolean preview) {
        if (preview) return Collections.singletonList("\u00a7cGyro: \u00a7f5s");
        if (!isOnCooldown()) return Collections.emptyList();
        long remaining = (COOLDOWN_MS - (System.currentTimeMillis() - lastUsedMs)) / 1000 + 1;
        return Collections.singletonList("\u00a7cGyro: \u00a7f" + remaining + "s");
    }

    public void markUsed() {
        lastUsedMs = System.currentTimeMillis();
    }

    public boolean isOnCooldown() {
        return (System.currentTimeMillis() - lastUsedMs) < COOLDOWN_MS;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (JefConfig.feature == null || !JefConfig.feature.general.gyroWandTimer) return;
        if (OverlayUtils.shouldHide()) return;
        if (!isOnCooldown()) return;
        render(false);
    }
}