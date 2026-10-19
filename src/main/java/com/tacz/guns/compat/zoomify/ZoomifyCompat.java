package com.tacz.guns.compat.zoomify;

/** Zoomify integration is not available; the field of view is never changed by it. */
public class ZoomifyCompat {
   public static void init() {
   }

   public static double getFov(double fov, float tickDelta) {
      return fov;
   }
}
