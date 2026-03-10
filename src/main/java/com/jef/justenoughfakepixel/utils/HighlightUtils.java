package com.jef.justenoughfakepixel.utils;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class HighlightUtils {

    private static final Pattern STRIP_CODES_PATTERN = Pattern.compile("(?i)§.");

    public static void renderHighlight(ItemStack stack, int x, int y, String searchText) {
        if (JefConfig.feature == null || !JefConfig.feature.misc.searchBar) return;
        if (searchText == null || searchText.trim().isEmpty()) return;
        if (stack == null || stack.getItem() == null) return;
        if (!matches(stack, searchText.trim().toLowerCase(Locale.ROOT))) return;

        String colorStr = JefConfig.feature.misc.searchBarHighlightColor;
        int highlightColor = ChromaColour.specialToChromaRGB(colorStr);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Gui.drawRect(x, y, x + 16, y + 16, highlightColor);

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
    }

    private static boolean matches(ItemStack stack, String query) {
        if (stack == null) return false;

        String display = stack.getDisplayName();
        if (display != null && stripCodes(display).toLowerCase(Locale.ROOT).contains(query))
            return true;

        List<String> tooltip = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
        if (tooltip != null) {
            for (String line : tooltip) {
                if (line != null && stripCodes(line).toLowerCase(Locale.ROOT).contains(query))
                    return true;
            }
        }

        return false;
    }

    private static String stripCodes(String s) {
        return s == null ? "" : STRIP_CODES_PATTERN.matcher(s).replaceAll("");
    }
}
