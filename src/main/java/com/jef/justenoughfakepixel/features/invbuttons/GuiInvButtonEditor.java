package com.jef.justenoughfakepixel.features.invbuttons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jef.justenoughfakepixel.core.config.gui.GlScissorStack;
import com.jef.justenoughfakepixel.core.config.gui.GuiElementTextField;
import com.jef.justenoughfakepixel.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class GuiInvButtonEditor extends GuiScreen {

    private static final ResourceLocation INVENTORY_TEX =
            new ResourceLocation("minecraft:textures/gui/container/inventory.png");
    private static final ResourceLocation EDITOR_TEX =
            new ResourceLocation("justenoughfakepixel", "invbuttons/editor.png");

    private static final String SHARE_PREFIX = "JEFBUTTONS/";
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private static final int xSize = 176;
    private static final int ySize = 166;
    private int guiLeft, guiTop;

    private static final int editorXSize = 150;
    private static final int editorYSize = 204;
    private int editorLeft, editorTop;

    private static final int BACKGROUND_TYPES = 5;
    private static final int ICON_TYPES = 3;
    private int iconTypeIndex = 0;

    private final GuiElementTextField commandTextField = new GuiElementTextField(
            "", editorXSize - 14, 16, GuiElementTextField.SCALE_TEXT);
    private final GuiElementTextField iconTextField = new GuiElementTextField(
            "", editorXSize - 14, 16, GuiElementTextField.SCALE_TEXT);

    private InventoryButton editingButton = null;

    private final List<String> searchedIcons = new ArrayList<>();
    private final ExecutorService searchES = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "JEF-BtnSearch"); t.setDaemon(true); return t;
    });
    private final AtomicInteger searchId = new AtomicInteger(0);
    private int itemScrollPx = 0;

    private static List<String> presetNames = null;
    private static List<List<InventoryButton>> presetButtonsList = null;
    private static Map<String, String> extraIconsCache = null;

    public GuiInvButtonEditor() {
        super();
        ensurePresetsLoaded();
        ensureExtraIconsLoaded();
    }

    private static void ensurePresetsLoaded() {
        if (presetNames != null) return;
        presetNames = new ArrayList<>();
        presetButtonsList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Minecraft.getMinecraft().getResourceManager()
                            .getResource(new ResourceLocation("justenoughfakepixel", "invbuttons/presets.json"))
                            .getInputStream(), StandardCharsets.UTF_8));
            JsonObject root = new JsonParser().parse(br).getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : root.entrySet()) {
                if (!e.getValue().isJsonArray()) continue;
                List<InventoryButton> btns = new ArrayList<>();
                for (JsonElement el : e.getValue().getAsJsonArray())
                    if (el.isJsonObject()) {
                        InventoryButton b = GSON.fromJson(el.getAsJsonObject(), InventoryButton.class);
                        if (b != null) btns.add(b);
                    }
                if (!btns.isEmpty()) { presetNames.add(e.getKey()); presetButtonsList.add(btns); }
            }
        } catch (Exception ignored) {}
    }

    private static void ensureExtraIconsLoaded() {
        if (extraIconsCache != null) return;
        extraIconsCache = new LinkedHashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Minecraft.getMinecraft().getResourceManager()
                            .getResource(new ResourceLocation("justenoughfakepixel", "invbuttons/extraicons.json"))
                            .getInputStream(), StandardCharsets.UTF_8));
            JsonObject root = new JsonParser().parse(br).getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : root.entrySet())
                if (e.getValue().isJsonPrimitive())
                    extraIconsCache.put(e.getKey(), "extra:" + e.getValue().getAsString());
        } catch (Exception ignored) {}
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        super.drawDefaultBackground();

        guiLeft = width  / 2 - xSize / 2;
        guiTop  = height / 2 - ySize / 2;

        GlStateManager.enableDepth();

        mc.getTextureManager().bindTexture(INVENTORY_TEX);
        GlStateManager.color(1, 1, 1, 1);
        Utils.drawTexturedRect(guiLeft, guiTop, xSize, ySize,
                0, xSize / 256f, 0, ySize / 256f, GL11.GL_NEAREST);

        for (InventoryButton btn : InventoryButtonStorage.getInstance().getButtons()) {
            int bx = guiLeft + btn.x + (btn.anchorRight  ? xSize : 0);
            int by = guiTop  + btn.y + (btn.anchorBottom ? ySize : 0);

            GlStateManager.color(1, 1, 1, btn.isActive() ? 1f : 0.5f);
            mc.getTextureManager().bindTexture(EDITOR_TEX);
            Utils.drawTexturedRect(bx, by, 18, 18,
                    btn.backgroundIndex * 18 / 256f,
                    (btn.backgroundIndex * 18 + 18) / 256f,
                    18 / 256f, 36 / 256f, GL11.GL_NEAREST);
            GlStateManager.color(1, 1, 1, 1);
            if (btn.isActive()) {
                if (btn.icon != null && !btn.icon.trim().isEmpty()) {
                    GlStateManager.enableDepth();
                    InvButtonIconRenderer.renderIcon(btn.icon, bx + 1, by + 1);
                }
            } else {
                fontRendererObj.drawString("+", bx + 6, by + 5, 0xffcccccc);
            }
        }

        drawClipboardPanel();
        drawPresetsPanel();
        if (editingButton != null) drawEditorPanel(mouseX, mouseY);
    }

    private void drawClipboardPanel() {
        int lx = guiLeft - 88 - 2 - 22;
        int ly = guiTop + 2;

        Gui.drawRect(lx, ly, lx + 88, ly + 20, validClipboard() ? 0xFF404040 : 0xFF282828);
        Gui.drawRect(lx + 1, ly + 1, lx + 87, ly + 19, 0xFF1a1a1a);
        fontRendererObj.drawString("Load preset",    lx + 4, ly + 4,  0xffaaaaaa);
        fontRendererObj.drawString("from Clipboard", lx + 4, ly + 12, 0xffaaaaaa);

        Gui.drawRect(lx, ly + 24, lx + 88, ly + 44, 0xFF404040);
        Gui.drawRect(lx + 1, ly + 25, lx + 87, ly + 43, 0xFF1a1a1a);
        fontRendererObj.drawString("Save preset",  lx + 4, ly + 28, 0xffaaaaaa);
        fontRendererObj.drawString("to Clipboard", lx + 4, ly + 36, 0xffaaaaaa);
    }

    private void drawPresetsPanel() {
        if (presetNames == null || presetNames.isEmpty()) return;
        int px = guiLeft + xSize + 22, py = guiTop;
        mc.getTextureManager().bindTexture(EDITOR_TEX);
        GlStateManager.color(1, 1, 1, 1);
        Utils.drawTexturedRect(px, py, 80, ySize,
                editorXSize / 256f, (editorXSize + 80) / 256f,
                41 / 256f, (41 + ySize) / 256f, GL11.GL_NEAREST);
        fontRendererObj.drawString("\u00a7nPresets", px + 8, py + 6, 0xffa0a0a0);
        for (int i = 0; i < presetNames.size(); i++)
            fontRendererObj.drawString(presetNames.get(i), px + 8, py + 20 + i * 10, 0xff909090);
    }

    private void drawEditorPanel(int mouseX, int mouseY) {
        int bx = guiLeft + editingButton.x + (editingButton.anchorRight  ? xSize : 0);
        int by = guiTop  + editingButton.y + (editingButton.anchorBottom ? ySize : 0);

        editorLeft = bx + 8 - editorXSize / 2;
        editorTop  = by + 18 + 2;
        boolean showArrow = true;
        if (editorTop + editorYSize + 5 > height) { editorTop = height - editorYSize - 5; showArrow = false; }
        if (editorLeft < 5)                        { editorLeft = 5;                       showArrow = false; }
        if (editorLeft + editorXSize + 5 > width)  { editorLeft = width - editorXSize - 5; showArrow = false; }

        GlStateManager.translate(0, 0, 300);

        mc.getTextureManager().bindTexture(EDITOR_TEX);
        GlStateManager.color(1, 1, 1, 1f);
        Utils.drawTexturedRect(editorLeft, editorTop, editorXSize, editorYSize,
                0, editorXSize / 256f, 41 / 256f, (41 + editorYSize) / 256f, GL11.GL_NEAREST);

        if (showArrow)
            Utils.drawTexturedRect(bx + 8 - 3, by + 18, 10, 5,
                    0, 6 / 256f, 36 / 256f, 41 / 256f, GL11.GL_NEAREST);

        fontRendererObj.drawString("Command", editorLeft + 7, editorTop + 7, 0xffa0a0a0);
        commandTextField.setSize(editorXSize - 14, 16);
        commandTextField.setText(commandTextField.getText().replaceAll("^ +", ""));
        commandTextField.setPrependText(commandTextField.getText().startsWith("/") ? "" : "\u00a77/\u00a7r");
        commandTextField.render(editorLeft + 7, editorTop + 19);

        fontRendererObj.drawString("Background", editorLeft + 7, editorTop + 40, 0xffa0a0a0);
        for (int i = 0; i < BACKGROUND_TYPES; i++) {
            if (i == editingButton.backgroundIndex)
                Gui.drawRect(editorLeft + 7 + 20 * i - 1, editorTop + 50 - 1,
                        editorLeft + 7 + 20 * i + 19, editorTop + 50 + 19, 0xff0000ff);
            mc.getTextureManager().bindTexture(EDITOR_TEX);
            GlStateManager.color(1, 1, 1, 1);
            Utils.drawTexturedRect(editorLeft + 7 + 20 * i, editorTop + 50, 18, 18,
                    i * 18 / 256f, (i * 18 + 18) / 256f, 0, 18 / 256f, GL11.GL_NEAREST);
        }

        fontRendererObj.drawString("Icon Type", editorLeft + 7, editorTop + 74, 0xffa0a0a0);
        String[] tabs = {"Items", "Skulls", "Extra"};
        for (int i = 0; i < ICON_TYPES; i++) {
            boolean sel = iconTypeIndex == i;
            Gui.drawRect(editorLeft + 7 + 48 * i, editorTop + 84,
                    editorLeft + 51 + 48 * i, editorTop + 96,
                    sel ? 0xff0055ff : 0xff333333);
            fontRendererObj.drawString(tabs[i], editorLeft + 9 + 48 * i, editorTop + 87,
                    sel ? 0xffffffff : 0xffaaaaaa);
        }

        fontRendererObj.drawString("Icon Search", editorLeft + 7, editorTop + 100, 0xffa0a0a0);
        iconTextField.render(editorLeft + 7, editorTop + 110);

        GlStateManager.enableDepth();
        ScaledResolution sr = new ScaledResolution(mc);
        GlScissorStack.push(editorLeft, editorTop + 130, editorLeft + editorXSize, editorTop + editorYSize - 8, sr);

        synchronized (searchedIcons) {
            int max = Math.max(0, (searchedIcons.size() - 1) / 6 * 20 - 40);
            int scroll = Math.min(itemScrollPx, max);

            int scrollBarH = searchedIcons.size() > 18
                    ? Math.max(2, (int) Math.ceil(3f * 54f / (searchedIcons.size() - 18))) : 54;
            if (scrollBarH > 54) scrollBarH = 54;
            int scrollBarY = (searchedIcons.size() > 18)
                    ? (int) Math.floor(54f * ((scroll / 20f) / ((searchedIcons.size() - 18) / 6f))) : 0;
            if (scrollBarY + scrollBarH > 54) scrollBarY = 54 - scrollBarH;
            Gui.drawRect(editorLeft + 137, editorTop + 139 + scrollBarY,
                    editorLeft + 139, editorTop + 139 + scrollBarY + scrollBarH, 0xff202020);

            int startIndex = Math.max(0, scroll / 20 * 6);
            int endIndex = Math.min(searchedIcons.size(), startIndex + 24);

            for (int i = startIndex; i < endIndex; i++) {
                int ix = editorLeft + 12 + ((i - startIndex) % 6) * 20;
                int iy = editorTop + 137 + ((i - startIndex) / 6) * 20 - (itemScrollPx % 20);
                mc.getTextureManager().bindTexture(EDITOR_TEX);
                GlStateManager.color(1, 1, 1, 1);
                Utils.drawTexturedRect(ix, iy, 18, 18, 18 / 256f, 36 / 256f, 0, 18 / 256f, GL11.GL_NEAREST);
                InvButtonIconRenderer.renderIcon(searchedIcons.get(i), ix + 1, iy + 1);
            }
        }

        GlScissorStack.pop(sr);
        GlStateManager.translate(0, 0, -300);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0 && editingButton != null) {
            scroll = -scroll;
            if (scroll > 1) scroll = 8; if (scroll < -1) scroll = -8;
            itemScrollPx += scroll * 2;
            if (itemScrollPx < 0) itemScrollPx = 0;
            synchronized (searchedIcons) {
                int max = Math.max(0, (searchedIcons.size() - 1) / 6 * 20 - 40);
                if (itemScrollPx > max) itemScrollPx = max;
            }
        }
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        super.mouseClicked(mx, my, btn);

        if (editingButton != null
                && mx >= editorLeft && mx <= editorLeft + editorXSize
                && my >= editorTop  && my <= editorTop  + editorYSize) {

            // command field
            if (mx >= editorLeft + 7 && mx <= editorLeft + editorXSize - 7
                    && my >= editorTop + 12 && my <= editorTop + 35) {
                commandTextField.mouseClicked(mx, my, btn);
                iconTextField.unfocus();
                editingButton.command = commandTextField.getText();
                InventoryButtonStorage.getInstance().save();
                return;
            }
            // icon search field
            if (mx >= editorLeft + 7 && mx <= editorLeft + editorXSize - 7
                    && my >= editorTop + 110 && my <= editorTop + 130) {
                iconTextField.mouseClicked(mx, my, btn);
                if (btn == 1) search();
                commandTextField.unfocus();
                return;
            }
            // background selector
            if (my >= editorTop + 50 && my <= editorTop + 68) {
                for (int i = 0; i < BACKGROUND_TYPES; i++)
                    if (mx >= editorLeft + 7 + 20 * i && mx <= editorLeft + 25 + 20 * i) {
                        editingButton.backgroundIndex = i;
                        InventoryButtonStorage.getInstance().save();
                        return;
                    }
            }
            // icon type tabs
            if (my >= editorTop + 84 && my <= editorTop + 96) {
                for (int i = 0; i < ICON_TYPES; i++)
                    if (mx >= editorLeft + 7 + 48 * i && mx <= editorLeft + 51 + 48 * i) {
                        if (iconTypeIndex != i) { iconTypeIndex = i; search(); }
                        return;
                    }
            }
            // icon grid click
            if (my >= editorTop + 130 && my <= editorTop + editorYSize - 8) {
                synchronized (searchedIcons) {
                    int max = Math.max(0, (searchedIcons.size() - 1) / 6 * 20 - 40);
                    int scroll = Math.min(itemScrollPx, max);
                    int startIndex = Math.max(0, scroll / 20 * 6);
                    int endIndex = Math.min(searchedIcons.size(), startIndex + 24);
                    for (int i = startIndex; i < endIndex; i++) {
                        int ix = editorLeft + 12 + ((i - startIndex) % 6) * 20;
                        int iy = editorTop + 137 + ((i - startIndex) / 6) * 20 - (itemScrollPx % 20);
                        if (mx >= ix && mx <= ix + 18 && my >= iy && my <= iy + 18) {
                            editingButton.icon = searchedIcons.get(i);
                            InventoryButtonStorage.getInstance().save();
                            return;
                        }
                    }
                }
            }
            return;
        }

        // click on button slot
        for (InventoryButton b : InventoryButtonStorage.getInstance().getButtons()) {
            int bx = guiLeft + b.x + (b.anchorRight  ? xSize : 0);
            int by = guiTop  + b.y + (b.anchorBottom ? ySize : 0);
            if (mx >= bx && mx <= bx + 18 && my >= by && my <= by + 18) {
                if (editingButton == b) {
                    editingButton = null;
                } else {
                    editingButton = b;
                    commandTextField.setText(b.command != null ? b.command : "");
                    iconTextField.setText("");
                    itemScrollPx = 0;
                    search();
                }
                return;
            }
        }

        // clipboard buttons
        int lx = guiLeft - 88 - 2 - 22, ly = guiTop + 2;
        if (mx >= lx && mx <= lx + 88) {
            if (my >= ly && my <= ly + 20 && validClipboard()) { loadFromClipboard(); return; }
            if (my >= ly + 24 && my <= ly + 44)               { saveToClipboard();   return; }
        }

        // preset click
        if (editingButton == null && presetNames != null) {
            int px = guiLeft + xSize + 22, py = guiTop;
            for (int i = 0; i < presetNames.size(); i++)
                if (mx >= px + 8 && mx <= px + 72 && my >= py + 16 + i * 10 && my <= py + 26 + i * 10) {
                    InventoryButtonStorage.getInstance().setButtons(new ArrayList<>(presetButtonsList.get(i)));
                    return;
                }
        }

        editingButton = null;
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        super.keyTyped(c, key);
        if (editingButton == null) return;
        if (commandTextField.getFocus()) {
            commandTextField.keyTyped(c, key);
            editingButton.command = commandTextField.getText();
            InventoryButtonStorage.getInstance().save();
        } else if (iconTextField.getFocus()) {
            String before = iconTextField.getText();
            iconTextField.keyTyped(c, key);
            if (!before.equals(iconTextField.getText())) search();
        }
    }

    @Override
    public void onGuiClosed() { InventoryButtonStorage.getInstance().save(); }

    private void search() {
        final int gen  = searchId.incrementAndGet();
        final String q = iconTextField.getText().trim().toLowerCase();
        final int type = iconTypeIndex;

        searchES.submit(() -> {
            if (searchId.get() != gen) return;
            List<String> results = new ArrayList<>();

            if (type == 0) {
                SkyblockItemCache cache = SkyblockItemCache.getInstance();
                Iterable<String> ids = cache.isLoaded() ? cache.getAllItemIds() : getBuiltinItemIds();
                for (String id : ids) {
                    if (q.isEmpty() || id.toLowerCase().contains(q)) results.add(id);
                }
            } else if (type == 1) {
                SkyblockItemCache cache = SkyblockItemCache.getInstance();
                Map<String, String> skulls = cache.isLoaded() ? cache.getSkullItems() : getBuiltinSkulls();
                for (Map.Entry<String, String> e : skulls.entrySet()) {
                    if (q.isEmpty() || e.getKey().toLowerCase().contains(q))
                        results.add("skull:" + e.getValue());
                }
            } else {
                if (extraIconsCache != null)
                    for (Map.Entry<String, String> e : extraIconsCache.entrySet())
                        if (q.isEmpty() || e.getKey().toLowerCase().contains(q))
                            results.add(e.getValue());
            }

            if (searchId.get() != gen) return;
            synchronized (searchedIcons) { searchedIcons.clear(); searchedIcons.addAll(results); }
            itemScrollPx = 0;
        });
    }

    private boolean validClipboard() {
        try {
            String raw = getClipboard();
            if (raw == null || raw.trim().isEmpty()) return false;
            String decoded = new String(Base64.getDecoder().decode(raw.trim()), StandardCharsets.UTF_8);
            int b = decoded.indexOf('[');
            if (b == -1) return false;
            new JsonParser().parse(decoded.substring(b)).getAsJsonArray();
            return true;
        } catch (Exception e) { return false; }
    }

    private void loadFromClipboard() {
        try {
            String raw = getClipboard();
            if (raw == null) return;
            String decoded = new String(Base64.getDecoder().decode(raw.trim()), StandardCharsets.UTF_8);
            int b = decoded.indexOf('[');
            if (b == -1) return;
            JsonArray arr = new JsonParser().parse(decoded.substring(b)).getAsJsonArray();
            List<InventoryButton> loaded = new ArrayList<>();
            for (JsonElement el : arr) {
                InventoryButton btn = null;
                if (el.isJsonObject()) {
                    btn = GSON.fromJson(el.getAsJsonObject(), InventoryButton.class);
                } else if (el.isJsonPrimitive()) {
                    try {
                        JsonElement inner = new JsonParser().parse(el.getAsString());
                        if (inner.isJsonObject()) btn = GSON.fromJson(inner.getAsJsonObject(), InventoryButton.class);
                    } catch (Exception ignored) {}
                }
                if (btn != null) loaded.add(btn);
            }
            if (!loaded.isEmpty()) InventoryButtonStorage.getInstance().setButtons(loaded);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void saveToClipboard() {
        try {
            String json = GSON.toJson(InventoryButtonStorage.getInstance().getButtons());
            String b64  = Base64.getEncoder().encodeToString(
                    (SHARE_PREFIX + json).getBytes(StandardCharsets.UTF_8));
            Utils.copyToClipboard(b64);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static String getClipboard() {
        try {
            return (String) java.awt.Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getData(java.awt.datatransfer.DataFlavor.stringFlavor);
        } catch (Exception e) { return null; }
    }

    private static List<String> getBuiltinItemIds() {
        return Arrays.asList("WORKBENCH", "ENDER_CHEST", "CHEST", "COMPASS",
                "GOLD_BLOCK", "EMERALD", "IRON_SWORD", "BOOK", "NETHER_STAR");
    }

    private static Map<String, String> getBuiltinSkulls() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("personal bank",  "e36e94f6c34a35465fce4a90f2e25976389eb9709a12273574ff70fd4daa6852");
        m.put("skyblock hub",   "d7cc6687423d0570d556ac53e0676cb563bbdd9717cd8269bdebed6f6d4e7bf8");
        m.put("private island", "c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56");
        m.put("dungeon hub",    "9b56895b9659896ad647f58599238af532d46db9c1b0389b8bbeb70999dab33d");
        m.put("dwarven mines",  "51539dddf9ed255ece6348193cd75012c82c93aec381f05572cecf7379711b3b");
        m.put("bazaar",         "c232e3820897429157619b0ee099fec0628f602fff12b695de54aef11d923ad7");
        m.put("crimson isle",   "c3687e25c632bce8aa61e0d64c24e694c3eea629ea944f4cf30dcfb4fbce071");
        m.put("garden",         "f4880d2c1e7b86e87522e20882656f45bafd42f94932b2c5e0d6ecaa490cb4c");
        m.put("museum",         "438cf3f8e54afc3b3f91d20a49f324dca1486007fe545399055524c17941f4dc");
        return m;
    }
}