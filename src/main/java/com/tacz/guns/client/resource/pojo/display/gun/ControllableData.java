package com.tacz.guns.client.resource.pojo.display.gun;

import com.google.gson.annotations.SerializedName;

public class ControllableData {
   @SerializedName("low_frequency")
   private float lowFrequency = 0.25F;
   @SerializedName("high_frequency")
   private float highFrequency = 0.5F;
   @SerializedName("time")
   private int timeInMs = 80;

   public float getLowFrequency() {
      return this.lowFrequency;
   }

   public float getHighFrequency() {
      return this.highFrequency;
   }

   public int getTimeInMs() {
      return this.timeInMs;
   }
}
