package com.jef.justenoughfakepixel.features.diana;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.function.IntSupplier;

/**
 * A single position editor that lets you drag all 4 Diana overlays independently.
 * Click and drag any overlay to move it. Arrow keys move the last clicked overlay.
 * Press R to reset all positions.
 */
public class GuiDianaOverlayEditor extends GuiScreen {

    private final GuiScreen parentScreen;
    private final Runnable saveCallback;

    private static class OverlayEntry {
        final String label;
        final Position position;
        final Position originalPosition;
        final IntSupplier w;
        final IntSupplier h;
        final Runnable renderer;

        OverlayEntry(String label, Position position, IntSupplier w, IntSupplier h, Runnable renderer) {
            this.label = label;
            this.position = position;
            this.originalPosition = position.clone();
            this.w = w;
            this.h = h;
            this.renderer = renderer;
        }

        int scaledW(float scale) { return (int)(w.getAsInt() * scale); }
        int scaledH(float scale) { return (int)(h.getAsInt() * scale); }
    }

    private final OverlayEntry[] overlays;
    private int draggedIndex = -1;
    private int focusedIndex = -1;
    private int grabbedX, grabbedY;

    public GuiDianaOverlayEditor(GuiScreen parent, Runnable saveCallback) {
        this.parentScreen = parent;
        this.saveCallback = saveCallback;

        DianaEventOverlay    event  = DianaEventOverlay.getInstance();
        DianaLootOverlay     loot   = DianaLootOverlay.getInstance();
        InqHealthOverlay     inq    = InqHealthOverlay.getInstance();
        DianaMobHealthOverlay mob   = DianaMobHealthOverlay.getInstance();

        float scale = JefConfig.feature.diana.overlayScale;

        overlays = new OverlayEntry[]{
                new OverlayEntry("Event",    JefConfig.feature.diana.eventOverlayPos,  event::getOverlayWidth, event::getOverlayHeight, () -> event.render(true)),
                new OverlayEntry("Loot",     JefConfig.feature.diana.lootOverlayPos,   loot::getOverlayWidth,  loot::getOverlayHeight,  () -> loot.render(true)),
                new OverlayEntry(" ",   JefConfig.feature.diana.inqHealthPos,     inq::getOverlayWidth,   inq::getOverlayHeight,   () -> inq.render(true)),
                new OverlayEntry(" ",   JefConfig.feature.diana.dianaMobHealthPos,mob::getOverlayWidth,   mob::getOverlayHeight,   () -> mob.render(true)),
        };
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        this.width  = sr.getScaledWidth();
        this.height = sr.getScaledHeight();
        mouseX = Mouse.getX() * width  / mc.displayWidth;
        mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;

        drawDefaultBackground();

        float scale = JefConfig.feature.diana.overlayScale;

        // Drag active overlay
        if (draggedIndex >= 0) {
            OverlayEntry e = overlays[draggedIndex];
            grabbedX += e.position.moveX(mouseX - grabbedX, e.scaledW(scale), sr);
            grabbedY += e.position.moveY(mouseY - grabbedY, e.scaledH(scale), sr);
        }

        // Render all overlays + highlight boxes
        for (int i = 0; i < overlays.length; i++) {
            OverlayEntry e = overlays[i];
            e.renderer.run();

            int x = e.position.getAbsX(sr, e.scaledW(scale));
            int y = e.position.getAbsY(sr, e.scaledH(scale));
            if (e.position.isCenterX()) x -= e.scaledW(scale) / 2;
            if (e.position.isCenterY()) y -= e.scaledH(scale) / 2;

            int boxColor = 0x80404040;
            Gui.drawRect(x, y, x + e.scaledW(scale), y + e.scaledH(scale), boxColor);
            mc.fontRendererObj.drawStringWithShadow(e.label, x + 2, y + 2, 0xFFFFFF);
        }

        Utils.drawStringCentered("Diana Overlay Editor", mc.fontRendererObj, width / 2, 8,  true, 0xFFFFFF);
        Utils.drawStringCentered("Drag overlays to move | R = reset all | ESC = back", mc.fontRendererObj, width / 2, 18, true, 0xAAAAAA);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;

        ScaledResolution sr = new ScaledResolution(mc);
        mouseX = Mouse.getX() * width  / mc.displayWidth;
        mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;
        float scale = JefConfig.feature.diana.overlayScale;

        for (int i = 0; i < overlays.length; i++) {
            OverlayEntry e = overlays[i];
            int x = e.position.getAbsX(sr, e.scaledW(scale));
            int y = e.position.getAbsY(sr, e.scaledH(scale));
            if (e.position.isCenterX()) x -= e.scaledW(scale) / 2;
            if (e.position.isCenterY()) y -= e.scaledH(scale) / 2;

            if (mouseX >= x && mouseX <= x + e.scaledW(scale) && mouseY >= y && mouseY <= y + e.scaledH(scale)) {
                draggedIndex = i;
                focusedIndex = i;
                grabbedX = mouseX;
                grabbedY = mouseY;
                break;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (draggedIndex >= 0) {
            saveCallback.run();
            draggedIndex = -1;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (draggedIndex < 0) return;

        ScaledResolution sr = new ScaledResolution(mc);
        mouseX = Mouse.getX() * width  / mc.displayWidth;
        mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;
        float scale = JefConfig.feature.diana.overlayScale;

        OverlayEntry e = overlays[draggedIndex];
        grabbedX += e.position.moveX(mouseX - grabbedX, e.scaledW(scale), sr);
        grabbedY += e.position.moveY(mouseY - grabbedY, e.scaledH(scale), sr);
        saveCallback.run();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        Keyboard.enableRepeatEvents(true);

        if (keyCode == Keyboard.KEY_ESCAPE) {
            saveCallback.run();
            mc.displayGuiScreen(parentScreen);
            return;
        }

        if (keyCode == Keyboard.KEY_R) {
            for (OverlayEntry e : overlays) e.position.set(e.originalPosition);
            saveCallback.run();
        }

        if (focusedIndex >= 0) {
            OverlayEntry e = overlays[focusedIndex];
            ScaledResolution sr = new ScaledResolution(mc);
            float scale = JefConfig.feature.diana.overlayScale;
            boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            int dist = shift ? 10 : 1;
            if      (keyCode == Keyboard.KEY_DOWN)  e.position.moveY( dist,  e.scaledH(scale), sr);
            else if (keyCode == Keyboard.KEY_UP)    e.position.moveY(-dist,  e.scaledH(scale), sr);
            else if (keyCode == Keyboard.KEY_LEFT)  e.position.moveX(-dist,  e.scaledW(scale), sr);
            else if (keyCode == Keyboard.KEY_RIGHT) e.position.moveX( dist,  e.scaledW(scale), sr);
            saveCallback.run();
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}