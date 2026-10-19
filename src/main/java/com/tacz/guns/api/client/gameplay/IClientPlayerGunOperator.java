package com.tacz.guns.api.client.gameplay;

import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.client.gameplay.LocalPlayerDataHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public interface IClientPlayerGunOperator {
   static IClientPlayerGunOperator fromLocalPlayer(LocalPlayer player) {
      return (IClientPlayerGunOperator)player;
   }

   ShootResult shoot();

   void draw(ItemStack var1);

   void bolt();

   void reload();

   void inspect();

   void fireSelect();

   void aim(boolean var1);

   void crawl(boolean var1);

   void melee();

   boolean isAim();

   boolean isCrawl();

   LocalPlayerDataHolder getDataHolder();

   float getClientAimingProgress(float var1);

   long getClientShootCoolDown();

   boolean isReadyToDraw();

   void resetDraw();

   boolean chargeShoot(boolean var1);

   float getChargeProgress();

   boolean isCharging();
}
