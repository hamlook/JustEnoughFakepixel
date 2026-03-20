package com.jef.justenoughfakepixel.core.features;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;

public class Debug {

    @Expose
    @ConfigOption(name = "Scoreboard Debug Key", desc = "Print scoreboard JSON to chat")
    @ConfigEditorKeybind(defaultKey = org.lwjgl.input.Keyboard.KEY_K)
    public int scoreboardDebugKey = org.lwjgl.input.Keyboard.KEY_K;
}