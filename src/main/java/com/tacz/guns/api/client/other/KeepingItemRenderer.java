package com.tacz.guns.api.client.other;

import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;

public interface KeepingItemRenderer {
   void keep(ItemStack var1, long var2);

   ItemStack getCurrentItem();

   static KeepingItemRenderer getRenderer() {
      return (KeepingItemRenderer)Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
   }
}
