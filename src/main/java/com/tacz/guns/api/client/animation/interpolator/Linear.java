package com.tacz.guns.api.client.animation.interpolator;

import com.tacz.guns.api.client.animation.AnimationChannelContent;

public class Linear implements Interpolator {
   private AnimationChannelContent content;

   @Override
   public void compile(AnimationChannelContent content) {
      this.content = content;
   }

   @Override
   public float[] interpolate(int indexFrom, int indexTo, float alpha) {
      int offset = this.content.values[indexFrom].length == 6 ? 3 : 0;
      float[] result = new float[3];

      for (int i = 0; i < 3; i++) {
         if (indexFrom == indexTo) {
            result[i] = this.content.values[indexFrom][i + offset];
         } else {
            result[i] = this.content.values[indexFrom][i + offset] * (1.0F - alpha) + this.content.values[indexTo][i] * alpha;
         }
      }

      return result;
   }

   public Linear clone() {
      try {
         Linear linear = (Linear)super.clone();
         linear.content = this.content;
         return linear;
      } catch (CloneNotSupportedException e) {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }
}
