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
            "\u00a7e03/15/26 \u00a78dh-1",              // 0  SERVER
            "\u00a7fLate Summer \u00a7b11th",            // 1  SEASON
            "\u00a7f10:40pm",                            // 2  TIME
            "\u00a77\u2672 Ironman",                     // 3  PROFILE_TYPE
            "\u32D6\u00a76 Hub",                         // 4  ISLAND
            "\u00a7b\u23E3 Village",                     // 5  LOCATION
            "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", // 6  EMPTY
            "\u00a7fPurse: \u00a7652,763,737",           // 7  PURSE
            "\u00a7fBank: \u00a76249M",                  // 8  BANK
            "\u00a7fBits: \u00a7b59,364",                // 9  BITS
            "\u00a7fGems: \u00a7a57,873",                // 10 GEMS
            "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", // 11 EMPTY
            "\u00a76Fishing Festival \u00a7f12m 30s",    // 12 EVENT
            "\u00a7dCookie Buff: \u00a7f3d 17h",         // 13 COOKIE
            "\u00a7fPower: \u00a7dSighted", // 14 POWER
            "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", // 15 EMPTY
            "\u00a7fFetchur: \u00a7eSand",               // 16 FETCHUR
            "\u00a7fSlayer Quest\n\u00a74Voidgloom Seraph IV\n\u00a77(1227/1,400) Combat XP",  // 17 SLAYER
            "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", // 18 EMPTY
            "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", // 19 EMPTY
            "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", // 20 EMPTY
            "\u00a78\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"  // 21 EMPTY
    })
    @ConfigAccordionId(id = 20)
    public List<Integer> scoreboardLines =
            new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17));

    @Expose
    public Position position = new Position(-2, 140);
}