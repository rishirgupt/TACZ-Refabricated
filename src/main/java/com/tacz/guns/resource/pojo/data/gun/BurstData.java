package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public class BurstData {
   @SerializedName("continuous_shoot")
   private boolean continuousShoot = false;
   @SerializedName("count")
   private int count = 3;
   @SerializedName("bpm")
   private int bpm = 200;
   @SerializedName("min_interval")
   private double minInterval = 1.0;

   public int getCount() {
      return this.count;
   }

   public int getBpm() {
      return this.bpm;
   }

   public double getMinInterval() {
      return this.minInterval;
   }

   public boolean isContinuousShoot() {
      return this.continuousShoot;
   }
}
