package com.jef.justenoughfakepixel.core.features;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;

public class Storage {

    @Expose
    @ConfigOption(name = "Enable/Disable Overlay",desc = "Whether to use the custom storage overlay or the default miencraft one.")
    @ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigOption(name = "Scrolling", desc = "Control how the scrolling feels while in the storage overlay")
    @ConfigEditorAccordion(id = 1)
    public boolean scrollAccordian = false;

    @Expose
    @ConfigOption(name = "Scroll Speed", desc = "Control how fast the scrolling in the storage overlay is")
    @ConfigEditorSliderAnnotation(minValue = 0.1f, maxValue = 3f, minStep = 0.01f)
    @ConfigAccordionId(id = 1)
    public float scrollSpeed = 1;

    @Expose
    @ConfigOption(name = "Smooth Scroll", desc = "Enable Smooth scrolling in the storage overlay")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 1)
    public boolean smoothScroll = false;

    @Expose
    @ConfigOption(name = "Horizontal Scrolling", desc = "Make the Scrolling system be horizontal instead of vertical")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 1)
    public boolean horizontalScroll = false;

    @Expose
    @ConfigOption(name = "Drag Scrolling", desc = "Scroll by dragging your mouse")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 1)
    public boolean dragScroll = true;

    @Expose
    @ConfigOption(name = "Scrolling Bar",desc = "Scroll by using a scrolling bar")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 1)
    public boolean barScroll = true;
}
