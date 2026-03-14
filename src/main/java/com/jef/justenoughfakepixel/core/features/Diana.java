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
    @ConfigOption(name = "Overlay Background", desc = "Background color for Diana overlays (alpha controls opacity)")
    @ConfigEditorColour
    public String overlayBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    public int overlayCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Overlay Scale", desc = "Size of Diana overlays")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    public float overlayScale = 1f;

    @Expose
    @ConfigOption(name = "Show Event Overlay", desc = "Show the Diana Event HUD (playtime, borrows, mob rates)")
    @ConfigEditorBoolean
    public boolean showEventOverlay = true;

    @Expose
    @ConfigOption(name = "Show Loot Overlay", desc = "Show the Diana Loot HUD (chimeras, drops)")
    @ConfigEditorBoolean
    public boolean showLootOverlay = true;

    @Expose
    @ConfigOption(name = "Show Inquisitor HP", desc = "Show a live HP bar for the nearest Minos Inquisitor")
    @ConfigEditorBoolean
    public boolean showInqHealthOverlay = true;

    @Expose
    @ConfigOption(name = "Show Diana Mob HP", desc = "Show a live HP bar for the nearest non-Inquisitor Diana mob - only appears after you dig one out")
    @ConfigEditorBoolean
    public boolean showDianaMobHealthOverlay = true;

    @Expose
    @ConfigOption(name = "Edit Overlay Positions", desc = "Drag all Diana overlays to reposition them individually")
    @ConfigEditorButton(runnableId = "openDianaOverlayEditor", buttonText = "Edit")
    public boolean editOverlayPosDummy = false;

    // Individual positions for each overlay
    @Expose public Position eventOverlayPos  = new Position(4, 200);
    @Expose public Position lootOverlayPos   = new Position(4, 310);
    @Expose public Position inqHealthPos     = new Position(4, 400);
    @Expose public Position dianaMobHealthPos = new Position(4, 420);
}