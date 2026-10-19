package com.tacz.guns.entity.shooter;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.custom.AdsModifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public class LivingEntityAim {
   private final LivingEntity shooter;
   private final ShooterDataHolder data;

   public LivingEntityAim(LivingEntity shooter, ShooterDataHolder data) {
      this.shooter = shooter;
      this.data = data;
   }

   public void aim(boolean isAim) {
      this.data.isAiming = isAim;
   }

   public void zoom() {
      if (this.data.currentGunItem != null) {
         ItemStack currentGunItem = this.data.currentGunItem.get();
         if (currentGunItem.getItem() instanceof IGun iGun) {
            Identifier var5 = iGun.getAttachmentId(currentGunItem, AttachmentType.SCOPE);
            CompoundTag scopeTag = iGun.getAttachmentTag(currentGunItem, AttachmentType.SCOPE);
            if (!DefaultAssets.isEmptyAttachmentId(var5) && scopeTag != null) {
               TimelessAPI.getCommonAttachmentIndex(var5).ifPresent(index -> {
                  int zoomNumber = AttachmentItemDataAccessor.getZoomNumberFromTag(scopeTag);
                  zoomNumber = ++zoomNumber % 2147483646;
                  AttachmentItemDataAccessor.setZoomNumberToTag(scopeTag, zoomNumber);
               });
            }

            iGun.setAttachmentTag(currentGunItem, AttachmentType.SCOPE, scopeTag);
         }
      }
   }

   public void tickAimingProgress() {
      if (this.data.currentGunItem != null && this.data.currentGunItem.get().getItem() instanceof IGun iGun) {
         ItemStack var8 = this.data.currentGunItem.get();
         Identifier gunId = iGun.getGunId(var8);
         Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
         if (gunIndexOptional.isEmpty()) {
            this.data.aimingProgress = 0.0F;
         } else {
            GunData gunData = gunIndexOptional.get().getGunData();
            float aimTime = gunData.getAimTime();
            if (this.data.cacheProperty != null) {
               aimTime = this.data.cacheProperty.<Float>getCache(AdsModifier.ID);
            }

            aimTime = Math.max(0.0F, aimTime);
            float alphaProgress = (float)(System.currentTimeMillis() - this.data.aimingTimestamp + 1L) / (aimTime * 1000.0F);
            if (this.data.isAiming) {
               this.data.aimingProgress += alphaProgress;
               if (this.data.aimingProgress > 1.0F) {
                  this.data.aimingProgress = 1.0F;
               }
            } else {
               this.data.aimingProgress -= alphaProgress;
               if (this.data.aimingProgress < 0.0F) {
                  this.data.aimingProgress = 0.0F;
               }
            }

            this.data.aimingTimestamp = System.currentTimeMillis();
         }
      } else {
         this.data.aimingProgress = 0.0F;
         this.data.aimingTimestamp = System.currentTimeMillis();
      }
   }

   public void tickSprint() {
      IGunOperator operator = IGunOperator.fromLivingEntity(this.shooter);
      ReloadState reloadState = operator.getSynReloadState();
      if (this.data.isAiming || reloadState.getStateType().isReloading() && !reloadState.getStateType().isReloadFinishing()) {
         this.shooter.setSprinting(false);
      }

      if (this.data.sprintTimestamp == -1L) {
         this.data.sprintTimestamp = System.currentTimeMillis();
      }

      if (this.data.currentGunItem != null) {
         ItemStack gunItem = this.data.currentGunItem.get();
         IGun iGun = IGun.getIGunOrNull(gunItem);
         if (iGun != null) {
            TimelessAPI.getCommonGunIndex(iGun.getGunId(gunItem)).ifPresentOrElse(gunIndex -> {
               float gunSprintTime = gunIndex.getGunData().getSprintTime();
               if (this.shooter.isSprinting() && !this.shooter.isCrouching()) {
                  this.data.sprintTimeS = this.data.sprintTimeS + (float)(System.currentTimeMillis() - this.data.sprintTimestamp) / 1000.0F;
                  if (this.data.sprintTimeS > gunSprintTime) {
                     this.data.sprintTimeS = gunSprintTime;
                  }
               } else {
                  this.data.sprintTimeS = this.data.sprintTimeS - (float)(System.currentTimeMillis() - this.data.sprintTimestamp) / 1000.0F;
                  if (this.data.sprintTimeS < 0.0F) {
                     this.data.sprintTimeS = 0.0F;
                  }
               }
            }, () -> this.data.sprintTimeS = 0.0F);
            this.data.sprintTimestamp = System.currentTimeMillis();
         }
      }
   }
}
