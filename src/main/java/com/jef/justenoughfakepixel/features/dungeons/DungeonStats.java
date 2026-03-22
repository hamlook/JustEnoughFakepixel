package com.jef.justenoughfakepixel.features.dungeons;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.JefOverlay;
import com.jef.justenoughfakepixel.utils.ChatUtils;
import com.jef.justenoughfakepixel.utils.ScoreboardUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterEvents
public class DungeonStats extends JefOverlay {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static final int OVERLAY_WIDTH  = 160;
    public static final int OVERLAY_HEIGHT = 160;

    private static final int[][] BOSS_COORDS = {
            {29, 71, 80},
            {32, 69, 11},
            {41, 69, 57},
            {45, 69, 20},
            {45, 69,  9},
            {32, 69,  7},
            {85,221, 21},
    };

    private static final Pattern BLOOD_DOOR    = Pattern.compile("The BLOOD DOOR has been opened!");
    private static final Pattern RUN_FAILED    = Pattern.compile("Warning! This dungeon will close in 10s");
    private static final Pattern BOSS_SLAIN    = Pattern.compile("Defeated (.+) in \\d");
    private static final Pattern SCORE_LINE    = Pattern.compile("Team Score: (\\d+) \\((.{1,2})\\) *(\\(NEW RECORD!\\))?");
    private static final Pattern XP_LINE       = Pattern.compile("(\\+[\\d,.]+\\s?\\w+ Experience)(?:\\(.+\\))?");
    private static final Pattern FLOOR_PAT     = Pattern.compile("\\(([EFM][\\d])\\)");
    private static final Pattern TIME_ELAPSED  = Pattern.compile("Time Elapsed: (\\d+)");

    private static final Pattern MAXOR_START    = Pattern.compile("BOSS.*Maxor.*WELL WELL WELL");
    private static final Pattern MAXOR_END      = Pattern.compile("BOSS.*Maxor.*TOO YOUNG TO DIE");
    private static final Pattern STORM_START    = Pattern.compile("BOSS.*Storm.*Pathetic Maxor");
    private static final Pattern STORM_END      = Pattern.compile("BOSS.*Storm.*At least my son");
    private static final Pattern TERMINAL_START = Pattern.compile("BOSS.*Goldor.*Who dares trespass");
    private static final Pattern GOLDOR_FIGHT   = Pattern.compile("The Core entrance is opening");
    private static final Pattern GOLDOR_END     = Pattern.compile("BOSS.*Goldor.*You have done it");
    private static final Pattern NECRON_START   = Pattern.compile("BOSS.*Necron.*Finally, I heard so much");
    private static final Pattern NECRON_END     = Pattern.compile("BOSS.*Necron.*The Catacombs.*are no more");
    private static final Pattern WITHER_START   = Pattern.compile("BOSS.*(?:WITHER KING.*You\\.\\.\\. again|The Wither King.*Ohh)");
    private static final Pattern WITHER_END     = Pattern.compile("BOSS.*WITHER KING.*My strengths are depleting");

    private static final Pattern TERRA_START = Pattern.compile("BOSS.*Sadan.*I am the bridge between this realm");
    private static final Pattern TERRA_END   = Pattern.compile("BOSS.*Sadan.*ENOUGH!");
    private static final Pattern GIANTS_END  = Pattern.compile("BOSS.*Sadan.*I.m sorry but I need to concentrate");
    private static final Pattern SADAN_END   = Pattern.compile("BOSS.*Sadan.*FATHER, FORGIVE ME");

