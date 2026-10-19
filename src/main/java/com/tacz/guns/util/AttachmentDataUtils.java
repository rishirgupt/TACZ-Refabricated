package com.tacz.guns.util;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.modifier.custom.ArmorIgnoreModifier;
import com.tacz.guns.resource.modifier.custom.DamageModifier;
import com.tacz.guns.resource.modifier.custom.ExplosionModifier;
import com.tacz.guns.resource.modifier.custom.HeadShotModifier;
import com.tacz.guns.resource.modifier.custom.WeightModifier;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunFireModeAdjustData;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public final class AttachmentDataUtils {
   public static void getAllAttachmentData(ItemStack gunItem, GunData gunData, Consumer<AttachmentData> dataConsumer) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun != null) {
         for (AttachmentType type : AttachmentType.values()) {
            if (type != AttachmentType.NONE) {
               Identifier attachmentId = iGun.getAttachmentId(gunItem, type);
               if (!DefaultAssets.isEmptyAttachmentId(attachmentId)) {
                  AttachmentData attachmentData = gunData.getExclusiveAttachments().get(attachmentId);
                  if (attachmentData != null) {
                     dataConsumer.accept(attachmentData);
                  } else {
                     TimelessAPI.getCommonAttachmentIndex(attachmentId).ifPresent(index -> dataConsumer.accept(index.getData()));
                  }
               }
            }
         }
      }
   }

   public static int getMagExtendLevel(ItemStack gunItem, GunData gunData) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun == null) {
         return 0;
      } else {
         Identifier attachmentId = iGun.getAttachmentId(gunItem, AttachmentType.EXTENDED_MAG);
         if (DefaultAssets.isEmptyAttachmentId(attachmentId)) {
            return 0;
         } else {
            AttachmentData attachmentData = gunData.getExclusiveAttachments().get(attachmentId);
            if (attachmentData != null) {
               int level = attachmentData.getExtendedMagLevel();
               return level <= 0 ? 0 : Math.min(level, 3);
            } else {
               return TimelessAPI.getCommonAttachmentIndex(attachmentId).map(index -> {
                  int levelx = index.getData().getExtendedMagLevel();
                  return levelx <= 0 ? 0 : Math.min(levelx, 3);
               }).orElse(0);
            }
         }
      }
   }

   public static int getAmmoCountWithAttachment(ItemStack gunItem, GunData gunData) {
      int[] extendedMagAmmoAmount = gunData.getExtendedMagAmmoAmount();
      if (extendedMagAmmoAmount == null) {
         return gunData.getAmmoAmount();
      }

      int level = getMagExtendLevel(gunItem, gunData);
      return level == 0 ? gunData.getAmmoAmount() : extendedMagAmmoAmount[level - 1];
   }

   public static double getWightWithAttachment(ItemStack gunItem, GunData gunData) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun == null) {
         return gunData.getWeight();
      }

      List<Modifier> modifiers = new ArrayList<>();

      for (AttachmentType type : AttachmentType.values()) {
         Identifier id = iGun.getAttachmentId(gunItem, type);
         AttachmentData attachmentData = gunData.getExclusiveAttachments().get(id);
         if (attachmentData != null) {
            JsonProperty<?> m = attachmentData.getModifier().get(WeightModifier.ID);
            if (m != null && m.getValue() instanceof Modifier modifier) {
               modifiers.add(modifier);
            } else {
               Modifier modifier = new Modifier();
               modifier.setAddend(attachmentData.getWeight());
               modifiers.add(modifier);
            }
         } else {
            TimelessAPI.getCommonAttachmentIndex(id).ifPresent(index -> {
               JsonProperty<?> mx = index.getData().getModifier().get(WeightModifier.ID);
               if (mx != null && mx.getValue() instanceof Modifier modifierxx) {
                  modifiers.add(modifierxx);
               } else {
                  Modifier modifierx = new Modifier();
                  modifierx.setAddend(index.getData().getWeight());
                  modifiers.add(modifierx);
               }
            });
         }
      }

      return AttachmentPropertyManager.eval(modifiers, gunData.getWeight());
   }

   public static boolean isExplodeEnabled(ItemStack gunItem, GunData gunData) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun == null) {
         return gunData.getBulletData().getExplosionData() != null ? gunData.getBulletData().getExplosionData().isExplode() : false;
      } else {
         return calcBooleanValue(
            gunItem, gunData, ExplosionModifier.ID, ExplosionModifier.ExplosionModifierValue.class, ExplosionModifier.ExplosionModifierValue::isExplode
         );
      }
   }

   public static double getArmorIgnoreWithAttachment(ItemStack gunItem, GunData gunData) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun == null) {
         return 0.0;
      }

      FireMode fireMode = iGun.getFireMode(gunItem);
      BulletData bulletData = gunData.getBulletData();
      GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
      ExtraDamage extraDamage = bulletData.getExtraDamage();
      float finalBase = extraDamage != null ? extraDamage.getArmorIgnore() : 0.0F;
      finalBase = fireModeAdjustData != null ? finalBase + fireModeAdjustData.getArmorIgnore() : finalBase;
      finalBase = (float)(finalBase * (Double)SyncConfig.ARMOR_IGNORE_BASE_MULTIPLIER.get());
      List<Modifier> modifiers = getModifiers(gunItem, gunData, ArmorIgnoreModifier.ID);
      return AttachmentPropertyManager.eval(modifiers, finalBase);
   }

   public static double getHeadshotMultiplier(ItemStack gunItem, GunData gunData) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun == null) {
         return 0.0;
      }

      FireMode fireMode = iGun.getFireMode(gunItem);
      BulletData bulletData = gunData.getBulletData();
      GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
      ExtraDamage extraDamage = bulletData.getExtraDamage();
      float finalBase = extraDamage != null ? extraDamage.getHeadShotMultiplier() : 0.0F;
      finalBase = fireModeAdjustData != null ? finalBase + fireModeAdjustData.getHeadShotMultiplier() : finalBase;
      finalBase = (float)(finalBase * (Double)SyncConfig.HEAD_SHOT_BASE_MULTIPLIER.get());
      List<Modifier> modifiers = getModifiers(gunItem, gunData, HeadShotModifier.ID);
      return AttachmentPropertyManager.eval(modifiers, finalBase);
   }

   public static double getDamageWithAttachment(ItemStack gunItem, GunData gunData) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun == null) {
         return 0.0;
      }

      FireMode fireMode = iGun.getFireMode(gunItem);
      BulletData bulletData = gunData.getBulletData();
      GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
      ExtraDamage extraDamage = bulletData.getExtraDamage();
      float rawDamage = bulletData.getDamageAmount();
      float finalBase = fireModeAdjustData != null ? fireModeAdjustData.getDamageAmount() : 0.0F;
      if (extraDamage != null && extraDamage.getDamageAdjust() != null) {
         finalBase += extraDamage.getDamageAdjust().get(0).getDamage();
      } else {
         finalBase += rawDamage;
      }

      finalBase = (float)(finalBase * (Double)SyncConfig.DAMAGE_BASE_MULTIPLIER.get());
      List<Modifier> modifiers = getModifiers(gunItem, gunData, DamageModifier.ID);
      return AttachmentPropertyManager.eval(modifiers, finalBase);
   }

   private static List<Modifier> getModifiers(ItemStack gunItem, GunData gunData, String id) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun == null) {
         return new ArrayList<>();
      }

      List<Modifier> modifiers = new ArrayList<>();

      for (AttachmentType type : AttachmentType.values()) {
         Identifier attachmentId = iGun.getAttachmentId(gunItem, type);
         if (!DefaultAssets.isEmptyAttachmentId(attachmentId)) {
            AttachmentData attachmentData = gunData.getExclusiveAttachments().get(attachmentId);
            if (attachmentData != null) {
               JsonProperty<?> m = attachmentData.getModifier().get(id);
               if (m != null && m.getValue() instanceof Modifier modifier) {
                  modifiers.add(modifier);
               }
            } else {
               CommonAttachmentIndex index = TimelessAPI.getCommonAttachmentIndex(attachmentId).orElse(null);
               if (index != null) {
                  JsonProperty<?> m = index.getData().getModifier().get(id);
                  if (m != null && m.getValue() instanceof Modifier modifier) {
                     modifiers.add(modifier);
                  }
               }
            }
         }
      }

      return modifiers;
   }

   private static <T> boolean calcBooleanValue(ItemStack gunItem, GunData gunData, String id, Class<T> clazz, AttachmentDataUtils.BooleanResolver<T> resolver) {
      IGun iGun = IGun.getIGunOrNull(gunItem);
      if (iGun == null) {
         return false;
      }

      for (AttachmentType type : AttachmentType.values()) {
         Identifier attachmentId = iGun.getAttachmentId(gunItem, type);
         if (!DefaultAssets.isEmptyAttachmentId(attachmentId)) {
            AttachmentData attachmentData = gunData.getExclusiveAttachments().get(attachmentId);
            if (attachmentData != null) {
               JsonProperty<?> m = attachmentData.getModifier().get(id);
               boolean value = resolve(m, resolver, clazz);
               if (value) {
                  return true;
               }
            } else {
               CommonAttachmentIndex index = TimelessAPI.getCommonAttachmentIndex(attachmentId).orElse(null);
               if (index != null) {
                  JsonProperty<?> m = index.getData().getModifier().get(id);
                  boolean value = resolve(m, resolver, clazz);
                  if (value) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static <T> boolean resolve(JsonProperty<?> raw, AttachmentDataUtils.BooleanResolver<T> data, Class<T> type) {
      return raw != null && raw.getValue() != null && raw.getValue().getClass().equals(type) ? data.apply((T)raw.getValue()) : false;
   }

   @FunctionalInterface
   public interface BooleanResolver<T> {
      boolean apply(T var1);
   }
}
