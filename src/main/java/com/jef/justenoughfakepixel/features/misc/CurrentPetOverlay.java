package com.jef.justenoughfakepixel.features.misc;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.core.config.utils.Position;
import com.jef.justenoughfakepixel.utils.ItemUtils;
import com.jef.justenoughfakepixel.utils.JefOverlay;
import com.jef.justenoughfakepixel.utils.OverlayUtils;
import com.jef.justenoughfakepixel.utils.ScoreboardUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Collections;
import java.util.List;

public class CurrentPetOverlay extends JefOverlay {

    private static final int SKULL_SIZE = 16;
    private static final int GAP        = 4;

    private static CurrentPetOverlay instance;

    public CurrentPetOverlay() {
        super(160, SKULL_SIZE + PADDING * 2);
        instance = this;
    }

    public static CurrentPetOverlay getInstance() { return instance; }

    @Override public Position getPosition()     { return JefConfig.feature.misc.currentPetPos; }
    @Override public float    getScale()        { return JefConfig.feature.misc.currentPetScale; }
    @Override public int      getBgColor()      { return ChromaColour.specialToChromaRGB(JefConfig.feature.misc.currentPetBgColor); }
    @Override public int      getCornerRadius() { return JefConfig.feature.misc.currentPetCornerRadius; }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (JefConfig.feature == null || !JefConfig.feature.misc.showCurrentPet) return;
        if (!ScoreboardUtils.isOnSkyblock()) return;
        if (OverlayUtils.shouldHide()) return;
        render(false);
    }

    @Override
    public List<String> getLines(boolean preview) {
        return Collections.emptyList();
    }

    @Override
    public void render(boolean preview) {
        if (JefConfig.feature == null) return;
        if (!preview && OverlayUtils.shouldHide()) return;

        Minecraft mc = Minecraft.getMinecraft();

        String formattedName;
        ItemStack skullItem = null;

        if (preview) {
            formattedName = "\u00a76[Lvl 100] Tiger";
        } else {
            String baseName = CurrentPetTracker.getInstance().getCurrentBaseName();
            if (baseName.isEmpty()) return;

            CachedPet cached = PetCache.getInstance().get(baseName);
            formattedName = (cached != null && !cached.formattedName.isEmpty())
                    ? cached.formattedName : baseName;

            if (cached != null && !cached.textureValue.isEmpty())
                skullItem = ItemUtils.createSkullWithTexture(cached.textureValue);
        }

        float scale = getScale();
        int textW = mc.fontRendererObj.getStringWidth(formattedName);
        int w = SKULL_SIZE + GAP + textW + PADDING * 2;
        int h = SKULL_SIZE + PADDING * 2;
        lastW = w;
        lastH = h;

        ScaledResolution sr  = new ScaledResolution(mc);
        Position         pos = getPosition();
        int x = pos.getAbsX(sr, (int)(w * scale));
        int y = pos.getAbsY(sr, (int)(h * scale));
        if (pos.isCenterX()) x -= (int)(w * scale / 2);
        if (pos.isCenterY()) y -= (int)(h * scale / 2);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(scale, scale, 1f);

        int bgColor = getBgColor();
        if ((bgColor >>> 24) != 0)
            drawRoundedRect(-PADDING, -PADDING, w, h - PADDING, getCornerRadius(), bgColor);

        if (skullItem != null) {
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            mc.getRenderItem().renderItemAndEffectIntoGUI(skullItem, 0, 0);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
        } else {
            Gui.drawRect(0, 0, SKULL_SIZE, SKULL_SIZE, 0xFF555555);
        }

        int textY = (SKULL_SIZE - mc.fontRendererObj.FONT_HEIGHT) / 2;
        mc.fontRendererObj.drawStringWithShadow(formattedName, SKULL_SIZE + GAP, textY, 0xFFFFFF);

        GL11.glPopMatrix();
    }
}