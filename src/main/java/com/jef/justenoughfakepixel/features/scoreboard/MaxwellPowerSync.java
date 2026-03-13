package com.jef.justenoughfakepixel.features.scoreboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jef.justenoughfakepixel.core.JefConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.*;

/**
 * Scans the "Accessory Bag Thaumaturgy" chest every tick while it is open,
 * caches the selected power name, and persists it to disk so it survives
 * game restarts.
 *
 * Saved to: config/JustEnoughFakepixel/maxwell_power.json
 *
 * Wire up in JefMod:
 *   preInit:    MaxwellPowerSync.getInstance().initFile(JefConfig.configDirectory);
 *   clientInit: MaxwellPowerSync.getInstance().load();
 *               MinecraftForge.EVENT_BUS.register(MaxwellPowerSync.getInstance());
 */
public class MaxwellPowerSync {

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static MaxwellPowerSync INSTANCE;

    public static MaxwellPowerSync getInstance() {
        if (INSTANCE == null) INSTANCE = new MaxwellPowerSync();
        return INSTANCE;
    }

    private MaxwellPowerSync() {}

    // ── Persistence ───────────────────────────────────────────────────────────
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private File file = null;

    private static class PowerData {
        String power = null;
    }

    private PowerData data = new PowerData();

    public void initFile(File configDir) {
        this.file = new File(configDir, "maxwell_power.json");
    }

    public void load() {
        if (file == null || !file.exists()) return;
        try (Reader r = new FileReader(file)) {
            PowerData loaded = GSON.fromJson(r, PowerData.class);
            if (loaded != null) data = loaded;
        } catch (Exception e) {
            System.err.println("[JEF/Maxwell] Failed to load maxwell_power.json: " + e.getMessage());
        }
    }

    private void save() {
        if (file == null) return;
        try (Writer w = new FileWriter(file)) {
            GSON.toJson(data, w);
        } catch (Exception e) {
            System.err.println("[JEF/Maxwell] Failed to save maxwell_power.json: " + e.getMessage());
        }
    }

    // ── Public accessor ───────────────────────────────────────────────────────
    public static String getPower() {
        if (INSTANCE == null) return null;
        return INSTANCE.data.power;
    }

    // ── Tick scan ─────────────────────────────────────────────────────────────
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChest)) return;

        GuiChest chest = (GuiChest) mc.currentScreen;
        IInventory inv = ((ContainerChest) chest.inventorySlots).getLowerChestInventory();

        String title = stripColor(inv.getDisplayName().getUnformattedText());
        if (!title.contains("Accessory Bag Thaumaturgy")) return;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack item = inv.getStackInSlot(i);
            if (item == null || !item.hasTagCompound()) continue;

            if (hasSelectedLine(item)) {
                String name = stripColor(item.getDisplayName()).trim();
                if (!name.isEmpty() && !name.equals(data.power)) {
                    data.power = name;
                    save(); // only save when value actually changes
                }
                return;
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private boolean hasSelectedLine(ItemStack item) {
        try {
            NBTTagList lore = item.getTagCompound()
                    .getCompoundTag("display")
                    .getTagList("Lore", 8);
            for (int i = 0; i < lore.tagCount(); i++) {
                if (stripColor(lore.getStringTagAt(i)).trim().equals("Power is selected!")) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static String stripColor(String s) {
        return s == null ? "" : s.replaceAll("\u00A7[0-9a-fklmnorA-FKLMNOR]", "");
    }
}