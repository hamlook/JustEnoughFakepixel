package com.jef.justenoughfakepixel.core.features;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;
import com.jef.justenoughfakepixel.core.config.utils.Position;

public class Diana {

    @Expose
    @ConfigOption(name = "Diana Tracker", desc = "Enables tracking")
    @ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Edit Overlay Positions", desc = "Drag all Diana overlays to reposition them individually")
    @ConfigEditorButton(runnableId = "openDianaOverlayEditor", buttonText = "Edit")
    public boolean editOverlayPosDummy = false;

    @Expose
    @ConfigOption(name = "Event Overlay", desc = "Diana Event HUD – playtime, burrows, and mob rates")
    @ConfigEditorAccordion(id = 1)
    public boolean eventAccordion = false;

    @Expose
    @ConfigOption(name = "Show Event Overlay", desc = "Show the Diana Event HUD")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 1)
    public boolean showEventOverlay = true;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the Event overlay (alpha controls opacity)")
    @ConfigEditorColour
    @ConfigAccordionId(id = 1)
    public String eventBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the Event overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 1)
    public int eventCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the Event overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    @ConfigAccordionId(id = 1)
    public float eventScale = 1f;

    @Expose
    @ConfigOption(name = "Loot Overlay", desc = "Diana Loot HUD – chimeras, rare drops, and coins")
    @ConfigEditorAccordion(id = 2)
    public boolean lootAccordion = false;

    @Expose
    @ConfigOption(name = "Show Loot Overlay", desc = "Show the Diana Loot HUD")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 2)
    public boolean showLootOverlay = true;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the Loot overlay (alpha controls opacity)")
    @ConfigEditorColour
    @ConfigAccordionId(id = 2)
    public String lootBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the Loot overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 2)
    public int lootCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the Loot overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    @ConfigAccordionId(id = 2)
    public float lootScale = 1f;

    @Expose
    @ConfigOption(name = "Inquisitor HP Overlay", desc = "Live HP bar for the nearest Minos Inquisitor")
    @ConfigEditorAccordion(id = 3)
    public boolean inqAccordion = false;

    @Expose
    @ConfigOption(name = "Show Inquisitor HP", desc = "Show a live HP bar for the nearest Minos Inquisitor")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 3)
    public boolean showInqHealthOverlay = true;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the Inquisitor HP overlay (alpha controls opacity)")
    @ConfigEditorColour
    @ConfigAccordionId(id = 3)
    public String inqBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the Inquisitor HP overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 3)
    public int inqCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the Inquisitor HP overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    @ConfigAccordionId(id = 3)
    public float inqScale = 1f;

    @Expose
    @ConfigOption(name = "Diana Mob HP Overlay", desc = "Live HP bar for the nearest non-Inquisitor Diana mob")
    @ConfigEditorAccordion(id = 4)
    public boolean mobAccordion = false;

    @Expose
    @ConfigOption(name = "Show Diana Mob HP", desc = "Show a live HP bar for the nearest non-Inquisitor Diana mob – only appears after you dig one out")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 4)
    public boolean showDianaMobHealthOverlay = true;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the Diana Mob HP overlay (alpha controls opacity)")
    @ConfigEditorColour
    @ConfigAccordionId(id = 4)
    public String mobBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the Diana Mob HP overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 4)
    public int mobCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the Diana Mob HP overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    @ConfigAccordionId(id = 4)
    public float mobScale = 1f;

    // positions
    @Expose public Position eventOverlayPos   = new Position(4, 200);
    @Expose public Position lootOverlayPos    = new Position(4, 310);
    @Expose public Position inqHealthPos      = new Position(4, 400);
    @Expose public Position dianaMobHealthPos = new Position(4, 420);
}