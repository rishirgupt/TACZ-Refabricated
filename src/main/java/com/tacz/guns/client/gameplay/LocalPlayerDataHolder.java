package com.tacz.guns.client.gameplay;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.player.LocalPlayer;

public class LocalPlayerDataHolder {
   public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(2);
   public long clientBaseTimestamp = -1L;
   public static float oldAimingProgress = 0.0F;
   public static long clientClickButtonTimestamp = -1L;
   private final LocalPlayer player;
   public volatile long clientShootTimestamp = -1L;
   public volatile long clientLastShootTimestamp = -1L;
   public volatile boolean isShootRecorded = true;
   public float chargeProgress = 0.0F;
   public boolean isCharging = false;
   public volatile boolean clientStateLock = false;
   public boolean isBolting = false;
   public float clientAimingProgress = 0.0F;
   public long clientAimingTimestamp = -1L;
   public boolean clientIsAiming = false;
   public long clientDrawTimestamp = -1L;
   @Nullable
   public ScheduledFuture<?> drawFuture = null;
   @Nullable
   public Predicate<IGunOperator> lockedCondition = null;
   public long lockTimestamp = -1L;

   public LocalPlayerDataHolder(LocalPlayer player) {
      this.player = player;
   }

   public void lockState(@Nullable Predicate<IGunOperator> lockedCondition) {
      this.clientStateLock = true;
      this.lockTimestamp = System.currentTimeMillis();
      this.lockedCondition = lockedCondition;
   }

   public void tickStateLock() {
      IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.player);
      ReloadState reloadState = gunOperator.getSynReloadState();
      long maxLockTime = 250L;
      long lockTime = System.currentTimeMillis() - this.lockTimestamp;
      if (lockTime >= maxLockTime || this.lockedCondition == null || this.lockedCondition.test(gunOperator)) {
         this.lockedCondition = null;
         if (!reloadState.getStateType().isReloading()) {
            long shootCoolDown = gunOperator.getSynShootCoolDown();
            if (shootCoolDown <= 0L) {
               if (gunOperator.getSynDrawCoolDown() <= 0L) {
                  if (!gunOperator.getSynIsBolting()) {
                     if (gunOperator.getSynMeleeCoolDown() <= 0L) {
                        this.clientStateLock = false;
                     }
                  }
               }
            }
         }
      }
   }

   public void reset() {
      this.isShootRecorded = true;
      this.clientShootTimestamp = -1L;
      this.chargeProgress = 0.0F;
      this.isCharging = false;
      this.clientIsAiming = false;
      this.clientAimingProgress = 0.0F;
      oldAimingProgress = 0.0F;
      this.isBolting = false;
      this.clientStateLock = false;
   }
}
