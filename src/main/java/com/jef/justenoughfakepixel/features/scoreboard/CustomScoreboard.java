package com.jef.justenoughfakepixel.features.scoreboard;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.utils.JefOverlay;
import com.jef.justenoughfakepixel.utils.ScoreboardUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CustomScoreboard extends JefOverlay {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int PAD_X       = 4;
    private static final int PAD_Y       = 4;
    private static final int LINE_GAP    = 1;
    private static final int SUPERSAMPLE = 2;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final int TITLE_COL = 0xFFFFAA00;

    // ── Line IDs — must match exampleText order in Scoreboard.java ────────────
    private static final int LINE_SERVER   = 0; // 03/13/26 hub-2
    private static final int LINE_SEASON   = 1; // Winter 13th
    private static final int LINE_TIME     = 2; // 9:30am ☀
    private static final int LINE_LOCATION = 3; // ⏣ Village
    private static final int LINE_PURSE    = 4;
    private static final int LINE_BANK     = 5;
    private static final int LINE_BITS     = 6;
    private static final int LINE_POWER    = 7;
    private static final int LINE_WEBSITE  = 8;

    // ── Patterns (ported from SkyHanni ScoreboardPattern + PurseApi + BitsApi) ─
    // Location: raw line contains ⏣ (normal) or ф (Rift)
    // SkyHanni: \s*(?<location>(?:§7⏣|§5ф) .*)
    private static final String LOC_SYMBOL_NORMAL = "\u23E3"; // ⏣
    private static final String LOC_SYMBOL_RIFT   = "\u0444"; // ф

    // Date/season: SkyHanni: \s*(?:(?:Late|Early) )?(?:Spring|Summer|Autumn|Winter) \d+(?:st|nd|rd|th)?.*
    private static final Pattern SEASON_PATTERN = Pattern.compile(
            "\\s*(?:(?:Late|Early) )?(?:Spring|Summer|Autumn|Winter) \\d+.*"
    );

    // Time: SkyHanni: \s*§7\d+:\d+(?:am|pm)\s*(?:§b☽|§e☀|§.⚡|§.☔)?.*
    // We match on stripped: digits:digits followed by am/pm
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "\\s*\\d+:\\d+(?:am|pm).*"
    );

    // Server/lobby code: SkyHanni: \s*§.(\d{2}/?){3} §8(?<code>.*)
    // Stripped it looks like: 03/13/26 hub-2
    private static final Pattern SERVER_PATTERN = Pattern.compile(
            "\\s*\\d{2}/\\d{2}/\\d{2}.*"
    );

    // Purse: SkyHanni: (?:§.)*(?:Piggy|Purse): §6(?<coins>[\d,.]+).*
    private static final Pattern PURSE_PATTERN = Pattern.compile(
            "(?:Piggy|Purse): [\\d,.]+"
    );

    // Bits: SkyHanni: ^Bits: §b(?<amount>[\d,.]+).*$
    private static final Pattern BITS_PATTERN = Pattern.compile(
            "Bits: [\\d,.]+"
    );

    // Bank: starts with Bank:
    private static final Pattern BANK_PATTERN = Pattern.compile(
            "Bank: .+"
    );

    // Website: contains fakepixel
    private static final Pattern WEBSITE_PATTERN = Pattern.compile(
            ".*fakepixel.*"
    );

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static CustomScoreboard instance;

    public CustomScoreboard() {
        super(130, 90);
        instance = this;
    }

    public static CustomScoreboard getInstance() { return instance; }

    public static boolean isActive() {
        return JefConfig.feature != null
                && JefConfig.feature.scoreboard != null
                && JefConfig.feature.scoreboard.enabled;
    }

    @Override public Position getPosition()    { return JefConfig.feature.scoreboard.position; }
    @Override public float    getScale()       { return JefConfig.feature.scoreboard.scale; }
    @Override public boolean  showBackground() { return false; }
    @Override protected boolean extraGuard()   { return isActive(); }

    // ── Render hook ───────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onRenderPost(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        // Hide on F3 debug screen but NOT when tab is held — scoreboard stays visible over tab list
        if (Minecraft.getMinecraft().gameSettings.showDebugInfo) return;
        render(false);
    }

    // ── Safe line order ───────────────────────────────────────────────────────
    // Gson deserializes List<Integer> as List<Double> from JSON, cast via Number.
    private static List<Integer> getLineOrder() {
        List<?> raw = JefConfig.feature.scoreboard.scoreboardLines;
        List<Integer> result = new ArrayList<>();
        if (raw == null) return result;
        for (Object o : raw) {
            if (o instanceof Number) result.add(((Number) o).intValue());
        }
        return result;
    }

    // ── Lines ─────────────────────────────────────────────────────────────────

    @Override
    public List<String> getLines(boolean preview) {
        List<String> lines = new ArrayList<>();

        if (preview) {
            // Use live lines in preview so position editor shows real scoreboard
            return getLines(false);
        }

        // ── Pull live sidebar ─────────────────────────────────────────────────
        List<String> raw = new ArrayList<>(ScoreboardUtils.getScoreboardLines());
        if (raw.isEmpty()) return lines;
        Collections.reverse(raw);

        // ── Classify each sidebar line ────────────────────────────────────────
        String serverRaw   = null;
        String seasonRaw   = null;
        String timeRaw     = null;
        String locationRaw = null;
        String purseRaw    = null;
        String bankRaw     = null;
        String bitsRaw     = null;
        String websiteRaw  = null;

        // Track which raw lines have been claimed so they don't appear twice
        List<String> claimed = new ArrayList<>();

        for (String l : raw) {
            String c = stripColor(l).trim();
            if (c.isEmpty()) continue;

            // Location — check RAW line for ⏣ (Hypixel), or ф (Rift)
            // Must check raw (not stripped) because symbol may be color-prefixed
            if (locationRaw == null && (l.contains(LOC_SYMBOL_NORMAL) || l.contains(LOC_SYMBOL_RIFT))) {
                locationRaw = l; claimed.add(l); continue;
            }
            // Server/date line: matches dd/mm/yy pattern
            if (serverRaw == null && SERVER_PATTERN.matcher(c).matches()) {
                serverRaw = l; claimed.add(l); continue;
            }
            // Season/day: Spring/Summer/Autumn/Winter + number
            if (seasonRaw == null && SEASON_PATTERN.matcher(c).matches()) {
                seasonRaw = l; claimed.add(l); continue;
            }
            // Time: digits:digits + am/pm
            if (timeRaw == null && TIME_PATTERN.matcher(c).matches()) {
                timeRaw = l; claimed.add(l); continue;
            }
            // Purse / Piggy
            if (purseRaw == null && PURSE_PATTERN.matcher(c).find()) {
                purseRaw = l; claimed.add(l); continue;
            }
            // Bank
            if (bankRaw == null && BANK_PATTERN.matcher(c).find()) {
                bankRaw = l; claimed.add(l); continue;
            }
            // Bits
            if (bitsRaw == null && BITS_PATTERN.matcher(c).find()) {
                bitsRaw = l; claimed.add(l); continue;
            }
            // Website
            if (websiteRaw == null && WEBSITE_PATTERN.matcher(c).matches()) {
                websiteRaw = l; claimed.add(l);
            }
        }

        // ── Title ─────────────────────────────────────────────────────────────
        String title = ScoreboardUtils.getServerId();
        if (title == null || title.trim().isEmpty()) title = "SKYBLOCK";
        lines.add("\u00A76\u00A7l" + title.trim());

        // ── Config-ordered known lines ────────────────────────────────────────
        for (int id : getLineOrder()) {
            switch (id) {
                case LINE_SERVER:
                    if (serverRaw != null) lines.add(serverRaw);
                    break;
                case LINE_SEASON:
                    if (seasonRaw != null) lines.add(seasonRaw);
                    break;
                case LINE_TIME:
                    if (timeRaw != null) lines.add(timeRaw);
                    break;
                case LINE_LOCATION:
                    if (locationRaw != null) lines.add(locationRaw);
                    break;
                case LINE_PURSE:
                    if (purseRaw != null) lines.add(purseRaw);
                    break;
                case LINE_BANK:
                    if (bankRaw != null) {
                        lines.add(bankRaw);
                    } else {
                        String bank = BankParser.getBank();
                        if (bank != null) lines.add("\u00A7fBank: \u00A76" + bank);
                    }
                    break;
                case LINE_BITS:
                    if (bitsRaw != null) lines.add(bitsRaw);
                    break;
                case LINE_POWER:
                    String power = MaxwellPowerSync.getPower();
                    if (power != null && ScoreboardUtils.isOnSkyblock()) {
                        lines.add("\u00A7fPower: \u00A7d" + power);
                    }
                    break;
                case LINE_WEBSITE:
                    // handled after unclaimed lines — always at bottom
                    break;
            }
        }

        // ── Unclaimed lines (quests, events, dungeons, etc.) ─────────────────
        for (String l : raw) {
            if (claimed.contains(l)) continue;
            String c = stripColor(l).trim();
            if (c.isEmpty()) continue;
            lines.add(l);
        }

        // ── Website always last ───────────────────────────────────────────────
        // Only add if LINE_WEBSITE is in the config order (player hasn't removed it)
        if (websiteRaw != null && getLineOrder().contains(LINE_WEBSITE)) {
            lines.add(websiteRaw);
        }

        return lines;
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(boolean preview) {
        if (JefConfig.feature == null) return;
        if (!preview && (!extraGuard() || Minecraft.getMinecraft().gameSettings.showDebugInfo)) return;

        List<String> lines = getLines(preview);
        if (lines.isEmpty()) return;

        Minecraft mc  = Minecraft.getMinecraft();
        float   scale = getScale();
        int     lh    = LINE_HEIGHT + LINE_GAP;
        int     ss    = SUPERSAMPLE;

        int maxW = 60;
        for (String line : lines)
            maxW = Math.max(maxW, mc.fontRendererObj.getStringWidth(stripColor(line)));

        int boxW = maxW + PAD_X * 2;
        int boxH = lines.size() * lh + PAD_Y * 2 - LINE_GAP;
        lastW = boxW;
        lastH = boxH;

        ScaledResolution sr  = new ScaledResolution(mc);
        Position         pos = getPosition();
        int x = pos.getAbsX(sr, (int)(boxW * scale));
        int y = pos.getAbsY(sr, (int)(boxH * scale));
        if (pos.isCenterX()) x -= (int)(boxW * scale / 2);
        if (pos.isCenterY()) y -= (int)(boxH * scale / 2);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(scale / ss, scale / ss, 1f);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int bgAlpha = (int)(JefConfig.feature.scoreboard.opacity / 100f * 255f);
        int r       = (int) JefConfig.feature.scoreboard.cornerRadius * ss;
        drawRoundedRect(0, 0, boxW * ss, boxH * ss, r, (bgAlpha << 24) | 0x101010);

        GL11.glScalef(ss, ss, 1f);

        String titleLine = lines.get(0);
        int titleW = mc.fontRendererObj.getStringWidth(stripColor(titleLine));
        int titleX = (boxW - titleW) / 2;
        mc.fontRendererObj.drawStringWithShadow(titleLine, titleX, PAD_Y, TITLE_COL);

        int textY = PAD_Y + lh;
        for (int i = 1; i < lines.size(); i++) {
            mc.fontRendererObj.drawStringWithShadow(lines.get(i), PAD_X, textY, 0xFFFFFF);
            textY += lh;
        }

        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    // ── Rounded rect ──────────────────────────────────────────────────────────

    private static void drawRoundedRect(int x, int y, int w, int h, int r, int color) {
        r = Math.min(r, Math.min(w, h) / 2);

        Gui.drawRect(x + r, y,           x + w - r, y + h,     color);
        Gui.drawRect(x,     y + r,       x + r,     y + h - r, color);
        Gui.drawRect(x + w - r, y + r,   x + w,     y + h - r, color);

        for (int i = 0; i < r; i++) {
            int cut = (int) Math.round(r - Math.sqrt(Math.max(0.0, (double) r * r - (double)(r - i - 1) * (r - i - 1))));
            Gui.drawRect(x + i,         y + cut,   x + i + 1,   y + r,       color);
            Gui.drawRect(x + w - i - 1, y + cut,   x + w - i,   y + r,       color);
            Gui.drawRect(x + i,         y + h - r, x + i + 1,   y + h - cut, color);
            Gui.drawRect(x + w - i - 1, y + h - r, x + w - i,   y + h - cut, color);
        }
    }

    private static String stripColor(String s) {
        return s == null ? "" : s.replaceAll("\u00A7[0-9a-fklmnorA-FKLMNOR]", "");
    }
}