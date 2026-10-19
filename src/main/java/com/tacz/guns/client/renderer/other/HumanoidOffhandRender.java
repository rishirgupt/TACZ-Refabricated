package com.tacz.guns.client.renderer.other;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.pojo.display.gun.LayerGunShow;
import com.tacz.guns.util.math.MathUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.render26.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class HumanoidOffhandRender {
   public static void renderGun(LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
      renderOffhandGun(entity, matrixStack, buffer, packedLight);
      renderHotbarGun(entity, matrixStack, buffer, packedLight);
   }

   private static void renderOffhandGun(LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
      ItemStack itemStack = entity.getOffhandItem();
      if (!itemStack.isEmpty()) {
         IGun iGun = IGun.getIGunOrNull(itemStack);
         if (iGun != null) {
            TimelessAPI.getGunDisplay(itemStack).ifPresent(index -> {
               LayerGunShow offhandShow = index.getOffhandShow();
               renderGunItem(entity, matrixStack, buffer, packedLight, itemStack, offhandShow);
            });
         }
      }
   }

   private static void renderHotbarGun(LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
      if (entity instanceof Player player) {
         Inventory inventory = player.getInventory();

         for (int i = 0; i < 9; i++) {
            if (i != inventory.getSelectedSlot()) {
               ItemStack stack = inventory.getItem(i);
               renderHotbarGun(entity, matrixStack, buffer, packedLight, stack, i);
            }
         }
      }
   }

   private static void renderHotbarGun(LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, ItemStack itemStack, int inventoryIndex) {
      if (!itemStack.isEmpty()) {
         IGun iGun = IGun.getIGunOrNull(itemStack);
         if (iGun != null) {
            TimelessAPI.getGunDisplay(itemStack).ifPresent(display -> {
               Int2ObjectArrayMap<LayerGunShow> hotbarShow = display.getHotbarShow();
               if (hotbarShow != null && !hotbarShow.isEmpty()) {
                  if (hotbarShow.containsKey(inventoryIndex)) {
                     LayerGunShow gunShow = (LayerGunShow)hotbarShow.get(inventoryIndex);
                     renderGunItem(entity, matrixStack, buffer, packedLight, itemStack, gunShow);
                  }
               }
            });
         }
      }
   }

   private static void renderGunItem(
      LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, ItemStack itemStack, LayerGunShow offhandShow
   ) {
            Vector3f pos = offhandShow.getPos();
      Vector3f rotate = offhandShow.getRotate();
      Vector3f scale = offhandShow.getScale();
      matrixStack.pushPose();
      matrixStack.translate(-pos.x() / 16.0F, 1.5 - pos.y() / 16.0F, pos.z() / 16.0F);
      matrixStack.scale(-scale.x(), -scale.y(), scale.z());
      Quaternionf rotation = new Quaternionf();
      MathUtil.toQuaternion((float)Math.toRadians(rotate.x), (float)Math.toRadians(rotate.y), (float)Math.toRadians(rotate.z), rotation);
      matrixStack.mulPose(rotation);
      net.minecraft.client.renderer.item.ItemStackRenderState itemState = new net.minecraft.client.renderer.item.ItemStackRenderState();
      Minecraft.getInstance().getItemModelResolver().updateForTopItem(itemState, itemStack, ItemDisplayContext.FIXED, entity.level(), null, entity.getId());
      if (com.tacz.guns.client.render26.GunRender.collector() != null) {
         com.tacz.guns.client.render26.GunRender.flushNow();
         itemState.submit(matrixStack, com.tacz.guns.client.render26.GunRender.collector(), packedLight, OverlayTexture.NO_OVERLAY, 0);
      }
      matrixStack.popPose();
   }
}
