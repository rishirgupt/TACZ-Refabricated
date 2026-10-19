package com.tacz.guns.api.item;

import net.minecraft.world.item.ItemStack;

public interface IAnimationItem {
   boolean isSame(ItemStack var1, ItemStack var2);

   static boolean matchesIgnoreCount(ItemStack pStack, ItemStack pOther) {
      return pStack == pOther ? true : ItemStack.isSameItemSameComponents(pStack, pOther);
   }
}