    private static final String C_FLOOR_NM = EnumChatFormatting.GREEN.toString();
    private static final String C_FLOOR_MM = EnumChatFormatting.RED.toString();
    private static final String C_DUNGEON  = EnumChatFormatting.YELLOW.toString();
    private static final String C_CLEAR    = EnumChatFormatting.GREEN.toString();
    private static final String C_BLOOD    = EnumChatFormatting.RED.toString();
    private static final String C_ENTRY    = EnumChatFormatting.GOLD.toString();
    private static final String C_BOSS     = EnumChatFormatting.DARK_RED.toString();
    private static final String C_MAXOR    = EnumChatFormatting.YELLOW.toString();
    private static final String C_STORM    = EnumChatFormatting.AQUA.toString();
    private static final String C_GOLDOR   = EnumChatFormatting.GOLD.toString();
    private static final String C_NECRON   = EnumChatFormatting.DARK_PURPLE.toString();
    private static final String C_WITHER   = EnumChatFormatting.DARK_GRAY.toString();
    private static final String C_TERRA    = EnumChatFormatting.GOLD.toString();
    private static final String C_GIANTS   = EnumChatFormatting.RED.toString();
    private static final String C_SADAN    = EnumChatFormatting.DARK_RED.toString();
    private static final String C_VAL      = EnumChatFormatting.WHITE.toString();
    private static final String C_PB       = EnumChatFormatting.DARK_GRAY.toString();
    private static final String C_NEWPB    = EnumChatFormatting.LIGHT_PURPLE.toString();

    private DungeonFloor currentFloor = DungeonFloor.NONE;
    private boolean inDungeon  = false;
    private boolean runFailed  = false;
    private boolean runEnded   = false;
    private long    runStart   = 0;

    private long clearedTime  = 0;
    private long bloodTime    = 0;
    private long bossTime     = 0;
    private long bossDeadTime = 0;

    private long maxorStart    = 0, maxorEnd    = 0;
    private long stormStart    = 0, stormEnd    = 0;
    private long terminalStart = 0, goldorFight = 0, goldorEnd = 0;
    private long necronStart   = 0, necronEnd   = 0;
    private long witherStart   = 0, witherEnd   = 0;

    private long terraStart  = 0, terraEnd  = 0;
    private long giantsStart = 0, giantsEnd = 0;
    private long sadanStart  = 0, sadanEnd  = 0;

    private int     lastClearedPct  = 0;
    private int     tickCounter     = 0;
    private final EndStats endStats = new EndStats();

    private static DungeonStats instance;

    public DungeonStats() {
        super(OVERLAY_WIDTH, OVERLAY_HEIGHT);
        instance = this;
    }

    public static DungeonStats getInstance() { return instance; }

    @Override public Position getPosition()     { return JefConfig.feature.dungeons.statsPos; }
    @Override public float    getScale()        { return JefConfig.feature.dungeons.statsScale; }
    @Override public int      getBgColor()      { return ChromaColour.specialToChromaRGB(JefConfig.feature.dungeons.statsBgColor); }
    @Override public int      getCornerRadius() { return JefConfig.feature.dungeons.statsCornerRadius; }
    @Override protected int   getBaseWidth()    { return 60; }
    @Override protected boolean extraGuard()    { return inDungeon && runStart != 0; }

    @Override
    protected boolean isEnabled() {
        return JefConfig.feature.dungeons.dungeonStats;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ++tickCounter % 10 != 0) return;
        if (JefConfig.feature == null || !JefConfig.feature.dungeons.dungeonStats) return;
        if (mc.thePlayer == null) return;

        if (!inDungeon) {
            for (String line : ScoreboardUtils.getCleanScoreboardLines()) {
                if (TIME_ELAPSED.matcher(line).find()) {
                    inDungeon = true;
                    runStart  = System.currentTimeMillis();
                    break;
                }
            }
            return;
        }

        if (runEnded) return;

        for (String line : ScoreboardUtils.getScoreboardLines()) {
            Matcher m = FLOOR_PAT.matcher(line);
            if (m.find()) { currentFloor = DungeonFloor.fromString(m.group(1)); break; }
        }

