package com.jef.justenoughfakepixel.utils;

import com.jef.justenoughfakepixel.features.diana.DianaTracker;
import com.jef.justenoughfakepixel.features.dungeons.DungeonStats;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RegisterEvents
public class PartyCommands {

    private static final long HELP_COOLDOWN_MS = 10_000L;
    private long lastHelpMs = 0L;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!ChatUtils.isFromServer(event)) return;
        String msg = ChatUtils.clean(event);
        if (!ChatUtils.isPartyMessage(msg)) return;

        String body = ChatUtils.getPartyBody(msg);
        if (body == null) return;
        body = body.toLowerCase();

        if (body.startsWith("!pb")) {
            String[] parts = body.split("\\s+");
            String arg1 = parts.length >= 2 ? parts[1] : null;
            String arg2 = parts.length >= 3 ? parts[2] : null;
            if (arg1 == null) { respond("Usage: !pb <floor> | !pb <floor> br | !pb p1-p5"); return; }
            respond(DungeonStats.getFormattedPb(arg1, arg2));
            return;
        }

        switch (body) {
            case "!burrows": respond(DianaTracker.getBorrowsMessage()); break;
            case "!inq":     respond(DianaTracker.getInqMessage());     break;
            case "!mobs":    respond(DianaTracker.getMobsMessage());    break;
            case "!time":    respond(DianaTracker.getTimeMessage());    break;
            case "!chim":    respond(DianaTracker.getChimMessage());    break;
            case "!stick":   respond(DianaTracker.getStickMessage());   break;
            case "!relic":   respond(DianaTracker.getRelicMessage());   break;
            case "!loot":    respond(DianaTracker.getLootMessage());    break;
            case "!help": {
                long now = System.currentTimeMillis();
                if (now - lastHelpMs < HELP_COOLDOWN_MS) break;
                lastHelpMs = now;
                printLocal(DianaTracker.getHelpMessage());
                break;
            }
        }
    }

    private void respond(String msg) {
        if (mc.thePlayer == null) return;
        scheduler.schedule(() -> {
            if (mc.thePlayer != null) mc.thePlayer.sendChatMessage("/pc " + msg);
        }, 1500, TimeUnit.MILLISECONDS);
    }

    private void printLocal(String msg) {
        if (mc.thePlayer == null) return;
        for (String line : msg.split("\n"))
            mc.thePlayer.addChatMessage(new ChatComponentText(line));
    }
}