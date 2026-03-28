package com.jef.justenoughfakepixel.core.config.editors;

import com.jef.justenoughfakepixel.JefMod;
import com.jef.justenoughfakepixel.core.config.gui.config.ConfigProcessor;
import com.jef.justenoughfakepixel.core.config.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiOptionEditorVersionDisplay extends GuiOptionEditor {

    private static final int HEIGHT = 40;

    public GuiOptionEditorVersionDisplay(ConfigProcessor.ProcessedOption option) {
        super(option);
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void render(int x, int y, int width) {
        Minecraft mc = Minecraft.getMinecraft();

        RenderUtils.drawFloatingRectDark(x, y, width, HEIGHT, true);

        String versionText = "v" + JefMod.VERSION;

        int fontHeight = mc.fontRendererObj.FONT_HEIGHT;
        int scaledHeight = fontHeight * 2;

        // ── CENTERED 2x VERSION TEXT ─────────────────
        GlStateManager.pushMatrix();

        int centerX = x + width / 2;
        int centerY = y + (HEIGHT - scaledHeight) / 2;

        GlStateManager.translate(centerX, centerY, 0);
        GlStateManager.scale(2f, 2f, 1f);

        int textWidth = mc.fontRendererObj.getStringWidth(versionText);

        mc.fontRendererObj.drawString(
                versionText,
                -textWidth / 2,
                0,
                0x55FF55,
                false
        );

        GlStateManager.popMatrix();
    }

    @Override
    public boolean mouseInput(int x, int y, int width, int mouseX, int mouseY) {
        return false;
    }

    @Override
    public boolean keyboardInput() {
        return false;
    }
}