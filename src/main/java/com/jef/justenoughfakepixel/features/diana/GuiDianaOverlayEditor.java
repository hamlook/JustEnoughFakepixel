package com.jef.justenoughfakepixel.features.diana;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.utils.Utils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class GuiDianaOverlayEditor extends GuiScreen {

    private final GuiScreen parentScreen;
    private final Runnable  saveCallback;

    private static class OverlayEntry {
        final String          label;
        final Position        position;
        final Position        originalPosition;
        final IntSupplier     w, h;
        final Supplier<Float> scaleSupplier;
        final Runnable        renderer;

        OverlayEntry(String label, Position position,
                     IntSupplier w, IntSupplier h,
                     Supplier<Float> scaleSupplier,
                     Runnable renderer) {
            this.label            = label;
            this.position         = position;
            this.originalPosition = position.clone();
            this.w                = w;
            this.h                = h;
            this.scaleSupplier    = scaleSupplier;
            this.renderer         = renderer;
        }

        float scale()  { return scaleSupplier.get(); }
        int   scaledW() { return (int)(w.getAsInt() * scale()); }
        int   scaledH() { return (int)(h.getAsInt() * scale()); }
    }

    private final OverlayEntry[] overlays;
    private int draggedIndex = -1;
    private int focusedIndex = -1;
    private int grabbedX, grabbedY;

    public GuiDianaOverlayEditor(GuiScreen parent, Runnable saveCallback) {
        this.parentScreen = parent;
        this.saveCallback = saveCallback;

        DianaEventOverlay     event = DianaEventOverlay.getInstance();
        DianaLootOverlay      loot  = DianaLootOverlay.getInstance();
        InqHealthOverlay      inq   = InqHealthOverlay.getInstance();
        DianaMobHealthOverlay mob   = DianaMobHealthOverlay.getInstance();

        overlays = new OverlayEntry[]{
                new OverlayEntry("Event", JefConfig.feature.diana.eventOverlayPos,
                        event::getOverlayWidth, event::getOverlayHeight,
                        () -> JefConfig.feature.diana.eventScale,
                        () -> event.render(true)),

                new OverlayEntry("Loot", JefConfig.feature.diana.lootOverlayPos,
                        loot::getOverlayWidth, loot::getOverlayHeight,
                        () -> JefConfig.feature.diana.lootScale,
                        () -> loot.render(true)),

                new OverlayEntry(" ", JefConfig.feature.diana.inqHealthPos,
                        inq::getOverlayWidth, inq::getOverlayHeight,
                        () -> JefConfig.feature.diana.inqScale,
                        () -> inq.render(true)),

                new OverlayEntry(" ", JefConfig.feature.diana.dianaMobHealthPos,
                        mob::getOverlayWidth, mob::getOverlayHeight,
                        () -> JefConfig.feature.diana.mobScale,
                        () -> mob.render(true)),
        };
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        ScaledResolution sr = new ScaledResolution(mc);
        this.width  = sr.getScaledWidth();
        this.height = sr.getScaledHeight();
        mouseX = Mouse.getX() * width  / mc.displayWidth;
        mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;

        drawDefaultBackground();

        if (draggedIndex >= 0) {
            OverlayEntry e = overlays[draggedIndex];
            grabbedX += e.position.moveX(mouseX - grabbedX, e.scaledW(), sr);
            grabbedY += e.position.moveY(mouseY - grabbedY, e.scaledH(), sr);
        }

        for (OverlayEntry e : overlays) {
            e.renderer.run();
            int x = e.position.getAbsX(sr, e.scaledW());
            int y = e.position.getAbsY(sr, e.scaledH());
            if (e.position.isCenterX()) x -= e.scaledW() / 2;
            if (e.position.isCenterY()) y -= e.scaledH() / 2;
            Gui.drawRect(x, y, x + e.scaledW(), y + e.scaledH(), 0x80404040);
            mc.fontRendererObj.drawStringWithShadow(e.label, x + 2, y + 2, 0xFFFFFF);
        }

        Utils.drawStringCentered("Diana Overlay Editor",                               mc.fontRendererObj, width / 2,  8, true, 0xFFFFFF);
        Utils.drawStringCentered("Drag overlays to move | R = reset all | ESC = back", mc.fontRendererObj, width / 2, 18, true, 0xAAAAAA);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;

        ScaledResolution sr = new ScaledResolution(mc);
        mouseX = Mouse.getX() * width  / mc.displayWidth;
        mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;

        for (int i = 0; i < overlays.length; i++) {
            OverlayEntry e = overlays[i];
            int x = e.position.getAbsX(sr, e.scaledW());
            int y = e.position.getAbsY(sr, e.scaledH());
            if (e.position.isCenterX()) x -= e.scaledW() / 2;
            if (e.position.isCenterY()) y -= e.scaledH() / 2;
            if (mouseX >= x && mouseX <= x + e.scaledW() && mouseY >= y && mouseY <= y + e.scaledH()) {
                draggedIndex = focusedIndex = i;
                grabbedX = mouseX;
                grabbedY = mouseY;
                break;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (draggedIndex >= 0) { saveCallback.run(); draggedIndex = -1; }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (draggedIndex < 0) return;

        ScaledResolution sr = new ScaledResolution(mc);
        mouseX = Mouse.getX() * width  / mc.displayWidth;
        mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;

        OverlayEntry e = overlays[draggedIndex];
        grabbedX += e.position.moveX(mouseX - grabbedX, e.scaledW(), sr);
        grabbedY += e.position.moveY(mouseY - grabbedY, e.scaledH(), sr);
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
            int dist = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) ? 10 : 1;
            if      (keyCode == Keyboard.KEY_DOWN)  e.position.moveY( dist, e.scaledH(), sr);
            else if (keyCode == Keyboard.KEY_UP)    e.position.moveY(-dist, e.scaledH(), sr);
            else if (keyCode == Keyboard.KEY_LEFT)  e.position.moveX(-dist, e.scaledW(), sr);
            else if (keyCode == Keyboard.KEY_RIGHT) e.position.moveX( dist, e.scaledW(), sr);
            saveCallback.run();
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}