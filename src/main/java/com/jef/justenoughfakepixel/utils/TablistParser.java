package com.jef.justenoughfakepixel.utils;

import com.jef.justenoughfakepixel.utils.ColorUtils;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.jef.justenoughfakepixel.features.scoreboard.BankParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Comparator;
import java.util.List;

public class TablistParser {

    private static ScoreboardUtils.Location currentLocation = ScoreboardUtils.Location.NONE;

    public static ScoreboardUtils.Location getCurrentLocation() { return currentLocation; }

    private static net.minecraft.util.IChatComponent getTabFooter() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer == null) return null;
            java.lang.reflect.Field f = mc.ingameGUI.getTabList().getClass().getDeclaredField("field_175255_h");
            f.setAccessible(true);
            return (net.minecraft.util.IChatComponent) f.get(mc.ingameGUI.getTabList());
        } catch (Exception e) {
            return null;
        }
    }

    public static String readGems() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return null;
        GuiPlayerTabOverlay tab = mc.ingameGUI.getTabList();
        List<NetworkPlayerInfo> infos = PLAYER_ORDERING.sortedCopy(mc.thePlayer.sendQueue.getPlayerInfoMap());
        boolean inServer = false;
        for (NetworkPlayerInfo info : infos) {
            String raw = tab.getPlayerName(info);
            if (raw == null || raw.isEmpty()) continue;
            String line = net.minecraft.util.StringUtils.stripControlCodes(raw).trim();
            if (line.isEmpty()) continue;
            if (line.equals("Server Info") || raw.contains("Server Info")) { inServer = true; continue; }
            if (inServer && (line.equals("Account Info") || line.equals("Player Stats"))) { inServer = false; continue; }
            if (inServer && line.startsWith("Gems: ")) return line.substring("Gems: ".length()).trim();
        }
        return null;
    }

    public static String readCookieBuff() {
        net.minecraft.util.IChatComponent footer = getTabFooter();
        if (footer == null) return null;
        String[] lines = net.minecraft.util.StringUtils.stripControlCodes(footer.getFormattedText()).split("\n");
        boolean sawCookie = false;
        for (String line : lines) {
            String l = line.trim();
            if (l.isEmpty()) continue;
            if (!sawCookie && l.contains("Cookie Buff")) { sawCookie = true; continue; }
            if (sawCookie && l.contains("Active")) continue;
            if (sawCookie) return l;
        }
        return null;
    }

    private static final int TICK_INTERVAL = 20;
    private int tickCounter = 0;

    private static final Ordering<NetworkPlayerInfo> PLAYER_ORDERING =
            Ordering.from(new PlayerComparator());

    private static class PlayerComparator implements Comparator<NetworkPlayerInfo> {
        @Override
        public int compare(NetworkPlayerInfo o1, NetworkPlayerInfo o2) {
            ScorePlayerTeam t1 = o1.getPlayerTeam();
            ScorePlayerTeam t2 = o2.getPlayerTeam();
            return ComparisonChain.start()
                    .compareTrueFirst(
                            o1.getGameType() != WorldSettings.GameType.SPECTATOR,
                            o2.getGameType() != WorldSettings.GameType.SPECTATOR)
                    .compare(t1 != null ? t1.getRegisteredName() : "",
                            t2 != null ? t2.getRegisteredName() : "")
                    .compare(o1.getGameProfile().getName(), o2.getGameProfile().getName())
                    .result();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if ((tickCounter = (tickCounter + 1) % TICK_INTERVAL) != 0) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        parseTablist(mc);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        currentLocation = ScoreboardUtils.Location.NONE;
        BankParser.clear();
    }

    private static void parseTablist(Minecraft mc) {
        GuiPlayerTabOverlay tab = mc.ingameGUI.getTabList();
        List<NetworkPlayerInfo> infos =
                PLAYER_ORDERING.sortedCopy(mc.thePlayer.sendQueue.getPlayerInfoMap());

        boolean inServerSection  = false;
        boolean inAccountSection = false;

        for (NetworkPlayerInfo info : infos) {
            String raw = tab.getPlayerName(info);
            if (raw == null || raw.isEmpty()) continue;

            String line = net.minecraft.util.StringUtils.stripControlCodes(raw).trim();

            if (raw.contains("\u00A73\u00A7l Server Info\u00A7r")) {
                inServerSection  = true;
                inAccountSection = false;
                continue;
            }
            if (raw.contains("\u00A76\u00A7lAccount Info") || line.equals("Account Info")) {
                inAccountSection = true;
                inServerSection  = false;
                continue;
            }
            if (raw.contains("\u00A72\u00A7lPlayer Stats\u00A7r")
                    || line.equals("Player Stats") || line.equals("Quests")
                    || line.equals("Party")        || line.equals("Dungeon")) {
                inServerSection  = false;
                inAccountSection = false;
                continue;
            }

            if (line.isEmpty()) continue;

            if (inServerSection) {
                if (line.startsWith("Dungeon: ")) {
                    currentLocation = ScoreboardUtils.Location.DUNGEON;
                    inServerSection = false;
                    continue;
                }
                if (line.startsWith("Server: ")) {
                    String s = line.substring("Server: ".length()).trim();
                    int dash = indexOfDashDigits(s);
                    if (dash >= 0) s = s.substring(0, dash + 1);
                    currentLocation = matchLocation(s);
                    inServerSection = false;
                    continue;
                }
            }

            if (inAccountSection) {
                if (line.startsWith("Bank: ")) {
                    BankParser.setBank(parseAmount(raw, line.substring("Bank: ".length())));
                    continue;
                }
                if (line.startsWith("Purse: ") || line.startsWith("Piggy: ")) {
                    int colon = line.indexOf(": ");
                    String amt = stripColor(raw.substring(raw.indexOf(": ") + 2)).trim();
                    BankParser.setPurse(amt.isEmpty() ? line.substring(colon + 2) : amt);
                    continue;
                }
            }
        }
    }

    private static String parseAmount(String raw, String fallback) {
        String afterColon = raw.substring(raw.indexOf(": ") + 2);
        String clean = stripColor(afterColon).trim();
        if (clean.contains(" / ")) {
            String[] parts = clean.split(" / ", 2);
            return parts[0].trim() + " \u00A77/ \u00A76" + parts[1].trim();
        }
        return clean.isEmpty() ? fallback : clean;
    }

    private static ScoreboardUtils.Location matchLocation(String s) {
        for (ScoreboardUtils.Location loc : ScoreboardUtils.Location.values()) {
            if (loc.main.isEmpty()) continue;
            if (loc.main.equals(s) || loc.sandbox.equals(s) || loc.alpha.equals(s))
                return loc;
        }
        return ScoreboardUtils.Location.NONE;
    }

    private static int indexOfDashDigits(String s) {
        for (int i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) == '-' && Character.isDigit(s.charAt(i + 1)))
                return i;
        }
        return -1;
    }

    private static String stripColor(String s) { return ColorUtils.stripColor(s); }
}