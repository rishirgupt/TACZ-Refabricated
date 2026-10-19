package com.tacz.guns.api.client.animation.interpolator;

public class InterpolatorUtil {
   public static Interpolator fromInterpolation(InterpolatorUtil.InterpolatorType interpolation) {
      switch (interpolation) {
         case SLERP:
            return new SLerp();
         case SPLINE:
            return new Spline();
         case STEP:
            return new Step();
         default:
            return new Linear();
      }
   }

   public enum InterpolatorType {
      LINEAR,
      SLERP,
      SPLINE,
      STEP;
   }
}
