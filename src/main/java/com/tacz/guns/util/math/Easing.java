package com.tacz.guns.util.math;

public class Easing {
   public static double easeOutCubic(double x) {
      return 1.0 - Math.pow(1.0 - x, 3.0);
   }
}
