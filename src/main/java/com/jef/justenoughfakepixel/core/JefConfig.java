package com.jef.justenoughfakepixel.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigEditor;
import com.jef.justenoughfakepixel.core.config.gui.GuiScreenElementWrapper;
import com.jef.justenoughfakepixel.core.config.command.JefCommand;
import com.jef.justenoughfakepixel.core.config.editors.GuiPositionEditor;
import com.jef.justenoughfakepixel.features.dungeons.DungeonBreakerOverlay;
import com.jef.justenoughfakepixel.features.misc.SearchBar;
import com.jef.justenoughfakepixel.features.diana.DianaEventOverlay;
import com.jef.justenoughfakepixel.features.misc.ItemPickupLog;
import com.jef.justenoughfakepixel.features.diana.GuiDianaOverlayEditor;
import com.jef.justenoughfakepixel.features.general.GyroWandOverlay;
import com.jef.justenoughfakepixel.features.mining.FetchurOverlay;
import com.jef.justenoughfakepixel.features.mining.PowderOverlay;
import com.jef.justenoughfakepixel.features.mining.PowderStats;
import com.jef.justenoughfakepixel.features.dungeons.DungeonStats;
import com.jef.justenoughfakepixel.features.dungeons.DungeonRoomOverlay;
import com.jef.justenoughfakepixel.features.misc.CurrentPetOverlay;
import com.jef.justenoughfakepixel.features.misc.PerformanceHUD;
import com.jef.justenoughfakepixel.features.scoreboard.CustomScoreboard;
import com.jef.justenoughfakepixel.features.waypoints.WaypointGroupGui;
import com.jef.justenoughfakepixel.features.fishing.trophy.TrophyFishOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JefConfig {

    public static Config feature;
    public static File configDirectory = new File("config/JustEnoughFakepixel");
    private static File configFile;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    public static final KeyBinding openGuiKey = new KeyBinding(
            "Open JEF GUI",
            Keyboard.KEY_P,
            "JustEnoughFakepixel"
    );

    public static GuiScreen screenToOpen = null;
    private static int screenTicks = 0;
    private static boolean waypointManagerKeyWasDown = false;
    private static boolean powderToggleKeyWasDown = false;
    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        init();
        MinecraftForge.EVENT_BUS.register(new JefConfig());
        ClientRegistry.registerKeyBinding(openGuiKey);
        ClientCommandHandler.instance.registerCommand(new JefCommand());
        registered = true;
    }

    public static void init() {
        if (!configDirectory.exists()) configDirectory.mkdirs();
        configFile = new File(configDirectory, "config.json");
        loadConfig();
    }

    private static void loadConfig() {
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8))) {
                feature = GSON.fromJson(reader, Config.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (feature == null) {
            feature = new Config();
            saveConfig();
        }
    }

    public static void saveConfig() {
        try {
            if (!configFile.exists()) configFile.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8))) {
                writer.write(GSON.toJson(feature));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openGui() {
        screenToOpen = new GuiScreenElementWrapper(new ConfigEditor(feature));
    }

    public static void openCategory(String categoryName) {
        screenToOpen = new GuiScreenElementWrapper(new ConfigEditor(feature, categoryName));
    }

    public static void openWaypointGroupGui() {
        screenToOpen = new GuiScreenElementWrapper(new WaypointGroupGui());
    }

    public static void openStatsEditor() {
        if (feature == null) return;
        DungeonStats stats = DungeonStats.getInstance();
        screenToOpen = new GuiPositionEditor(
                feature.dungeons.statsPos,
                stats::getOverlayWidth,
                stats::getOverlayHeight,
                () -> stats.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withOverlayScale(feature.dungeons.statsScale)
                .withParent(Minecraft.getMinecraft().currentScreen);
    }

    public static void openDungeonRoomOverlayEditor() {
        if (feature == null) return;
        DungeonRoomOverlay overlay = DungeonRoomOverlay.getInstance();
        if (overlay == null) return;
        screenToOpen = new GuiPositionEditor(
                feature.dungeons.dungeonRoomOverlayPos,
                overlay::getOverlayWidth,
                overlay::getOverlayHeight,
                () -> overlay.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withOverlayScale(feature.dungeons.dungeonRoomOverlayScale)
                .withParent(Minecraft.getMinecraft().currentScreen);
    }

    public static void openHudEditor() {
        if (feature == null) return;
        PerformanceHUD hud = PerformanceHUD.getInstance();
        screenToOpen = new GuiPositionEditor(
                feature.misc.hudPos,
                hud::getOverlayWidth,
                hud::getOverlayHeight,
                () -> hud.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withOverlayScale(feature.misc.hudScale)
                .withParent(Minecraft.getMinecraft().currentScreen);
    }

    public static void openFetchurEditor() {
        if (feature == null) return;
        FetchurOverlay fetchur = FetchurOverlay.getInstance();
        screenToOpen = new GuiPositionEditor(
                feature.mining.fetchurOverlayPos,
                fetchur::getOverlayWidth,
                fetchur::getOverlayHeight,
                () -> fetchur.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withOverlayScale(feature.mining.fetchurOverlayScale)
                .withParent(Minecraft.getMinecraft().currentScreen);
    }

    public static void openDianaOverlayEditor() {
        if (feature == null) return;
        screenToOpen = new GuiDianaOverlayEditor(
                Minecraft.getMinecraft().currentScreen,
                JefConfig::saveConfig
        );
    }

    public static void openScoreboardEditor() {
        if (feature == null) return;
        CustomScoreboard sb = CustomScoreboard.getInstance();
        screenToOpen = new GuiPositionEditor(
                feature.scoreboard.position,
                sb::getOverlayWidth,
                sb::getOverlayHeight,
                () -> sb.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withOverlayScale(feature.scoreboard.scale)
                .withParent(Minecraft.getMinecraft().currentScreen);
    }

    public static void openSearchBarEditor() {
        if (feature == null) return;
        SearchBar sb = SearchBar.getInstance();
        screenToOpen = new GuiPositionEditor(
                feature.misc.searchBarPos,
                sb::getOverlayWidth,
                sb::getOverlayHeight,
                () -> sb.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withParent(Minecraft.getMinecraft().currentScreen);
    }

    public static void openCurrentPetEditor() {
        if (feature == null) return;
        CurrentPetOverlay overlay = CurrentPetOverlay.getInstance();
        if (overlay == null) return;
        overlay.render(true);
        screenToOpen = new GuiPositionEditor(
                feature.misc.currentPetPos,
                overlay::getOverlayWidth,
                overlay::getOverlayHeight,
                () -> overlay.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withOverlayScale(feature.misc.currentPetScale)
                .withParent(Minecraft.getMinecraft().currentScreen);
    }

    public static void openItemPickupLogEditor() {
        if (feature == null) return;
        ItemPickupLog overlay = ItemPickupLog.getInstance();
        if (overlay == null) return;
        overlay.render(true);
        screenToOpen = new GuiPositionEditor(
                feature.misc.itemPickupLogPos,
                overlay::getOverlayWidth,
                overlay::getOverlayHeight,
                () -> overlay.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withOverlayScale(feature.misc.itemPickupLogScale)
                .withParent(Minecraft.getMinecraft().currentScreen);
    }

    public static void openGyroWandEditor() {
        if (feature == null) return;
        GyroWandOverlay overlay = GyroWandOverlay.getInstance();
        if (overlay == null) return;
        screenToOpen = new GuiPositionEditor(
                feature.general.gyroWandPos,
                overlay::getOverlayWidth,
                overlay::getOverlayHeight,
                () -> overlay.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withOverlayScale(feature.general.gyroWandScale)
                .withParent(Minecraft.getMinecraft().currentScreen);
    }

    public static void openPowderEditor() {
        if (feature == null) return;
        PowderOverlay overlay = PowderOverlay.getInstance();
        if (overlay == null) return;
        screenToOpen = new GuiPositionEditor(
                feature.mining.powderOverlayPos,
                overlay::getOverlayWidth,
                overlay::getOverlayHeight,
                () -> overlay.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withOverlayScale(feature.mining.powderOverlayScale)
                .withParent(Minecraft.getMinecraft().currentScreen);
    }
    public static void openDungeonBreakerEditor() {
        if (feature == null) return;
        DungeonBreakerOverlay overlay = DungeonBreakerOverlay.getInstance();
        if (overlay == null) return;
        screenToOpen = new GuiPositionEditor(
                feature.dungeons.dungeonBreakerPos,
                overlay::getOverlayWidth,
                overlay::getOverlayHeight,
                () -> overlay.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withOverlayScale(feature.dungeons.dungeonBreakerScale)
                .withParent(Minecraft.getMinecraft().currentScreen);
    }

    public static void openInvButtonEditor() {
        screenToOpen = new com.jef.justenoughfakepixel.features.invbuttons.GuiInvButtonEditor();
    }

    public static void openTrophyFishEditor() {
        if (feature == null) return;
        TrophyFishOverlay overlay = TrophyFishOverlay.getInstance();
        if (overlay == null) return;
        screenToOpen = new GuiPositionEditor(
                feature.fishing.trophyFishPos,
                overlay::getOverlayWidth,
                overlay::getOverlayHeight,
                () -> overlay.render(true),
                JefConfig::saveConfig,
                JefConfig::saveConfig
        ).withOverlayScale(feature.fishing.trophyFishScale)
                .withParent(Minecraft.getMinecraft().currentScreen);
    }

    public static void resetPowderTracker() {
        PowderStats.getInstance().reset();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getMinecraft().thePlayer == null) return;

        if (screenToOpen != null) {
            screenTicks++;
            if (screenTicks == 5) {
                Minecraft.getMinecraft().displayGuiScreen(screenToOpen);
                screenTicks = 0;
                screenToOpen = null;
            }
        }

        if (openGuiKey.isPressed() && Minecraft.getMinecraft().currentScreen == null) openGui();

        boolean managerKeyDown = feature != null
                && feature.waypoints.waypointManagerKey != org.lwjgl.input.Keyboard.KEY_NONE
                && org.lwjgl.input.Keyboard.isKeyDown(feature.waypoints.waypointManagerKey);

        if (managerKeyDown && !waypointManagerKeyWasDown && Minecraft.getMinecraft().currentScreen == null)
            openWaypointGroupGui();

        waypointManagerKeyWasDown = managerKeyDown;

        if (feature != null
                && feature.mining.powderToggleKey != org.lwjgl.input.Keyboard.KEY_NONE
                && org.lwjgl.input.Keyboard.isKeyDown(feature.mining.powderToggleKey)
                && !powderToggleKeyWasDown
                && Minecraft.getMinecraft().currentScreen == null) {
            PowderStats.getInstance().toggleTracking();
        }

        powderToggleKeyWasDown = feature != null
                && feature.mining.powderToggleKey != org.lwjgl.input.Keyboard.KEY_NONE
                && org.lwjgl.input.Keyboard.isKeyDown(feature.mining.powderToggleKey);
    }
}