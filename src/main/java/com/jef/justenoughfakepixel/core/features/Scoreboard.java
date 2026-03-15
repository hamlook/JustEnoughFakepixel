package com.jef.justenoughfakepixel.core.features;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;
import com.jef.justenoughfakepixel.core.config.utils.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scoreboard {

    @Expose
    @ConfigOption(name = "Custom Scoreboard", desc = "Settings for the custom scoreboard overlay")
    @ConfigEditorAccordion(id = 20)
    public boolean scoreboardAccordion = false;

    @Expose
    @ConfigOption(name = "Enable", desc = "Replace the vanilla sidebar with a custom scoreboard")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 20)
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the scoreboard")
    @ConfigEditorColour
    @ConfigAccordionId(id = 20)
    public String scoreboardBg = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the scoreboard corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 20f, minStep = 1f)
    @ConfigAccordionId(id = 20)
    public float cornerRadius = 8f;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the scoreboard")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 2.5f, minStep = 0.1f)
    @ConfigAccordionId(id = 20)
    public float scale = 1.0f;

    @Expose
    @ConfigOption(name = "Edit Position", desc = "Drag to reposition the scoreboard")
    @ConfigEditorButton(runnableId = "openScoreboardEditor", buttonText = "Edit")
    @ConfigAccordionId(id = 20)
    public boolean editPosDummy = false;

    @Expose
    @ConfigOption(name = "Scoreboard Lines", desc = "Choose which lines to show and drag to reorder. Lines not found on the scoreboard are hidden automatically.")
    @ConfigEditorDraggableList(exampleText = {
            "§e03/15/26 §8dh-1",           // 0  SERVER
            "§fLate Summer §b11th",             // 1  SEASON
            "§f10:40pm",                        // 2  TIME
            "§7♲ Ironman",                      // 11 PROFILE_TYPE
            "§b⏣ Hub",                          // 3  LOCATION
            "§fPurse: §652,763,737",            // 4  PURSE
            "§fBank: §6249M",                   // 5  BANK
            "§fBits: §b59,364",                 // 6  BITS
            "§fPower: §dSighted §8(1,863)",     // 7  POWER
            "§fFetchur: §eSand",                // 8  FETCHUR
            "§fSlayer Quest",                   // 9  SLAYER
            "§fGems: §a57,873",                 // 10 GEMS
            "§6Fishing Festival §f12m 30s",     // 12 EVENT
            "§6Cookie Buff: §f3d 17h",           // 13 COOKIE
            "§8─────────────────"              // 14 EMPTY LINE
    })
    @ConfigAccordionId(id = 20)
    public List<Integer> scoreboardLines =
            new ArrayList<>(Arrays.asList(0, 1, 2, 3, 14, 4, 5, 6, 14, 7, 8, 14, 9, 10, 11, 12, 13));

    @Expose
    public Position position = new Position(-2, 10);
}