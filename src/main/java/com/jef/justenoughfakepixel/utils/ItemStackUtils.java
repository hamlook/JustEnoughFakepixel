// Credit: Skytils (https://github.com/Skytils/SkytilsMod) (AGPLv3)

package com.jef.justenoughfakepixel.utils;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.events.RenderItemOverlayEvent;
import com.jef.justenoughfakepixel.utils.ColorUtils;
import com.jef.justenoughfakepixel.utils.ItemUtils;
import com.jef.justenoughfakepixel.utils.RomanNumeralParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemStackUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onItemOverlay(RenderItemOverlayEvent event) {
        if (JefConfig.feature == null) return;
        String tip = getTip(event.stack);
        if (tip == null || tip.isEmpty()) return;
        drawTip(tip, event.x, event.y);
    }

    private String getTip(ItemStack stack) {
        String id = ItemUtils.getInternalName(stack);
        if (id.isEmpty()) id = null;
        if (id == null) return null;

        if (id.equals("ENCHANTED_BOOK") && JefConfig.feature.misc.itemStackTips) {
            return getEnchantLevel(stack);
        }

        if (JefConfig.feature.misc.itemStackTips && isInContainer("Catacombs Gate")) {
            return getDungeonFloor(id);
        }

        return null;
    }

    /** Extracts the last word of the display name and converts it from roman numeral to integer. */
    private String getEnchantLevel(ItemStack stack) {
        String name = ColorUtils.stripColor(stack.getDisplayName());
        if (name.isEmpty()) return null;
        String[] parts = name.split(" ");
        String last = parts[parts.length - 1];
        // roman numerals are all uppercase letters only
        if (last.isEmpty() || !last.chars().allMatch(c -> "IVXLCDM".indexOf(c) >= 0)) return null;
        if (!RomanNumeralParser.isValid(last)) return null;
        int level = RomanNumeralParser.parse(last);
        return level > 0 ? String.valueOf(level) : null;
    }

    private String getDungeonFloor(String id) {
        String suffix = null;
        if (id.startsWith("MASTER_CATACOMBS_PASS_")) {
            suffix = id.substring("MASTER_CATACOMBS_PASS_".length());
        } else if (id.startsWith("CATACOMBS_PASS_")) {
            suffix = id.substring("CATACOMBS_PASS_".length());
        }
        if (suffix == null) return null;
        try {
            int floor = Integer.parseInt(suffix) - 3;
            return floor > 0 ? String.valueOf(floor) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void drawTip(String tip, int x, int y) {
        FontRenderer fr = mc.fontRendererObj;
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        fr.drawStringWithShadow(tip, x + 17 - fr.getStringWidth(tip), y + 9, 0xFFFFFF);
        GlStateManager.enableDepth();
    }

    private boolean isInContainer(String name) {
        if (!(mc.currentScreen instanceof GuiChest)) return false;
        ContainerChest cc = (ContainerChest) ((GuiChest) mc.currentScreen).inventorySlots;
        return name.equals(ColorUtils.stripColor(cc.getLowerChestInventory().getDisplayName().getUnformattedText()));
    }

}