package com.tacz.guns.entity.shooter;

import cn.sh1rocu.tacz.api.mixin.ForcePoseInjection;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;

public class LivingEntityCrawl {
   private final LivingEntity shooter;
   private final ShooterDataHolder data;

   public LivingEntityCrawl(LivingEntity shooter, ShooterDataHolder data) {
      this.shooter = shooter;
      this.data = data;
   }

   public void crawl(boolean isCrawl) {
      this.data.isCrawling = isCrawl;
   }

   public void tickCrawling() {
      if (this.data.currentGunItem != null && this.data.currentGunItem.get().getItem() instanceof IGun iGun) {
         ItemStack var4 = this.data.currentGunItem.get();
         if (!iGun.isCanCrawl(var4)) {
            this.data.isCrawling = false;
            this.setCrawlPose();
         } else {
            Identifier gunId = iGun.getGunId(var4);
            if (TimelessAPI.getCommonGunIndex(gunId).isEmpty()) {
               this.data.isCrawling = false;
               this.setCrawlPose();
            } else if (!this.shooter.isSpectator()
               && !this.shooter.isPassenger()
               && !this.shooter.jumping
               && !this.shooter.isSwimming()
               && this.shooter.onGround()) {
               this.setCrawlPose();
            } else {
               this.data.isCrawling = false;
               this.setCrawlPose();
            }
         }
      } else {
         this.data.isCrawling = false;
         this.setCrawlPose();
      }
   }

   private void setCrawlPose() {
      if (this.data.isCrawling) {
         if (this.shooter instanceof Player player) {
            ((ForcePoseInjection)player).tacz$setForcedPose(Pose.SWIMMING);
         } else {
            this.shooter.setPose(Pose.SWIMMING);
         }
      } else if (this.shooter instanceof Player player) {
         ((ForcePoseInjection)player).tacz$setForcedPose(null);
      }
   }
}