        if (clearedTime == 0 && bossTime == 0) {
            for (String line : ScoreboardUtils.getCleanScoreboardLines()) {
                if (!line.startsWith("Dungeon Cleared: ")) continue;
                try {
                    int pct = Integer.parseInt(line.replace("Dungeon Cleared: ", "").replace("%", "").trim());
                    if (pct == 100 && lastClearedPct < 100) clearedTime = elapsed();
                    lastClearedPct = pct;
                } catch (NumberFormatException ignored) {}
                break;
            }
        }

        if (bossTime == 0 && currentFloor != DungeonFloor.NONE) {
            int[] c = getBossCoords(currentFloor);
            if (c != null) {
                double dx = mc.thePlayer.posX - c[0];
                double dy = mc.thePlayer.posY - c[1];
                double dz = mc.thePlayer.posZ - c[2];
                if (dx*dx + dy*dy + dz*dz <= 30*30) {
                    bossTime = elapsed();
                    if (clearedTime == 0) clearedTime = bossTime;
                }
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!ChatUtils.isFromServer(event)) return;
        if (JefConfig.feature == null || !JefConfig.feature.dungeons.dungeonStats) return;

        String clean = ChatUtils.clean(event);

        if (!inDungeon) return;

        if (!runEnded) {
            if (bloodTime == 0 && BLOOD_DOOR.matcher(clean).find()) {
                bloodTime = elapsed();
                checkPhasePb(currentFloor.name() + "_blood", bloodTime, "Blood Rush");
                return;
            }

            if (RUN_FAILED.matcher(clean).find()) {
                runFailed    = true;
                runEnded     = true;
                bossDeadTime = elapsed();
                freezeOpenPhases();
                printEndStats();
                return;
            }

            if (currentFloor.isF6orM6()) {
                if (TERRA_START.matcher(clean).find()  && terraStart  == 0) terraStart  = elapsed();
                if (TERRA_END.matcher(clean).find()    && terraEnd    == 0) {
                    terraEnd    = elapsed();
                    giantsStart = terraEnd;
                    checkPhasePb(currentFloor.name() + "_terra", terraEnd - terraStart, "Terracotta");
                }
                if (GIANTS_END.matcher(clean).find()   && giantsEnd   == 0) {
                    giantsEnd  = elapsed();
                    sadanStart = giantsEnd;
                    checkPhasePb(currentFloor.name() + "_giants", giantsEnd - giantsStart, "Giants");
                }
                if (SADAN_END.matcher(clean).find()    && sadanEnd    == 0) {
                    sadanEnd          = elapsed();
                    bossDeadTime      = sadanEnd;
                    runEnded          = true;
                    endStats.bossName = "Sadan";
                    freezeOpenPhases();
                    checkPhasePb(currentFloor.name() + "_sadan", sadanEnd - sadanStart, "Sadan");
                    checkAndSaveRunPb();
                }
            }

            if (currentFloor.isF7orM7()) {
                if (MAXOR_START.matcher(clean).find()    && maxorStart    == 0) maxorStart    = elapsed();
                if (MAXOR_END.matcher(clean).find()      && maxorEnd      == 0) {
                    maxorEnd = elapsed();
                    checkPhasePb(currentFloor.name() + "_p1", maxorEnd - maxorStart, "P1 (Maxor)");
                }
                if (STORM_START.matcher(clean).find()    && stormStart    == 0) stormStart    = elapsed();
                if (STORM_END.matcher(clean).find()      && stormEnd      == 0) {
                    stormEnd = elapsed();
                    checkPhasePb(currentFloor.name() + "_p2", stormEnd - stormStart, "P2 (Storm)");
                }
                if (TERMINAL_START.matcher(clean).find() && terminalStart == 0) terminalStart = elapsed();
                if (GOLDOR_FIGHT.matcher(clean).find()   && goldorFight   == 0) goldorFight   = elapsed();
                if (GOLDOR_END.matcher(clean).find()     && goldorEnd     == 0) {
                    goldorEnd    = elapsed();
                    necronStart  = goldorEnd;
                    if (terminalStart > 0)
                        checkPhasePb(currentFloor.name() + "_p3", goldorEnd - terminalStart, "P3 (Terminals + Goldor)");
                }
                if (NECRON_START.matcher(clean).find()   && necronStart   == 0) necronStart   = elapsed();
                if (NECRON_END.matcher(clean).find()     && necronEnd     == 0) {
                    necronEnd = elapsed();
                    checkPhasePb(currentFloor.name() + "_p4", necronEnd - necronStart, "P4 (Necron)");
                    if (!currentFloor.isMasterMode()) {
                        bossDeadTime = necronEnd;
                        runEnded     = true;
                        checkAndSaveRunPb();
                    }
                }
                if (WITHER_START.matcher(clean).find()   && witherStart   == 0) witherStart   = elapsed();
                if (WITHER_END.matcher(clean).find()     && witherEnd     == 0) {
                    witherEnd    = elapsed();
                    bossDeadTime = witherEnd;
                    runEnded     = true;
                    checkPhasePb(currentFloor.name() + "_p5", witherEnd - witherStart, "P5 (Wither King)");
                    checkAndSaveRunPb();
                }
            }

            Matcher m = BOSS_SLAIN.matcher(clean);
            if (m.find()) {
                bossDeadTime      = elapsed();
                runEnded          = true;
                endStats.bossName = m.group(1).trim();
                freezeOpenPhases();
                checkAndSaveRunPb();
                printEndStats();
            }
        }

        Matcher m;
        m = SCORE_LINE.matcher(clean); if (m.find()) { endStats.score = m.group(1); endStats.grade = m.group(2); endStats.scorePB = m.group(3) != null && !m.group(3).isEmpty(); }
        m = XP_LINE.matcher(clean);    if (m.find()) endStats.xp.add(m.group(1).replace("Experience", "EXP").replace("Catacombs", "Cata"));
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) { reset(); }

