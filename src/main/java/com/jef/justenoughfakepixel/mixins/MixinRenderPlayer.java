package com.jef.justenoughfakepixel.mixins;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {

    private static final float SCALE = 1f / 1.8f;

    @Inject(method = "preRenderCallback(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V",
            at = @At("TAIL"))
    private void jef$tinyKebap(AbstractClientPlayer player, float partialTicks, CallbackInfo ci) {
        if (!"TURKISHKEBAP".equals(player.getName())) return;
        GlStateManager.scale(SCALE, SCALE, SCALE);
    }
    @Inject(method = "preRenderCallback(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V",
        at = @At("TAIL"))
    private void jef$tinysoup(AbstractClientPlayer player, float partialTicks, CallbackInfo ci) {
        if (!"mansoob201".equals(player.getName())) return;
        GlStateManager.scale(SCALE, SCALE, SCALE);
    }
     @Inject(method = "preRenderCallback(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V",
        at = @At("TAIL"))
    private void jef$tinyprotocol(AbstractClientPlayer player, float partialTicks, CallbackInfo ci) {
        if (!"kanishka007".equals(player.getName())) return;
        GlStateManager.scale(SCALE, SCALE, SCALE);
    }
}
