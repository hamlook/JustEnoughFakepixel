package com.jef.justenoughfakepixel.features.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jef.justenoughfakepixel.utils.ChatUtils;
import com.jef.justenoughfakepixel.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrentPetTracker {

    private static final Pattern SUMMONED     = Pattern.compile("^You summoned your (.+)!$");
    private static final Pattern AUTOPET      = Pattern.compile("^Autopet equipped your \\[Lvl \\d+\\] (.+)!$");
    private static final Pattern LEVEL_PREFIX = Pattern.compile("^\\[Lvl \\d+\\] ");

    private static final String PETS_CONTAINER = "Pets";
    private static final String ACTIVE_LORE    = "Click to despawn";

    private static final Gson GSON = new GsonBuilder().create();

    private static CurrentPetTracker INSTANCE;
    public static CurrentPetTracker getInstance() {
        if (INSTANCE == null) INSTANCE = new CurrentPetTracker();
        return INSTANCE;
    }

    private CurrentPetTracker() {}

    private File file;
    private String currentBaseName = "";
    private boolean scannedCurrentContainer = false;

    public void initFile(File configDir) {
        file = new File(configDir, "current_pet.json");
    }

    public void load() {
        if (file == null || !file.exists()) return;
        try (Reader r = new FileReader(file)) {
            String loaded = GSON.fromJson(r, String.class);
            if (loaded != null) currentBaseName = loaded;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {
        if (file == null) return;
        try {
            if (!file.exists()) file.createNewFile();
            try (Writer w = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
                w.write(GSON.toJson(currentBaseName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentBaseName() { return currentBaseName; }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        scannedCurrentContainer = false;
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (scannedCurrentContainer) return;
        if (!(event.gui instanceof GuiChest)) return;
        if (!(((GuiChest) event.gui).inventorySlots instanceof ContainerChest)) return;

        ContainerChest container = (ContainerChest) ((GuiChest) event.gui).inventorySlots;
        String title = container.getLowerChestInventory().getDisplayName().getUnformattedText();
        if (!title.startsWith(PETS_CONTAINER)) return;

        scannedCurrentContainer = true;
        scanContainer(container);
    }

    private void scanContainer(ContainerChest container) {
        PetCache cache = PetCache.getInstance();

        for (Slot slot : container.inventorySlots) {
            if (slot.inventory == Minecraft.getMinecraft().thePlayer.inventory) continue;
            ItemStack item = slot.getStack();
            if (item == null || item.getItem() == null) continue;

            String texture = ItemUtils.getSkullTexture(item);
            if (texture.isEmpty()) continue;

            String formatted = item.getDisplayName();
            String base = LEVEL_PREFIX.matcher(
                    StringUtils.stripControlCodes(formatted)).replaceFirst("").trim();
            if (base.isEmpty()) continue;

            cache.update(base, formatted, texture);

            if (ItemUtils.getLoreLine(item, ACTIVE_LORE) != null && !base.equals(currentBaseName)) {
                currentBaseName = base;
                save();
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!ChatUtils.isFromServer(event)) return;
        String raw = StringUtils.stripControlCodes(event.message.getUnformattedText()).trim();

        Matcher m = SUMMONED.matcher(raw);
        if (!m.matches()) m = AUTOPET.matcher(raw);
        if (!m.matches()) return;

        String name = m.group(1).trim();
        if (name.equals(currentBaseName)) return;

        currentBaseName = name;
        save();
    }
}