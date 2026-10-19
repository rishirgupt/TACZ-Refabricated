package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;
import javax.annotation.Nullable;

public class GunMeleeData {
   @SerializedName("distance")
   private float distance = 1.0F;
   @SerializedName("cooldown")
   private float cooldown = 1.0F;
   @SerializedName("default")
   @Nullable
   private GunDefaultMeleeData defaultMeleeData = null;

   public float getDistance() {
      return this.distance;
   }

   public float getCooldown() {
      return this.cooldown;
   }

   @Nullable
   public GunDefaultMeleeData getDefaultMeleeData() {
      return this.defaultMeleeData;
   }
}
