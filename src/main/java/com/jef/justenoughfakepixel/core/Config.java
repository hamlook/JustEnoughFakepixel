package com.jef.justenoughfakepixel.core;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class Config {

    @Expose
    @Category(name = "General", desc = "General features")
    public final GeneralCategory general = new GeneralCategory();

    public static class GeneralCategory {

        @Expose
        @ConfigOption(name = "Damage Splashes", desc = "Settings for damage number nametags")
        @ConfigEditorAccordion(id = 0)
        public boolean damageSplashesAccordion = false;

        @Expose
        @ConfigOption(name = "Hide Crit Splashes", desc = "Hides crit damage nametags (\u2727 stars)")
        @ConfigEditorBoolean
        @ConfigAccordionId(id = 0)
        public boolean hideCritSplashes = false;

        @Expose
        @ConfigOption(name = "Hide Non-Crit Splashes", desc = "Hides gray and fire-aspect damage numbers")
        @ConfigEditorBoolean
        @ConfigAccordionId(id = 0)
        public boolean hideNonCritSplashes = false;

        @Expose
        @ConfigOption(name = "Roman Numerals", desc = "Converts Roman numerals to integers in tooltips and tab list")
        @ConfigEditorBoolean
        public boolean romanNumerals = true;

        @Expose
        @ConfigOption(name = "Prevent Cursor Reset", desc = "Prevents the mouse cursor from resetting when opening GUIs")
        @ConfigEditorBoolean
        public boolean preventCursorReset = true;

        @Expose
        @ConfigOption(name = "Skyblock ID", desc = "Shows the skyblock item ID at the bottom of item tooltips")
        @ConfigEditorBoolean
        public boolean showSkyblockId = true;

        @Expose
        @ConfigOption(name = "Disable Enchant Glint", desc = "Removes the enchantment glint effect")
        @ConfigEditorBoolean
        public boolean disableEnchantGlint = false;

        @Expose
        @ConfigOption(name = "Brewing helper", desc = "Highlights brewing stands when done brewing")
        @ConfigEditorBoolean
        public boolean colorBrewingStands = true;

    }

    @Expose
    @Category(name = "Misc", desc = "Misc features")
    public final miscCategory misc = new miscCategory();

    public static class miscCategory {

        @Expose
        @ConfigOption(name = "Performance HUD", desc = "Shows FPS, TPS and Ping on screen")
        @ConfigEditorAccordion(id = 3)
        public boolean performanceHudAccordion = false;

        @Expose
        @ConfigOption(name = "Enable", desc = "Show the performance HUD")
        @ConfigEditorBoolean
        @ConfigAccordionId(id = 3)
        public boolean performanceHud = false;

        @Expose
        @ConfigOption(name = "Show FPS", desc = "Show FPS counter")
        @ConfigEditorBoolean
        @ConfigAccordionId(id = 3)
        public boolean hudShowFps = true;

        @Expose
        @ConfigOption(name = "Show TPS", desc = "Show server TPS")
        @ConfigEditorBoolean
        @ConfigAccordionId(id = 3)
        public boolean hudShowTps = true;

        @Expose
        @ConfigOption(name = "Show Ping", desc = "Show current ping")
        @ConfigEditorBoolean
        @ConfigAccordionId(id = 3)
        public boolean hudShowPing = true;

        @Expose
        @ConfigOption(name = "Vertical", desc = "Stack entries vertically, otherwise horizontal")
        @ConfigEditorBoolean
        @ConfigAccordionId(id = 3)
        public boolean hudVertical = true;

        @Expose
        @ConfigOption(name = "Background", desc = "Draw a dark background behind the HUD")
        @ConfigEditorBoolean
        @ConfigAccordionId(id = 3)
        public boolean hudBackground = true;

        @Expose
        @ConfigOption(name = "Edit HUD Position", desc = "Drag the HUD to reposition it")
        @ConfigEditorButton(runnableId = "openHudEditor", buttonText = "Edit")
        @ConfigAccordionId(id = 3)
        public boolean editHudPosDummy = false;

        @Expose
        @ConfigOption(name = "HUD Scale", desc = "Size of the performance HUD")
        @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
        @ConfigAccordionId(id = 3)
        public float hudScale = 1f;

        @Expose
        public Position hudPos = new Position(2, 2);

        @Expose
        @ConfigOption(name = "Item Stack Tips", desc = "Shows enchant levels on books and floor numbers on Catacombs passes")
        @ConfigEditorBoolean
        public boolean itemStackTips = true;


        @Expose
        @ConfigOption(name = "Skill XP Display", desc = "Hold SHIFT on a skill item to see XP remaining to max level")
        @ConfigEditorBoolean
        public boolean skillXpDisplay = true;

        @Expose
        @ConfigOption(name = "Search Bar", desc = "Search bar settings")
        @ConfigEditorAccordion(id = 10)
        public boolean searchBarAccordion = false;

        @Expose
        @ConfigOption(name = "Enable", desc = "Shows a search bar in supported GUIs")
        @ConfigEditorBoolean
        @ConfigAccordionId(id = 10)
        public boolean searchBar = true;

        @Expose
        @ConfigOption(name = "Highlight Color", desc = "Color used to highlight matching items in the search results")
        @ConfigEditorColour
        @ConfigAccordionId(id = 10)
        public String searchBarHighlightColor = "0:102:255:0:0";

        @Expose
        @ConfigOption(name = "No Swap Animation", desc = "Removes the item lowering animation when switching hotbar slots")
        @ConfigEditorBoolean
        public boolean noItemSwitchAnimation = true;

        @Expose
        @ConfigOption(name = "Show Own Nametag", desc = "Shows your own nametag in third person")
        @ConfigEditorBoolean
        public boolean showOwnNametag = false;

        @Expose
        @ConfigOption(name = "Disable Entity Fire", desc = "Hides the fire overlay rendered on burning entities")
        @ConfigEditorBoolean
        public boolean disableEntityFire = false;


    }

    @Expose
    @Category(name = "Waypoints", desc = "Waypoints config & GUI")
    public final WaypointsCategory waypoints = new WaypointsCategory();

    public static class WaypointsCategory {

        @Expose
        @ConfigOption(name = "Manage Waypoints", desc = "Open waypoint manager")
        @ConfigEditorButton(runnableId = "openWaypointGroupGui", buttonText = "Open")
        public boolean manageGroupsDummy = false;

        @Expose
        @ConfigOption(name = "Manager Key", desc = "Keybind to open the waypoint manager")
        @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
        public int waypointManagerKey = Keyboard.KEY_NONE;

        @Expose
        @ConfigOption(name = "Colors", desc = "Waypoint rendering colors")
        @ConfigEditorAccordion(id = 1)
        public boolean colorsAccordion = false;

        @Expose
        @ConfigOption(name = "Box Colour", desc = "Colour of the ESP box drawn around the next waypoint")
        @ConfigEditorColour
        @ConfigAccordionId(id = 1)
        public String boxColour = "0:217:255:255:0";

        @Expose
        @ConfigOption(name = "Tracer Colour", desc = "Colour of the tracer from your position to the next waypoint")
        @ConfigEditorColour
        @ConfigAccordionId(id = 1)
        public String tracerColour = "0:255:255:255:0";

        @Expose
        @ConfigOption(name = "Label Colour", desc = "Colour of the waypoint name / number above each waypoint")
        @ConfigEditorColour
        @ConfigAccordionId(id = 1)
        public String labelColour = "0:255:255:255:255";

        @Expose
        @ConfigOption(name = "Distance Colour", desc = "Colour of the distance number shown next to each waypoint label")
        @ConfigEditorColour
        @ConfigAccordionId(id = 1)
        public String distanceLabelColour = "0:255:85:255:255";

        @Expose
        @ConfigOption(name = "Auto Advance", desc = "Settings for automatic waypoint progression")
        @ConfigEditorAccordion(id = 2)
        public boolean autoAdvanceAccordion = false;

        @Expose
        @ConfigOption(name = "Advance Range", desc = "How close (blocks) you must be to the next waypoint before the timer starts")
        @ConfigEditorSliderAnnotation(minValue = 1f, maxValue = 30f, minStep = 0.5f)
        @ConfigAccordionId(id = 2)
        public float advanceRange = 5.0f;

        @Expose
        @ConfigOption(name = "Advance Delay (ms)", desc = "How long (ms) you must stay within range before the waypoint auto-advances")
        @ConfigEditorSliderAnnotation(minValue = 250f, maxValue = 10000f, minStep = 250f)
        @ConfigAccordionId(id = 2)
        public float advanceDelayMs = 2000f;

    }

    @Expose
    @Category(name = "Mining", desc = "Mining features")
    public final MiningCategory mining = new MiningCategory();

    public static class MiningCategory {

        @Expose
        @ConfigOption(name = "Fetchur Helper", desc = "Prints what Fetchur wants in chat")
        @ConfigEditorBoolean
        public boolean fetchurHelper = true;

    }

    @Expose
    @Category(name = "Dungeons", desc = "Dungeon features")
    public final DungeonsCategory dungeons = new DungeonsCategory();

    public static class DungeonsCategory {

        @Expose
        @ConfigOption(name = "Blood Mob Highlight", desc = "Highlight blood room mobs")
        @ConfigEditorDropdown(values = {"Box", "Outline", "Off"})
        public int bloodMobHighlight = 2;

        @Expose
        @ConfigOption(name = "Dungeon Overlay", desc = "Shows run timers overlay and end-of-run stats in chat")
        @ConfigEditorBoolean
        public boolean dungeonStats = false;

        @Expose
        @ConfigOption(name = "Dungeon overlay Background", desc = "Draw a dark background behind the stats overlay")
        @ConfigEditorBoolean
        public boolean statsBackground = true;

        @Expose
        @ConfigOption(name = "Edit Dungeon overlay position", desc = "Drag the overlay to reposition it")
        @ConfigEditorButton(runnableId = "openStatsEditor", buttonText = "Edit")
        public boolean editStatsPosDummy = false;

        @Expose
        @ConfigOption(name = "Overlay Scale", desc = "Size of the dungeon stats overlay")
        @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
        public float statsScale = 1f;

        @Expose
        public Position statsPos = new Position(4, 100);

        @Expose
        public Map<String, Long> floorPbs = new HashMap<>();

        public long getPb(String key) {
            Long v = floorPbs.get(key);
            return v == null ? 0L : v;
        }

        public void setPb(String key, long ms) {
            floorPbs.put(key, ms);
        }

    }

    public void executeRunnable(String runnableId) {
        switch (runnableId) {
            case "openWaypointGroupGui":
                JefConfig.openWaypointGroupGui();
                break;
            case "openStatsEditor":
                JefConfig.openStatsEditor();
                break;
            case "openHudEditor":
                JefConfig.openHudEditor();
                break;
        }
    }
}
