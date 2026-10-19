package com.tacz.guns.api.item.builder;

import com.google.common.collect.Maps;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.item.gun.GunItemManager;
import com.tacz.guns.init.ModItems;
import java.util.EnumMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.HolderLookup.Provider;

public final class GunItemBuilder {
   private int count = 1;
   private int ammoCount = 0;
   private boolean heatData = false;
   private Identifier gunId;
   private FireMode fireMode = FireMode.UNKNOWN;
   private boolean bulletInBarrel = false;
   private EnumMap<AttachmentType, Identifier> attachments = Maps.newEnumMap(AttachmentType.class);

   private GunItemBuilder() {
   }

   public static GunItemBuilder create() {
      return new GunItemBuilder();
   }

   public GunItemBuilder setCount(int count) {
      this.count = Math.max(count, 1);
      return this;
   }

   public GunItemBuilder setAmmoCount(int count) {
      this.ammoCount = Math.max(count, 0);
      return this;
   }

   public GunItemBuilder setId(Identifier id) {
      this.gunId = id;
      return this;
   }

   public GunItemBuilder setFireMode(FireMode fireMode) {
      this.fireMode = fireMode;
      return this;
   }

   public GunItemBuilder setAmmoInBarrel(boolean ammoInBarrel) {
      this.bulletInBarrel = ammoInBarrel;
      return this;
   }

   public GunItemBuilder putAttachment(AttachmentType type, Identifier attachmentId) {
      this.attachments.put(type, attachmentId);
      return this;
   }

   public GunItemBuilder putAllAttachment(EnumMap<AttachmentType, Identifier> attachments) {
      this.attachments = attachments;
      return this;
   }

   public GunItemBuilder setHeatData(boolean heatData) {
      this.heatData = heatData;
      return this;
   }

   public ItemStack forceBuild(Provider provider) {
      ItemStack gun = new ItemStack(ModItems.MODERN_KINETIC_GUN, this.count);
      if (gun.getItem() instanceof IGun iGun) {
         iGun.setGunId(gun, this.gunId);
         iGun.setFireMode(gun, this.fireMode);
         iGun.setCurrentAmmoCount(gun, this.ammoCount);
         iGun.setBulletInBarrel(gun, this.bulletInBarrel);
         if (this.heatData) {
            iGun.setHeatAmount(gun, 0.0F);
         }

         this.attachments.forEach((type, id) -> {
            ItemStack attachmentStack = AttachmentItemBuilder.create().setId(id).build();
            iGun.installAttachment(provider, gun, attachmentStack);
         });
      }

      return gun;
   }

   public ItemStack build(Provider provider) {
      String itemType = TimelessAPI.getCommonGunIndex(this.gunId).map(index -> index.getPojo().getItemType()).orElse(null);
      if (itemType == null) {
         return ItemStack.EMPTY;
      }

      AbstractGunItem gunItemRegistryObject = GunItemManager.getGunItemRegistryObject(itemType);
      if (gunItemRegistryObject == null) {
         return ItemStack.EMPTY;
      }

      ItemStack gun = new ItemStack(gunItemRegistryObject, this.count);
      if (gun.getItem() instanceof IGun iGun) {
         iGun.setGunId(gun, this.gunId);
         iGun.setFireMode(gun, this.fireMode);
         iGun.setCurrentAmmoCount(gun, this.ammoCount);
         iGun.setBulletInBarrel(gun, this.bulletInBarrel);
         this.attachments.forEach((type, id) -> {
            ItemStack attachmentStack = AttachmentItemBuilder.create().setId(id).build();
            iGun.installAttachment(provider, gun, attachmentStack);
         });
      }

      return gun;
   }
}
