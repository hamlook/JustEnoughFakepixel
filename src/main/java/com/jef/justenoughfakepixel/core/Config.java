package com.jef.justenoughfakepixel.core;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.features.*;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;

import java.awt.Desktop;
import java.net.URI;

public class Config {

    @Expose
    @Category(name = "About", desc = "Links, credits & used software")
    public final About about = new About();

    @Expose
    @Category(name = "Quality of life", desc = "QOL features")
    public final Qol qol = new Qol();

    @Expose
    @Category(name = "Scoreboard", desc = "Custom scoreboard panel")
    public final Scoreboard scoreboard = new Scoreboard();

    @Expose
    @Category(name = "Misc", desc = "Misc features")
    public final Misc misc = new Misc();

    @Expose
    @Category(name = "Waypoints", desc = "Waypoints config & GUI")
    public final Waypoints waypoints = new Waypoints();

    @Expose
    @Category(name = "Mining", desc = "Mining features")
    public final Mining mining = new Mining();

    @Expose
    @Category(name = "Diana", desc = "Diana event tracking & overlays")
    public final Diana diana = new Diana();

    @Expose
    @Category(name = "Dungeons", desc = "Dungeon features")
    public final Dungeons dungeons = new Dungeons();

    @Expose
    @Category(name = "Farming", desc = "Farming features")
    public final Farming farming = new Farming();

    @Expose
    @Category(name = "Fishing", desc = "Fishing features")
    public final Fishing fishing = new Fishing();

    @Expose
    @Category(name = "Debug", desc = "Debug tools")
    public final Debug debug = new Debug();

    public void executeRunnable(String runnableId) {
        switch (runnableId) {
            // ── runnables ───────────────────────────────────────────
            case "openScoreboardEditor":         JefConfig.openScoreboardEditor();          break;
            case "openWaypointGroupGui":          JefConfig.openWaypointGroupGui();          break;
            case "openStatsEditor":               JefConfig.openStatsEditor();               break;
            case "openHudEditor":                 JefConfig.openHudEditor();                 break;
            case "openFetchurEditor":             JefConfig.openFetchurEditor();             break;
            case "openDianaOverlayEditor":        JefConfig.openDianaOverlayEditor();        break;
            case "openSearchBarEditor":           JefConfig.openSearchBarEditor();           break;
            case "openCurrentPetEditor":          JefConfig.openCurrentPetEditor();          break;
            case "openItemPickupLogEditor":       JefConfig.openItemPickupLogEditor();       break;
            case "openGyroWandEditor":            JefConfig.openGyroWandEditor();            break;
            case "openPowderEditor":              JefConfig.openPowderEditor();              break;
            case "openInvButtonEditor":           JefConfig.openInvButtonEditor();           break;
            case "resetPowderTracker":            JefConfig.resetPowderTracker();            break;
            case "openDungeonBreakerEditor":      JefConfig.openDungeonBreakerEditor();      break;
            case "openTrophyFishEditor":          JefConfig.openTrophyFishEditor();          break;
            case "openDungeonRoomOverlayEditor":  JefConfig.openDungeonRoomOverlayEditor();  break;

            // ── About: links ─────────────────────────────────────────────────
            case "openDiscord":  openUrl("https://discord.gg/tdMFbmhFTb");                           break;
            case "openGithub":   openUrl("https://github.com/hamlook/justenoughfakepixel");          break;

            // ── About: used software ─────────────────────────────────────────
            case "openLicenseForge":         openUrl("https://github.com/MinecraftForge/MinecraftForge");        break;
            case "openLicenseMixin":         openUrl("https://github.com/SpongePowered/Mixin/");                 break;
            case "openLicenseMoulConfig":    openUrl("https://github.com/NotEnoughUpdates/MoulConfig");          break;
            case "openLicenseLombok":        openUrl("https://projectlombok.org/");                              break;
            case "openLicenseReflections":   openUrl("https://github.com/ronmamo/reflections");                  break;
            case "openLicenseJavassist":     openUrl("https://github.com/jboss-javassist/javassist");            break;
            case "openLicenseJbAnnotations": openUrl("https://github.com/JetBrains/java-annotations");           break;
        }
    }

    private static void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ignored) {}
    }
}