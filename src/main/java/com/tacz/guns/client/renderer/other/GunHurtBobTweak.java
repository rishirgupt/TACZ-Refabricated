package com.tacz.guns.client.renderer.other;

import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.math.Axis;

public class GunHurtBobTweak {
   private static long hurtByGunTimeStamp = -1L;
   private static float lastTweakMultiplier = 0.05F;

   public static boolean onHurtBobTweak(LocalPlayer player, PoseStack matrixStack, float partialTicks) {
      if (System.currentTimeMillis() - hurtByGunTimeStamp > 500L) {
         return false;
      }

      float zRot = player.hurtTime - partialTicks;
      if (zRot < 0.0F) {
         return true;
      }

      zRot /= player.hurtDuration;
      zRot = Mth.sin(zRot * zRot * zRot * zRot * (float) Math.PI);
      float yRot = player.getHurtDir();
      yRot *= lastTweakMultiplier;
      zRot *= lastTweakMultiplier;
      matrixStack.mulPose(Axis.YP.rotationDegrees(-yRot));
      matrixStack.mulPose(Axis.XP.rotationDegrees(-zRot * 14.0F));
      matrixStack.mulPose(Axis.YP.rotationDegrees(yRot));
      return true;
   }

   public static void markTimestamp(float tweakMultiplier) {
      hurtByGunTimeStamp = System.currentTimeMillis();
      lastTweakMultiplier = tweakMultiplier;
   }
}
