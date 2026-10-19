package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public class FireSound {
   @SerializedName("fire_multiplier")
   private float fireMultiplier = 1.0F;
   @SerializedName("silence_multiplier")
   private float silenceMultiplier = 1.0F;

   public float getFireMultiplier() {
      return this.fireMultiplier;
   }

   public float getSilenceMultiplier() {
      return this.silenceMultiplier;
   }
}
