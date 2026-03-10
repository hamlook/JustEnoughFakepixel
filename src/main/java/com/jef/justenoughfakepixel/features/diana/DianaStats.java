package com.jef.justenoughfakepixel.features.diana;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;

import java.io.*;

/**
 * Singleton that owns the persisted {@link DianaData} and all transient runtime state.
 * Call {@link #initFile(File)} from JefMod.preInit and {@link #load()} from JefMod.clientInit.
 */
public class DianaStats {

    private static DianaStats INSTANCE;

    public static DianaStats getInstance() {
        if (INSTANCE == null) INSTANCE = new DianaStats();
        return INSTANCE;
    }

    private DianaStats() {}


    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Minecraft mc = Minecraft.getMinecraft();

    private File      file = null;
    private DianaData data = new DianaData();

    // Transient
    public volatile boolean idlePaused    = false;
    public volatile String  lastDropType  = null;   // "feather"|"souvenir"|"crown"|"coins"|"shelmet"|"remedies"|"plushie"
    public volatile long    lastDropAmount = 0L;

    // File I/O

    public void initFile(File configDir) {
        this.file = new File(configDir, "diana_stats.json");
    }

    public void load() {
        if (file == null || !file.exists()) return;
        try (Reader r = new FileReader(file)) {
            DianaData loaded = GSON.fromJson(r, DianaData.class);
            if (loaded != null) data = loaded;
        } catch (Exception e) {
            System.err.println("[JEF/Diana] Failed to load diana_stats.json: " + e.getMessage());
        }
    }

    public void save() {
        if (file == null) return;
        try (Writer w = new FileWriter(file)) {
            GSON.toJson(data, w);
        } catch (Exception e) {
            System.err.println("[JEF/Diana] Failed to save diana_stats.json: " + e.getMessage());
        }
    }


    public void reset() {
        data           = new DianaData();
        idlePaused     = false;
        lastDropType   = null;
        lastDropAmount = 0L;
    }


    public DianaData getData() { return data; }

    /**
     * Tracking is active when the Ancestral Spade is in the player's hotbar
     * and the player has not been idle for 20+ seconds.
     */
    public boolean isTracking() {
        return hasSpadeInHotbar() && !idlePaused;
    }

    /**
     * Returns true if Ancestral Spade is present in any hotbar slot (0–8).
     */
    public static boolean hasSpadeInHotbar() {
        if (mc.thePlayer == null) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.hasDisplayName()
                    && StringUtils.stripControlCodes(stack.getDisplayName()).contains("Ancestral Spade")) {
                return true;
            }
        }
        return false;
    }


    public double getBph() {
        if (data.sessionStartMs <= 0 || data.totalBurrows == 0) return 0.0;
        long elapsed = System.currentTimeMillis() - data.sessionStartMs;
        if (elapsed < 1_000L) return 0.0;
        return data.totalBurrows / (elapsed / 3_600_000.0);
    }


    public double getInqChance() {
        if (data.totalInqs == 0 || data.totalMobs == 0) return -1.0;
        return (double) data.totalInqs / data.totalMobs * 100.0;
    }
}