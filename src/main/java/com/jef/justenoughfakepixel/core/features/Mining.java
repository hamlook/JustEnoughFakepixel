package com.jef.justenoughfakepixel.core.features;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mining {

    @Expose
    @ConfigOption(name = "Fetchur Overlay", desc = "Settings for the Fetchur item overlay")
    @ConfigEditorAccordion(id = 20)
    public boolean fetchurAccordion = false;

    @Expose
    @ConfigOption(name = "Show Fetchur Overlay", desc = "Shows today's Fetchur item on screen while in Skyblock")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 20)
    public boolean showFetchurOverlay = true;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the overlay (alpha controls opacity)")
    @ConfigEditorColour
    @ConfigAccordionId(id = 20)
    public String overlayBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 20)
    public int overlayCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Overlay Scale", desc = "Size of the Fetchur overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    @ConfigAccordionId(id = 20)
    public float fetchurOverlayScale = 1f;

    @Expose
    @ConfigOption(name = "Edit Overlay Position", desc = "Drag to reposition the Fetchur overlay")
    @ConfigEditorButton(runnableId = "openFetchurEditor", buttonText = "Edit")
    @ConfigAccordionId(id = 20)
    public boolean editFetchurPosDummy = false;

    @Expose
    public Position fetchurOverlayPos = new Position(4, 4);

    @Expose
    @ConfigOption(name = "Gemstone Powder Tracker", desc = "Tracks gemstone powder and chest drops in Crystal Hollows")
    @ConfigEditorAccordion(id = 21)
    public boolean powderTrackerAccordion = false;

    @Expose
    @ConfigOption(name = "Enable", desc = "Show the Gemstone Powder Tracker overlay while in Crystal Hollows")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 21)
    public boolean powderTracker = true;

    @Expose
    @ConfigOption(name = "Toggle Key", desc = "Keybind to pause/resume the powder tracker")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    @ConfigAccordionId(id = 21)
    public int powderToggleKey = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the powder tracker overlay")
    @ConfigEditorColour
    @ConfigAccordionId(id = 21)
    public String powderBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the powder tracker overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 21)
    public int powderCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the powder tracker overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    @ConfigAccordionId(id = 21)
    public float powderOverlayScale = 1f;

    @Expose
    @ConfigOption(name = "Edit Position", desc = "Drag to reposition the powder tracker overlay")
    @ConfigEditorButton(runnableId = "openPowderEditor", buttonText = "Edit")
    @ConfigAccordionId(id = 21)
    public boolean editPowderPosDummy = false;

    @Expose
    @ConfigOption(name = "Reset Tracker", desc = "Wipe all tracked powder and drop data")
    @ConfigEditorButton(runnableId = "resetPowderTracker", buttonText = "Reset")
    @ConfigAccordionId(id = 21)
    public boolean resetPowderDummy = false;

    @Expose
    @ConfigOption(name = "Display Lines", desc = "Choose which lines to show and drag to reorder")
    @ConfigEditorDraggableList(exampleText = {
            "\u00a7b\u00a7lGemstone Powder Tracker",                    // 0
            "\u00a7d420 Chests \u00a77(120/h)",                         // 1
            "\u00a7b2x Powder: \u00a7aActive!",                         // 2
            "\u00a7b1,337 Gemstone Powder \u00a77(2.5K/h)",             // 3
            "\u00a7b12 Diamond Essence",                                 // 4
            "\u00a766 Gold Essence",                                     // 5
            "\u00a788 Oil Barrels",                                      // 6
            "\u00a753 Ascension Ropes",                                  // 7
            "\u00a792 Wishing Compasses",                                // 8
            "\u00a721 Jungle Hearts",                                    // 9
            "\u00a77512 Hard Stone \u00a78(5 compact) \u00a77(1.5K/h)", // 10
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a7cRuby",         // 11
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a7bSapphire",     // 12
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a76Amber",        // 13
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a75Amethyst",     // 14
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a7aJade",         // 15
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a7eTopaz",        // 16
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a7cJasper",       // 17
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a7fOpal",         // 18
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a76Citrine",      // 19
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a73Aquamarine",   // 20
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a7aPeridot",      // 21
            "\u00a751-\u00a793-\u00a7a4-\u00a7f0 \u00a78Onyx",         // 22
            "\u00a733-\u00a7c2-\u00a7e1-\u00a7a1-\u00a791 \u00a7fGoblin Eggs" // 23
    })
    @ConfigAccordionId(id = 21)
    public List<Integer> powderDisplayLines = new ArrayList<>(
            Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));

    @Expose
    public Position powderOverlayPos = new Position(4, 60);

    @Expose
    @ConfigOption(name = "HOTM Powder Display", desc = "Powder cost info on HOTM perk tooltips")
    @ConfigEditorAccordion(id = 22)
    public boolean hotmPowderAccordion = false;

    @Expose
    @ConfigOption(name = "Powder Spent", desc = "Show total powder invested vs max cost on each HOTM perk tooltip")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 22)
    public boolean hotmPowderSpent = true;

    @Expose
    @ConfigOption(name = "Powder Spent Format", desc = "How to display the powder spent amount")
    @ConfigEditorDropdown(values = {"Number", "Percentage", "Number and Percentage"})
    @ConfigAccordionId(id = 22)
    public int hotmPowderSpentDesign = 0;

    @Expose
    @ConfigOption(name = "Powder for Next 10 Levels", desc = "Hold Shift on a perk to see powder cost for the next 10 upgrades")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 22)
    public boolean hotmPowderFor10Levels = true;
}