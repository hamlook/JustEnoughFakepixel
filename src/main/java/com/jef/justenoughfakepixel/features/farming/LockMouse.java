package com.jef.justenoughfakepixel.features.farming;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.utils.KeybindHelper;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@RegisterEvents
public class LockMouse {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final String PREFIX = EnumChatFormatting.GREEN + " " + EnumChatFormatting.RESET;

    private boolean keyWasDown = false;

    public static boolean isLocked() {
        return JefConfig.feature != null && JefConfig.feature.farming.lockMouse;
    }

    public static void setLocked(boolean locked) {
        if (JefConfig.feature == null) return;
        JefConfig.feature.farming.lockMouse = locked;
        JefConfig.saveConfig();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    PREFIX + (locked
                            ? EnumChatFormatting.GREEN + "Mouse locked."
                            : EnumChatFormatting.RED + "Mouse unlocked.")));
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (JefConfig.feature == null || mc.thePlayer == null || mc.currentScreen != null) return;

        boolean keyDown = KeybindHelper.isKeyDown(JefConfig.feature.farming.lockMouseKey);
        if (keyDown && !keyWasDown) setLocked(!isLocked());
        keyWasDown = keyDown;
    }
}