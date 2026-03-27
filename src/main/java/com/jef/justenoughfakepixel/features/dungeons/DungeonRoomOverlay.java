package com.jef.justenoughfakepixel.features.dungeons;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.JefOverlay;
import com.jef.justenoughfakepixel.utils.ScoreboardUtils;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

@RegisterEvents
public class DungeonRoomOverlay extends JefOverlay {

    private static DungeonRoomOverlay instance;
    public static String currentRoomName = null;
    public static String currentRoomNotes = null;

    public DungeonRoomOverlay() {
        super(100, 20);
        instance = this;
    }

    public static DungeonRoomOverlay getInstance() { return instance; }

    @Override public Position getPosition()     { return JefConfig.feature.dungeons.dungeonRoomOverlayPos; }
    @Override public float    getScale()        { return JefConfig.feature.dungeons.dungeonRoomOverlayScale; }
    @Override public int      getBgColor()      { return ChromaColour.specialToChromaRGB(JefConfig.feature.dungeons.dungeonRoomOverlayBgColor); }
    @Override public int      getCornerRadius() { return JefConfig.feature.dungeons.dungeonRoomOverlayCornerRadius; }

    @Override
    protected boolean isEnabled() {
        return JefConfig.feature.dungeons.dungeonRoomOverlay;
    }

    @Override
    protected boolean extraGuard() {
        return ScoreboardUtils.getCurrentLocation() == ScoreboardUtils.Location.DUNGEON
                && !DungeonStats.isInBossFight();
    }

    @Override
    public List<String> getLines(boolean preview) {
        List<String> out = new ArrayList<>();

        if (preview) {
            out.add(EnumChatFormatting.AQUA + "Room: " + EnumChatFormatting.GREEN + "Puzzle" + EnumChatFormatting.WHITE + " - " + EnumChatFormatting.GREEN + "Box" + EnumChatFormatting.WHITE + " [" + EnumChatFormatting.YELLOW + "3 secrets" + EnumChatFormatting.WHITE + "]");
            out.add(EnumChatFormatting.YELLOW + "Example note about this room");
            return out;
        }

        if (currentRoomName != null) {
            out.add(EnumChatFormatting.AQUA + "Room: " + EnumChatFormatting.GREEN + currentRoomName);
            if (currentRoomNotes != null) {
                out.add(EnumChatFormatting.YELLOW + currentRoomNotes);
            }
        }

        return out;
    }
}