package com.jef.justenoughfakepixel.features.general;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterEvents
public class DamageSplashes {

    private static final char S = '\u00a7';

    private static final Pattern CRIT     = Pattern.compile(S + "f\u2727((?:" + S + ".[0-9.,kKmMbBtT])+?)" + S + ".\u2727(.*)");
    private static final Pattern OVERLOAD = Pattern.compile("(" + S + ".)\u272f((?:" + S + ".[0-9.,kKmMbBtT])+)(" + S + ".)\u272f" + S + "r");
    private static final Pattern NO_CRIT  = Pattern.compile("(" + S + ".)([0-9,.]*[0-9](?:[kKmMbBtT])?)(.*)");

    private static final long CACHE_TTL = 500L;

    private static class Entry {
        final String text; final boolean hide; final long time;
        Entry(String text, boolean hide, long time) { this.text = text; this.hide = hide; this.time = time; }
    }

    private final Map<EntityLivingBase, Entry> cache = new WeakHashMap<>();

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre event) {
        if (JefConfig.feature == null) return;
        boolean hideCrit    = JefConfig.feature.general.hideCritSplashes;
        boolean hideNonCrit = JefConfig.feature.general.hideNonCritSplashes;
        if (!hideCrit && !hideNonCrit) return;

        EntityLivingBase entity = event.entity;
        if (entity instanceof EntityPlayer) return;

        IChatComponent comp = entity.getDisplayName();
        if (comp == null) return;
        String text = comp.getFormattedText();
        if (text == null || text.isEmpty()) return;

        long now = System.currentTimeMillis();
        Entry cached = cache.get(entity);
        if (cached != null && now - cached.time <= CACHE_TTL && text.equals(cached.text)) {
            if (cached.hide) event.setCanceled(true);
            return;
        }

        boolean hasStar  = text.indexOf('\u2727') != -1 || text.indexOf('\u272f') != -1;
        boolean hasDigit = false;
        for (int i = 0; i < text.length(); i++) { char c = text.charAt(i); if (c >= '0' && c <= '9') { hasDigit = true; break; } }

        boolean hide = false;

        if (hideCrit && hasStar && hasDigit) {
            hide = CRIT.matcher(text).matches() || OVERLOAD.matcher(text).matches();
        }

        if (!hide && hideNonCrit && hasDigit) {
            Matcher m = NO_CRIT.matcher(text);
            if (m.matches()) {
                char code = m.group(1).length() >= 2 ? m.group(1).charAt(1) : '\0';
                boolean gray = code == '7', fire = code == '6' || code == 'c' || code == 'C';
                String rest  = m.group(3);
                boolean cleanEnd = rest == null || rest.isEmpty() || rest.equals("\u00a7r");
                if ((gray || fire) && cleanEnd) hide = true;
            } else if (text.matches("^\u00a77[0-9,.]+[kKmMbBtT]?$") || text.matches("^\u00a7[6c][0-9,.]+[kKmMbBtT]?$")) {
                hide = true;
            }
        }

        cache.put(entity, new Entry(text, hide, now));
        if (hide) event.setCanceled(true);
    }
}