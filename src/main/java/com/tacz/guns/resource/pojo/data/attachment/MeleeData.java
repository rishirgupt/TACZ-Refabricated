package com.tacz.guns.resource.pojo.data.attachment;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MeleeData {
   @SerializedName("distance")
   private float distance = 1.0F;
   @SerializedName("range_angle")
   private float rangeAngle = 30.0F;
   @SerializedName("cooldown")
   private float cooldown = 0.0F;
   @SerializedName("damage")
   private float damage = 0.0F;
   @SerializedName("knockback")
   private float knockback = 0.0F;
   @SerializedName("prep")
   private float prepTime = 0.1F;
   @SerializedName("effects")
   private List<EffectData> effects = Lists.newArrayList();

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

   public List<EffectData> getEffects() {
      return this.effects;
   }
}
