package com.jef.justenoughfakepixel.features.misc;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.JefOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

@RegisterEvents
public class ItemPickupLog extends JefOverlay {

    public static final long LIFESPAN_MS = 5_000L;
    private static final int MAX_LINES = 15;
    public static final int OVERLAY_WIDTH  = 140;
    public static final int OVERLAY_HEIGHT = 80;

    private static class LogEntry {
        final String displayName;
        int amount;
        long timestamp;

        LogEntry(String displayName, int amount) {
            this.displayName = displayName;
            this.amount = amount;
            this.timestamp = System.currentTimeMillis();
        }

        void add(int delta) {
            this.amount += delta;
            this.timestamp = (this.amount == 0)
                    ? System.currentTimeMillis() - LIFESPAN_MS
                    : System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > LIFESPAN_MS;
        }
    }

    private static ItemPickupLog instance;
    private ItemStack[] previousInventory = null;
    private final LinkedList<LogEntry> log = new LinkedList<>();

    public ItemPickupLog() {
        super(OVERLAY_WIDTH, OVERLAY_HEIGHT);
        instance = this;
    }

    public static ItemPickupLog getInstance() { return instance; }

    @Override public Position getPosition()     { return JefConfig.feature.misc.itemPickupLogPos; }
    @Override public float    getScale()        { return JefConfig.feature.misc.itemPickupLogScale; }
    @Override public int      getBgColor()      { return ChromaColour.specialToChromaRGB(JefConfig.feature.misc.itemPickupLogBgColor); }
    @Override public int      getCornerRadius() { return JefConfig.feature.misc.itemPickupLogCornerRadius; }

    @Override
    protected boolean isEnabled() {
        return JefConfig.feature.misc.itemPickupLog;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        resetSnapshot();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || !isEnabled()) {
            previousInventory = null;
            return;
        }

        log.removeIf(LogEntry::isExpired);

        ItemStack[] current = mc.thePlayer.inventory.mainInventory;

        if (previousInventory != null && previousInventory.length == current.length) {
            diffAndLog(previousInventory, current);
        }

        previousInventory = copyInventory(current);
    }

    private void diffAndLog(ItemStack[] prev, ItemStack[] curr) {
        Map<String, Integer> prevMap = new HashMap<>();
        Map<String, Integer> currMap = new HashMap<>();

        for (int i = 0; i < prev.length; i++) {
            accumulate(prevMap, prev[i]);
            accumulate(currMap, curr[i]);
        }

        Set<String> allKeys = new HashSet<>(prevMap.keySet());
        allKeys.addAll(currMap.keySet());

        for (String name : allKeys) {
            int delta = currMap.getOrDefault(name, 0) - prevMap.getOrDefault(name, 0);
            if (delta == 0) continue;
            addOrMerge(name, delta);
        }
    }

    private static void accumulate(Map<String, Integer> map, ItemStack stack) {
        if (stack == null) return;
        map.merge(stack.getDisplayName(), stack.stackSize, Integer::sum);
    }

    private void addOrMerge(String name, int delta) {
        for (LogEntry entry : log) {
            if (entry.displayName.equals(name)) {
                boolean sameSign = (delta > 0 && entry.amount > 0) || (delta < 0 && entry.amount < 0);
                if (sameSign) {
                    entry.add(delta);
                    return;
                }
            }
        }
        log.addLast(new LogEntry(name, delta));
    }

    @Override
    public List<String> getLines(boolean preview) {
        List<String> lines = new ArrayList<>();

        if (preview) {
            lines.add("§a+ 1x §fAspect of the End");
            lines.add("§c- 32x §fEnchanted Cobblestone");
            lines.add("§a+ 5x §fMithril Ore");
            lines.add("§a+ 64x §fEnchanted Oak Wood");
            lines.add("§c- 1x §fBoat");
            return lines;
        }

        int start = Math.max(0, log.size() - MAX_LINES);
        List<LogEntry> visible = new ArrayList<>(log).subList(start, log.size());
        for (int i = visible.size() - 1; i >= 0; i--) {
            LogEntry e = visible.get(i);
            String sign = e.amount > 0 ? "§a+" : "§c-";
            lines.add(sign + " " + Math.abs(e.amount) + "x §r" + e.displayName);
        }
        return lines;
    }

    public void resetSnapshot() {
        previousInventory = null;
        log.clear();
    }

    private static ItemStack[] copyInventory(ItemStack[] src) {
        ItemStack[] copy = new ItemStack[src.length];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i] != null ? ItemStack.copyItemStack(src[i]) : null;
        }
        return copy;
    }
}