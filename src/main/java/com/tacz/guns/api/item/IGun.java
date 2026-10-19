package com.tacz.guns.api.item;

import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.core.HolderLookup.Provider;
import org.jetbrains.annotations.NotNull;

public interface IGun {
   @Nullable
   static IGun getIGunOrNull(@Nullable ItemStack stack) {
      if (stack == null) {
         return null;
      } else {
         return stack.getItem() instanceof IGun iGun ? iGun : null;
      }
   }

   @Deprecated
   static boolean mainhandHoldGun(LivingEntity livingEntity) {
      return livingEntity.getMainHandItem().getItem() instanceof IGun;
   }

   static boolean mainHandHoldGun(LivingEntity livingEntity) {
      return livingEntity.getMainHandItem().getItem() instanceof IGun;
   }

   @Deprecated
   static FireMode getMainhandFireMode(LivingEntity livingEntity) {
      ItemStack mainHandItem = livingEntity.getMainHandItem();
      return mainHandItem.getItem() instanceof IGun iGun ? iGun.getFireMode(mainHandItem) : FireMode.UNKNOWN;
   }

   static FireMode getMainHandFireMode(LivingEntity livingEntity) {
      ItemStack mainHandItem = livingEntity.getMainHandItem();
      return mainHandItem.getItem() instanceof IGun iGun ? iGun.getFireMode(mainHandItem) : FireMode.UNKNOWN;
   }

   float getAimingZoom(ItemStack var1);

   boolean useDummyAmmo(ItemStack var1);

   int getDummyAmmoAmount(ItemStack var1);

   void setDummyAmmoAmount(ItemStack var1, int var2);

   void addDummyAmmoAmount(ItemStack var1, int var2);

   boolean hasMaxDummyAmmo(ItemStack var1);

   int getMaxDummyAmmoAmount(ItemStack var1);

   void setMaxDummyAmmoAmount(ItemStack var1, int var2);

   boolean hasAttachmentLock(ItemStack var1);

   void setAttachmentLock(ItemStack var1, boolean var2);

   @NotNull
   Identifier getGunId(ItemStack var1);

   void setGunId(ItemStack var1, @Nullable Identifier var2);

   @NotNull
   Identifier getGunDisplayId(ItemStack var1);

   void setGunDisplayId(ItemStack var1, @Nullable Identifier var2);

   int getLevel(int var1);

   int getExp(int var1);

   int getMaxLevel();

   int getLevel(ItemStack var1);

   int getExp(ItemStack var1);

   int getExpToNextLevel(ItemStack var1);

   int getExpCurrentLevel(ItemStack var1);

   FireMode getFireMode(ItemStack var1);

   void setFireMode(ItemStack var1, @Nullable FireMode var2);

   int getCurrentAmmoCount(ItemStack var1);

   void setCurrentAmmoCount(ItemStack var1, int var2);

   void reduceCurrentAmmoCount(ItemStack var1);

   default <T> T modifyProperty(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter, GunProperty<?> id, Class<T> type, T original) {
      return this.modifyProperty(dataHolder, gunItem, shooter, id.name(), type, original);
   }

   default <T> T modifyProperty(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter, String id, Class<T> type, T original) {
      return this.modifyProperty(dataHolder, gunItem, shooter, "modify_property", id, type, original);
   }

   default <T> T modifyProperty(
      ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter, String luaMethodName, String id, Class<T> type, T original
   ) {
      return original;
   }

   void dropAllAmmo(Player var1, ItemStack var2);

   @Nonnull
   ItemStack getAttachment(Provider var1, ItemStack var2, AttachmentType var3);

   @Nonnull
   ItemStack getBuiltinAttachment(ItemStack var1, AttachmentType var2);

   @Nullable
   CompoundTag getAttachmentTag(ItemStack var1, AttachmentType var2);

   void setAttachmentTag(ItemStack var1, AttachmentType var2, CompoundTag var3);

   @Nonnull
   Identifier getBuiltInAttachmentId(ItemStack var1, AttachmentType var2);

   @Nonnull
   Identifier getAttachmentId(ItemStack var1, AttachmentType var2);

   void installAttachment(Provider var1, @Nonnull ItemStack var2, @Nonnull ItemStack var3);

   void unloadAttachment(Provider var1, @Nonnull ItemStack var2, AttachmentType var3);

   boolean allowAttachment(ItemStack var1, ItemStack var2);

   boolean allowAttachmentType(ItemStack var1, AttachmentType var2);

   boolean hasBulletInBarrel(ItemStack var1);

   void setBulletInBarrel(ItemStack var1, boolean var2);

   boolean useInventoryAmmo(ItemStack var1);

   boolean hasInventoryAmmo(LivingEntity var1, ItemStack var2, boolean var3);

   int getRPM(ItemStack var1);

   boolean isCanCrawl(ItemStack var1);

   boolean hasCustomLaserColor(ItemStack var1);

   int getLaserColor(ItemStack var1);

   void setLaserColor(ItemStack var1, int var2);

   boolean hasHeatData(ItemStack var1);

   boolean isOverheatLocked(ItemStack var1);

   void setOverheatLocked(ItemStack var1, boolean var2);

   void setHeatAmount(ItemStack var1, float var2);

   float lerpRPM(ItemStack var1);

   float lerpInaccuracy(ItemStack var1);

   float getHeatAmount(ItemStack var1);
}
