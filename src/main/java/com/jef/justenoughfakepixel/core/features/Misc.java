package com.jef.justenoughfakepixel.core.features;

import com.google.gson.annotations.Expose;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigAnnotations.*;
import com.jef.justenoughfakepixel.core.config.utils.Position;

public class Misc {

    @Expose
    @ConfigOption(name = "Performance HUD", desc = "Settings for the performance HUD")
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
    @ConfigOption(name = "Background Color", desc = "Background color of the HUD (alpha controls opacity)")
    @ConfigEditorColour
    @ConfigAccordionId(id = 3)
    public String hudBgColor = "0:136:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of HUD corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 3)
    public int hudCornerRadius = 4;

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
    @ConfigOption(name = "Search Bar", desc = "Search bar settings")
    @ConfigEditorAccordion(id = 10)
    public boolean searchBarAccordion = false;

    @Expose
    @ConfigOption(name = "Enable", desc = "Shows a search bar in supported GUIs")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 10)
    public boolean searchBar = true;

    @Expose
    @ConfigOption(name = "Highlight Color", desc = "Color used to highlight matching items in search results")
    @ConfigEditorColour
    @ConfigAccordionId(id = 10)
    public String searchBarHighlightColor = "0:102:255:0:0";

    @Expose
    @ConfigOption(name = "Edit Search Bar Position", desc = "Drag to reposition the search bar")
    @ConfigEditorButton(runnableId = "openSearchBarEditor", buttonText = "Edit")
    @ConfigAccordionId(id = 10)
    public boolean editSearchBarPosDummy = false;

    @Expose
    @ConfigOption(name = "Current Pet", desc = "Shows your active pet as a HUD overlay")
    @ConfigEditorAccordion(id = 11)
    public boolean currentPetAccordion = false;

    @Expose
    @ConfigOption(name = "Enable", desc = "Show the current pet overlay")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 11)
    public boolean showCurrentPet = true;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the pet overlay (alpha controls opacity)")
    @ConfigEditorColour
    @ConfigAccordionId(id = 11)
    public String currentPetBgColor = "0:0:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the pet overlay corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 11)
    public int currentPetCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the pet overlay")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    @ConfigAccordionId(id = 11)
    public float currentPetScale = 1.5f;

    @Expose
    @ConfigOption(name = "Edit Position", desc = "Drag to reposition the pet overlay")
    @ConfigEditorButton(runnableId = "openCurrentPetEditor", buttonText = "Edit")
    @ConfigAccordionId(id = 11)
    public boolean editCurrentPetPosDummy = false;

    @Expose
    @ConfigOption(name = "Item Pickup Log", desc = "Settings for the item pickup log")
    @ConfigEditorAccordion(id = 20)
    public boolean itemPickupLogAccordion = false;

    @Expose
    @ConfigOption(name = "Enable", desc = "Show a HUD list of recently picked-up or lost items")
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 20)
    public boolean itemPickupLog = true;

    @Expose
    @ConfigOption(name = "Background Color", desc = "Background color of the log (alpha controls opacity)")
    @ConfigEditorColour
    @ConfigAccordionId(id = 20)
    public String itemPickupLogBgColor = "160:0:0:0:0";

    @Expose
    @ConfigOption(name = "Corner Radius", desc = "Roundness of the log corners")
    @ConfigEditorSliderAnnotation(minValue = 0f, maxValue = 12f, minStep = 1f)
    @ConfigAccordionId(id = 20)
    public int itemPickupLogCornerRadius = 4;

    @Expose
    @ConfigOption(name = "Scale", desc = "Size of the item pickup log")
    @ConfigEditorSliderAnnotation(minValue = 0.5f, maxValue = 3f, minStep = 0.1f)
    @ConfigAccordionId(id = 20)
    public float itemPickupLogScale = 1f;

    @Expose
    @ConfigOption(name = "Edit Position", desc = "Drag to reposition the item pickup log")
    @ConfigEditorButton(runnableId = "openItemPickupLogEditor", buttonText = "Edit")
    @ConfigAccordionId(id = 20)
    public boolean editItemPickupLogPosDummy = false;

    @Expose
    public Position itemPickupLogPos = new Position(2, 60);

    @Expose
    public Position searchBarPos = new Position(4, 4);

    @Expose
    public Position hudPos = new Position(2, 2);

    @Expose
    public Position currentPetPos = new Position(18, 14);

    @Expose
    @ConfigOption(name = "Item Stack Tips", desc = "Shows enchant levels on books and floor numbers on Catacombs passes")
    @ConfigEditorBoolean
    public boolean itemStackTips = true;

    @Expose
    @ConfigOption(name = "Skill XP Display", desc = "Hold SHIFT on a skill item to see XP remaining to max level")
    @ConfigEditorBoolean
    public boolean skillXpDisplay = true;

    @Expose
    @ConfigOption(name = "No Swap Animation", desc = "Removes the item lowering animation when switching hotbar slots")
    @ConfigEditorBoolean
    public boolean noItemSwitchAnimation = true;

    @Expose
    @ConfigOption(name = "Show Own Nametag", desc = "Shows your own nametag in third person")
    @ConfigEditorBoolean
    public boolean showOwnNametag = true;

    @Expose
    @ConfigOption(name = "Disable Entity Fire", desc = "Hides the fire overlay rendered on burning entities")
    @ConfigEditorBoolean
    public boolean disableEntityFire = true;
}