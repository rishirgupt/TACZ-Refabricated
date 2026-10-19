package com.tacz.guns.api.item;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public interface IAmmo {
   @Nullable
   static IAmmo getIAmmoOrNull(@Nullable ItemStack stack) {
      if (stack == null) {
         return null;
      } else {
         return stack.getItem() instanceof IAmmo iAmmo ? iAmmo : null;
      }
   }

   Identifier getAmmoId(ItemStack var1);

   void setAmmoId(ItemStack var1, @Nullable Identifier var2);

   boolean isAmmoOfGun(ItemStack var1, ItemStack var2);
}
