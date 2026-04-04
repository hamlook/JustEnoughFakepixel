package com.jef.justenoughfakepixel.features.dungeons.caseopening;

import com.jef.justenoughfakepixel.DebugLogger;
import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.utils.StringUtils;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.features.dungeons.overlays.DungeonStats;
import com.jef.justenoughfakepixel.features.dungeons.utils.DungeonFloor;
import com.jef.justenoughfakepixel.utils.RomanNumeralParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

@RegisterEvents
public class ChestListener {

    public static GuiChest originalGui;

    private static WorldClient lastWorld = null;

    private boolean isCroesus = false;
    private boolean isCatacombsChestList = false;
    private int chestID = -1;

    private final Map<Integer, Boolean> crOpenedChestOb = new HashMap<>();
    private final Map<Integer, Boolean> crOpenedChestBr = new HashMap<>();
    private boolean openedChestOb = false;
    private boolean openedChestBr = false;

    private DungeonDropData.Floor curFloor;
    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII"};

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (JefConfig.feature == null || !JefConfig.feature.dungeons.caseOpeningAnimation) return;
        if (!(event.gui instanceof GuiChest)) return;

        originalGui = (GuiChest) event.gui;
        if (!(originalGui.inventorySlots instanceof ContainerChest)) return;

        ContainerChest container = (ContainerChest) originalGui.inventorySlots;
        IInventory lower = container.getLowerChestInventory();
        if (!lower.hasCustomName()) return;

        String name = lower.getDisplayName().getUnformattedText();
        DebugLogger.log("[ChestListener] GUI opened: \"" + name + "\"");

        if (name.contains("Croesus")) {
            isCroesus = true;
            isCatacombsChestList = false;
            DebugLogger.log("[ChestListener] Croesus detected");
            return;
        }

        if (name.contains("Catacombs")) {
            if (!isCroesus) {
                DebugLogger.log("[ChestListener] Catacombs GUI seen but not in Croesus flow, ignoring");
                return;
            }
            isCatacombsChestList = true;

            boolean isMaster = name.contains("Master Mode") || name.contains("Master");
            DungeonDropData.Floor detected = null;
            for (String token : name.split("[\\s()]+")) {
                if (!RomanNumeralParser.isValid(token)) continue;
                try {
                    int num = RomanNumeralParser.parse(token);
                    if (num < 1 || num > 7) continue;
                    String key = (isMaster ? "M" : "") + ROMAN[num - 1];
                    detected = DungeonDropData.Floor.valueOf(key);
                    break;
                } catch (Exception ignored) {}
            }
            curFloor = detected;
            DebugLogger.log("[ChestListener] Catacombs chest list: floor=" + curFloor
                    + ", master=" + isMaster + ", raw name=\"" + name + "\"");
            return;
        }

        if (!name.endsWith(" Chest")) return;

        String materialName = name.substring(0, name.length() - " Chest".length()).trim();
        DungeonDropData.CaseMaterial parsedMaterial = null;
        try {
            parsedMaterial = DungeonDropData.CaseMaterial.valueOf(
                    materialName.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException ignored) {}

        if (parsedMaterial != DungeonDropData.CaseMaterial.OBSIDIAN
                && parsedMaterial != DungeonDropData.CaseMaterial.BEDROCK) {
            DebugLogger.log("[ChestListener] Chest \"" + name + "\" not Obsidian/Bedrock, skipping");
            return;
        }

        DungeonDropData.CaseMaterial curMaterial = parsedMaterial;
        DebugLogger.log("[ChestListener] Chest detected: material=" + curMaterial
                + ", isCroesus=" + isCroesus + ", curFloor=" + curFloor + ", chestID=" + chestID);

        if (isCroesus) {
            if (curMaterial == DungeonDropData.CaseMaterial.BEDROCK) {
                if (crOpenedChestBr.containsKey(chestID)) {
                    DebugLogger.log("[ChestListener] Already opened BR chest " + chestID + ", skipping");
                    return;
                }
                crOpenedChestBr.put(chestID, true);
            } else {
                if (crOpenedChestOb.containsKey(chestID)) {
                    DebugLogger.log("[ChestListener] Already opened OB chest " + chestID + ", skipping");
                    return;
                }
                crOpenedChestOb.put(chestID, true);
            }
        } else {
            if (curMaterial == DungeonDropData.CaseMaterial.BEDROCK) {
                if (openedChestBr) { DebugLogger.log("[ChestListener] Already opened BR, skipping"); return; }
                openedChestBr = true;
            } else {
                if (openedChestOb) { DebugLogger.log("[ChestListener] Already opened OB, skipping"); return; }
                openedChestOb = true;
            }
            DungeonStats stats = DungeonStats.getInstance();
            DungeonFloor df = stats != null ? stats.getCurrentFloor() : DungeonFloor.NONE;
            curFloor = DungeonDropData.Floor.fromDungeonFloor(df);
            DebugLogger.log("[ChestListener] In-dungeon chest — DungeonStats floor: " + df + " → " + curFloor);
        }

        if (curFloor == null) {
            DebugLogger.log("[ChestListener] ERROR: floor is null! Cannot start animation. "
                    + "Make sure you navigate through Croesus → chest list first.");
            return;
        }

        DebugLogger.log("[ChestListener] Intercepting chest → floor=" + curFloor + ", material=" + curMaterial);
        event.gui = new GuiInterceptChest(container, curFloor, curMaterial);
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
        if (!isCatacombsChestList) return;

        GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
        Slot hovered = chest.getSlotUnderMouse();
        if (hovered == null || !hovered.getHasStack()) return;

        String name = hovered.getStack().getDisplayName();

        name = StringUtils.cleanColour(name);
        name = StringUtils.clean(name);

        boolean isObChest = name.equalsIgnoreCase("Obsidian") && !crOpenedChestOb.containsKey(chestID);
        boolean isBrChest = name.equalsIgnoreCase("Bedrock") && !crOpenedChestBr.containsKey(chestID);
        if (!isObChest && !isBrChest) return;

        if (event.toolTip.size() > 3) {
            String first = event.toolTip.get(0);
            String last1 = event.toolTip.get(event.toolTip.size() - 1);
            String last2 = event.toolTip.get(event.toolTip.size() - 2);
            event.toolTip.clear();
            event.toolTip.add(first);
            event.toolTip.add("§7Hidden");
            event.toolTip.add(last2);
            event.toolTip.add(last1);
        }
    }

    @SubscribeEvent
    public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!(event.gui instanceof GuiChest)) return;
        Slot slot = ((GuiChest) event.gui).getSlotUnderMouse();
        if (slot != null && org.lwjgl.input.Mouse.getEventButtonState()
                && isCroesus && !isCatacombsChestList) {
            chestID = slot.slotNumber;
            DebugLogger.log("[ChestListener] Croesus slot clicked: chestID=" + chestID);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        WorldClient currentWorld = Minecraft.getMinecraft().theWorld;
        if (currentWorld != null && currentWorld != lastWorld) {
            lastWorld = currentWorld;
            isCroesus = false;
            isCatacombsChestList = false;
            chestID = -1;
            openedChestOb = false;
            openedChestBr = false;
            crOpenedChestOb.clear();
            crOpenedChestBr.clear();
        }
    }
}