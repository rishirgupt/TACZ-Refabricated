package com.tacz.guns.inventory.tooltip;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class AmmoBoxTooltip implements TooltipComponent {
   private final ItemStack ammoBox;
   private final ItemStack ammo;
   private final int count;

   public AmmoBoxTooltip(ItemStack ammoBox, ItemStack ammo, int count) {
      this.ammoBox = ammoBox;
      this.ammo = ammo;
      this.count = count;
   }

   public ItemStack getAmmoBox() {
      return this.ammoBox;
   }

   public ItemStack getAmmo() {
      return this.ammo;
   }

   public int getCount() {
      return this.count;
   }
}
