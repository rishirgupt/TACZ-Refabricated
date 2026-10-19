package com.tacz.guns.mixin.client;

import com.tacz.guns.client.gui.overlay.ScopeLens;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
   @Inject(method = "render", at = @At("HEAD"))
   private void tacz$captureFrameForScopeLens(CallbackInfo ci) {
      ScopeLens.captureFrame();
   }
}
