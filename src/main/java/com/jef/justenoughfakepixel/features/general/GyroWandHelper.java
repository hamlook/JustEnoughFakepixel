package com.jef.justenoughfakepixel.features.general;

import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.core.config.utils.RenderUtils;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.ChatUtils;
import com.jef.justenoughfakepixel.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@RegisterEvents
public class GyroWandHelper {

    private static final String GYRO_ID     = "GYROKINETIC_WAND";
    private static final int    RING_RADIUS = 10;
    private static final int    RING_STEPS  = 64;
    private static final double REACH = 100.0;

    private static final float[] COLOR_READY    = { 0.6f, 0.1f, 0.8f, 0.6f };
    private static final float[] COLOR_COOLDOWN = { 1.0f, 0.2f, 0.2f, 0.6f };

    private boolean isEnabled() {
        return JefConfig.feature != null && JefConfig.feature.general.gyroWand;
    }

    public static boolean isHoldingGyroStatic() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer != null && GYRO_ID.equals(ItemUtils.getInternalName(mc.thePlayer.getHeldItem()));
    }

    private boolean isOnCooldown() {
        GyroWandOverlay overlay = GyroWandOverlay.getInstance();
        return overlay != null && overlay.isOnCooldown();
    }

    private Vec3 getTargetPos(EntityPlayer player, float partialTicks) {
        Vec3 eyes = player.getPositionEyes(partialTicks);
        Vec3 look = player.getLookVec();
        Vec3 end  = eyes.addVector(look.xCoord * REACH, look.yCoord * REACH, look.zCoord * REACH);

        double x = Math.floor(eyes.xCoord);
        double y = Math.floor(eyes.yCoord);
        double z = Math.floor(eyes.zCoord);

        double dx = end.xCoord - eyes.xCoord;
        double dy = end.yCoord - eyes.yCoord;
        double dz = end.zCoord - eyes.zCoord;

        double stepX = Math.signum(dx);
        double stepY = Math.signum(dy);
        double stepZ = Math.signum(dz);

        double invDx = dx != 0 ? 1.0 / dx : Double.MAX_VALUE;
        double invDy = dy != 0 ? 1.0 / dy : Double.MAX_VALUE;
        double invDz = dz != 0 ? 1.0 / dz : Double.MAX_VALUE;

        double tDeltaX = Math.abs(invDx * stepX);
        double tDeltaY = Math.abs(invDy * stepY);
        double tDeltaZ = Math.abs(invDz * stepZ);

        double tMaxX = Math.abs((x + Math.max(stepX, 0) - eyes.xCoord) * invDx);
        double tMaxY = Math.abs((y + Math.max(stepY, 0) - eyes.yCoord) * invDy);
        double tMaxZ = Math.abs((z + Math.max(stepZ, 0) - eyes.zCoord) * invDz);

        double endX = Math.floor(end.xCoord);
        double endY = Math.floor(end.yCoord);
        double endZ = Math.floor(end.zCoord);

        for (int i = 0; i < 1000; i++) {
            BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
            net.minecraft.block.Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();

            if (block != net.minecraft.init.Blocks.air) {
                return new Vec3(x + 0.5, y + 1.0, z + 0.5);
            }

            if (x == endX && y == endY && z == endZ) return null;

            if (tMaxX <= tMaxY && tMaxX <= tMaxZ) {
                tMaxX += tDeltaX;
                x += stepX;
            } else if (tMaxY <= tMaxZ) {
                tMaxY += tDeltaY;
                y += stepY;
            } else {
                tMaxZ += tDeltaZ;
                z += stepZ;
            }
        }

        return null;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isEnabled() || !isHoldingGyroStatic()) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        Vec3 target = getTargetPos(player, event.partialTicks);
        if (target == null) return;

        double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

        float[] color = isOnCooldown() ? COLOR_COOLDOWN : COLOR_READY;
        float thickness = JefConfig.feature.general.gyroWandThickness;

        try {
            GL11.glPushMatrix();
            GL11.glTranslated(target.xCoord - px, target.yCoord - py, target.zCoord - pz);
            RenderUtils.drawWorldCircle(RING_RADIUS, RING_STEPS, thickness, color[0], color[1], color[2], color[3]);
        } finally {
            GL11.glPopMatrix();
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isEnabled()) return;
        String msg = ChatUtils.clean(event);
        if (msg.contains("Gravity Storm") && msg.contains("Mana")) {
            GyroWandOverlay overlay = GyroWandOverlay.getInstance();
            if (overlay != null) overlay.markUsed();
        }
    }
}