package com.tacz.guns.api.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public interface IAmmoBox {
   Identifier getAmmoId(ItemStack var1);

   int getAmmoCount(ItemStack var1);

   void setAmmoId(ItemStack var1, Identifier var2);

   void setAmmoCount(ItemStack var1, int var2);

   boolean isAmmoBoxOfGun(ItemStack var1, ItemStack var2);

   ItemStack setAmmoLevel(ItemStack var1, int var2);

   int getAmmoLevel(ItemStack var1);

   boolean isCreative(ItemStack var1);

   boolean isAllTypeCreative(ItemStack var1);

   ItemStack setCreative(ItemStack var1, boolean var2);
}
