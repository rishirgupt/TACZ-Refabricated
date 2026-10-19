package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MoveSpeed {
   @SerializedName("base")
   private float baseMultiplier = 0.0F;
   @SerializedName("aim")
   private float aimMultiplier = 0.0F;
   @SerializedName("reload")
   private float reloadMultiplier = 0.0F;

   public MoveSpeed() {
   }

   public MoveSpeed(float baseMultiplier, float aimMultiplier, float reloadMultiplier) {
      this.baseMultiplier = baseMultiplier;
      this.aimMultiplier = aimMultiplier;
      this.reloadMultiplier = reloadMultiplier;
   }

   public float getBaseMultiplier() {
      return this.baseMultiplier;
   }

   public float getAimMultiplier() {
      return this.aimMultiplier;
   }

   public float getReloadMultiplier() {
      return this.reloadMultiplier;
   }

   public static MoveSpeed of(MoveSpeed moveSpeed, List<MoveSpeed> modifiers) {
      MoveSpeed result = new MoveSpeed();
      result.baseMultiplier = moveSpeed.baseMultiplier;
      result.aimMultiplier = moveSpeed.aimMultiplier;
      result.reloadMultiplier = moveSpeed.reloadMultiplier;

      for (MoveSpeed modifier : modifiers) {
         result.baseMultiplier = result.baseMultiplier + modifier.baseMultiplier;
         result.aimMultiplier = result.aimMultiplier + modifier.aimMultiplier;
         result.reloadMultiplier = result.reloadMultiplier + modifier.reloadMultiplier;
      }

      result.baseMultiplier = Math.max(0.0F, result.baseMultiplier);
      result.aimMultiplier = Math.max(0.0F, result.aimMultiplier);
      result.reloadMultiplier = Math.max(0.0F, result.reloadMultiplier);
      return result;
   }
}
