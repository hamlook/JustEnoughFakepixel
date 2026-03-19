package com.jef.justenoughfakepixel.features.misc;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

@RegisterEvents
public class SearchBar {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final ResourceLocation TEX_NORMAL =
            new ResourceLocation("justenoughfakepixel", "textures/gui/search_bar.png");
    private static final ResourceLocation TEX_GOLD =
            new ResourceLocation("justenoughfakepixel", "textures/gui/search_bar_gold.png");

    private static final DecimalFormat CALC_FORMAT = new DecimalFormat("#,##0.##########");
    private static final Set<Character> CALC_SYMBOLS = new HashSet<>(Arrays.asList('+', '-', '*', '/', 'x', '(', ')'));
    private static final Map<ResourceLocation, Boolean> RESOURCE_CACHE = new HashMap<>();

    private static final SearchBar INSTANCE = new SearchBar();
    public static SearchBar getInstance() { return INSTANCE; }

    private static final int BAR_WIDTH  = 170;
    private static final int BAR_HEIGHT = 20;

    public int getOverlayWidth()  { return BAR_WIDTH; }
    public int getOverlayHeight() { return BAR_HEIGHT; }

    public void render(boolean preview) {
        ScaledResolution sr = new ScaledResolution(MC);
        com.jef.justenoughfakepixel.core.config.utils.Position pos =
                JefConfig.feature.misc.searchBarPos;
        int x = pos.getAbsX(sr, BAR_WIDTH);
        int y = pos.getAbsY(sr, BAR_HEIGHT);
        if (pos.isCenterX()) x -= BAR_WIDTH / 2;
        if (pos.isCenterY()) y -= BAR_HEIGHT / 2;

        Gui.drawRect(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF2C2C2C);
        Gui.drawRect(x + 1, y + 1, x + BAR_WIDTH - 1, y + BAR_HEIGHT - 1, 0xFF111111);
        MC.fontRendererObj.drawStringWithShadow("Search...", x + 5, y + BAR_HEIGHT / 2 - 4, 0x8F8F8F);
    }

    private static GuiTextField searchBar;
    private static String searchText = "";
    private static String lastCalcInput = "";
    private static String lastCalcResult = null;

    public static String getSearchText() { return isCalcMode() ? "" : searchText; }

    /** Returns true when the search text looks like a calculator expression — skip item highlighting in this case. */
    public static boolean isCalcMode() {
        for (char c : searchText.toCharArray())
            if (CALC_SYMBOLS.contains(c)) return true;
        return false;
    }

    private static boolean isEnabled() {
        return JefConfig.feature != null && JefConfig.feature.misc.searchBar;
    }

    private static boolean isSupportedGui(Object gui) {
        return gui instanceof GuiInventory || gui instanceof GuiChest;
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!isEnabled() || !isSupportedGui(event.gui)) return;

        int w = BAR_WIDTH, h = BAR_HEIGHT;

        ScaledResolution sr = new ScaledResolution(MC);
        com.jef.justenoughfakepixel.core.config.utils.Position pos =
                JefConfig.feature.misc.searchBarPos;
        int x = pos.getAbsX(sr, w);
        int y = pos.getAbsY(sr, h);
        if (pos.isCenterX()) x -= w / 2;
        if (pos.isCenterY()) y -= h / 2;

