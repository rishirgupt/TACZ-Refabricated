package com.tacz.guns.compat.controllable;

import com.tacz.guns.api.item.gun.FireMode;
import net.minecraft.world.item.ItemStack;

/** Controllable has no 26.2 release, so there is no controller rumble to drive. */
public class ControllableCompat {
   public static void init() {
   }

   public static void onGunShoot(ItemStack gunItem, FireMode fireMode) {
   }
}
