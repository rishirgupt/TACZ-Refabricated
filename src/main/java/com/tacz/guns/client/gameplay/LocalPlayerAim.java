package com.tacz.guns.client.gameplay;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.network.message.ClientMessagePlayerAim;
import com.tacz.guns.resource.modifier.custom.AdsModifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.client.player.LocalPlayer;

public class LocalPlayerAim {
   private final LocalPlayerDataHolder data;
   private final LocalPlayer player;

   public LocalPlayerAim(LocalPlayerDataHolder data, LocalPlayer player) {
      this.data = data;
      this.player = player;
   }

   public void aim(boolean isAim) {
      ItemStack mainHandItem = this.player.getMainHandItem();
      if (mainHandItem.getItem() instanceof IGun iGun) {
         Identifier gunId = iGun.getGunId(mainHandItem);
         TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
            this.data.clientIsAiming = isAim;
            ClientPlayNetworking.send(new ClientMessagePlayerAim(isAim));
         });
      }
   }

   public float getClientAimingProgress(float partialTicks) {
      return Mth.lerp(partialTicks, LocalPlayerDataHolder.oldAimingProgress, this.data.clientAimingProgress);
   }

   public boolean isAim() {
      return this.data.clientIsAiming;
   }

   public void tickAimingProgress() {
      ItemStack mainHandItem = this.player.getMainHandItem();
      if (mainHandItem.getItem() instanceof IGun iGun) {
         if (System.currentTimeMillis() - this.data.clientDrawTimestamp < 0L) {
            this.data.clientIsAiming = false;
         }

         Identifier gunId = iGun.getGunId(mainHandItem);
         TimelessAPI.getCommonGunIndex(gunId).ifPresentOrElse(index -> {
            float alphaProgress = this.getAlphaProgress(index.getGunData());
            this.aimProgressCalculate(alphaProgress);
         }, () -> {
            this.data.clientAimingProgress = 0.0F;
            LocalPlayerDataHolder.oldAimingProgress = 0.0F;
         });
      } else {
         this.data.clientAimingProgress = 0.0F;
         LocalPlayerDataHolder.oldAimingProgress = 0.0F;
      }
   }

   private void aimProgressCalculate(float alphaProgress) {
      LocalPlayerDataHolder.oldAimingProgress = this.data.clientAimingProgress;
      if (this.data.clientIsAiming) {
         this.data.clientAimingProgress += alphaProgress;
         if (this.data.clientAimingProgress > 1.0F) {
            this.data.clientAimingProgress = 1.0F;
         }
      } else {
         this.data.clientAimingProgress -= alphaProgress;
         if (this.data.clientAimingProgress < 0.0F) {
            this.data.clientAimingProgress = 0.0F;
         }
      }

      this.data.clientAimingTimestamp = System.currentTimeMillis();
   }

   private float getAlphaProgress(GunData gunData) {
      float aimTime = gunData.getAimTime();
      IGunOperator operator = IGunOperator.fromLivingEntity(this.player);
      if (operator.getCacheProperty() != null) {
         aimTime = operator.getCacheProperty().<Float>getCache(AdsModifier.ID);
      }

      aimTime = Math.max(0.0F, aimTime);
      return (float)(System.currentTimeMillis() - this.data.clientAimingTimestamp + 1L) / (aimTime * 1000.0F);
   }
}
