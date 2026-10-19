package com.tacz.guns.api.client.animation.interpolator;

import com.tacz.guns.api.client.animation.AnimationChannelContent;

public class Spline implements Interpolator {
   @Override
   public void compile(AnimationChannelContent content) {
   }

   @Override
   public float[] interpolate(int indexFrom, int indexTo, float alpha) {
      return new float[]{0.0F, 0.0F, 0.0F, 1.0F};
   }

   @Override
   public Interpolator clone() {
      return null;
   }
}
