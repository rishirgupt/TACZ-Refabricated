package com.tacz.guns.entity.shooter;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.GunMeleeEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunMelee;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.attachment.MeleeData;
import com.tacz.guns.resource.pojo.data.gun.GunDefaultMeleeData;
import com.tacz.guns.resource.pojo.data.gun.GunMeleeData;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public class LivingEntityMelee {
   private final LivingEntity shooter;
   private final ShooterDataHolder data;
   private final LivingEntityDrawGun draw;

   public LivingEntityMelee(LivingEntity shooter, ShooterDataHolder data, LivingEntityDrawGun draw) {
      this.shooter = shooter;
      this.data = data;
      this.draw = draw;
   }

   public void melee() {
      if (this.data.currentGunItem != null) {
         if (this.draw.getDrawCoolDown() == 0L) {
            if (!this.data.isBolting) {
               long coolDown = this.getMeleeCoolDown();
               if (coolDown == 0L) {
                  ItemStack currentGunItem = this.data.currentGunItem.get();
                  GunMeleeEvent gunMeleeEvent = new GunMeleeEvent(this.shooter, currentGunItem, LogicalSide.SERVER);
                  ((GunMeleeEvent.Callback)GunMeleeEvent.CALLBACK.invoker()).post(gunMeleeEvent);
                  if (!gunMeleeEvent.isCanceled()) {
                     NetworkHandler.sendToTrackingEntity(new ServerMessageGunMelee(this.shooter.getId(), currentGunItem), this.shooter);
                     if (currentGunItem.getItem() instanceof AbstractGunItem logicGun) {
                        this.data.meleeTimestamp = System.currentTimeMillis();
                        Identifier muzzleId = logicGun.getAttachmentId(currentGunItem, AttachmentType.MUZZLE);
                        MeleeData muzzleMeleeData = this.getMeleeData(muzzleId);
                        if (muzzleMeleeData != null) {
                           float prepTime = muzzleMeleeData.getPrepTime();
                           this.data.meleePrepTickCount = (int)Math.max(0.0F, prepTime * 20.0F);
                           return;
                        }

                        Identifier stockId = logicGun.getAttachmentId(currentGunItem, AttachmentType.STOCK);
                        MeleeData stockMeleeData = this.getMeleeData(stockId);
                        if (stockMeleeData != null) {
                           float prepTime = stockMeleeData.getPrepTime();
                           this.data.meleePrepTickCount = (int)Math.max(0.0F, prepTime * 20.0F);
                           return;
                        }

                        Identifier gunId = logicGun.getGunId(currentGunItem);
                        TimelessAPI.getCommonGunIndex(gunId).ifPresent(index -> {
                           GunDefaultMeleeData defaultMeleeData = index.getGunData().getMeleeData().getDefaultMeleeData();
                           if (defaultMeleeData != null) {
                              float prepTime = defaultMeleeData.getPrepTime();
                              this.data.meleePrepTickCount = (int)Math.max(0.0F, prepTime * 20.0F);
                           }
                        });
                     }
                  }
               }
            }
         }
      }
   }

   public void scheduleTickMelee() {
      if (this.data.meleePrepTickCount > 0) {
         this.data.meleePrepTickCount--;
      } else {
         if (this.data.meleePrepTickCount == 0) {
            this.data.meleePrepTickCount = -1;
            if (this.data.currentGunItem == null) {
               return;
            }

            ItemStack currentGunItem = this.data.currentGunItem.get();
            if (currentGunItem.getItem() instanceof AbstractGunItem logicGun) {
               logicGun.melee(this.data, this.shooter, currentGunItem);
            }
         }
      }
   }

   public long getMeleeCoolDown() {
      if (this.data.currentGunItem == null) {
         return 0L;
      } else {
         ItemStack currentGunItem = this.data.currentGunItem.get();
         if (currentGunItem.getItem() instanceof IGun iGun) {
            Identifier gunId = iGun.getGunId(currentGunItem);
            Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(gunId);
            return gunIndex.<Long>map(index -> {
               GunMeleeData meleeData = index.getGunData().getMeleeData();
               Identifier muzzleId = iGun.getAttachmentId(currentGunItem, AttachmentType.MUZZLE);
               MeleeData muzzleMeleeData = this.getMeleeData(muzzleId);
               if (muzzleMeleeData != null) {
                  return this.getTotalCooldownTime(meleeData, muzzleMeleeData.getCooldown());
               }

               Identifier stockId = iGun.getAttachmentId(currentGunItem, AttachmentType.STOCK);
               MeleeData stockMeleeData = this.getMeleeData(stockId);
               if (stockMeleeData != null) {
                  return this.getTotalCooldownTime(meleeData, stockMeleeData.getCooldown());
               }

               GunDefaultMeleeData defaultMeleeData = meleeData.getDefaultMeleeData();
               float defaultMeleeCooldownTime = defaultMeleeData == null ? 0.0F : defaultMeleeData.getCooldown();
               return this.getTotalCooldownTime(meleeData, defaultMeleeCooldownTime);
            }).orElse(-1L);
         } else {
            return 0L;
         }
      }
   }

   private long getTotalCooldownTime(GunMeleeData meleeData, float extraCooldownTime) {
      float totalCooldownTime = meleeData.getCooldown() + extraCooldownTime;
      long coolDown = (long)(totalCooldownTime * 1000.0F) - (System.currentTimeMillis() - this.data.meleeTimestamp);
      coolDown -= 5L;
      return coolDown < 0L ? 0L : coolDown;
   }

   @Nullable
   private MeleeData getMeleeData(Identifier attachmentId) {
      return DefaultAssets.isEmptyAttachmentId(attachmentId)
         ? null
         : TimelessAPI.getCommonAttachmentIndex(attachmentId).map(index -> index.getData().getMeleeData()).orElse(null);
   }
}
