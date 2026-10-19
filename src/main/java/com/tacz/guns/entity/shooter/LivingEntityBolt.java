package com.tacz.guns.entity.shooter;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public class LivingEntityBolt {
   private final ShooterDataHolder data;
   private final LivingEntityDrawGun draw;
   private final LivingEntityShoot shoot;
   private final LivingEntity shooter;

   public LivingEntityBolt(ShooterDataHolder data, LivingEntity shooter, LivingEntityDrawGun draw, LivingEntityShoot shoot) {
      this.data = data;
      this.draw = draw;
      this.shoot = shoot;
      this.shooter = shooter;
   }

   public void bolt() {
      if (this.data.currentGunItem != null) {
         ItemStack currentGunItem = this.data.currentGunItem.get();
         if (currentGunItem.getItem() instanceof AbstractGunItem iGun) {
            Identifier gunId = iGun.getGunId(currentGunItem);
            TimelessAPI.getCommonGunIndex(gunId).ifPresent(gunIndex -> {
               if (this.shoot.getShootCoolDown() == 0L) {
                  if (!this.data.reloadStateType.isReloading()) {
                     if (this.draw.getDrawCoolDown() == 0L) {
                        if (!this.data.isBolting) {
                           IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.shooter);
                           Bolt boltType = gunIndex.getGunData().getBolt();
                           boolean useInventoryAmmo = iGun.useInventoryAmmo(currentGunItem);
                           boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(currentGunItem) && boltType != Bolt.OPEN_BOLT;
                           boolean hasInventoryAmmo = iGun.hasInventoryAmmo(this.shooter, currentGunItem, gunOperator.needCheckAmmo());
                           boolean noAmmo = useInventoryAmmo && !hasInventoryAmmo || !useInventoryAmmo && iGun.getCurrentAmmoCount(currentGunItem) < 1;
                           if (boltType == Bolt.MANUAL_ACTION) {
                              if (!hasAmmoInBarrel) {
                                 if (!noAmmo) {
                                    this.data.boltTimestamp = System.currentTimeMillis();
                                    this.data.isBolting = iGun.startBolt(this.data, currentGunItem, this.shooter);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            });
         }
      }
   }

   public void tickBolt() {
      if (this.data.isBolting) {
         if (this.data.currentGunItem == null) {
            this.data.isBolting = false;
         } else {
            ItemStack currentGunItem = this.data.currentGunItem.get();
            if (currentGunItem.getItem() instanceof AbstractGunItem iGun) {
               Identifier gunId = iGun.getGunId(currentGunItem);
               Optional<com.tacz.guns.resource.index.CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(gunId);
               this.data.isBolting = gunIndex.<Boolean>map(index -> iGun.tickBolt(this.data, currentGunItem, this.shooter)).orElse(false);
            } else {
               this.data.isBolting = false;
            }
         }
      }
   }
}