    @Override
    public List<String> getLines(boolean preview) {
        DungeonStats s   = getInstance();
        boolean ended    = s != null && s.runEnded;
        long now         = ended ? (s.bossDeadTime > 0 ? s.bossDeadTime : s.elapsed()) : (s != null ? s.elapsed() : 0);
        DungeonFloor f   = s != null ? s.currentFloor : DungeonFloor.F7;
        boolean mm       = f.isMasterMode();
        String floorCol  = mm ? C_FLOOR_MM : C_FLOOR_NM;
        String floorName = f == DungeonFloor.NONE ? "?" : f.name();

        List<String> out = new ArrayList<>();

        out.add(floorCol + "Floor: " + C_VAL + (preview ? "F7" : floorName));
        out.add(C_DUNGEON + "Dungeon: " + C_VAL + (preview ? "4:32.100" : fmt(now)));

        long cleared = s != null ? s.clearedTime : 0;
        long blood   = s != null ? s.bloodTime   : 0;
        long boss    = s != null ? s.bossTime     : 0;
        long dead    = s != null ? s.bossDeadTime : 0;

        if (preview || now > 0)   out.add(line(C_CLEAR, "Clear",      cleared, now, preview, "3:10.500"));
        if (preview || now > 0)   out.add(line(C_BLOOD, "Blood Rush", blood,   now, preview, "3:45.200"));
        if (preview || blood > 0) out.add(line(C_ENTRY, "Boss Entry", boss,    now, preview, "3:55.100"));

        if (preview || boss > 0) {
            long bossDur = dead > 0 ? dead - boss : (boss > 0 ? now - boss : 0);
            if (dead > 0)
                out.add(C_BOSS + "Boss took: " + C_VAL + (preview ? "0:47.300" : fmt(dead - boss)));
            else if (boss > 0)
                out.add(C_BOSS + "Boss: " + C_VAL + fmt(bossDur));
        }

        if (preview || f.isF7orM7()) {
            addPhase(out, C_MAXOR,  "Maxor",      s != null ? s.maxorStart     : 0, s != null ? s.maxorEnd     : 0, now, preview, "0:18.000");
            addPhase(out, C_STORM,  "Storm",       s != null ? s.stormStart    : 0, s != null ? s.stormEnd     : 0, now, preview, "0:12.000");
            addPhase(out, C_GOLDOR, "Terminals",   s != null ? s.terminalStart : 0, s != null ? s.goldorFight  : 0, now, preview, "0:20.000");
            addPhase(out, C_GOLDOR, "Goldor",      s != null ? s.goldorFight   : 0, s != null ? s.goldorEnd    : 0, now, preview, "0:08.000");
            addPhase(out, C_NECRON, "Necron",      s != null ? s.necronStart   : 0, s != null ? s.necronEnd    : 0, now, preview, "0:05.000");
            if (preview || mm)
                addPhase(out, C_WITHER, "Wither King", s != null ? s.witherStart : 0, s != null ? s.witherEnd : 0, now, preview, "0:04.000");
        }

        if (preview || f.isF6orM6()) {
            addPhase(out, C_TERRA,  "Terracotta", s != null ? s.terraStart  : 0, s != null ? s.terraEnd  : 0, now, preview, "0:45.000");
            addPhase(out, C_GIANTS, "Giants",     s != null ? s.giantsStart : 0, s != null ? s.giantsEnd : 0, now, preview, "0:30.000");
            addPhase(out, C_SADAN,  "Sadan",      s != null ? s.sadanStart  : 0, s != null ? s.sadanEnd  : 0, now, preview, "0:20.000");
        }

        out.removeIf(l -> l == null);
        return out;
    }

