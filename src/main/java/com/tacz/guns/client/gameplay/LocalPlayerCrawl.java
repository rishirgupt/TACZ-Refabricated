package com.tacz.guns.client.gameplay;

import cn.sh1rocu.tacz.api.mixin.ForcePoseInjection;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.network.message.ClientMessagePlayerCrawl;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.client.player.LocalPlayer;

public class LocalPlayerCrawl {
   private static final int COOLDOWN_TICKS = 10;
   private final LocalPlayer player;
   private boolean isCrawling = false;
   private int crawCooldownTicks = 0;

   public LocalPlayerCrawl(LocalPlayer player) {
      this.player = player;
   }

   public void crawl(boolean isCrawl) {
      ItemStack mainHandItem = this.player.getMainHandItem();
      if (mainHandItem.getItem() instanceof IGun iGun) {
         if (iGun.isCanCrawl(mainHandItem)) {
            if (this.crawCooldownTicks <= 0) {
               if (!this.player.isSpectator() && !this.player.isPassenger() && this.player.onGround()) {
                  Identifier gunId = iGun.getGunId(mainHandItem);
                  TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
                     this.isCrawling = isCrawl;
                     this.crawCooldownTicks = 10;
                     ClientPlayNetworking.send(new ClientMessagePlayerCrawl(isCrawl));
                  });
               }
            }
         }
      }
   }

   public void tickCrawl() {
      if (this.crawCooldownTicks > 0) {
         this.crawCooldownTicks--;
      }

      ItemStack mainHandItem = this.player.getMainHandItem();
      if (mainHandItem.getItem() instanceof IGun iGun) {
         if (!iGun.isCanCrawl(mainHandItem)) {
            this.isCrawling = false;
            this.setCrawlPose();
         } else {
            Identifier gunId = iGun.getGunId(mainHandItem);
            if (TimelessAPI.getCommonGunIndex(gunId).isEmpty()) {
               this.isCrawling = false;
               this.setCrawlPose();
            } else if (!this.player.isSpectator()
               && !this.player.isPassenger()
               && !this.player.jumping
               && !this.player.isSwimming()
               && this.player.onGround()) {
               this.setCrawlPose();
            } else {
               this.isCrawling = false;
               this.setCrawlPose();
            }
         }
      } else {
         this.isCrawling = false;
         this.setCrawlPose();
      }
   }

   public boolean isCrawling() {
      return this.isCrawling;
   }

   private void setCrawlPose() {
      if (this.isCrawling) {
         ((ForcePoseInjection)this.player).tacz$setForcedPose(Pose.SWIMMING);
      } else {
         ((ForcePoseInjection)this.player).tacz$setForcedPose(null);
      }
   }
}
