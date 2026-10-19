package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public class ChargeData {
   @SerializedName("type")
   private ChargeType chargeType = ChargeType.AUTO;
   @SerializedName("increase_per_tick")
   private float increasePerTick = 0.2F;
   @SerializedName("decrease_per_tick")
   private float decreasePerTick = 0.5F;
   @SerializedName("decrease_on_fire")
   private float decreaseOnFire = 0.0F;
   @SerializedName("max_charge")
   private float maxCharge = 1.0F;
   @SerializedName("fire_threshold")
   private float fireThreshold = 0.6F;
   @SerializedName("charge_during_cooldown")
   private boolean chargeDuringCooldown = true;

   public ChargeType getChargeType() {
      return this.chargeType;
   }

   public float getIncreasePerTick() {
      return this.increasePerTick;
   }

   public float getDecreasePerTick() {
      return this.decreasePerTick;
   }

   public float getDecreaseOnFire() {
      return this.decreaseOnFire;
   }

   public float getMaxCharge() {
      return this.maxCharge;
   }

   public float getFireThreshold() {
      return this.fireThreshold;
   }

   public boolean isChargeDuringCooldown() {
      return this.chargeDuringCooldown;
   }
}
