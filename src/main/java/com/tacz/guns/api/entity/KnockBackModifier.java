package com.tacz.guns.api.entity;

import net.minecraft.world.entity.LivingEntity;

public interface KnockBackModifier {
   static KnockBackModifier fromLivingEntity(LivingEntity entity) {
      return (KnockBackModifier)entity;
   }

   void resetKnockBackStrength();

   double getKnockBackStrength();

   void setKnockBackStrength(double var1);
}
