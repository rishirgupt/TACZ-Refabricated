package com.tacz.guns.entity.shooter;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageSyncBaseTimestamp;
import com.tacz.guns.network.message.event.ServerMessageGunShoot;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.ChargeData;
import com.tacz.guns.resource.pojo.data.gun.ChargeType;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

public class LivingEntityShoot {
   private final LivingEntity shooter;
   private final ShooterDataHolder data;
   private final LivingEntityDrawGun draw;

   public LivingEntityShoot(LivingEntity shooter, ShooterDataHolder data, LivingEntityDrawGun draw) {
      this.shooter = shooter;
      this.data = data;
      this.draw = draw;
   }

   public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp) {
      return this.shoot(pitch, yaw, timestamp, 0.0F, false);
   }

   public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress) {
      return this.shoot(pitch, yaw, timestamp, chargeProgress, true);
   }

   private ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress, boolean hasChargeContext) {
      if (this.data.currentGunItem == null) {
         return ShootResult.NOT_DRAW;
      } else {
         ItemStack currentGunItem = this.data.currentGunItem.get();
         if (!(currentGunItem.getItem() instanceof IGun iGun)) {
            return ShootResult.NOT_GUN;
         } else {
            Identifier gunId = iGun.getGunId(currentGunItem);
            Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
            if (gunIndexOptional.isEmpty()) {
               return ShootResult.ID_NOT_EXIST;
            }

            CommonGunIndex gunIndex = gunIndexOptional.get();
            if ((Boolean)SyncConfig.SERVER_SHOOT_COOLDOWN_V.get()) {
               long coolDown = this.getShootCoolDown(timestamp);
               if (coolDown == -1L) {
                  return ShootResult.UNKNOWN_FAIL;
               }

               if (coolDown > 0L) {
                  return ShootResult.COOL_DOWN;
               }
            }

            if ((Boolean)SyncConfig.SERVER_SHOOT_NETWORK_V.get()) {
               MinecraftServer server = Objects.requireNonNull(this.shooter.level().getServer());
               double tickTime = Math.max(server.getTickTimesNanos()[server.getTickCount() % 100] * 1.0E-6, 50.0);
               long alpha = System.currentTimeMillis() - this.data.baseTimestamp - timestamp;
               if (alpha < -300L || alpha > 300.0 + tickTime * 2.0) {
                  if (this.shooter instanceof ServerPlayer player) {
                     NetworkHandler.sendToClientPlayer(ServerMessageSyncBaseTimestamp.INSTANCE, player);
                  }

                  return ShootResult.NETWORK_FAIL;
               }
            }

            if (this.data.reloadStateType.isReloading()) {
               return ShootResult.IS_RELOADING;
            }

            if (this.draw.getDrawCoolDown() != 0L) {
               return ShootResult.IS_DRAWING;
            }

            if (this.data.isBolting) {
               return ShootResult.IS_BOLTING;
            }

            if (this.data.sprintTimeS > 0.0F) {
               return ShootResult.IS_SPRINTING;
            }

            ChargeData chargeData = gunIndex.getGunData().getChargeData(iGun.getFireMode(currentGunItem));
            if (hasChargeContext && !this.isChargeProgressReasonable(chargeData, chargeProgress)) {
               return ShootResult.UNKNOWN_FAIL;
            }

            IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.shooter);
            Bolt boltType = gunIndex.getGunData().getBolt();
            boolean useInventoryAmmo = iGun.useInventoryAmmo(currentGunItem);
            boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(currentGunItem) && boltType != Bolt.OPEN_BOLT;
            boolean hasInventoryAmmo = iGun.hasInventoryAmmo(this.shooter, currentGunItem, gunOperator.needCheckAmmo()) || hasAmmoInBarrel;
            int ammoCount = iGun.getCurrentAmmoCount(currentGunItem) + (hasAmmoInBarrel ? 1 : 0);
            boolean noAmmo = useInventoryAmmo && !hasInventoryAmmo || !useInventoryAmmo && ammoCount < 1;
            if (noAmmo) {
               return ShootResult.NO_AMMO;
            }

            if (gunIndex.getGunData().hasHeatData() && iGun.isOverheatLocked(currentGunItem)) {
               return ShootResult.OVERHEATED;
            }

            if (boltType == Bolt.MANUAL_ACTION && !hasAmmoInBarrel) {
               return ShootResult.NEED_BOLT;
            }

            if (boltType == Bolt.CLOSED_BOLT && !hasAmmoInBarrel) {
               if (useInventoryAmmo) {
                  this.consumeAmmoFromPlayer(1, currentGunItem, gunOperator.needCheckAmmo());
               } else {
                  iGun.reduceCurrentAmmoCount(currentGunItem);
               }

               iGun.setBulletInBarrel(currentGunItem, true);
            }

            GunShootEvent gunShootEvent = new GunShootEvent(this.shooter, currentGunItem, LogicalSide.SERVER);
            ((GunShootEvent.Callback)GunShootEvent.CALLBACK.invoker()).post(gunShootEvent);
            if (gunShootEvent.isCanceled()) {
               return ShootResult.FORGE_EVENT_CANCEL;
            }

            NetworkHandler.sendToTrackingEntity(new ServerMessageGunShoot(this.shooter.getId(), currentGunItem), this.shooter);
            this.data.lastShootTimestamp = this.data.shootTimestamp;
            this.data.heatTimestamp = System.currentTimeMillis();
            this.data.shootTimestamp = timestamp;
            this.data.chargeProgress = this.validateChargeProgress(chargeData, chargeProgress, hasChargeContext);
            if (iGun instanceof AbstractGunItem logicGun) {
               logicGun.shoot(this.data, currentGunItem, pitch, yaw, this.shooter);
            }

            return ShootResult.SUCCESS;
         }
      }
   }

   private boolean isChargeProgressReasonable(ChargeData chargeData, float chargeProgress) {
      float tolerance = 0.001F;
      if (!Float.isFinite(chargeProgress)) {
         return false;
      }

      if (chargeData == null) {
         return Math.abs(chargeProgress) <= 0.001F;
      }

      if (chargeProgress < -0.001F) {
         return false;
      }

      float minimumProgress = Math.min(chargeData.getFireThreshold(), chargeData.getMaxCharge());
      return chargeProgress + 0.001F < minimumProgress ? false : !(chargeProgress > this.getMaxReasonableChargeProgress(chargeData) + 0.001F);
   }

   private float getMaxReasonableChargeProgress(ChargeData chargeData) {
      float extraTicks = 4.0F;
      float startProgress = this.getChargeProgressAfterLastFire(chargeData);
      float elapsedTicks = Math.max((float)this.getChargeElapsedMillis() / 50.0F, 0.0F) + 4.0F;
      float maxProgress = startProgress + elapsedTicks * Math.max(chargeData.getIncreasePerTick(), 0.0F);
      return Math.min(maxProgress, chargeData.getMaxCharge());
   }

   private float getChargeProgressAfterLastFire(ChargeData chargeData) {
      if (this.data.shootTimestamp < 0L) {
         return 0.0F;
      } else {
         return chargeData.getChargeType() == ChargeType.DELAY ? 0.0F : Math.max(0.0F, this.data.chargeProgress - chargeData.getDecreaseOnFire());
      }
   }

   private long getChargeElapsedMillis() {
      if (this.data.shootTimestamp >= 0L) {
         long startTimestamp = this.data.baseTimestamp + this.data.shootTimestamp;
         return System.currentTimeMillis() - startTimestamp;
      } else {
         return this.data.drawTimestamp >= 0L ? System.currentTimeMillis() - this.data.drawTimestamp : 0L;
      }
   }

   private float validateChargeProgress(ChargeData chargeData, float chargeProgress, boolean hasChargeContext) {
      if (!hasChargeContext || !Float.isFinite(chargeProgress)) {
         return 0.0F;
      } else {
         return chargeData == null ? 0.0F : Math.max(0.0F, Math.min(chargeProgress, chargeData.getMaxCharge()));
      }
   }

   public long getShootCoolDown() {
      return this.getShootCoolDown(System.currentTimeMillis() - this.data.baseTimestamp);
   }

   public long getShootCoolDown(long timestamp) {
      if (this.data.currentGunItem == null) {
         return 0L;
      } else {
         ItemStack currentGunItem = this.data.currentGunItem.get();
         if (currentGunItem.getItem() instanceof IGun iGun) {
            Identifier gunId = iGun.getGunId(currentGunItem);
            Optional<com.tacz.guns.resource.index.CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(gunId);
            FireMode fireMode = iGun.getFireMode(currentGunItem);
            long interval = timestamp - this.data.shootTimestamp;
            return fireMode == FireMode.BURST ? gunIndex.<Long>map(index -> {
               long coolDown = (long)(index.getGunData().getBurstData().getMinInterval() * 1000.0) - interval;
               coolDown -= 5L;
               return Math.max(coolDown, 0L);
            }).orElse(-1L) : gunIndex.<Long>map(index -> {
               long coolDown = index.getGunData().getShootInterval(this.shooter, fireMode, currentGunItem) - interval;
               coolDown -= 5L;
               return Math.max(coolDown, 0L);
            }).orElse(-1L);
         } else {
            return 0L;
         }
      }
   }

   public void consumeAmmoFromPlayer(int neededAmount, ItemStack itemStack, boolean needCheckAmmo) {
      if (itemStack.getItem() instanceof AbstractGunItem abstractGunItem) {
         if (needCheckAmmo) {
            if (abstractGunItem.useDummyAmmo(itemStack)) {
               abstractGunItem.findAndExtractDummyAmmo(itemStack, neededAmount);
            } else {
               this.shooter.tacz$getItemHandler(null).map(cap -> abstractGunItem.findAndExtractInventoryAmmo(cap, itemStack, neededAmount));
            }
         }
      }
   }
}