    private static String line(String color, String label, long locked, long now, boolean preview, String previewVal) {
        if (preview)    return color + label + " took: " + C_VAL + previewVal;
        if (locked > 0) return color + label + " took: " + C_VAL + fmt(locked);
        return color + label + ": " + C_VAL + fmt(now);
    }

    private static void addPhase(List<String> out, String color, String name, long start, long end, long now, boolean preview, String previewVal) {
        if (preview) { out.add(color + name + " took: " + C_VAL + previewVal); return; }
        if (start == 0) return;
        if (end > 0)    out.add(color + name + " took: " + C_VAL + fmt(end - start));
        else            out.add(color + name + ": "      + C_VAL + fmt(now - start));
    }

    private void printEndStats() {
        String floor = currentFloor == DungeonFloor.NONE ? "?" : currentFloor.name();
        String sep   = EnumChatFormatting.DARK_GRAY + "————————————————————";
        String fc    = currentFloor.isMasterMode() ? C_FLOOR_MM : C_FLOOR_NM;

        send(sep);
        send(fc + EnumChatFormatting.BOLD + floor + (currentFloor.isMasterMode() ? " MM" : "")
                + (runFailed ? EnumChatFormatting.RED + " FAILED" : EnumChatFormatting.RESET + " End Stats"));

        if (endStats.bossName != null)
            send(EnumChatFormatting.RED + "☠ " + endStats.bossName);
        if (endStats.score != null)
            send(EnumChatFormatting.YELLOW + "Score: " + endStats.score + " (" + endStats.grade + ")"
                    + (endStats.scorePB ? EnumChatFormatting.LIGHT_PURPLE + " (PB!)" : ""));

        for (String xp : endStats.xp) send(EnumChatFormatting.DARK_AQUA + xp);

        if (clearedTime  > 0) send(C_CLEAR + "Clear took: "  + C_VAL + fmt(clearedTime)             + pbTag(floor + "_clear"));
        if (bloodTime    > 0) send(C_BLOOD + "Blood Rush: "  + C_VAL + fmt(bloodTime)               + pbTag(floor + "_blood"));
        if (bossTime     > 0) send(C_ENTRY + "Boss Entry: "  + C_VAL + fmt(bossTime)                + pbTag(floor + "_entry"));
        if (bossDeadTime > 0 && bossTime > 0)
            send(C_BOSS  + "Boss took: "   + C_VAL + fmt(bossDeadTime - bossTime) + pbTag(floor + "_boss"));

        if (currentFloor.isF6orM6()) {
            if (terraEnd  > 0) send(C_TERRA  + "Terracotta took: " + C_VAL + fmt(terraEnd  - terraStart)  + pbTag(floor + "_terra"));
            if (giantsEnd > 0) send(C_GIANTS + "Giants took: "     + C_VAL + fmt(giantsEnd - giantsStart) + pbTag(floor + "_giants"));
            if (sadanEnd  > 0) send(C_SADAN  + "Sadan took: "      + C_VAL + fmt(sadanEnd  - sadanStart)  + pbTag(floor + "_sadan"));
        }

        if (currentFloor.isF7orM7()) {
            if (maxorEnd      > 0) send(C_MAXOR  + "Maxor took: "               + C_VAL + fmt(maxorEnd     - maxorStart)    + pbTag(floor + "_p1"));
            if (stormEnd      > 0) send(C_STORM  + "Storm took: "               + C_VAL + fmt(stormEnd     - stormStart)    + pbTag(floor + "_p2"));
            if (goldorFight   > 0) send(C_GOLDOR + "Terminals took: "           + C_VAL + fmt(goldorFight  - terminalStart));
            if (goldorEnd     > 0) send(C_GOLDOR + "Goldor took: "              + C_VAL + fmt(goldorEnd    - goldorFight));
            if (terminalStart > 0 && goldorEnd > 0)
                send(C_GOLDOR + "P3 (Terminal+Goldor): "     + C_VAL + fmt(goldorEnd    - terminalStart)  + pbTag(floor + "_p3"));
            if (necronEnd     > 0) send(C_NECRON + "Necron took: "              + C_VAL + fmt(necronEnd    - necronStart)    + pbTag(floor + "_p4"));
            if (witherEnd     > 0) send(C_WITHER + "Wither King took: "         + C_VAL + fmt(witherEnd    - witherStart)    + pbTag(floor + "_p5"));
        }
        send(sep);
    }

