package com.tacz.guns.util.math;

import java.util.Date;
import java.util.Random;

public class PerlinNoise {
   private final Random random = new Random();
   private final float rangeDown;
   private final float rangeUp;
   private final long periodMs;
   private float prevNum;
   private float num;
   private long prevTime = new Date().getTime();
   private boolean reverse = false;

   public PerlinNoise(float rangeDown, float rangeUp, long periodMs) {
      this.rangeDown = rangeDown;
      this.rangeUp = rangeUp;
      this.periodMs = periodMs;
      this.prevNum = this.random.nextFloat() * (rangeUp - rangeDown) + rangeDown;
      this.num = this.random.nextFloat() * (rangeUp - rangeDown) + rangeDown;
      if (this.reverse && this.prevNum * this.num > 0.0F) {
         this.num = -this.num;
      }
   }

   private static double easeInterpolate(double x) {
      return 3.0 * Math.pow(x, 2.0) - 2.0 * Math.pow(x, 3.0);
   }

   public void setReverse(boolean reverse) {
      this.reverse = reverse;
   }

   public float getValue() {
      long periodTime = new Date().getTime() - this.prevTime;
      long repeat = periodTime / this.periodMs;
      long partialTime = periodTime % this.periodMs;
      this.prevTime = this.prevTime + repeat * this.periodMs;
      double x = easeInterpolate((double)partialTime / this.periodMs);
      if (repeat == 1L) {
         this.prevNum = this.num;
         this.num = this.random.nextFloat() * (this.rangeUp - this.rangeDown) + this.rangeDown;
         if (this.reverse && this.prevNum * this.num > 0.0F) {
            this.num = -this.num;
         }
      } else if (repeat > 1L) {
         this.prevNum = this.random.nextFloat() * (this.rangeUp - this.rangeDown) + this.rangeDown;
         this.num = this.random.nextFloat() * (this.rangeUp - this.rangeDown) + this.rangeDown;
         if (this.reverse && this.prevNum * this.num > 0.0F) {
            this.num = -this.num;
         }
      }

      return (float)(this.prevNum * (1.0 - x) + this.num * x);
   }
}
