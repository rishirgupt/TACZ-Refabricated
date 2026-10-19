package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public class ExplosionData {
   @SerializedName("explode")
   private boolean explode;
   @SerializedName("radius")
   private float radius;
   @SerializedName("damage")
   private float damage;
   @SerializedName("knockback")
   private boolean knockback;
   @SerializedName("destroy_block")
   private boolean destroyBlock;
   @SerializedName("delay")
   private float delay;

   public ExplosionData(boolean explode, float radius, float damage, boolean knockback, float delay, boolean destroyBlock) {
      this.explode = explode;
      this.radius = radius;
      this.damage = damage;
      this.knockback = knockback;
      this.delay = delay;
      this.destroyBlock = destroyBlock;
   }

   public boolean isExplode() {
      return this.explode;
   }

   public float getRadius() {
      return this.radius;
   }

   public float getDamage() {
      return this.damage;
   }

   public boolean isKnockback() {
      return this.knockback;
   }

   public boolean isDestroyBlock() {
      return this.destroyBlock;
   }

   public float getDelay() {
      return this.delay;
   }
}
