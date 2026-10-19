package com.tacz.guns.client.gameplay;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.mixin.accessor.BlockableEventLoopAccessor;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessagePlayerShoot;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.SilenceModifier;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.ChargeData;
import com.tacz.guns.resource.pojo.data.gun.ChargeType;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.sound.SoundManager;
import it.unimi.dsi.fastutil.Pair;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

public class LocalPlayerShoot {
   private static final Predicate<IGunOperator> SHOOT_LOCKED_CONDITION = operator -> operator.getSynShootCoolDown() > 0L;
   private final LocalPlayerDataHolder data;
   private final LocalPlayer player;

   public LocalPlayerShoot(LocalPlayerDataHolder data, LocalPlayer player) {
      this.data = data;
      this.player = player;
   }

   public boolean chargeShoot(boolean isCharging) {
      IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.player);
      ItemStack mainHandItem = this.player.getMainHandItem();
      if (!(mainHandItem.getItem() instanceof IGun iGun)) {
         this.data.chargeProgress = 0.0F;
         return false;
      } else {
         Identifier gunId = iGun.getGunId(mainHandItem);
         Optional gunIndexOptional = TimelessAPI.getClientGunIndex(gunId);
         GunDisplayInstance display = TimelessAPI.getGunDisplay(mainHandItem).orElse(null);
         if (!gunIndexOptional.isEmpty() && display != null) {
            ClientGunIndex gunIndex = (ClientGunIndex)gunIndexOptional.get();
            GunData gunData = gunIndex.getGunData();
            FireMode fireMode = iGun.getFireMode(mainHandItem);
            ChargeData chargeData = gunData.getChargeData(fireMode);
            if (chargeData == null) {
               return isCharging;
            }

            boolean canChargeDuringCooldown = chargeData.isChargeDuringCooldown() || this.getCoolDown(iGun, mainHandItem, gunData) < 50L;
            boolean canCharge = canChargeDuringCooldown && this.preCheck(iGun, gunOperator, gunIndex, mainHandItem, display, gunData, isCharging) == null;
            float chargeProgress = this.data.chargeProgress;
            ChargeType type = chargeData.getChargeType();
            if (type == ChargeType.AUTO) {
               if (isCharging && canCharge) {
                  this.data.isCharging = true;
                  this.data.chargeProgress = Math.min(chargeProgress + chargeData.getIncreasePerTick(), chargeData.getMaxCharge());
                  return this.data.chargeProgress >= chargeData.getMaxCharge();
               }

               this.data.isCharging = false;
               this.data.chargeProgress = Math.max(chargeProgress - chargeData.getDecreasePerTick(), 0.0F);
            } else if (type == ChargeType.HOLD) {
               if (isCharging && canCharge) {
                  this.data.isCharging = true;
                  this.data.chargeProgress = Math.min(chargeProgress + chargeData.getIncreasePerTick(), chargeData.getMaxCharge());
               } else {
                  if (canChargeDuringCooldown && chargeProgress >= chargeData.getFireThreshold()) {
                     return true;
                  }

                  this.data.isCharging = false;
                  this.data.chargeProgress = Math.max(chargeProgress - chargeData.getDecreasePerTick(), 0.0F);
               }
            } else if (type == ChargeType.DELAY) {
               if ((isCharging || chargeProgress > 0.0F) && canCharge) {
                  this.data.isCharging = true;
                  this.data.chargeProgress = Math.min(chargeProgress + chargeData.getIncreasePerTick(), chargeData.getMaxCharge());
                  return this.data.chargeProgress >= chargeData.getMaxCharge();
               }

               this.data.isCharging = false;
               this.data.chargeProgress = Math.max(chargeProgress - chargeData.getDecreasePerTick(), 0.0F);
            }

            return false;
         } else {
            return false;
         }
      }
   }

   public ShootResult shoot() {
      IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.player);
      ItemStack mainHandItem = this.player.getMainHandItem();
      if (mainHandItem.getItem() instanceof IGun iGun) {
         Identifier gunId = iGun.getGunId(mainHandItem);
         Optional gunIndexOptional = TimelessAPI.getClientGunIndex(gunId);
         GunDisplayInstance display = TimelessAPI.getGunDisplay(mainHandItem).orElse(null);
         if (!gunIndexOptional.isEmpty() && display != null) {
            ClientGunIndex gunIndex = (ClientGunIndex)gunIndexOptional.get();
            GunData gunData = gunIndex.getGunData();
            long coolDown = this.getCoolDown(iGun, mainHandItem, gunData);
            if (!this.data.isShootRecorded) {
               return ShootResult.COOL_DOWN;
            }

            if (this.data.clientStateLock && this.data.lockedCondition != SHOOT_LOCKED_CONDITION && this.data.lockedCondition != null) {
               this.data.isShootRecorded = true;
               return ShootResult.IS_DRAWING;
            }

            if (coolDown >= 50L) {
               return ShootResult.COOL_DOWN;
            }

            ShootResult result = this.preCheck(iGun, gunOperator, gunIndex, mainHandItem, display, gunData, true);
            if (result != null) {
               return result;
            }

            if (gunOperator.getSynSprintTime() > 0.0F) {
               return ShootResult.IS_SPRINTING;
            }

            GunShootEvent event = new GunShootEvent(this.player, mainHandItem, LogicalSide.CLIENT);
            ((GunShootEvent.Callback)GunShootEvent.CALLBACK.invoker()).post(event);
            if (event.isCanceled()) {
               return ShootResult.FORGE_EVENT_CANCEL;
            }

            this.data.lockState(SHOOT_LOCKED_CONDITION);
            this.data.isShootRecorded = false;
            float finalChargeProgress = this.data.chargeProgress;
            this.doShoot(display, iGun, mainHandItem, gunData, coolDown, finalChargeProgress);
            FireMode fireMode = iGun.getFireMode(mainHandItem);
            ChargeData chargeData = gunData.getChargeData(fireMode);
            if (chargeData != null) {
               if (chargeData.getChargeType() == ChargeType.DELAY) {
                  this.data.chargeProgress = 0.0F;
               } else {
                  this.data.chargeProgress = Math.max(0.0F, this.data.chargeProgress - chargeData.getDecreaseOnFire());
               }
            }

            return ShootResult.SUCCESS;
         } else {
            return ShootResult.ID_NOT_EXIST;
         }
      } else {
         return ShootResult.NOT_GUN;
      }
   }

   @Nullable
   private ShootResult preCheck(
      IGun iGun, IGunOperator gunOperator, ClientGunIndex gunIndex, ItemStack mainHandItem, GunDisplayInstance display, GunData gunData, boolean playDrySound
   ) {
      if (System.currentTimeMillis() - LocalPlayerDataHolder.clientClickButtonTimestamp < 50L) {
         return ShootResult.COOL_DOWN;
      }

      if (gunOperator.getSynReloadState().getStateType().isReloading()) {
         return ShootResult.IS_RELOADING;
      }

      if (gunOperator.getSynDrawCoolDown() != 0L) {
         return ShootResult.IS_DRAWING;
      }

      if (gunOperator.getSynIsBolting()) {
         return ShootResult.IS_BOLTING;
      }

      if (gunOperator.getSynMeleeCoolDown() != 0L) {
         return ShootResult.IS_MELEE;
      }

      Bolt boltType = gunIndex.getGunData().getBolt();
      boolean useInventoryAmmo = iGun.useInventoryAmmo(mainHandItem);
      boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(mainHandItem) && boltType != Bolt.OPEN_BOLT;
      boolean hasInventoryAmmo = iGun.hasInventoryAmmo(this.player, mainHandItem, gunOperator.needCheckAmmo()) || hasAmmoInBarrel;
      int ammoCount = iGun.getCurrentAmmoCount(mainHandItem) + (hasAmmoInBarrel ? 1 : 0);
      boolean noAmmo = useInventoryAmmo && !hasInventoryAmmo || !useInventoryAmmo && ammoCount < 1;
      if (noAmmo) {
         if (playDrySound) {
            SoundPlayManager.playDryFireSound(this.player, display);
         }

         return ShootResult.NO_AMMO;
      } else if (gunData.hasHeatData() && iGun.isOverheatLocked(mainHandItem)) {
         if (playDrySound) {
            SoundPlayManager.playDryFireSound(this.player, display);
         }

         return ShootResult.OVERHEATED;
      } else if (boltType == Bolt.MANUAL_ACTION && !hasAmmoInBarrel) {
         IClientPlayerGunOperator.fromLocalPlayer(this.player).bolt();
         return ShootResult.NEED_BOLT;
      } else {
         return null;
      }
   }

   private void doShoot(GunDisplayInstance display, IGun iGun, ItemStack mainHandItem, GunData gunData, long delay, float chargeProgress) {
      FireMode fireMode = iGun.getFireMode(mainHandItem);
      Bolt boltType = gunData.getBolt();
      boolean consumeAmmo = IGunOperator.fromLivingEntity(this.player).consumesAmmoOrNot();
      boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(mainHandItem) && boltType != Bolt.OPEN_BOLT;
      int ammoCount = consumeAmmo ? iGun.getCurrentAmmoCount(mainHandItem) + (hasAmmoInBarrel ? 1 : 0) : Integer.MAX_VALUE;
      long period = fireMode == FireMode.BURST ? gunData.getBurstShootInterval() : 1L;
      int maxCount = Math.min(ammoCount, fireMode == FireMode.BURST ? gunData.getBurstData().getCount() : 1);
      AtomicInteger count = new AtomicInteger(0);
      LocalPlayerDataHolder.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
         if (count.get() == 0) {
            this.data.isShootRecorded = true;
         }

         if (gunData.hasHeatData() && iGun.isOverheatLocked(mainHandItem)) {
            ScheduledFuture<?> future = (ScheduledFuture<?>)Thread.currentThread();
            future.cancel(false);
         } else if (count.get() < maxCount && !this.player.isDeadOrDying()) {
            if (count.get() == 0) {
               if (this.data.clientStateLock && this.data.lockedCondition != SHOOT_LOCKED_CONDITION && this.data.lockedCondition != null) {
                  return;
               }

               this.data.clientLastShootTimestamp = this.data.clientShootTimestamp;
               this.data.clientShootTimestamp = System.currentTimeMillis();
               ClientPlayNetworking.send(new ClientMessagePlayerShoot(this.data.clientShootTimestamp - this.data.clientBaseTimestamp, chargeProgress));
            }

            ((BlockableEventLoopAccessor)Minecraft.getInstance()).tacz$submitAsync(() -> {
               GunFireEvent event = new GunFireEvent(this.player, mainHandItem, LogicalSide.CLIENT);
               ((GunFireEvent.Callback)GunFireEvent.CALLBACK.invoker()).post(event);
               boolean fire = !event.isCanceled();
               if (fire) {
                  AnimationStateMachine<?> animationStateMachine = display.getAnimationStateMachine();
                  if (animationStateMachine != null) {
                     animationStateMachine.trigger("shoot");
                  }

                  boolean useSilenceSound = this.useSilenceSound();
                  SoundPlayManager.stopPlayGunSound(display, SoundManager.INSPECT_SOUND);
                  if (useSilenceSound) {
                     SoundPlayManager.playSilenceSound(this.player, display, gunData);
                  } else {
                     SoundPlayManager.playShootSound(this.player, display, gunData);
                  }
               }
            });
            count.getAndIncrement();
         } else {
            ScheduledFuture<?> future = (ScheduledFuture<?>)Thread.currentThread();
            future.cancel(false);
         }
      }, delay, period, TimeUnit.MILLISECONDS);
   }

   private boolean useSilenceSound() {
      AttachmentCacheProperty cacheProperty = IGunOperator.fromLivingEntity(this.player).getCacheProperty();
      if (cacheProperty != null) {
         Pair<Integer, Boolean> silence = cacheProperty.getCache(SilenceModifier.ID);
         return (Boolean)silence.right();
      } else {
         return false;
      }
   }

   private long getCoolDown(IGun iGun, ItemStack mainHandItem, GunData gunData) {
      FireMode fireMode = iGun.getFireMode(mainHandItem);
      long coolDown;
      if (fireMode == FireMode.BURST) {
         coolDown = (long)(gunData.getBurstData().getMinInterval() * 1000.0) - (System.currentTimeMillis() - this.data.clientShootTimestamp);
      } else {
         coolDown = gunData.getShootInterval(this.player, fireMode, mainHandItem) - (System.currentTimeMillis() - this.data.clientShootTimestamp);
      }

      return Math.max(coolDown, 0L);
   }

   public long getClientShootCoolDown() {
      ItemStack mainHandItem = this.player.getMainHandItem();
      IGun iGun = IGun.getIGunOrNull(mainHandItem);
      if (iGun == null) {
         return -1L;
      }

      Identifier gunId = iGun.getGunId(mainHandItem);
      Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
      return gunIndexOptional.<Long>map(commonGunIndex -> this.getCoolDown(iGun, mainHandItem, commonGunIndex.getGunData())).orElse(-1L);
   }
}
