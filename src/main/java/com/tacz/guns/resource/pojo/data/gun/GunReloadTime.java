package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public class GunReloadTime {
   @SerializedName("empty")
   private float emptyTime = 2.5F;
   @SerializedName("tactical")
   private float tacticalTime = 2.0F;

   public float getEmptyTime() {
      return this.emptyTime;
   }

   public float getTacticalTime() {
      return this.tacticalTime;
   }
}