    private void freezeOpenPhases() {
        long now = elapsed();
        if (currentFloor.isF6orM6()) {
            if (terraStart  > 0 && terraEnd  == 0) terraEnd  = now;
            if (giantsStart > 0 && giantsEnd == 0) giantsEnd = now;
            if (sadanStart  > 0 && sadanEnd  == 0) sadanEnd  = now;
        }
        if (currentFloor.isF7orM7()) {
            if (stormStart    > 0 && stormEnd    == 0) stormEnd    = now;
            if (terminalStart > 0 && goldorFight == 0) goldorFight = now;
            if (goldorFight   > 0 && goldorEnd   == 0) goldorEnd   = now;
            if (necronStart   > 0 && necronEnd   == 0) necronEnd   = now;
            if (witherStart   > 0 && witherEnd   == 0) witherEnd   = now;
        }
    }

    private void checkAndSaveRunPb() {
        if (JefConfig.feature == null || currentFloor == DungeonFloor.NONE || bossDeadTime == 0) return;
        savePbIfBetter(currentFloor.name() + "_boss",  bossDeadTime - bossTime, null);
        savePbIfBetter(currentFloor.name() + "_total", bossDeadTime, "Total");
    }

    private void checkPhasePb(String key, long duration, String phaseName) {
        if (JefConfig.feature == null || duration <= 0) return;
        long prev = JefConfig.feature.dungeons.getPb(key);
        if (prev == 0 || duration < prev) {
            JefConfig.feature.dungeons.setPb(key, duration);
            JefConfig.saveConfig();
            if (phaseName != null) announceNewPb(phaseName, duration);
        }
    }

    private void savePbIfBetter(String key, long duration, String phaseName) {
        checkPhasePb(key, duration, phaseName);
    }

