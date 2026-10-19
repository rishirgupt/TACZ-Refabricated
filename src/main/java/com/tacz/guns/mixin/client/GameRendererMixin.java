package com.tacz.guns.mixin.client;

import com.tacz.guns.api.client.event.RenderItemInHandBobEvent;
import com.tacz.guns.api.client.event.RenderLevelBobEvent;
import com.tacz.guns.client.renderer.other.GunHurtBobTweak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements com.tacz.guns.client.gui.overlay.HandBob {
   @Shadow
   protected abstract void bobHurt(net.minecraft.client.renderer.state.level.CameraRenderState cameraState, PoseStack poseStack);

   @Shadow
   protected abstract void bobView(net.minecraft.client.renderer.state.level.CameraRenderState cameraState, PoseStack poseStack);

   @Override
   public void tacz$applyBob(net.minecraft.client.renderer.state.level.CameraRenderState cameraState, PoseStack poseStack) {
      this.bobHurt(cameraState, poseStack);
      if (Minecraft.getInstance().options.bobView().get()) {
         this.bobView(cameraState, poseStack);
      }
   }

   @Unique
   private boolean tacz$useFovSetting;

   public Minecraft getMinecraft() {
      return Minecraft.getInstance();
   }

   @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
   public void onBobHurt(net.minecraft.client.renderer.state.level.CameraRenderState cameraState, PoseStack pMatrixStack, CallbackInfo ci) {
      if (this.getMinecraft().getCameraEntity() instanceof LocalPlayer player
         && !player.isDeadOrDying()
         && GunHurtBobTweak.onHurtBobTweak(player, pMatrixStack, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false))) {
         ci.cancel();
      } else {
         boolean cancel;
         if (!this.tacz$useFovSetting) {
            RenderItemInHandBobEvent.BobHurt event = new RenderItemInHandBobEvent.BobHurt();
            ((RenderItemInHandBobEvent.HurtCallback)RenderItemInHandBobEvent.HURT.invoker()).post(event);
            cancel = event.isCanceled();
         } else {
            RenderLevelBobEvent.BobHurt event = new RenderLevelBobEvent.BobHurt();
            ((RenderLevelBobEvent.HurtCallback)RenderLevelBobEvent.HURT.invoker()).post(event);
            cancel = event.isCanceled();
         }

         if (cancel) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "renderItemInHand", at = @At("HEAD"))
   private void tacz$captureHandView(net.minecraft.client.renderer.state.level.CameraRenderState camera, float partialTick, org.joml.Matrix4fc view, CallbackInfo ci) {
      com.tacz.guns.client.gui.overlay.ScopeLens.setHandView(view, camera.hudFov);
   }

   @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
   public void onBobView(net.minecraft.client.renderer.state.level.CameraRenderState cameraState, PoseStack pMatrixStack, CallbackInfo ci) {
      boolean cancel;
      if (!this.tacz$useFovSetting) {
         RenderItemInHandBobEvent.BobView event = new RenderItemInHandBobEvent.BobView();
         ((RenderItemInHandBobEvent.ViewCallback)RenderItemInHandBobEvent.VIEW.invoker()).post(event);
         cancel = event.isCanceled();
      } else {
         RenderLevelBobEvent.BobView event = new RenderLevelBobEvent.BobView();
         ((RenderLevelBobEvent.ViewCallback)RenderLevelBobEvent.VIEW.invoker()).post(event);
         cancel = event.isCanceled();
      }

      if (cancel) {
         ci.cancel();
      }
   }

}
