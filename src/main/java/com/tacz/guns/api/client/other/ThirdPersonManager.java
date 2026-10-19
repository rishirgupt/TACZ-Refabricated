package com.tacz.guns.api.client.other;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.client.model.geom.ModelPart;

public final class ThirdPersonManager {
   private static final Map<String, IThirdPersonAnimation> CACHE = Maps.newHashMap();
   private static final String RESERVED_DEFAULT_NAME = "default";
   private static final IThirdPersonAnimation DEFAULT = new IThirdPersonAnimation() {
      @Override
      public void animateGunHold(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart body, ModelPart head) {
         rightArm.yRot = -0.3F + head.yRot;
         leftArm.yRot = 0.8F + head.yRot;
         rightArm.xRot = -1.4F + head.xRot;
         leftArm.xRot = -1.4F + head.xRot;
      }

      @Override
      public void animateGunAim(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart body, ModelPart head, float aimProgress) {
         float lerp1 = Mth.lerp(aimProgress, 0.3F, 0.35F);
         float lerp2 = Mth.lerp(aimProgress, 1.4F, 1.6F);
         rightArm.yRot = -lerp1 + head.yRot;
         leftArm.yRot = 0.8F + head.yRot;
         rightArm.xRot = -lerp2 + head.xRot;
         leftArm.xRot = -lerp2 + head.xRot;
      }
   };
   private static final String MINI_GUN_NAME = "minigun";
   private static final IThirdPersonAnimation MINI_GUN = new IThirdPersonAnimation() {
      @Override
      public void animateGunHold(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart body, ModelPart head) {
         body.yRot = head.yRot + 0.8F;
         double cosTheta = Math.cos(-body.yRot);
         double sinTheta = Math.sin(-body.yRot);
         float x = rightArm.x;
         rightArm.x = (float)(x * cosTheta);
         rightArm.z = (float)(x * sinTheta);
         rightArm.yRot = -1.0F + body.yRot;
         rightArm.xRot = -0.1F + body.xRot;
         float x2 = leftArm.x;
         leftArm.x = (float)(x2 * cosTheta);
         leftArm.z = (float)(x2 * sinTheta);
         leftArm.yRot = -0.1F + body.yRot;
         leftArm.xRot = -1.0F + body.xRot;
      }

      @Override
      public void animateGunAim(LivingEntity entity, ModelPart rightArm, ModelPart leftArm, ModelPart body, ModelPart head, float aimProgress) {
         this.animateGunHold(entity, rightArm, leftArm, body, head);
      }
   };

   public static void registerDefault() {
      CACHE.put("default", DEFAULT);
      CACHE.put("minigun", MINI_GUN);
   }

   public static void register(String name, IThirdPersonAnimation animation) {
      if (!name.equals("default")) {
         CACHE.put(name, animation);
      }
   }

   public static IThirdPersonAnimation getAnimation(String name) {
      return CACHE.getOrDefault(name, DEFAULT);
   }
}
