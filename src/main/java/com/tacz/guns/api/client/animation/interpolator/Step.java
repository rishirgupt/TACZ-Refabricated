package com.tacz.guns.api.client.animation.interpolator;

import com.tacz.guns.api.client.animation.AnimationChannelContent;
import java.util.Arrays;

public class Step implements Interpolator {
   private AnimationChannelContent content;

   @Override
   public void compile(AnimationChannelContent content) {
      this.content = content;
   }

   @Override
   public float[] interpolate(int indexFrom, int indexTo, float alpha) {
      int offset = switch (this.content.values[indexFrom].length) {
         case 6 -> 3;
         case 8 -> 4;
         default -> 0;
      };
      float[] result;
      if (!(alpha < 1.0F) && indexFrom != indexTo) {
         int length = this.content.values[indexTo].length;

         length = switch (length) {
            case 6 -> 3;
            case 8 -> 4;
            default -> length;
         };
         result = Arrays.copyOfRange(this.content.values[indexTo], 0, length);
      } else {
         result = Arrays.copyOfRange(this.content.values[indexFrom], offset, this.content.values[indexFrom].length);
      }

      return result;
   }

   public Step clone() {
      try {
         Step step = (Step)super.clone();
         step.content = this.content;
         return step;
      } catch (CloneNotSupportedException e) {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }
}
