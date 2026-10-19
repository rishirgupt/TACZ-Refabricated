package com.tacz.guns.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.model.functional.ShellRender;
import com.tacz.guns.client.render26.EntityStateKeys;
import com.tacz.guns.client.render26.GunRender;
import com.tacz.guns.client.renderer.other.HumanoidOffhandRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
   @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;FF)V", at = @At("TAIL"))
   private void tacz$renderBodyGuns(PoseStack poseStack, SubmitNodeCollector collector, int light, ArmedEntityRenderState state, float yRot, float xRot, CallbackInfo ci) {
      LivingEntity entity = state.getData(EntityStateKeys.ENTITY);
      MuzzleFlashRender.isSelf = false;
      ShellRender.isSelf = false;
      if (entity != null) {
         GunRender.begin(collector);
         try {
            HumanoidOffhandRender.renderGun(entity, poseStack, GunRender.bufferSource(), light);
         } finally {
            GunRender.end();
         }
      }
   }

   @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
   private void tacz$armHead(ArmedEntityRenderState state, ItemStackRenderState item, ItemStack stack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
      LivingEntity entity = state.getData(EntityStateKeys.ENTITY);
      if (entity == null) {
         return;
      }

      if (entity.equals(Minecraft.getInstance().player)) {
         MuzzleFlashRender.isSelf = true;
         ShellRender.isSelf = true;
      }

      if (IGun.mainHandHoldGun(entity) && arm == HumanoidArm.LEFT) {
         ci.cancel();
      }
   }

   @Inject(method = "submitArmWithItem", at = @At("TAIL"))
   private void tacz$armTail(ArmedEntityRenderState state, ItemStackRenderState item, ItemStack stack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int light, CallbackInfo ci) {
      MuzzleFlashRender.isSelf = false;
      ShellRender.isSelf = false;
   }
}
