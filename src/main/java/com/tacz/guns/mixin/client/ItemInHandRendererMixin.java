package com.tacz.guns.mixin.client;

import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.api.client.other.KeepingItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import com.tacz.guns.client.render26.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin implements KeepingItemRenderer {
   @Shadow
   private float mainHandHeight;
   @Shadow
   private float oMainHandHeight;
   @Shadow
   private ItemStack mainHandItem;
   @Unique
   private ItemStack tacz$KeepItem;
   @Unique
   private long tacz$KeepTimeMs;
   @Unique
   private long tacz$KeepTimestamp;

   @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
   public void beforeHandRender(float pPartialTicks, PoseStack pMatrixStack, net.minecraft.client.renderer.SubmitNodeCollector pCollector, LocalPlayer pPlayerEntity, int pCombinedLight, CallbackInfo ci) {
      ((BeforeRenderHandEvent.Callback)BeforeRenderHandEvent.CALLBACK.invoker()).post(new BeforeRenderHandEvent(pMatrixStack));
   }

   @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
   private void tacz$renderHand(
      net.minecraft.client.player.AbstractClientPlayer player,
      float tickDelta,
      float pitch,
      net.minecraft.world.InteractionHand hand,
      float swingProgress,
      ItemStack stack,
      float equipProgress,
      PoseStack poseStack,
      net.minecraft.client.renderer.SubmitNodeCollector collector,
      int light,
      CallbackInfo ci
   ) {
      cn.sh1rocu.simplebedrockmodel.api.event.RenderHandEvent event = new cn.sh1rocu.simplebedrockmodel.api.event.RenderHandEvent(player, hand, stack, poseStack, tickDelta, pitch, swingProgress, equipProgress, light);
      com.tacz.guns.client.render26.GunRender.begin(collector);
      try {
         ((cn.sh1rocu.simplebedrockmodel.api.event.RenderHandEvent.Callback)cn.sh1rocu.simplebedrockmodel.api.event.RenderHandEvent.EVENT.invoker()).post(event);
      } finally {
         com.tacz.guns.client.render26.GunRender.end();
      }

      if (event.isCanceled()) {
         ci.cancel();
      }
   }

   @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
   public void cancelEquippedProgress(CallbackInfo ci) {
      if (com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler.FirstPersonRenderHandler.shouldLockVanilla()) {
         this.mainHandHeight = 1.0F;
         this.oMainHandHeight = 1.0F;
         ci.cancel();
      }
   }

   @Unique
   @Override
   public void keep(ItemStack itemStack, long timeMs) {
      long time = System.currentTimeMillis() - this.tacz$KeepTimestamp;
      if (time >= this.tacz$KeepTimeMs) {
         this.tacz$KeepTimeMs = timeMs;
         this.tacz$KeepTimestamp = System.currentTimeMillis();
         this.tacz$KeepItem = itemStack;
         this.mainHandItem = itemStack;
      }
   }

   @Override
   public ItemStack getCurrentItem() {
      if (Minecraft.getInstance().player == null) {
         return this.mainHandItem;
      }

      if (this.tacz$KeepItem != null) {
         long time = System.currentTimeMillis() - this.tacz$KeepTimestamp;
         if (time < this.tacz$KeepTimeMs) {
            return this.tacz$KeepItem;
         }

         this.tacz$KeepItem = null;
      }

      return this.mainHandItem;
   }
}
