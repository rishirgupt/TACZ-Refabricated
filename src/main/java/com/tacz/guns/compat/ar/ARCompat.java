package com.tacz.guns.compat.ar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/** Accelerated Rendering has no 26.2 release; every hook is a no-op and nothing is ever accelerated. */
public class ARCompat {
   public static final String MOD_ID = "acceleratedrendering";
   public static boolean LOADED = false;

   public static void init() {
   }

   public static boolean shouldAccelerate() {
      return false;
   }

   public static boolean isAccelerated(VertexConsumer vertexConsumer) {
      return false;
   }

   public static void setRenderingLevel() {
   }

   public static void resetRenderingLevel() {
   }

   public static void setRenderLayer(int layer) {
   }

   public static void setRenderBeforeFunction(Runnable runnable) {
   }

   public static void setRenderAfterFunction(Runnable runnable) {
   }

   public static void resetRenderLayer() {
   }

   public static void resetRenderBeforeFunction() {
   }

   public static void resetRenderAfterFunction() {
   }

   public static void disableAcceleration() {
   }

   public static void resetAcceleration() {
   }

   public static void renderLaser(VertexConsumer extension, float z, float width, boolean fadeOut, PoseStack poseStack, int color) {
   }
}
