package com.tacz.guns.compat.shouldersurfing;

/** Shoulder Surfing has no 26.2 release, so there is never a third-person crosshair to account for. */
public final class ShoulderSurfingCompat {
   public static void init() {
   }

   public static boolean showCrosshair() {
      return false;
   }

   public static boolean isInstalled() {
      return false;
   }
}
