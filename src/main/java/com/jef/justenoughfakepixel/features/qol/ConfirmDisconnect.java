package com.jef.justenoughfakepixel.features.qol;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterEvents
public class ConfirmDisconnect {

    private boolean confirm = false;
    private long lastClick = 0L;
    private GuiButton disconnectButton = null;

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.gui instanceof GuiIngameMenu)) return;

        confirm = false;
        lastClick = 0L;
        disconnectButton = null;

        for (Object obj : event.buttonList) {
            GuiButton btn = (GuiButton) obj;
            if (btn.id == 1) {
                disconnectButton = btn;
                break;
            }
        }
    }

    @SubscribeEvent
    public void onAction(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (!(event.gui instanceof GuiIngameMenu)) return;

        if (JefConfig.feature == null || !JefConfig.feature.qol.confirmDisconnect) return;

        if (event.button.id != 1) return;

        if (System.currentTimeMillis() - lastClick > 3000L) {
            confirm = false;
        }

        if (!confirm) {
            event.setCanceled(true);

            confirm = true;
            lastClick = System.currentTimeMillis();

            event.button.displayString = "§cPress again to confirm";
        } else {
            confirm = false;
        }
    }

    @SubscribeEvent
    public void onDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.gui instanceof GuiIngameMenu)) return;

        if (confirm && System.currentTimeMillis() - lastClick > 2000L) {
            confirm = false;

            if (disconnectButton != null) {
                disconnectButton.displayString = "Disconnect";
            }
        }
    }
}