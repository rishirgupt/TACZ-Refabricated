package com.tacz.guns.api.item.nbt;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

public interface AmmoBoxItemDataAccessor extends IAmmoBox {
   String AMMO_ID_TAG = "AmmoId";
   String AMMO_COUNT_TAG = "AmmoCount";
   String CREATIVE_TAG = "Creative";
   String ALL_TYPE_CREATIVE_TAG = "AllTypeCreative";
   String LEVEL_TAG = "Level";

   @Override
   default Identifier getAmmoId(ItemStack ammoBox) {
      CompoundTag tag = ((CustomData)ammoBox.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return tag.contains("AmmoId") ? Identifier.parse(tag.getStringOr("AmmoId", "")) : DefaultAssets.EMPTY_AMMO_ID;
   }

   @Override
   default void setAmmoId(ItemStack ammoBox, Identifier ammoId) {
      ammoBox.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putString("AmmoId", ammoId.toString())));
   }

   @Override
   default int getAmmoCount(ItemStack ammoBox) {
      CompoundTag tag = ((CustomData)ammoBox.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      if (this.isAllTypeCreative(ammoBox) || this.isCreative(ammoBox)) {
         return Integer.MAX_VALUE;
      } else {
         return tag.contains("AmmoCount") ? tag.getIntOr("AmmoCount", 0) : 0;
      }
   }

   @Override
   default void setAmmoCount(ItemStack ammoBox, int count) {
      ammoBox.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
         if (this.isCreative(ammoBox)) {
            tag.putInt("AmmoCount", Integer.MAX_VALUE);
         } else {
            tag.putInt("AmmoCount", count);
         }
      }));
   }

   @Override
   default boolean isAmmoBoxOfGun(ItemStack gun, ItemStack ammoBox) {
      if (!(gun.getItem() instanceof IGun iGun && ammoBox.getItem() instanceof IAmmoBox iAmmoBox)) {
         return false;
      } else {
         if (this.isAllTypeCreative(ammoBox)) {
            return true;
         }

         Identifier ammoId = iAmmoBox.getAmmoId(ammoBox);
         if (ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
            return false;
         }

         Identifier gunId = iGun.getGunId(gun);
         return TimelessAPI.getCommonGunIndex(gunId).map(gunIndex -> gunIndex.getGunData().getAmmoId().equals(ammoId)).orElse(false);
      }
   }

   @Override
   default ItemStack setAmmoLevel(ItemStack ammoBox, int level) {
      ammoBox.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putInt("Level", Math.max(level, 0))));
      return ammoBox;
   }

   @Override
   default int getAmmoLevel(ItemStack ammoBox) {
      CompoundTag tag = ((CustomData)ammoBox.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return tag.contains("Level") ? tag.getIntOr("Level", 0) : 0;
   }

   @Override
   default boolean isCreative(ItemStack ammoBox) {
      CompoundTag tag = ((CustomData)ammoBox.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return tag.contains("Creative") ? tag.getBooleanOr("Creative", false) : false;
   }

   @Override
   default boolean isAllTypeCreative(ItemStack ammoBox) {
      CompoundTag tag = ((CustomData)ammoBox.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      return tag.contains("AllTypeCreative") ? tag.getBooleanOr("AllTypeCreative", false) : false;
   }

   @Override
   default ItemStack setCreative(ItemStack ammoBox, boolean isAllType) {
      ammoBox.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
         if (isAllType) {
            if (tag.contains("Creative")) {
               tag.remove("Creative");
            }

            tag.putBoolean("AllTypeCreative", true);
         } else {
            if (tag.contains("AllTypeCreative")) {
               tag.remove("AllTypeCreative");
            }

            tag.putBoolean("Creative", true);
         }
      }));
      return ammoBox;
   }
}
