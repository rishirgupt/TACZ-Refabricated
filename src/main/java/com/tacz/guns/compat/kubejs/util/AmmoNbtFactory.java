package com.tacz.guns.compat.kubejs.util;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.item.AmmoItem;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class AmmoNbtFactory extends TimelessItemNbtFactory<AmmoItem, AmmoNbtFactory> {
   public AmmoNbtFactory(@Nonnull AmmoItem item) {
      super(item);
   }

   public AmmoNbtFactory() {
      super((AmmoItem)TimelessItemType.AMMO.getItem());
   }

   @Override
   public ItemStack build() {
      ItemStack stack = new ItemStack(this.item, this.count);
      if (this.item instanceof IAmmo iAmmo) {
         iAmmo.setAmmoId(stack, this.id);
      }

      return stack;
   }
}
