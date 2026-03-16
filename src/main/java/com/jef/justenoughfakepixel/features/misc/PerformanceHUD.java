package com.jef.justenoughfakepixel.features.misc;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.events.PacketReceiveStatsEvent;
import com.jef.justenoughfakepixel.events.PacketReceiveTimeUpdateEvent;
import com.jef.justenoughfakepixel.utils.JefOverlay;
import com.jef.justenoughfakepixel.utils.OverlayUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class PerformanceHUD extends JefOverlay {

    public static final int OVERLAY_WIDTH  = 100;
    public static final int OVERLAY_HEIGHT = 45;

    private static final String C_LABEL = EnumChatFormatting.AQUA.toString();
    private static final String C_VAL   = EnumChatFormatting.WHITE.toString();

    private static final int    TPS_SAMPLES = 5;
    private static final long[] tpsTimes    = new long[TPS_SAMPLES];
    private static int   tpsHead  = 0;
    private static int   tpsCount = 0;
    private static float currentTps = 20f;

    private static final long PING_TIMEOUT_NS     = 5_000_000_000L;
    private static final int  PING_INTERVAL_TICKS = 100;

    private static long   pingSentAt     = -1L;
    private static double pingMs         = -1;
    private static int    ticksSincePing = 0;

    private static PerformanceHUD instance;

    public PerformanceHUD() {
        super(OVERLAY_WIDTH, OVERLAY_HEIGHT);
        instance = this;
    }

    public static PerformanceHUD getInstance() { return instance; }

    @Override public Position getPosition()     { return JefConfig.feature.misc.hudPos; }
    @Override public float    getScale()        { return JefConfig.feature.misc.hudScale; }
    @Override public int      getBgColor()      { return ChromaColour.specialToChromaRGB(JefConfig.feature.misc.hudBgColor); }
    @Override public int      getCornerRadius() { return JefConfig.feature.misc.hudCornerRadius; }

    @SubscribeEvent
    public void onTimeUpdate(PacketReceiveTimeUpdateEvent event) {
        long now = System.currentTimeMillis();
        if (tpsCount > 0) {
            int prev = (tpsHead - 1 + TPS_SAMPLES) % TPS_SAMPLES;
            long delta = now - tpsTimes[prev];
            if (delta > 0) currentTps = Math.max(0f, Math.min(20f, 20_000f / delta));
        }
        tpsTimes[tpsHead] = now;
        tpsHead = (tpsHead + 1) % TPS_SAMPLES;
        if (tpsCount < TPS_SAMPLES) tpsCount++;
    }

    @SubscribeEvent
    public void onStats(PacketReceiveStatsEvent event) {
        if (pingSentAt < 0) return;
        pingMs = Math.abs(System.nanoTime() - pingSentAt) / 1_000_000.0;
        pingSentAt = -1L;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) return;

        if (pingSentAt >= 0 && System.nanoTime() - pingSentAt > PING_TIMEOUT_NS) {
            pingSentAt = -1L;
            ticksSincePing = 0;
        }

        if (pingSentAt >= 0) return;
        if (++ticksSincePing < PING_INTERVAL_TICKS) return;
        ticksSincePing = 0;
        pingSentAt = System.nanoTime();
        mc.thePlayer.sendQueue.getNetworkManager().sendPacket(
                new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS));
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        tpsCount = 0; tpsHead = 0; currentTps = 20f;
        pingSentAt = -1L; pingMs = -1; ticksSincePing = 0;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (JefConfig.feature == null || !JefConfig.feature.misc.performanceHud) return;
        if (OverlayUtils.shouldHide()) return;
        render(false);
    }

    @Override
    public List<String> getLines(boolean preview) {
        if (JefConfig.feature == null) return new ArrayList<>();
        List<String> out = new ArrayList<>();
        if (JefConfig.feature.misc.hudShowFps)
            out.add(C_LABEL + "FPS: "  + C_VAL + (preview ? 60     : Minecraft.getDebugFPS()));
        if (JefConfig.feature.misc.hudShowTps)
            out.add(C_LABEL + "TPS: "  + C_VAL + (preview ? "20.0" : String.format("%.1f", currentTps)));
        if (JefConfig.feature.misc.hudShowPing)
            out.add(C_LABEL + "Ping: " + C_VAL + (preview ? "42ms" : formatPing()));
        return out;
    }

    @Override
    public void render(boolean preview) {
        if (JefConfig.feature == null) return;
        if (!preview && OverlayUtils.shouldHide()) return;

        List<String> lines = getLines(preview);
        if (lines.isEmpty()) return;

        Minecraft mc    = Minecraft.getMinecraft();
        float     scale = getScale();
        boolean   vert  = JefConfig.feature.misc.hudVertical;

        int w = estimateWidth(mc, lines, vert);
        int h = vert ? lines.size() * LINE_HEIGHT + PADDING * 2 : LINE_HEIGHT + PADDING * 2;
        lastW = w; lastH = h;

        ScaledResolution sr  = new ScaledResolution(mc);
        Position         pos = getPosition();
        int x = pos.getAbsX(sr, (int)(w * scale));
        int y = pos.getAbsY(sr, (int)(h * scale));
        if (pos.isCenterX()) x -= (int)(w * scale / 2);
        if (pos.isCenterY()) y -= (int)(h * scale / 2);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(scale, scale, 1f);

        int bgColor = getBgColor();
        if ((bgColor >>> 24) != 0)
            drawRoundedRect(-PADDING, -PADDING, w, h - PADDING, getCornerRadius(), bgColor);

        if (vert) {
            int dy = 0;
            for (String line : lines) {
                mc.fontRendererObj.drawStringWithShadow(line, 0, dy, 0xFFFFFF);
                dy += LINE_HEIGHT;
            }
        } else {
            int cx = 0;
            for (String line : lines) {
                mc.fontRendererObj.drawStringWithShadow(line, cx, 0, 0xFFFFFF);
                cx += mc.fontRendererObj.getStringWidth(line) + 6;
            }
        }

        GL11.glPopMatrix();
    }

    private static String formatPing() {
        return pingMs < 0 ? "..." : String.format("%.0fms", pingMs);
    }

    private static int estimateWidth(Minecraft mc, List<String> lines, boolean vertical) {
        if (vertical) {
            int max = OVERLAY_WIDTH;
            for (String l : lines) max = Math.max(max, mc.fontRendererObj.getStringWidth(l) + PADDING * 2);
            return max;
        }
        int total = 0;
        for (String l : lines) total += mc.fontRendererObj.getStringWidth(l) + 6;
        return Math.max(total, OVERLAY_WIDTH);
    }
}