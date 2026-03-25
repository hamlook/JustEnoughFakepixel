package com.jef.justenoughfakepixel.features.mining;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterEvents
public class HotmPowderDisplay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final String HOTM_TITLE = "Heart of the Mountain";

    private static final Pattern LEVEL_PATTERN = Pattern.compile("§(.)Level (\\d+)");

    private static final Pattern COST_PATTERN = Pattern.compile("^(?:§.)*§7Cost$");

    private static final String SHIFT_HINT =
            EnumChatFormatting.DARK_GRAY + "[SHIFT: powder for next 10 levels]";

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (JefConfig.feature == null) return;
        if (!JefConfig.feature.mining.hotmPowderSpent && !JefConfig.feature.mining.hotmPowderFor10Levels) return;
        if (event.toolTip == null || event.itemStack == null) return;
        if (!HOTM_TITLE.equals(getContainerName())) return;

        String displayName = ColorUtils.stripColor(event.itemStack.getDisplayName());
        HotmPerkData perk = HotmPerkData.findByGuiName(displayName);
        if (perk == null) return;

        int rawLevel = parseRawLevel(event.toolTip);
        if (rawLevel < 0) return;
        rawLevel = Math.min(rawLevel, perk.maxLevel);

        List<String> tip = event.toolTip;

        if (JefConfig.feature.mining.hotmPowderSpent) {
            tip.add(2, buildSpentLine(perk, rawLevel));
        }

        if (JefConfig.feature.mining.hotmPowderFor10Levels && rawLevel < perk.maxLevel) {
            int costIndex = indexOfCostLine(tip);

            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                if (!tip.contains(SHIFT_HINT)) tip.add(SHIFT_HINT);
            } else {
                tip.remove(SHIFT_HINT);

                int targetLevel  = Math.min(rawLevel + 10, perk.maxLevel);
                int levels       = targetLevel - rawLevel;
                long cost10      = perk.calculateTotalCost(targetLevel) - perk.calculateTotalCost(rawLevel);
                String levelWord = levels == 1 ? "level" : "levels";

                String line = "§7" + perk.powderType.displayName + " for "
                        + levels + " " + levelWord + ": §e" + formatNumber(cost10);

                int insertAt = costIndex >= 0 ? costIndex + 2 : tip.size();
                if (insertAt > tip.size()) insertAt = tip.size();
                tip.add(insertAt, line);
            }
        }
    }

    private static String buildSpentLine(HotmPerkData perk, int rawLevel) {
        long spent   = perk.calculateTotalCost(rawLevel);
        long max     = perk.totalCostMaxLevel;
        boolean isMax = rawLevel >= perk.maxLevel;
        double pct    = max > 0 ? (spent * 100.0 / max) : 100.0;

        String label = "§7" + perk.powderType.displayName + " spent: ";

        switch (JefConfig.feature.mining.hotmPowderSpentDesign) {
            case 1:
                return isMax
                        ? label + "§e" + fmt2(pct) + "% §7(§aMax level§7)"
                        : label + "§e" + fmt2(pct) + "%§7 of max";
            case 2:
                return isMax
                        ? label + "§e" + formatNumber(max) + " §7(§aMax level§7)"
                        : label + "§e" + formatNumber(spent) + "§7/§e" + formatNumber(max)
                                + "§7 (§e" + fmt2(pct) + "%§7)";
            default:
                return isMax
                        ? label + "§e" + formatNumber(max) + " §7(§aMax level§7)"
                        : label + "§e" + formatNumber(spent) + "§7 / §e" + formatNumber(max);
        }
    }

    private static int parseRawLevel(List<String> tooltip) {
        for (String line : tooltip) {
            Matcher m = LEVEL_PATTERN.matcher(line);
            if (!m.find()) continue;
            try {
                int level  = Integer.parseInt(m.group(2));
                boolean blueEgg = m.group(1).equals("b");
                return blueEgg ? Math.max(0, level - 1) : level;
            } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private static int indexOfCostLine(List<String> tooltip) {
        for (int i = 0; i < tooltip.size(); i++) {
            if (COST_PATTERN.matcher(tooltip.get(i)).matches()) return i;
        }
        return -1;
    }

    private static String getContainerName() {
        if (!(mc.currentScreen instanceof GuiChest)) return null;
        try {
            ContainerChest cc = (ContainerChest) ((GuiChest) mc.currentScreen).inventorySlots;
            return ColorUtils.stripColor(cc.getLowerChestInventory().getDisplayName().getUnformattedText());
        } catch (Exception e) {
            return null;
        }
    }

    private static String formatNumber(long n) {
        return String.format("%,d", n);
    }

    private static String fmt2(double d) {
        return String.format("%.2f", d);
    }
}