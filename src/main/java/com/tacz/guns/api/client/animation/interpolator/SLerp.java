package com.tacz.guns.api.client.animation.interpolator;

import com.tacz.guns.api.client.animation.AnimationChannelContent;

public class SLerp implements Interpolator {
   private AnimationChannelContent content;

   @Override
   public void compile(AnimationChannelContent content) {
      this.content = content;
   }

   @Override
   public float[] interpolate(int indexFrom, int indexTo, float alpha) {
      int offset = this.content.values[indexFrom].length == 8 ? 4 : 0;
      float ax = this.content.values[indexFrom][offset];
      float ay = this.content.values[indexFrom][1 + offset];
      float az = this.content.values[indexFrom][2 + offset];
      float aw = this.content.values[indexFrom][3 + offset];
      float bx = indexFrom == indexTo ? this.content.values[indexFrom][offset] : this.content.values[indexTo][0];
      float by = indexFrom == indexTo ? this.content.values[indexFrom][1 + offset] : this.content.values[indexTo][1];
      float bz = indexFrom == indexTo ? this.content.values[indexFrom][2 + offset] : this.content.values[indexTo][2];
      float bw = indexFrom == indexTo ? this.content.values[indexFrom][3 + offset] : this.content.values[indexTo][3];
      float[] result = new float[4];
      float dot = ax * bx + ay * by + az * bz + aw * bw;
      if (dot < 0.0F) {
         bx = -bx;
         by = -by;
         bz = -bz;
         bw = -bw;
         dot = -dot;
      }

      float epsilon = 1.0E-6F;
      float s0;
      float s1;
      if (1.0 - dot > epsilon) {
         float omega = (float)Math.acos(dot);
         float invSinOmega = 1.0F / (float)Math.sin(omega);
         s0 = (float)Math.sin((1.0 - alpha) * omega) * invSinOmega;
         s1 = (float)Math.sin(alpha * omega) * invSinOmega;
      } else {
         s0 = 1.0F - alpha;
         s1 = alpha;
      }

      float rx = s0 * ax + s1 * bx;
      float ry = s0 * ay + s1 * by;
      float rz = s0 * az + s1 * bz;
      float rw = s0 * aw + s1 * bw;
      result[0] = rx;
      result[1] = ry;
      result[2] = rz;
      result[3] = rw;
      return result;
   }

   public SLerp clone() {
      try {
         SLerp sLerp = (SLerp)super.clone();
         sLerp.content = this.content;
         return sLerp;
      } catch (CloneNotSupportedException e) {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }
}
