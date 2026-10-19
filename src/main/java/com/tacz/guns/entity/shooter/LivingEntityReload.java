package com.tacz.guns.entity.shooter;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.event.common.GunReloadEvent;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunReload;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public class LivingEntityReload {
   private final LivingEntity shooter;
   private final ShooterDataHolder data;
   private final LivingEntityDrawGun draw;
   private final LivingEntityShoot shoot;

   public LivingEntityReload(LivingEntity shooter, ShooterDataHolder data, LivingEntityDrawGun draw, LivingEntityShoot shoot) {
      this.shooter = shooter;
      this.data = data;
      this.draw = draw;
      this.shoot = shoot;
   }

   public void reload() {
      if (this.data.currentGunItem != null) {
         ItemStack currentGunItem = this.data.currentGunItem.get();
         if (currentGunItem.getItem() instanceof AbstractGunItem gunItem) {
            Identifier gunId = gunItem.getGunId(currentGunItem);
            TimelessAPI.getCommonGunIndex(gunId)
               .ifPresent(
                  gunIndex -> {
                     if (!gunItem.useInventoryAmmo(currentGunItem)) {
                        if (!this.data.reloadStateType.isReloading()) {
                           if (this.shoot.getShootCoolDown() == 0L) {
                              if (this.draw.getDrawCoolDown() == 0L) {
                                 if (!this.data.isBolting) {
                                    if (!IGunOperator.fromLivingEntity(this.shooter).needCheckAmmo() || gunItem.canReload(this.shooter, currentGunItem)) {
                                       GunReloadEvent gunReloadEvent = new GunReloadEvent(this.shooter, currentGunItem, LogicalSide.SERVER);
                                       ((GunReloadEvent.Callback)GunReloadEvent.CALLBACK.invoker()).post(gunReloadEvent);
                                       if (!gunReloadEvent.isCanceled()) {
                                          NetworkHandler.sendToTrackingEntity(
                                             new ServerMessageGunReload(this.shooter.getId(), currentGunItem), this.shooter
                                          );
                                          Bolt boltType = gunIndex.getGunData().getBolt();
                                          int ammoCount = gunItem.getCurrentAmmoCount(currentGunItem)
                                             + (gunItem.hasBulletInBarrel(currentGunItem) && boltType != Bolt.OPEN_BOLT ? 1 : 0);
                                          if (ammoCount <= 0) {
                                             this.data.reloadStateType = ReloadState.StateType.EMPTY_RELOAD_FEEDING;
                                          } else {
                                             this.data.reloadStateType = ReloadState.StateType.TACTICAL_RELOAD_FEEDING;
                                          }

                                          this.data.reloadTimestamp = System.currentTimeMillis();
                                          if (!gunItem.startReload(this.data, currentGunItem, this.shooter)) {
                                             this.data.reloadStateType = ReloadState.StateType.NOT_RELOADING;
                                             this.data.reloadTimestamp = -1L;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               );
         }
      }
   }

   public void cancelReload() {
      if (this.data.currentGunItem != null) {
         ItemStack currentGunItem = this.data.currentGunItem.get();
         if (currentGunItem.getItem() instanceof AbstractGunItem gunItem) {
            if (this.data.reloadStateType.isReloading()) {
               gunItem.interruptReload(this.data, currentGunItem, this.shooter);
            }
         }
      }
   }

   public ReloadState tickReloadState() {
      ReloadState result = new ReloadState();
      if (this.data.reloadTimestamp == -1L) {
         return result;
      }

      if (this.data.currentGunItem != null) {
         ItemStack currentGunItem = this.data.currentGunItem.get();
         if (currentGunItem != null && currentGunItem.getItem() instanceof AbstractGunItem abstractGunItem) {
            result = abstractGunItem.tickReload(this.data, currentGunItem, this.shooter);
         }
      }

      this.data.reloadStateType = result.getStateType();
      if (!result.getStateType().isReloading()) {
         this.data.reloadTimestamp = -1L;
      }

      return result;
   }
}
