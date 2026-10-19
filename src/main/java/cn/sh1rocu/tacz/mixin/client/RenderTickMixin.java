package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fires the render tick event around the game renderer. */
@Mixin(net.minecraft.client.renderer.GameRenderer.class)
public abstract class RenderTickMixin {
   @Inject(method = "render", at = @At("HEAD"))
   private void tacz$renderTickStart(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
      ((RenderTickEvent.Callback)RenderTickEvent.EVENT.invoker()).post(new RenderTickEvent(Minecraft.getInstance(), RenderTickEvent.Phase.START, deltaTracker));
   }

   @Inject(method = "render", at = @At("RETURN"))
   private void tacz$renderTickEnd(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
      ((RenderTickEvent.Callback)RenderTickEvent.EVENT.invoker()).post(new RenderTickEvent(Minecraft.getInstance(), RenderTickEvent.Phase.END, deltaTracker));
   }
}
