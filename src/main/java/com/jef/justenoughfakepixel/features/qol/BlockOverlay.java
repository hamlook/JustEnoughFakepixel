package com.jef.justenoughfakepixel.features.qol;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.editors.ChromaColour;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.render.WorldRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Color;

@RegisterEvents
public class BlockOverlay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        if (!JefConfig.feature.qol.blockSelectionOverlay) return;
        if (event.target == null || event.target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        event.setCanceled(true);

        int argb = ChromaColour.specialToChromaRGB(JefConfig.feature.qol.blockSelectionColor);
        Color color = new Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);

        if (JefConfig.feature.qol.blockSelectionMode == 0) {
            WorldRenderUtils.drawFilledBlock(event.target.getBlockPos(), color);
        } else {
            WorldRenderUtils.drawSelectionBox(event.target.getBlockPos(), color,
                    JefConfig.feature.qol.blockSelectionThickness);
        }
    }
}