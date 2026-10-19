package com.tacz.guns.entity.shooter;

import com.tacz.guns.config.common.GunConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class LivingEntityAmmoCheck {
   private final LivingEntity shooter;

   public LivingEntityAmmoCheck(LivingEntity shooter) {
      this.shooter = shooter;
   }

   public boolean needCheckAmmo() {
      return this.shooter instanceof Player player ? !player.isCreative() : true;
   }

   public boolean consumesAmmoOrNot() {
      return !(this.shooter instanceof Player player) ? true : !player.isCreative() || (Boolean)GunConfig.CREATIVE_PLAYER_CONSUME_AMMO.get();
   }
}
