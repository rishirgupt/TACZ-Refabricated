package com.tacz.guns.resource.pojo.data.gun;

import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.util.HitboxHelper;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Pose;

public enum InaccuracyType {
   @SerializedName("stand")
   STAND,
   @SerializedName("move")
   MOVE,
   @SerializedName("sneak")
   SNEAK,
   @SerializedName("lie")
   LIE,
   @SerializedName("aim")
   AIM;

   public static InaccuracyType getInaccuracyType(LivingEntity livingEntity) {
      float aimingProgress = IGunOperator.fromLivingEntity(livingEntity).getSynAimingProgress();
      if (aimingProgress == 1.0F) {
         return AIM;
      } else if (!livingEntity.isSwimming() && livingEntity.getPose() == Pose.SWIMMING) {
         return LIE;
      } else if (livingEntity.getPose() == Pose.CROUCHING) {
         return SNEAK;
      } else {
         return isMove(livingEntity) ? MOVE : STAND;
      }
   }

   public static Map<InaccuracyType, Float> getDefaultInaccuracy() {
      Map<InaccuracyType, Float> inaccuracy = Maps.newHashMap();
      inaccuracy.put(STAND, 5.0F);
      inaccuracy.put(MOVE, 5.75F);
      inaccuracy.put(SNEAK, 3.5F);
      inaccuracy.put(LIE, 2.5F);
      inaccuracy.put(AIM, 0.15F);
      return inaccuracy;
   }

   private static boolean isMove(LivingEntity livingEntity) {
      double distance = 0.6 * Math.hypot(livingEntity.getX() - livingEntity.xo, livingEntity.getZ() - livingEntity.zo);
      if (livingEntity instanceof Player player) {
         distance = HitboxHelper.getPlayerVelocity(player).length();
      }

      return distance > 0.05F;
   }

   public boolean isAim() {
      return this == AIM;
   }
}
