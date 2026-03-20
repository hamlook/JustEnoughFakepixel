package com.jef.justenoughfakepixel.features.mining;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.ChatUtils;
import com.jef.justenoughfakepixel.utils.ItemUtils;
import com.jef.justenoughfakepixel.utils.ScoreboardUtils;
import com.jef.justenoughfakepixel.utils.TablistParser;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterEvents
public class PowderTracker {

    private static final Pattern CHEST_UNCOVERED   = Pattern.compile("You uncovered a treasure chest!");

    private static final Pattern GEMSTONE_POWDER   = Pattern.compile("Gemstone Powder x([\\d,]+)");
    private static final Pattern DIAMOND_ESSENCE   = Pattern.compile("Diamond Essence x([\\d,]+)");
    private static final Pattern GOLD_ESSENCE      = Pattern.compile("Gold Essence x([\\d,]+)");
    private static final Pattern OIL_BARREL        = Pattern.compile("Oil Barrel x([\\d,]+)");
    private static final Pattern ASCENSION_ROPE    = Pattern.compile("Ascension Rope x([\\d,]+)");
    private static final Pattern WISHING_COMPASS   = Pattern.compile("Wishing Compass x([\\d,]+)");
    private static final Pattern JUNGLE_HEART      = Pattern.compile("Jungle Heart x([\\d,]+)");
    private static final Pattern COMPACT           = Pattern.compile("COMPACT! You found an? Enchanted Hard Stone!");

    private static final Pattern GEMSTONE_DROP     = Pattern.compile(
            "\\S (Rough|Flawed|Fine|Flawless) " +
                    "(Ruby|Sapphire|Amber|Amethyst|Jade|Topaz|Jasper|Opal|Citrine|Aquamarine|Peridot|Onyx) " +
                    "Gemstone x([\\d,]+)"
    );

    private static final Pattern GOBLIN_EGG        = Pattern.compile("Goblin Egg x([\\d,]+)$");
    private static final Pattern GREEN_GOBLIN_EGG  = Pattern.compile("Green Goblin Egg x([\\d,]+)");
    private static final Pattern RED_GOBLIN_EGG    = Pattern.compile("Red Goblin Egg x([\\d,]+)");
    private static final Pattern YELLOW_GOBLIN_EGG = Pattern.compile("Yellow Goblin Egg x([\\d,]+)");
    private static final Pattern BLUE_GOBLIN_EGG   = Pattern.compile("Blue Goblin Egg x([\\d,]+)");

    private static int tickCounter = 0;
    private static ItemStack[] lastInventory = new ItemStack[36];

    public static boolean isDoublePowder() {
        return TablistParser.isEventActive("2x Powder");
    }

    public static String getDoublePowderTimeLeft() {
        if (!isDoublePowder()) return null;
        return TablistParser.getActiveEventTimeLeft();
    }

    public static boolean isEnabled() {
        return JefConfig.feature != null
                && JefConfig.feature.mining.powderTracker
                && PowderStats.getInstance().isTrackingEnabled();
    }

    private static boolean isActive() {
        return isEnabled()
                && ScoreboardUtils.getCurrentLocation() == ScoreboardUtils.Location.CRYSTAL_HOLLOWS;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isActive()) return;

        tickCounter++;

        if (tickCounter % 20 == 0)
            PowderStats.getInstance().tickRates();

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.currentScreen != null) return;

        ItemStack[] inv = mc.thePlayer.inventory.mainInventory;
        for (int i = 0; i < inv.length; i++) {
            ItemStack current = inv[i];
            ItemStack prev    = lastInventory[i];

            if (current == null) {
                lastInventory[i] = null;
                continue;
            }

            if (!ItemUtils.getInternalName(current).equals("ENCHANTED_HARD_STONE")) {
                lastInventory[i] = current.copy();
                continue;
            }

            if (prev == null) {
                lastInventory[i] = current.copy();
                continue;
            }

            int gained = current.stackSize - prev.stackSize;
            if (gained > 0) {
                PowderStats stats = PowderStats.getInstance();
                stats.getData().hardStone += gained;
                stats.save();
            }

            lastInventory[i] = current.copy();
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!ChatUtils.isFromServer(event)) return;
        if (!isActive()) return;

        String msg = ChatUtils.clean(event);
        PowderStats stats = PowderStats.getInstance();
        PowderData  data  = stats.getData();

        if (CHEST_UNCOVERED.matcher(msg).find()) {
            data.totalChestsPicked++;
            stats.save();
            return;
        }

        if (COMPACT.matcher(msg).find()) {
            data.hardStoneCompacted++;
            stats.save();
            return;
        }

        Matcher m;

        m = GEMSTONE_POWDER.matcher(msg);
        if (m.find()) {
            data.gemstonePowder += parseLong(m.group(1));
            stats.save();
            return;
        }

        m = GEMSTONE_DROP.matcher(msg);
        if (m.find()) {
            String key = PowderStats.gemKey(m.group(1), m.group(2));
            data.gemstones.put(key, data.gemstones.getOrDefault(key, 0L) + parseLong(m.group(3)));
            stats.save();
            return;
        }

        m = DIAMOND_ESSENCE.matcher(msg);
        if (m.find()) { data.diamondEssence   += parseLong(m.group(1)); stats.save(); return; }

        m = GOLD_ESSENCE.matcher(msg);
        if (m.find()) { data.goldEssence      += parseLong(m.group(1)); stats.save(); return; }

        m = OIL_BARREL.matcher(msg);
        if (m.find()) { data.oilBarrels       += parseLong(m.group(1)); stats.save(); return; }

        m = ASCENSION_ROPE.matcher(msg);
        if (m.find()) { data.ascensionRopes   += parseLong(m.group(1)); stats.save(); return; }

        m = WISHING_COMPASS.matcher(msg);
        if (m.find()) { data.wishingCompasses += parseLong(m.group(1)); stats.save(); return; }

        m = JUNGLE_HEART.matcher(msg);
        if (m.find()) { data.jungleHearts     += parseLong(m.group(1)); stats.save(); return; }

        m = GOBLIN_EGG.matcher(msg);
        if (m.find()) { data.goblinEgg        += parseLong(m.group(1)); stats.save(); return; }

        m = GREEN_GOBLIN_EGG.matcher(msg);
        if (m.find()) { data.greenGoblinEgg   += parseLong(m.group(1)); stats.save(); return; }

        m = RED_GOBLIN_EGG.matcher(msg);
        if (m.find()) { data.redGoblinEgg     += parseLong(m.group(1)); stats.save(); return; }

        m = YELLOW_GOBLIN_EGG.matcher(msg);
        if (m.find()) { data.yellowGoblinEgg  += parseLong(m.group(1)); stats.save(); return; }

        m = BLUE_GOBLIN_EGG.matcher(msg);
        if (m.find()) { data.blueGoblinEgg    += parseLong(m.group(1)); stats.save(); }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        lastInventory = new ItemStack[36];
        PowderStats.getInstance().onWorldChange();
    }

    private static long parseLong(String s) {
        try { return Long.parseLong(s.replace(",", "")); }
        catch (NumberFormatException e) { return 0L; }
    }
}