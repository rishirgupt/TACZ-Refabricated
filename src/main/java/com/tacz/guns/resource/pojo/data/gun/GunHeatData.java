package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public class GunHeatData {
   @SerializedName("max")
   private float heatMax = 100.0F;
   @SerializedName("per_shot")
   private float heatPerShot = 3.0F;
   @SerializedName("cooling_multiplier")
   private float coolingMultiplier = 1.0F;
   @SerializedName("cooling_delay")
   private long coolingDelay = 1000L;
   @SerializedName("over_heat_time")
   private long overHeatTime = 3000L;
   @SerializedName("min_inaccuracy")
   private float minInaccuracy = 1.0F;
   @SerializedName("max_inaccuracy")
   private float maxInaccuracy = 1.0F;
   @SerializedName("min_rpm_mod")
   private float minRpmMod = 1.0F;
   @SerializedName("max_rpm_mod")
   private float maxRpmMod = 1.0F;

   public long getCoolingDelay() {
      return this.coolingDelay;
   }

   public float getHeatMax() {
      return this.heatMax;
   }

   public float getHeatPerShot() {
      return this.heatPerShot;
   }

   public long getOverHeatTime() {
      return this.overHeatTime;
   }

   public float getMinInaccuracy() {
      return this.minInaccuracy;
   }

   public float getMaxInaccuracy() {
      return this.maxInaccuracy;
   }

   public float getCoolingMultiplier() {
      return this.coolingMultiplier;
   }

   public float getMinRpmMod() {
      return this.minRpmMod;
   }

   public float getMaxRpmMod() {
      return this.maxRpmMod;
   }
}
