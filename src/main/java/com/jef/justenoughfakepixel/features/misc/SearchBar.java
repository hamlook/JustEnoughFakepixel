package com.jef.justenoughfakepixel.features.misc;

import com.jef.justenoughfakepixel.core.JefConfig;
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
import net.minecraft.item.ItemStack;
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
import java.util.regex.Pattern;

public class SearchBar {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final ResourceLocation SEARCH_BAR_NORMAL =
            new ResourceLocation("justenoughfakepixel", "textures/gui/search_bar.png");

    private static final ResourceLocation SEARCH_BAR_GOLD =
            new ResourceLocation("justenoughfakepixel", "textures/gui/search_bar_gold.png");

    private static final DecimalFormat CALC_FORMAT = new DecimalFormat("#,##0.##");
    private static final Pattern STRIP_CODES_PATTERN = Pattern.compile("(?i)§.");
    private static final Map<ResourceLocation, Boolean> RESOURCE_CACHE = new HashMap<>();

    private static GuiTextField searchBar;
    private static String searchText = "";
    private static String lastCalcInput = "";
    private static String lastCalcResult = null;

    public static String getSearchText() {
        return searchText;
    }

    private static boolean isEnabled() {
        return JefConfig.feature != null
                && JefConfig.feature.misc.searchBar;
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!isEnabled()) return;
        if (!(event.gui instanceof GuiContainer)) return;
        if (!(event.gui instanceof GuiInventory) && !(event.gui instanceof GuiChest)) return;

        int w = 170;
        int h = 20;
        int x = event.gui.width / 2 - w / 2;
        int y = event.gui.height - 10 - h;

