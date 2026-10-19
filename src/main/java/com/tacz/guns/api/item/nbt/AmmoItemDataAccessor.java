package com.tacz.guns.api.item.nbt;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

public interface AmmoItemDataAccessor extends IAmmo {
   String AMMO_ID_TAG = "AmmoId";

   @Nonnull
   @Override
   default Identifier getAmmoId(ItemStack ammo) {
      CompoundTag nbt = ((CustomData)ammo.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).tag;
      if (nbt.contains("AmmoId")) {
         Identifier gunId = Identifier.tryParse(nbt.getStringOr("AmmoId", ""));
         return Objects.requireNonNullElse(gunId, DefaultAssets.EMPTY_AMMO_ID);
      } else {
         return DefaultAssets.EMPTY_AMMO_ID;
      }
   }

   @Override
   default void setAmmoId(ItemStack ammo, @Nullable Identifier ammoId) {
      ammo.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> {
         if (ammoId != null) {
            tag.putString("AmmoId", ammoId.toString());
            if (ammo.getItem() instanceof IAmmo) {
               int maxStackSize = TimelessAPI.getCommonAmmoIndex(ammoId).map(CommonAmmoIndex::getStackSize).orElse(1);
               ammo.set(DataComponents.MAX_STACK_SIZE, maxStackSize);
            }
         } else {
            tag.putString("AmmoId", DefaultAssets.DEFAULT_AMMO_ID.toString());
         }
      }));
   }

   @Override
   default boolean isAmmoOfGun(ItemStack gun, ItemStack ammo) {
      if (gun.getItem() instanceof IGun iGun && ammo.getItem() instanceof IAmmo iAmmo) {
         Identifier gunId = iGun.getGunId(gun);
         Identifier ammoId = iAmmo.getAmmoId(ammo);
         return TimelessAPI.getCommonGunIndex(gunId).map(gunIndex -> gunIndex.getGunData().getAmmoId().equals(ammoId)).orElse(false);
      } else {
         return false;
      }
   }
}
