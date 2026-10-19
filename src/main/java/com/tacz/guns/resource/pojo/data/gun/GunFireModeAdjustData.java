package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public class GunFireModeAdjustData {
   @SerializedName("damage")
   private float damageAmount = 0.0F;
   @SerializedName("rpm")
   private int roundsPerMinute = 0;
   @SerializedName("speed")
   private float speed = 0.0F;
   @SerializedName("knockback")
   private float knockback = 0.0F;
   @SerializedName("armor_ignore")
   private float armorIgnore = 0.0F;
   @SerializedName("head_shot_multiplier")
   private float headShotMultiplier = 0.0F;
   @SerializedName("aim_inaccuracy")
   private float aimInaccuracy = 0.0F;
   @SerializedName("other_inaccuracy")
   private float otherInaccuracy = 0.0F;

   public float getDamageAmount() {
      return this.damageAmount;
   }

   public int getRoundsPerMinute() {
      return this.roundsPerMinute;
   }

   public float getSpeed() {
      return this.speed;
   }

   public float getKnockback() {
      return this.knockback;
   }

   public float getArmorIgnore() {
      return this.armorIgnore;
   }

   public float getHeadShotMultiplier() {
      return this.headShotMultiplier;
   }

   public float getAimInaccuracy() {
      return this.aimInaccuracy;
   }

   public float getOtherInaccuracy() {
      return this.otherInaccuracy;
   }
}
