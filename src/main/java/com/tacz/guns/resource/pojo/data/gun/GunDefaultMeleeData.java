package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public class GunDefaultMeleeData {
   @SerializedName("animation_type")
   private String animationType = "melee_push";
   @SerializedName("distance")
   private float distance = 1.0F;
   @SerializedName("range_angle")
   private float rangeAngle = 30.0F;
   @SerializedName("cooldown")
   private float cooldown = 0.0F;
   @SerializedName("damage")
   private float damage = 0.0F;
   @SerializedName("knockback")
   private float knockback = 0.2F;
   @SerializedName("prep")
   private float prepTime = 0.1F;

   public String getAnimationType() {
      return this.animationType;
   }

   public float getDistance() {
      return this.distance;
   }

   public float getRangeAngle() {
      return this.rangeAngle;
   }

   public float getCooldown() {
      return this.cooldown;
   }

   public float getDamage() {
      return this.damage;
   }

   public float getKnockback() {
      return this.knockback;
   }

   public float getPrepTime() {
      return this.prepTime;
   }
}
