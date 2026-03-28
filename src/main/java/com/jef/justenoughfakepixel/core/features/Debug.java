package com.jef.justenoughfakepixel.core.features;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;
import org.lwjgl.input.Keyboard;

public class Debug {

    @Expose
    @ConfigOption(name = "Scoreboard Debug", desc = "Debug tools for the scoreboard")
    @ConfigEditorAccordion(id = 44)
    public boolean scoreboardDebugAccordion = false;

    @Expose
    @ConfigOption(name = "Enable", desc = "Enable scoreboard debug mode (allows the debug key to print scoreboard JSON to chat)")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 44)
    public boolean scoreboardDebug = false;

    @Expose
    @ConfigOption(name = "Debug Key", desc = "Print scoreboard JSON to chat (only works when Scoreboard Debug is enabled)")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    @ConfigAccordionId(id = 44)
    public int scoreboardDebugKey = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Room Overlay: Show Hash", desc = "Show room hash in the dungeon room overlay when the room is not detected")
    @ConfigEditorBoolean
    public boolean dungeonRoomDebug = false;
}