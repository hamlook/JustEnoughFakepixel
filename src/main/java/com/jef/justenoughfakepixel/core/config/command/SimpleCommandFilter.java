package com.jef.justenoughfakepixel.core.config.command;

import com.jef.justenoughfakepixel.init.RegisterEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;

@RegisterEvents
public class SimpleCommandFilter {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        String msg = String.valueOf(event.message);
        if (msg == null || msg.isEmpty()) return;
        if (msg.charAt(0) == '/') return;

        String firstWord = msg.split(" ")[0].toLowerCase(Locale.ROOT);
        if (!SimpleCommand.getSlashOnlyNames().contains(firstWord)) return;

        event.setCanceled(true);
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.sendChatMessage("/" + msg);
        } else {
            mc.ingameGUI.getChatGUI().printChatMessage(
                    new ChatComponentText("\u00a7c[JEF] \u00a77You must be in a world to use commands."));
        }
    }
}