    private void announceNewPb(String phase, long duration) {
        String msg = C_NEWPB + "NEW PB " + phase + ": " + C_VAL + fmt(duration);
        send(msg);
        scheduler.schedule(() -> {
            if (mc.thePlayer != null)
                mc.thePlayer.sendChatMessage("/pc NEW PB " + phase + ": " + fmt(duration));
        }, 1500, TimeUnit.MILLISECONDS);
    }

    private String pbTag(String key) {
        if (JefConfig.feature == null) return "";
        long p = JefConfig.feature.dungeons.getPb(key);
        return p > 0 ? C_PB + " (PB: " + fmt(p) + ")" : "";
    }

    public static String getFormattedPb(String arg1, String arg2) {
        if (JefConfig.feature == null) return "No data";

        DungeonFloor floor = DungeonFloor.fromString(arg1.toUpperCase());
        if (floor == DungeonFloor.NONE) return "Unknown floor: " + arg1;

        if (arg2 == null) {
            long pb = JefConfig.feature.dungeons.getPb(floor.name() + "_total");
            return pb > 0 ? floor.name() + " PB: " + fmt(pb) : floor.name() + ": No PB";
        }

        if (arg2.equalsIgnoreCase("br")) {
            long pb = JefConfig.feature.dungeons.getPb(floor.name() + "_blood");
            return pb > 0 ? floor.name() + " blood rush PB: " + fmt(pb) : floor.name() + " blood rush: No PB";
        }

        if (arg2.toLowerCase().startsWith("p")) {
            String phase = arg2.toLowerCase();
            String key   = floor.name() + "_" + phase;
            long pb = JefConfig.feature.dungeons.getPb(key);
            String label = phaseLabel(phase);
            return pb > 0 ? floor.name() + " " + label + ": " + fmt(pb) : floor.name() + " " + label + ": No PB";
        }

        return "Usage: !pb <floor> | !pb <floor> br | !pb <floor> p1-p5";
    }

    private static String phaseLabel(String phase) {
        switch (phase) {
            case "p1": return "P1 (Maxor)";
            case "p2": return "P2 (Storm)";
            case "p3": return "P3 (Terminals+Goldor)";
            case "p4": return "P4 (Necron)";
            case "p5": return "P5 (Wither King)";
            default:   return phase.toUpperCase();
        }
    }

    private void reset() {
        inDungeon = false; runFailed = false; runEnded = false;
        currentFloor = DungeonFloor.NONE; runStart = 0;
        clearedTime = bloodTime = bossTime = bossDeadTime = 0;
        maxorStart = maxorEnd = stormStart = stormEnd = 0;
        terminalStart = goldorFight = goldorEnd = necronStart = necronEnd = 0;
        witherStart = witherEnd = 0;
        terraStart = terraEnd = giantsStart = giantsEnd = sadanStart = sadanEnd = 0;
        lastClearedPct = 0; endStats.reset();
    }

    private long elapsed() { return runStart == 0 ? 0 : System.currentTimeMillis() - runStart; }

    private static String fmt(long ms) {
        if (ms <= 0) return "0:00.000";
        long s = ms / 1000;
        return (s / 60) + ":" + String.format("%02d", s % 60) + "." + String.format("%03d", ms % 1000);
    }

    private static int[] getBossCoords(DungeonFloor floor) {
        int idx;
        switch (floor) {
            case F1: case M1: idx = 0; break;
            case F2: case M2: idx = 1; break;
            case F3: case M3: idx = 2; break;
            case F4: case M4: idx = 3; break;
            case F5: case M5: idx = 4; break;
            case F6: case M6: idx = 5; break;
            case F7: case M7: idx = 6; break;
            default: idx = -1; break;
        }
        return idx >= 0 ? BOSS_COORDS[idx] : null;
    }

    private static void send(String msg) {
        if (mc.thePlayer != null) mc.thePlayer.addChatMessage(new ChatComponentText(msg));
    }

    private static class EndStats {
        String bossName, score, grade;
        boolean scorePB;
        final List<String> xp = new ArrayList<>();
        void reset() { bossName = score = grade = null; scorePB = false; xp.clear(); }
    }
}