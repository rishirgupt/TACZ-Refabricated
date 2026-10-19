package com.tacz.guns.inventory.tooltip;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class GunTooltip implements TooltipComponent {
   private final ItemStack gun;
   private final IGun iGun;
   private final Identifier ammoId;
   private final CommonGunIndex gunIndex;

   public GunTooltip(ItemStack gun, IGun iGun, Identifier ammoId, CommonGunIndex gunIndex) {
      this.gun = gun;
      this.iGun = iGun;
      this.ammoId = ammoId;
      this.gunIndex = gunIndex;
   }

   public ItemStack getGun() {
      return this.gun;
   }

   public IGun getIGun() {
      return this.iGun;
   }

   public Identifier getAmmoId() {
      return this.ammoId;
   }

   public CommonGunIndex getGunIndex() {
      return this.gunIndex;
   }
}