        searchBar = new GuiTextField(0, MC.fontRendererObj, x, y, w, h);
        searchBar.setCanLoseFocus(false);
        searchBar.setMaxStringLength(100);
        searchBar.setEnableBackgroundDrawing(false);
        searchBar.setFocused(false);
        searchBar.setText(searchText);
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!isEnabled()) return;
        if (!(event.gui instanceof GuiContainer)) return;
        if (searchBar == null || !Keyboard.getEventKeyState() || !searchBar.isFocused()) return;

        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_ESCAPE) return;

        if (searchBar.textboxKeyTyped(Keyboard.getEventCharacter(), key)) {
            searchText = searchBar.getText();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!isEnabled()) return;
        if (!(event.gui instanceof GuiContainer)) return;
        if (searchBar == null || !Mouse.getEventButtonState()) return;

        int mouseX = Mouse.getEventX() * event.gui.width / MC.displayWidth;
        int mouseY = event.gui.height - Mouse.getEventY() * event.gui.height / MC.displayHeight - 1;

        boolean inside = mouseX >= searchBar.xPosition &&
                mouseX <= searchBar.xPosition + searchBar.width &&
                mouseY >= searchBar.yPosition &&
                mouseY <= searchBar.yPosition + searchBar.height;

        searchBar.setFocused(inside);

        if (inside) {
            searchBar.mouseClicked(mouseX, mouseY, Mouse.getEventButton());
        }
    }

    @SubscribeEvent
    public void onDrawGui(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!isEnabled()) return;
        if (!(event.gui instanceof GuiContainer)) return;
        if (!(event.gui instanceof GuiInventory) && !(event.gui instanceof GuiChest)) return;
        if (searchBar == null) return;

        String text = searchBar.getText();
        drawSearchBar(searchBar, text);
    }

    private static String calcSuffix(String text) {
        if (text == null || text.isEmpty()) return null;

        if (!lastCalcInput.equals(text)) {
            lastCalcInput = text;
            try {
                lastCalcResult = CALC_FORMAT.format(Calculator.calculate(text));
            } catch (Calculator.CalculatorException ignored) {
                lastCalcResult = null;
            }
        }

        return lastCalcResult == null ? null : "§e= §a" + lastCalcResult;
    }

    private static void drawSearchBar(GuiTextField field, String rawText) {
        int x = field.xPosition;
        int y = field.yPosition;
        int w = field.width;
        int h = field.height;

        GlStateManager.color(1f, 1f, 1f, 1f);

        String suffix = calcSuffix(rawText);
        if (!drawSearchBarTexture(suffix != null ? SEARCH_BAR_GOLD : SEARCH_BAR_NORMAL, x, y, w, h)) {
            Gui.drawRect(x, y, x + w, y + h, 0xFF2C2C2C);
            Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF111111);
        }

        FontRenderer fr = MC.fontRendererObj;
        int textY = y - 4 + h / 2;
        int maxTextWidth = Math.max(8, w - 10);

        String visibleText = fr.trimStringToWidth(
                suffix != null ? rawText + " " + suffix : rawText,
                maxTextWidth
        );

        if (field.isFocused()) {
            fr.drawStringWithShadow(visibleText, x + 5, textY, 0xFFFFFFFF);
        } else {
            fr.drawString(visibleText, x + 5, textY, 0x8F8F8F);
        }

        if (field.isFocused() && System.currentTimeMillis() % 1000 > 500) {
            int cursor = Math.max(0,
                    Math.min(field.getCursorPosition(), rawText.length()));
            int beforeWidth = fr.getStringWidth(
                    fr.trimStringToWidth(rawText.substring(0, cursor), maxTextWidth)
            );

            Gui.drawRect(
                    x + 5 + beforeWidth,
                    y - 5 + h / 2,
                    x + 6 + beforeWidth,
                    y - 4 + 9 + h / 2,
                    0xFFFFFFFF
            );
        }
    }

    private static boolean drawSearchBarTexture(ResourceLocation texture, int x, int y, int w, int h) {
        if (!resourceExists(texture)) return false;

        MC.getTextureManager().bindTexture(texture);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        for (int yi = 0; yi <= 2; yi++) {
            for (int xi = 0; xi <= 2; xi++) {

                float uMin = 0f, uMax = 4f / 20f;
                int partX = x, partW = 4;

                if (xi == 1) {
                    partX += 4;
                    uMin = 4f / 20f;
                    uMax = 16f / 20f;
                    partW = w - 8;
                } else if (xi == 2) {
                    partX += w - 4;
                    uMin = 16f / 20f;
                    uMax = 1f;
                }

                float vMin = 0f, vMax = 4f / 20f;
                int partY = y, partH = 4;

                if (yi == 1) {
                    partY += 4;
                    vMin = 4f / 20f;
                    vMax = 16f / 20f;
                    partH = h - 8;
                } else if (yi == 2) {
                    partY += h - 4;
                    vMin = 16f / 20f;
                    vMax = 1f;
                }

                drawTexturedRect(partX, partY, partW, partH, uMin, uMax, vMin, vMax);
            }
        }

        GlStateManager.disableBlend();
        return true;
    }

    private static void drawTexturedRect(int x, int y, int w, int h,
                                         float uMin, float uMax,
                                         float vMin, float vMax) {

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + h, 0).tex(uMin, vMax).endVertex();
        wr.pos(x + w, y + h, 0).tex(uMax, vMax).endVertex();
        wr.pos(x + w, y, 0).tex(uMax, vMin).endVertex();
        wr.pos(x, y, 0).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    private static boolean resourceExists(ResourceLocation location) {
        return RESOURCE_CACHE.computeIfAbsent(location, loc -> {
            try {
                MC.getResourceManager().getResource(loc);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        });
    }

    private static String stripCodes(String s) {
        return s == null ? "" :
                STRIP_CODES_PATTERN.matcher(s).replaceAll("");
    }

    public static class Calculator {

        public enum TokenType { NUMBER, BINOP, LPAREN, RPAREN, POSTOP }

        public static class Token {
            public TokenType type;
            String operatorValue;
            long numericValue;
            int exponent;
            int tokenStart;
            int tokenLength;
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
            int startIndex = token.tokenStart + token.tokenLength;
            for (int j = 0; j + startIndex < source.length(); j++) {
                char d = source.charAt(j + startIndex);
                int d0 = DIGITS.indexOf(d);
                if (d0 == -1) return;
                if (decimals) token.exponent--;
                token.numericValue = token.numericValue * 10 + d0;
                token.tokenLength++;
            }
        }

        public static List<Token> lex(String source) throws CalculatorException {
            List<Token> tokens = new ArrayList<>();
            for (int i = 0; i < source.length(); ) {
                char c = source.charAt(i);
                if (Character.isWhitespace(c)) { i++; continue; }

                Token token = new Token();
                token.tokenStart = i;

                if (BINOPS.indexOf(c) != -1) {
                    token.tokenLength = 1;
                    token.type = TokenType.BINOP;
                    token.operatorValue = String.valueOf(c);
                } else if (POSTOPS.indexOf(c) != -1) {
                    token.tokenLength = 1;
                    token.type = TokenType.POSTOP;
                    token.operatorValue = String.valueOf(c);
                } else if (c == ')') {
                    token.tokenLength = 1;
                    token.type = TokenType.RPAREN;
                    token.operatorValue = ")";
                } else if (c == '(') {
                    token.tokenLength = 1;
                    token.type = TokenType.LPAREN;
                    token.operatorValue = "(";
                } else if (c == '.') {
                    token.tokenLength = 1;
                    token.type = TokenType.NUMBER;
                    readDigitsInto(token, source, true);
                    if (token.tokenLength == 1) throw new CalculatorException("Invalid number literal", i, 1);
                } else if (DIGITS.indexOf(c) != -1) {
                    token.type = TokenType.NUMBER;
                    readDigitsInto(token, source, false);
                    if (i + token.tokenLength < source.length() && source.charAt(i + token.tokenLength) == '.') {
                        token.tokenLength++;
                        readDigitsInto(token, source, true);
                    }
                } else {
                    throw new CalculatorException("Unknown character: " + c, i, 1);
                }

                tokens.add(token);
                i += token.tokenLength;
            }
            return tokens;
        }

        private static int getPrecedence(Token token) throws CalculatorException {
            switch (token.operatorValue) {
                case "+": case "-": return 0;
                case "*": case "/": case "x": return 1;
                default: throw new CalculatorException("Unknown operator " + token.operatorValue, token.tokenStart, token.tokenLength);
            }
        }

        public static List<Token> shuntingYard(List<Token> toShunt) throws CalculatorException {
            Deque<Token> op = new ArrayDeque<>();
            List<Token> out = new ArrayList<>();

            for (Token t : toShunt) {
                switch (t.type) {
                    case NUMBER:
                        out.add(t);
                        break;
                    case BINOP:
                        int p = getPrecedence(t);
                        while (!op.isEmpty() && op.peek().type != TokenType.LPAREN && getPrecedence(op.peek()) >= p) {
                            out.add(op.pop());
                        }
                        op.push(t);
                        break;
                    case LPAREN:
                        op.push(t);
                        break;
                    case RPAREN:
                        while (true) {
                            if (op.isEmpty()) throw new CalculatorException("Unbalanced right parenthesis", t.tokenStart, t.tokenLength);
                            Token l = op.pop();
                            if (l.type == TokenType.LPAREN) break;
                            out.add(l);
                        }
                        break;
                    case POSTOP:
                        out.add(t);
                        break;
                }
            }
            while (!op.isEmpty()) {
                Token l = op.pop();
                if (l.type == TokenType.LPAREN) throw new CalculatorException("Unbalanced left parenthesis", l.tokenStart, l.tokenLength);
                out.add(l);
            }
            return out;
        }

        public static BigDecimal evaluate(List<Token> rpnTokens) throws CalculatorException {
            Deque<BigDecimal> values = new ArrayDeque<>();
            try {
                for (Token cmd : rpnTokens) {
                    switch (cmd.type) {
                        case NUMBER:
                            values.push(new BigDecimal(cmd.numericValue).scaleByPowerOfTen(cmd.exponent));
                            break;
                        case BINOP: {
                            BigDecimal right = values.pop().setScale(2, RoundingMode.HALF_UP);
                            BigDecimal left  = values.pop().setScale(2, RoundingMode.HALF_UP);
                            switch (cmd.operatorValue) {
                                case "x": case "*": values.push(left.multiply(right).setScale(2, RoundingMode.HALF_UP)); break;
                                case "/":
                                    try { values.push(left.divide(right, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP)); }
                                    catch (ArithmeticException e) { throw new CalculatorException("Division by zero", cmd.tokenStart, cmd.tokenLength); }
                                    break;
                                case "+": values.push(left.add(right).setScale(2, RoundingMode.HALF_UP)); break;
                                case "-": values.push(left.subtract(right).setScale(2, RoundingMode.HALF_UP)); break;
                                default: throw new CalculatorException("Unknown operator " + cmd.operatorValue, cmd.tokenStart, cmd.tokenLength);
                            }
                            break;
                        }
                        case LPAREN: case RPAREN:
                            throw new CalculatorException("Unexpected unshunted token", cmd.tokenStart, cmd.tokenLength);
                        case POSTOP: {
                            BigDecimal v = values.pop();
                            switch (cmd.operatorValue) {
                                case "s": values.push(v.multiply(new BigDecimal(64)).setScale(2, RoundingMode.HALF_UP)); break;
                                case "k": values.push(v.multiply(new BigDecimal(1_000)).setScale(2, RoundingMode.HALF_UP)); break;
                                case "m": values.push(v.multiply(new BigDecimal(1_000_000)).setScale(2, RoundingMode.HALF_UP)); break;
                                case "b": values.push(v.multiply(new BigDecimal(1_000_000_000)).setScale(2, RoundingMode.HALF_UP)); break;
                                case "t": values.push(v.multiply(new BigDecimal("1000000000000")).setScale(2, RoundingMode.HALF_UP)); break;
                                default: throw new CalculatorException("Unknown postop " + cmd.operatorValue, cmd.tokenStart, cmd.tokenLength);
                            }
                            break;
                        }
                    }
                }
                return values.pop().stripTrailingZeros();
            } catch (NoSuchElementException e) {
                throw new CalculatorException("Unfinished expression", 0, 0);
            }
        }
    }
}
