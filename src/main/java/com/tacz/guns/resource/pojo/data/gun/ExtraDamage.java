package com.tacz.guns.resource.pojo.data.gun;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import java.util.LinkedList;
import org.jetbrains.annotations.Nullable;

public class ExtraDamage {
   @SerializedName("armor_ignore")
   private float armorIgnore = 0.0F;
   @SerializedName("head_shot_multiplier")
   private float headShotMultiplier = 1.0F;
   @SerializedName("damage_adjust")
   private LinkedList<ExtraDamage.DistanceDamagePair> damageAdjust = Lists.newLinkedList();

   public float getArmorIgnore() {
      return this.armorIgnore;
   }

   public float getHeadShotMultiplier() {
      return this.headShotMultiplier;
   }

   @Nullable
   public LinkedList<ExtraDamage.DistanceDamagePair> getDamageAdjust() {
      return this.damageAdjust.isEmpty() ? null : this.damageAdjust;
   }

   public static class DistanceDamagePair {
      @SerializedName("distance")
      private float distance;
      @SerializedName("damage")
      private float damage;

      public DistanceDamagePair(float distance, float damage) {
         this.distance = distance;
         this.damage = damage;
      }

      public float getDistance() {
         return this.distance;
      }

      public float getDamage() {
         return this.damage;
      }
   }
}
