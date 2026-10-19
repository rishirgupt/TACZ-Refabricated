package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public class BulletData {
   @SerializedName("life")
   private float lifeSecond = 10.0F;
   @SerializedName("bullet_amount")
   private int bulletAmount = 1;
   @SerializedName("damage")
   private float damageAmount = 5.0F;
   @SerializedName("extra_damage")
   @Nullable
   private ExtraDamage extraDamage = null;
   @SerializedName("speed")
   private float speed = 5.0F;
   @SerializedName("gravity")
   private float gravity = 0.0F;
   @SerializedName("knockback")
   private float knockback = 0.0F;
   @SerializedName("friction")
   private float friction = 0.01F;
   @SerializedName("pierce")
   private int pierce = 1;
   @SerializedName("ignite")
   private Ignite ignite = new Ignite(false);
   @SerializedName("ignite_entity_time")
   private int igniteEntityTime = 2;
   @SerializedName("tracer_count_interval")
   private int tracerCountInterval = -1;
   @SerializedName("explosion")
   @Nullable
   private ExplosionData explosionData;

   public float getLifeSecond() {
      return this.lifeSecond;
   }

   public int getBulletAmount() {
      return this.bulletAmount;
   }

   public float getDamageAmount() {
      return this.damageAmount;
   }

   @Nullable
   public ExtraDamage getExtraDamage() {
      return this.extraDamage;
   }

   public float getSpeed() {
      return this.speed;
   }

   public float getGravity() {
      return this.gravity;
   }

   public float getKnockback() {
      return this.knockback;
   }

   public float getFriction() {
      return this.friction;
   }

   public int getPierce() {
      return this.pierce;
   }

   public Ignite getIgnite() {
      return this.ignite;
   }

   public int getIgniteEntityTime() {
      return this.igniteEntityTime;
   }

   public boolean hasTracerAmmo() {
      return this.tracerCountInterval >= 0;
   }

   public int getTracerCountInterval() {
      return this.tracerCountInterval;
   }

   @Nullable
   public ExplosionData getExplosionData() {
      return this.explosionData;
   }
}