        searchBar = new GuiTextField(0, MC.fontRendererObj, x, y, w, h);
        searchBar.setCanLoseFocus(false);
        searchBar.setMaxStringLength(100);
        searchBar.setEnableBackgroundDrawing(false);
        searchBar.setFocused(false);
        searchBar.setText(searchText);
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!isEnabled() || !(event.gui instanceof GuiContainer)) return;
        if (searchBar == null || !Keyboard.getEventKeyState() || !searchBar.isFocused()) return;
        if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) return;

        if (searchBar.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey())) {
            searchText = searchBar.getText();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!isEnabled() || !(event.gui instanceof GuiContainer)) return;
        if (searchBar == null || !Mouse.getEventButtonState()) return;

        int mouseX = Mouse.getEventX() * event.gui.width / MC.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / MC.displayHeight - 1;

        boolean inside = mouseX >= searchBar.xPosition
                && mouseX <= searchBar.xPosition + searchBar.width
                && mouseY >= searchBar.yPosition
                && mouseY <= searchBar.yPosition + searchBar.height;

        searchBar.setFocused(inside);
        if (inside) searchBar.mouseClicked(mouseX, mouseY, Mouse.getEventButton());
    }

    @SubscribeEvent
    public void onDrawGui(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!isEnabled() || !isSupportedGui(event.gui) || searchBar == null) return;
        drawSearchBar(searchBar, searchBar.getText());
    }

    // drawing

    private static void drawSearchBar(GuiTextField field, String text) {
        int x = field.xPosition, y = field.yPosition;
        int w = field.width,     h = field.height;

        GlStateManager.color(1f, 1f, 1f, 1f);

        String suffix = calcSuffix(text);
        if (!drawTexture(suffix != null ? TEX_GOLD : TEX_NORMAL, x, y, w, h)) {
            Gui.drawRect(x, y, x + w, y + h, 0xFF2C2C2C);
            Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF111111);
        }

        FontRenderer fr = MC.fontRendererObj;
        int textY      = y - 4 + h / 2;
        int maxWidth   = Math.max(8, w - 10);
        String display = fr.trimStringToWidth(suffix != null ? text + " " + suffix : text, maxWidth);

        if (field.isFocused()) {
            fr.drawStringWithShadow(display, x + 5, textY, 0xFFFFFFFF);
            if (System.currentTimeMillis() % 1000 > 500) {
                int cursor      = Math.min(field.getCursorPosition(), text.length());
                int beforeWidth = fr.getStringWidth(fr.trimStringToWidth(text.substring(0, cursor), maxWidth));
                Gui.drawRect(x + 5 + beforeWidth, y - 5 + h / 2, x + 6 + beforeWidth, y + 4 + h / 2, 0xFFFFFFFF);
            }
        } else {
            fr.drawString(display, x + 5, textY, 0x8F8F8F);
        }
    }

    private static String calcSuffix(String text) {
        if (text == null || text.isEmpty()) return null;
        if (!text.equals(lastCalcInput)) {
            lastCalcInput = text;
            try {
                lastCalcResult = CALC_FORMAT.format(Calculator.calculate(text));
            } catch (Calculator.CalculatorException ignored) {
                lastCalcResult = null;
            }
        }
        return lastCalcResult == null ? null : "§e= §a" + lastCalcResult;
    }

    private static boolean drawTexture(ResourceLocation texture, int x, int y, int w, int h) {
        if (!resourceExists(texture)) return false;

        MC.getTextureManager().bindTexture(texture);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        for (int yi = 0; yi <= 2; yi++) {
            for (int xi = 0; xi <= 2; xi++) {
                float uMin = 0f, uMax = 4f / 20f;
                int px = x, pw = 4;
                if (xi == 1) { px += 4;     uMin = 4f / 20f; uMax = 16f / 20f; pw = w - 8; }
                else if (xi == 2) { px += w - 4; uMin = 16f / 20f; uMax = 1f; }

                float vMin = 0f, vMax = 4f / 20f;
                int py = y, ph = 4;
                if (yi == 1) { py += 4;     vMin = 4f / 20f; vMax = 16f / 20f; ph = h - 8; }
                else if (yi == 2) { py += h - 4; vMin = 16f / 20f; vMax = 1f; }

                drawTexturedRect(px, py, pw, ph, uMin, uMax, vMin, vMax);
            }
        }

        GlStateManager.disableBlend();
        return true;
    }

    private static void drawTexturedRect(int x, int y, int w, int h,
                                         float uMin, float uMax, float vMin, float vMax) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x,     y + h, 0).tex(uMin, vMax).endVertex();
        wr.pos(x + w, y + h, 0).tex(uMax, vMax).endVertex();
        wr.pos(x + w, y,     0).tex(uMax, vMin).endVertex();
        wr.pos(x,     y,     0).tex(uMin, vMin).endVertex();
        Tessellator.getInstance().draw();
    }

    private static boolean resourceExists(ResourceLocation location) {
        return RESOURCE_CACHE.computeIfAbsent(location, loc -> {
            try { MC.getResourceManager().getResource(loc); return true; }
            catch (Exception ignored) { return false; }
        });
    }


    public static class Calculator {

        public enum TokenType { NUMBER, BINOP, LPAREN, RPAREN, POSTOP }

        public static class Token {
            public TokenType type;
            String operatorValue;
            long numericValue;
            int exponent, tokenStart, tokenLength;
        }

        public static class CalculatorException extends Exception {
            int offset, length;
            public CalculatorException(String message, int offset, int length) {
                super(message);
                this.offset = offset;
                this.length = length;
            }
        }

        private static final String BINOPS  = "+-*/x";
        private static final String POSTOPS = "mkbts";
        private static final String DIGITS  = "0123456789";

        public static BigDecimal calculate(String source) throws CalculatorException {
            return evaluate(shuntingYard(lex(source.toLowerCase(Locale.ROOT))));
        }

        private static void readDigitsInto(Token token, String source, boolean decimals) {
            int start = token.tokenStart + token.tokenLength;
            for (int j = 0; j + start < source.length(); j++) {
                int d = DIGITS.indexOf(source.charAt(j + start));
                if (d == -1) return;
                if (decimals) token.exponent--;
                token.numericValue = token.numericValue * 10 + d;
                token.tokenLength++;
            }
        }

        public static List<Token> lex(String source) throws CalculatorException {
            List<Token> tokens = new ArrayList<>();
            for (int i = 0; i < source.length(); ) {
                char c = source.charAt(i);
                if (Character.isWhitespace(c)) { i++; continue; }

                Token t = new Token();
                t.tokenStart = i;

                if      (BINOPS.indexOf(c)  != -1) { t.tokenLength = 1; t.type = TokenType.BINOP;  t.operatorValue = String.valueOf(c); }
                else if (POSTOPS.indexOf(c) != -1) { t.tokenLength = 1; t.type = TokenType.POSTOP; t.operatorValue = String.valueOf(c); }
                else if (c == ')')                 { t.tokenLength = 1; t.type = TokenType.RPAREN; t.operatorValue = ")"; }
                else if (c == '(')                 { t.tokenLength = 1; t.type = TokenType.LPAREN; t.operatorValue = "("; }
                else if (c == '.') {
                    t.tokenLength = 1; t.type = TokenType.NUMBER;
                    readDigitsInto(t, source, true);
                    if (t.tokenLength == 1) throw new CalculatorException("Invalid number literal", i, 1);
                } else if (DIGITS.indexOf(c) != -1) {
                    t.type = TokenType.NUMBER;
                    readDigitsInto(t, source, false);
                    if (i + t.tokenLength < source.length() && source.charAt(i + t.tokenLength) == '.') {
                        t.tokenLength++;
                        readDigitsInto(t, source, true);
                    }
                } else {
                    throw new CalculatorException("Unknown character: " + c, i, 1);
                }

                tokens.add(t);
                i += t.tokenLength;
            }
            return tokens;
        }

        private static int getPrecedence(Token t) throws CalculatorException {
            switch (t.operatorValue) {
                case "+": case "-": return 0;
                case "*": case "/": case "x": return 1;
                default: throw new CalculatorException("Unknown operator " + t.operatorValue, t.tokenStart, t.tokenLength);
            }
        }

        public static List<Token> shuntingYard(List<Token> tokens) throws CalculatorException {
            Deque<Token> op  = new ArrayDeque<>();
            List<Token>  out = new ArrayList<>();

            for (Token t : tokens) {
                switch (t.type) {
                    case NUMBER: out.add(t); break;
                    case BINOP:
                        int p = getPrecedence(t);
                        while (!op.isEmpty() && op.peek().type != TokenType.LPAREN && getPrecedence(op.peek()) >= p)
                            out.add(op.pop());
                        op.push(t);
                        break;
                    case LPAREN: op.push(t); break;
                    case RPAREN:
                        while (true) {
                            if (op.isEmpty()) throw new CalculatorException("Unbalanced right parenthesis", t.tokenStart, t.tokenLength);
                            Token l = op.pop();
                            if (l.type == TokenType.LPAREN) break;
                            out.add(l);
                        }
                        break;
                    case POSTOP: out.add(t); break;
                }
            }
            while (!op.isEmpty()) {
                Token l = op.pop();
                if (l.type == TokenType.LPAREN) throw new CalculatorException("Unbalanced left parenthesis", l.tokenStart, l.tokenLength);
                out.add(l);
            }
            return out;
        }

        public static BigDecimal evaluate(List<Token> rpn) throws CalculatorException {
            Deque<BigDecimal> stack = new ArrayDeque<>();
            try {
                for (Token t : rpn) {
                    switch (t.type) {
                        case NUMBER:
                            stack.push(new BigDecimal(t.numericValue).scaleByPowerOfTen(t.exponent));
                            break;
                        case BINOP: {
                            BigDecimal r = stack.pop();
                            BigDecimal l = stack.pop();
                            switch (t.operatorValue) {
                                case "x": case "*": stack.push(l.multiply(r).setScale(2, RoundingMode.HALF_UP)); break;
                                case "/":
                                    try { BigDecimal result = l.divide(r, 10, RoundingMode.HALF_UP).stripTrailingZeros(); stack.push(result.scale() < 2 ? result.setScale(2) : result); }
                                    catch (ArithmeticException e) { throw new CalculatorException("Division by zero", t.tokenStart, t.tokenLength); }
                                    break;
                                case "+": stack.push(l.add(r).setScale(2, RoundingMode.HALF_UP)); break;
                                case "-": stack.push(l.subtract(r).setScale(2, RoundingMode.HALF_UP)); break;
                                default:  throw new CalculatorException("Unknown operator " + t.operatorValue, t.tokenStart, t.tokenLength);
                            }
                            break;
                        }
                        case POSTOP: {
                            BigDecimal v = stack.pop();
                            switch (t.operatorValue) {
                                case "s": stack.push(v.multiply(new BigDecimal(64)));             break;
                                case "k": stack.push(v.multiply(new BigDecimal(1_000)));          break;
                                case "m": stack.push(v.multiply(new BigDecimal(1_000_000)));      break;
                                case "b": stack.push(v.multiply(new BigDecimal(1_000_000_000)));  break;
                                case "t": stack.push(v.multiply(new BigDecimal("1000000000000"))); break;
                                default:  throw new CalculatorException("Unknown postop " + t.operatorValue, t.tokenStart, t.tokenLength);
                            }
                            break;
                        }
                        default: throw new CalculatorException("Unexpected token", t.tokenStart, t.tokenLength);
                    }
                }
                return stack.pop().stripTrailingZeros();
            } catch (NoSuchElementException e) {
                throw new CalculatorException("Unfinished expression", 0, 0);
            }
        }
    }
}