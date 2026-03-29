package com.jef.justenoughfakepixel.core.features;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;
import com.jef.justenoughfakepixel.core.config.utils.Position;

import java.util.HashMap;
import java.util.Map;

public class Dungeons {
    

    @Expose
    @ConfigOption(name = "Blood Mob Highlight", desc = "Highlight blood room mobs. Box = bounding box, Outline = body glow, Off = disabled")
    @ConfigEditorDropdown(values = {"Box", "Outline", "Off"})
    public int bloodMobHighlight = 2;

    @Expose
    @ConfigOption(name = "Blood Mob Color", desc = "Color used for blood mob box and outline highlight")
    @ConfigEditorColour
    public String bloodMobColor = "200:255:50:50:255";


    @Expose
    @ConfigOption(name = "Boss Highlights", desc = "Highlight dungeon bosses and their minions")
    @ConfigEditorAccordion(id = 40)
    public boolean bossHighlightAccordion = false;

    @Expose
    @ConfigOption(name = "Bonzo Highlight", desc = "Highlight Bonzo (Floor 1 boss). Box = bounding box, Outline = body glow, Off = disabled")
    @ConfigEditorDropdown(values = {"Box", "Outline", "Off"})
    @ConfigAccordionId(id = 40)
    public int bonzoHighlight = 2;

    @Expose
    @ConfigOption(name = "Bonzo Color", desc = "Color used for Bonzo highlight")
    @ConfigEditorColour
    @ConfigAccordionId(id = 40)
    public String bonzoColor = "200:255:140:0:255";

    @Expose
    @ConfigOption(name = "Scarf Highlight", desc = "Highlight Scarf (Floor 2 boss). Box = bounding box, Outline = body glow, Off = disabled")
    @ConfigEditorDropdown(values = {"Box", "Outline", "Off"})
    @ConfigAccordionId(id = 40)
    public int scarfHighlight = 2;

    @Expose
    @ConfigOption(name = "Scarf Color", desc = "Color used for Scarf highlight")
    @ConfigEditorColour
    @ConfigAccordionId(id = 40)
    public String scarfColor = "200:180:0:255:255";

    @Expose
    @ConfigOption(name = "Scarf's Minions Highlight", desc = "Highlight Scarf's undead minions. Box = bounding box, Outline = body glow, Off = disabled")
    @ConfigEditorDropdown(values = {"Box", "Outline", "Off"})
    @ConfigAccordionId(id = 40)
    public int scarfMinionHighlight = 2;

    @Expose
    @ConfigOption(name = "Scarf's Minions Color", desc = "Color used for Scarf's minions highlight")
    @ConfigEditorColour
    @ConfigAccordionId(id = 40)
    public String scarfMinionColor = "150:180:0:200:255";

    @Expose
    @ConfigOption(name = "Professor Highlight", desc = "Highlight The Professor and his guardians (Floor 3 boss). Box = bounding box, Outline = body glow, Off = disabled")
    @ConfigEditorDropdown(values = {"Box", "Outline", "Off"})
    @ConfigAccordionId(id = 40)
    public int professorHighlight = 2;

    @Expose
    @ConfigOption(name = "Professor Color", desc = "Color used for The Professor and his guardians highlight")
    @ConfigEditorColour
    @ConfigAccordionId(id = 40)
    public String professorColor = "200:0:200:255:255";


    @Expose
    @ConfigOption(name = "Dungeon Overlay", desc = "Run timers, end-of-run stats and overlay settings")
    @ConfigEditorAccordion(id = 41)
    public boolean dungeonOverlayAccordion = false;

    @Expose
    @ConfigOption(name = "Enable", desc = "Shows run timers overlay and end-of-run stats in chat")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 41)
    public boolean dungeonStats = false;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the stats overlay (alpha controls opacity)")
    @ConfigEditorColour
    @ConfigAccordionId(id = 41)
    public String statsBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 41)
    public int statsCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Overlay Scale", desc = "Size of the dungeon stats overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    @ConfigAccordionId(id = 41)
    public float statsScale = 1f;

    @Expose
    @ConfigOption(name = "Edit Position", desc = "Drag the overlay to reposition it")
    @ConfigEditorButton(runnableId = "openStatsEditor", buttonText = "Edit")
    @ConfigAccordionId(id = 41)
    public boolean editStatsPosDummy = false;

    @Expose
    public Position statsPos = new Position(4, 100);


    @Expose
    @ConfigOption(name = "Dungeon Breaker Overlay", desc = "Shows Dungeon Breaker charges while in dungeons")
    @ConfigEditorAccordion(id = 42)
    public boolean dungeonBreakerAccordion = false;

    @Expose
    @ConfigOption(name = "Enable", desc = "Show the Dungeon Breaker charge overlay (only visible in dungeons with the item in hotbar)")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 42)
    public boolean dungeonBreakerOverlay = false;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the overlay (alpha controls opacity; 0 = fully transparent)")
    @ConfigEditorColour
    @ConfigAccordionId(id = 42)
    public String dungeonBreakerBgColor = "0:0:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 42)
    public int dungeonBreakerCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the Dungeon Breaker overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    @ConfigAccordionId(id = 42)
    public float dungeonBreakerScale = 1f;

    @Expose
    @ConfigOption(name = "Edit Position", desc = "Drag the overlay to reposition it")
    @ConfigEditorButton(runnableId = "openDungeonBreakerEditor", buttonText = "Edit")
    @ConfigAccordionId(id = 42)
    public boolean editDungeonBreakerPosDummy = false;

    @Expose
    public Position dungeonBreakerPos = new Position(4, 120);

    @Expose
    @ConfigOption(name = "Dungeon Room Overlay", desc = "Shows the name of your current dungeon room on screen")
    @ConfigEditorAccordion(id = 43)
    public boolean dungeonRoomOverlayAccordion = false;

    @Expose
    @ConfigOption(name = "Enable", desc = "Show current room name overlay while in dungeons")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 43)
    public boolean dungeonRoomOverlay = false;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the overlay")
    @ConfigEditorColour
    @ConfigAccordionId(id = 43)
    public String dungeonRoomOverlayBgColor = "0:0:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 43)
    public int dungeonRoomOverlayCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the room name overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    @ConfigAccordionId(id = 43)
    public float dungeonRoomOverlayScale = 1f;

    @Expose
    @ConfigOption(name = "Edit Position", desc = "Drag the overlay to reposition it")
    @ConfigEditorButton(runnableId = "openDungeonRoomOverlayEditor", buttonText = "Edit")
    @ConfigAccordionId(id = 43)
    public boolean editDungeonRoomOverlayPosDummy = false;

    @Expose
    public Position dungeonRoomOverlayPos = new Position(4, 140);

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