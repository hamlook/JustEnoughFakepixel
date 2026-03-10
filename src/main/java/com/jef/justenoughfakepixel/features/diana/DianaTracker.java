package com.jef.justenoughfakepixel.features.diana;

import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DianaTracker {

    // "You dug out a Griffin Borrow! (2/4)" — note: game says "Borrow" not "Burrow"
    private static final Pattern BURROW_DIG =
            Pattern.compile("You dug out a Griffin Borrow! \\(([1-4])/4\\)");

    // "Uh oh! You dug out Minos Inquisitor"
    private static final Pattern MOB_SPAWN =
            Pattern.compile("Uh oh! You dug out (.+)");

    // Rare mob drops
    private static final Pattern RARE_STICK    = Pattern.compile("RARE DROP! Daedalus Stick");
    private static final Pattern RARE_RELIC    = Pattern.compile("RARE DROP! Minos Relic");
    private static final Pattern RARE_CHIMERA  = Pattern.compile("RARE DROP! Chimera [IVX]+");

    // Rare mob drops
    private static final Pattern RARE_SHELMET  = Pattern.compile("RARE DROP! Dwarf Turtle Shelmet");
    private static final Pattern RARE_REMEDIES = Pattern.compile("RARE DROP! Antique Remedies");
    private static final Pattern RARE_PLUSHIE  = Pattern.compile("RARE DROP! Crochet Tiger Plushie");

    // Burrow treasure drops
    private static final Pattern DROP_FEATHER  = Pattern.compile("RARE DROP! You dug out a Griffin Feather");
    private static final Pattern DROP_SOUVENIR = Pattern.compile("RARE DROP! You dug out a Washed-up Souvenir");
    private static final Pattern DROP_CROWN    = Pattern.compile("RARE DROP! You dug out a Crown of Greed");
    private static final Pattern DROP_COINS    = Pattern.compile("RARE DROP! You dug out ([\\d,]+) Coins");
    private static final Pattern GRIFFIN_DOUBLED = Pattern.compile("Your Griffin doubled your rewards?!");

    // Party chat
    private static final Pattern PARTY_MSG =
            Pattern.compile("^Party > (?:\\[[^]]*])?\\s*\\w{1,16}:\\s*(.+)$");


    private static final int IDLE_TICKS = 20 * 20;   // 20 seconds × 20 ticks/s

    private double lastX = Double.MAX_VALUE;
    private double lastY = Double.MAX_VALUE;
    private double lastZ = Double.MAX_VALUE;
    private int    idleTick = 0;

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null) return;

        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;

        if (x != lastX || y != lastY || z != lastZ) {
            // Player moved — reset idle counter and unpause
            lastX = x; lastY = y; lastZ = z;
            idleTick = 0;
            DianaStats.getInstance().idlePaused = false;
        } else {
            idleTick++;
            if (idleTick >= IDLE_TICKS) {
                DianaStats.getInstance().idlePaused = true;
            }
        }
    }


    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (mc.thePlayer == null) return;
        String msg = StringUtils.stripControlCodes(event.message.getUnformattedText());
        DianaStats stats = DianaStats.getInstance();
        handleBurrowDrops(msg, stats);
        handleRareMobDrops(msg, stats);

        if (!stats.isTracking()) return;

        handleBurrowDig(msg, stats);
        handleMobSpawn(msg, stats);
    }


    private void handleBurrowDig(String msg, DianaStats stats) {
        if (!BURROW_DIG.matcher(msg).find()) return;
        DianaData d = stats.getData();
        d.totalBurrows++;
        if (d.sessionStartMs < 0) d.sessionStartMs = System.currentTimeMillis();
        stats.save();
    }


    private void handleMobSpawn(String msg, DianaStats stats) {
        Matcher m = MOB_SPAWN.matcher(msg);
        if (!m.find()) return;

        String mobName = m.group(1).trim();
        DianaData d = stats.getData();
        d.totalMobs++;

        switch (mobName) {
            case "Minos Inquisitor":
                d.mobsSinceInq = 0;
                d.inqsSinceChimera++;
                d.totalInqs++;
                break;
            case "Minotaur":
                d.mobsSinceInq++;
                d.minotaursSinceStick++;
                d.totalMinotaurs++;
                break;
            case "Minos Champion":
                d.mobsSinceInq++;
                d.champsSinceRelic++;
                d.totalChamps++;
                break;
            default:
                d.mobsSinceInq++;
                break;
        }
        stats.save();
    }

    private void handleRareMobDrops(String msg, DianaStats stats) {
        DianaData d = stats.getData();
        boolean changed = false;

        if (RARE_STICK.matcher(msg).find())    { d.minotaursSinceStick = 0;    changed = true; }
        if (RARE_RELIC.matcher(msg).find())    { d.champsSinceRelic = 0;       changed = true; }
        if (RARE_CHIMERA.matcher(msg).find())  { d.inqsSinceChimera = 0;       changed = true; }
        if (RARE_SHELMET.matcher(msg).find())  { d.dwarfTurtleShelmets++;      changed = true; }
        if (RARE_REMEDIES.matcher(msg).find()) { d.antiqueRemedies++;           changed = true; }
        if (RARE_PLUSHIE.matcher(msg).find())  { d.crochetTigerPlushies++;     changed = true; }

        if (changed) stats.save();
    }


    private void handleBurrowDrops(String msg, DianaStats stats) {
        DianaData d = stats.getData();

        // Griffin doubled
        if (GRIFFIN_DOUBLED.matcher(msg).find()) {
            applyDoubledReward(stats);
            return;
        }

        // Detect a new drop.
        if (DROP_FEATHER.matcher(msg).find()) {
            d.griffinFeathers++;
            stats.lastDropType   = "feather";
            stats.lastDropAmount = 1L;
            stats.save();

        } else if (DROP_SOUVENIR.matcher(msg).find()) {
            d.souvenirs++;
            stats.lastDropType   = "souvenir";
            stats.lastDropAmount = 1L;
            stats.save();

        } else if (DROP_CROWN.matcher(msg).find()) {
            d.crownsOfGreed++;
            stats.lastDropType   = "crown";
            stats.lastDropAmount = 1L;
            stats.save();

        } else {
            Matcher coins = DROP_COINS.matcher(msg);
            if (coins.find()) {
                long amount = parseLong(coins.group(1));
                d.totalCoins += amount;
                stats.lastDropType   = "coins";
                stats.lastDropAmount = amount;
                stats.save();
            }
        }
    }

    private void applyDoubledReward(DianaStats stats) {
        if (stats.lastDropType == null) return;
        DianaData d = stats.getData();
        switch (stats.lastDropType) {
            case "feather":   d.griffinFeathers++;                   break;
            case "souvenir":  d.souvenirs++;                         break;
            case "crown":     d.crownsOfGreed++;                     break;
            case "coins":     d.totalCoins += stats.lastDropAmount;  break;
        }
        // Clear so a second doubled message doesn't double again
        stats.lastDropType   = null;
        stats.lastDropAmount = 0L;
        stats.save();
    }


    public static String getBphMessage() {
        DianaStats s = DianaStats.getInstance();
        DianaData  d = s.getData();
        return String.format("[Diana] BPH: %.1f  (%d burrows total)", s.getBph(), d.totalBurrows);
    }

    public static String getInqMessage() {
        DianaStats s = DianaStats.getInstance();
        DianaData  d = s.getData();
        double chance = s.getInqChance();
        String chanceStr = chance < 0 ? "N/A" : String.format("%.2f%%", chance);
        return String.format("[Diana] Mobs since Inq: %d | Inqs since Chimera: %d | Inq rate: %s",
                d.mobsSinceInq, d.inqsSinceChimera, chanceStr);
    }

    public static String getStickMessage() {
        return "[Diana] Minotaurs since Daedalus Stick: "
                + DianaStats.getInstance().getData().minotaursSinceStick;
    }

    public static String getRelicMessage() {
        return "[Diana] Champs since Minos Relic: "
                + DianaStats.getInstance().getData().champsSinceRelic;
    }

    public static String getDropsMessage() {
        DianaData d = DianaStats.getInstance().getData();
        return String.format(
                "[Diana] Feathers: %d | Souvenirs: %d | Crowns: %d | Shelmets: %d | Remedies: %d | Plushies: %d | Coins: %s",
                d.griffinFeathers, d.souvenirs, d.crownsOfGreed,
                d.dwarfTurtleShelmets, d.antiqueRemedies, d.crochetTigerPlushies,
                formatCoinsStatic(d.totalCoins));
    }

    public static String getHelpMessage() {
        return "\u00a73[Diana Party Commands]\u00a7r\n"
                + "\u00a7e!bph \u00a77- Your burrows per hour\n"
                + "\u00a7e!inq \u00a77- Mobs/inqs since last inq + spawn rate\n"
                + "\u00a7e!stick \u00a77- Minotaurs since Daedalus Stick\n"
                + "\u00a7e!relic \u00a77- Champs since Minos Relic\n"
                + "\u00a7e!drops \u00a77- All tracked burrow drops\n"
                + "\u00a7e!help \u00a77- This message";
    }


    private long parseLong(String s) {
        try { return Long.parseLong(s.replace(",", "")); }
        catch (NumberFormatException e) { return 0L; }
    }

    private static String formatCoinsStatic(long coins) {
        if (coins >= 1_000_000) return String.format("%.2fM", coins / 1_000_000.0);
        if (coins >= 1_000)     return String.format("%.1fk", coins / 1_000.0);
        return String.valueOf(coins);
    }
}